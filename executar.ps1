param([switch]$RedefinirCredencial)

$ErrorActionPreference = 'Stop'
$project = $PSScriptRoot
$target = 'SoaresRecuperadoraPostgreSQL'

if (-not ('WindowsCredentialStore' -as [type])) {
Add-Type @'
using System;
using System.Runtime.InteropServices;

public static class WindowsCredentialStore {
    [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
    private struct CREDENTIAL {
        public UInt32 Flags;
        public UInt32 Type;
        public IntPtr TargetName;
        public IntPtr Comment;
        public long LastWritten;
        public UInt32 CredentialBlobSize;
        public IntPtr CredentialBlob;
        public UInt32 Persist;
        public UInt32 AttributeCount;
        public IntPtr Attributes;
        public IntPtr TargetAlias;
        public IntPtr UserName;
    }

    [DllImport("Advapi32.dll", EntryPoint="CredReadW", CharSet=CharSet.Unicode, SetLastError=true)]
    private static extern bool CredRead(string target, UInt32 type, UInt32 flags, out IntPtr credential);
    [DllImport("Advapi32.dll", EntryPoint="CredWriteW", CharSet=CharSet.Unicode, SetLastError=true)]
    private static extern bool CredWrite(ref CREDENTIAL credential, UInt32 flags);
    [DllImport("Advapi32.dll", EntryPoint="CredDeleteW", CharSet=CharSet.Unicode, SetLastError=true)]
    private static extern bool CredDelete(string target, UInt32 type, UInt32 flags);
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

    public static void Delete(string target) {
        CredDelete(target, 1, 0);
    }
}
'@
}

if ($RedefinirCredencial) {
    $saved = $null
} else {
    $saved = [WindowsCredentialStore]::Read($target)
}
if ($null -eq $saved) {
    Write-Host 'Primeira execucao: os dados serao guardados no Gerenciador de Credenciais do Windows.'
    $dbUser = Read-Host 'Usuario PostgreSQL (geralmente postgres)'
    $secure = Read-Host 'Senha do PostgreSQL' -AsSecureString
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try { $dbPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr) }
    [WindowsCredentialStore]::Save($target, $dbUser, $dbPassword)
} else {
    $dbUser = $saved[0]
    $dbPassword = $saved[1]
}

$jdk = Get-ChildItem -Path (Join-Path $env:USERPROFILE '.vscode\extensions\redhat.java-*\jre\*\bin\javac.exe') -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName
if (-not $jdk) { throw 'Nao encontrei javac.exe. Confira o JDK selecionado no VS Code.' }
$java = $jdk -replace 'javac\.exe$', 'java.exe'
$driver = Join-Path $project 'lib\postgresql-42.7.14.jar'
$source = Join-Path $project 'RecuperadoraFarois.java'
$build = Join-Path $env:TEMP 'soares-recuperadora-classes'
New-Item -ItemType Directory -Force -Path $build | Out-Null

$env:PGUSER = $dbUser
$env:PGPASSWORD = $dbPassword
Push-Location $project
try {
    & $jdk -cp ".;$driver" -d $build $source
    if ($LASTEXITCODE -ne 0) { throw 'A compilacao falhou.' }
    & $java -cp "$build;$driver" RecuperadoraFarois
} finally {
    Pop-Location
    Remove-Item Env:PGUSER, Env:PGPASSWORD -ErrorAction SilentlyContinue
}
