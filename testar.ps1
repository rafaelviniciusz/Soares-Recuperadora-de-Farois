$ErrorActionPreference = 'Stop'
$project = $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($project)) {
    $project = (Get-Location).Path
}
if (-not (Test-Path -LiteralPath (Join-Path $project 'RecuperadoraFarois.java'))) {
    throw 'Abra o terminal na pasta do projeto antes de executar testar.ps1.'
}
$jdk = Get-ChildItem -Path (Join-Path $env:USERPROFILE '.vscode\extensions\redhat.java-*\jre\*\bin\javac.exe') -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName
if (-not $jdk) { throw 'Nao encontrei javac.exe no JDK do VS Code.' }
$java = $jdk -replace 'javac\.exe$', 'java.exe'
$lib = Join-Path $project 'lib'
$classpath = "$lib\junit-4.13.2.jar;$lib\hamcrest-3.0.0.jar;$lib\postgresql-42.7.14.jar"
$build = Join-Path $env:TEMP 'soares-recuperadora-testes'
New-Item -ItemType Directory -Force -Path $build | Out-Null

if ($null -eq ([System.Management.Automation.PSTypeName]'SoaresTestCredentialStore').Type) {
    Add-Type @'
using System;
using System.Runtime.InteropServices;

public static class SoaresTestCredentialStore {
    [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
    private struct CREDENTIAL {
        public UInt32 Flags; public UInt32 Type; public IntPtr TargetName; public IntPtr Comment;
        public long LastWritten; public UInt32 CredentialBlobSize; public IntPtr CredentialBlob;
        public UInt32 Persist; public UInt32 AttributeCount; public IntPtr Attributes;
        public IntPtr TargetAlias; public IntPtr UserName;
    }
    [DllImport("Advapi32.dll", EntryPoint="CredReadW", CharSet=CharSet.Unicode, SetLastError=true)]
    private static extern bool CredRead(string target, UInt32 type, UInt32 flags, out IntPtr credential);
    [DllImport("Advapi32.dll", EntryPoint="CredWriteW", CharSet=CharSet.Unicode, SetLastError=true)]
    private static extern bool CredWrite(ref CREDENTIAL credential, UInt32 flags);
    [DllImport("Advapi32.dll", SetLastError=true)]
    private static extern void CredFree(IntPtr buffer);

    public static string[] Read(string target) {
        IntPtr buffer;
        if (!CredRead(target, 1, 0, out buffer)) return null;
        try {
            CREDENTIAL c = (CREDENTIAL)Marshal.PtrToStructure(buffer, typeof(CREDENTIAL));
            return new string[] { Marshal.PtrToStringUni(c.UserName),
                Marshal.PtrToStringUni(c.CredentialBlob, (int)(c.CredentialBlobSize / 2)) };
        } finally { CredFree(buffer); }
    }

    public static void Save(string target, string user, string password) {
        IntPtr targetPtr = Marshal.StringToCoTaskMemUni(target);
        IntPtr userPtr = Marshal.StringToCoTaskMemUni(user);
        IntPtr passPtr = Marshal.StringToCoTaskMemUni(password);
        try {
            CREDENTIAL c = new CREDENTIAL();
            c.Type = 1; c.TargetName = targetPtr; c.UserName = userPtr;
            c.CredentialBlob = passPtr; c.CredentialBlobSize = (UInt32)(password.Length * 2);
            c.Persist = 2;
            if (!CredWrite(ref c, 0)) throw new System.ComponentModel.Win32Exception(Marshal.GetLastWin32Error());
        } finally {
            Marshal.ZeroFreeCoTaskMemUnicode(passPtr);
            Marshal.FreeCoTaskMem(userPtr); Marshal.FreeCoTaskMem(targetPtr);
        }
    }
}
'@
}

$saved = [SoaresTestCredentialStore]::Read('SoaresRecuperadoraPostgreSQL')
if ($null -eq $saved) {
    Write-Host 'Credenciais nao encontradas. Elas serao salvas no Gerenciador do Windows.'
    $dbUser = Read-Host 'Usuario PostgreSQL (geralmente postgres)'
    $secure = Read-Host 'Senha do PostgreSQL' -AsSecureString
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try { $dbPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr) }
    [SoaresTestCredentialStore]::Save('SoaresRecuperadoraPostgreSQL', $dbUser, $dbPassword)
    $saved = @($dbUser, $dbPassword)
}
$env:PGUSER = $saved[0]
$env:PGPASSWORD = $saved[1]

Push-Location $project
try {
    Write-Host 'Compilando os testes JUnit...'
    & $jdk -cp $classpath -d $build 'RecuperadoraFarois.java' 'testes\RecuperadoraFaroisTest.java'
    if ($LASTEXITCODE -ne 0) { throw 'A compilacao dos testes falhou.' }
    Write-Host 'Executando testes unitario e de integracao com PostgreSQL...'
    & $java -cp "$build;$classpath" org.junit.runner.JUnitCore RecuperadoraFaroisTest
    if ($LASTEXITCODE -ne 0) { throw 'Um ou mais testes falharam.' }
} finally {
    Remove-Item Env:PGUSER, Env:PGPASSWORD -ErrorAction SilentlyContinue
    $saved = $null
    Pop-Location
}
