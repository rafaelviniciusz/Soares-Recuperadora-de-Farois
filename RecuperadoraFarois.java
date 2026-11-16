import java.util.Arrays;
import java.util.Scanner;
public class RecuperadoraFarois {
   static class Produto { // classe equivalente à struct de um produto
       int id;
       String nome;
       String categoria;
       float preco;
       int quantidade;
       String descricao;
   }
   // Em Java, não é necessário declarar protótipos antes das funções.
   // O estado abaixo substitui o vetor e os ponteiros usados no código C.
   static Produto[] vetor = null;
   static int quantidade = 0;
   static int capacidade = 0;
   static final Scanner entrada = new Scanner(System.in);
   //==========função login admin==========
   static int loginAdmin() {
       String usuario;
       String senha;
       System.out.println("\n===== LOGIN ADMINISTRATIVO =====");
       System.out.print("Usuario: ");
       usuario = entrada.nextLine().trim();
       System.out.print("Senha: ");
       senha = entrada.nextLine().trim();
       if (usuario.equals("admin") && senha.equals("1234")) {
           System.out.println("\nLogin realizado com sucesso!");
           return 1;
       }
       System.out.println("\nUsuario ou senha incorretos!");
       return 0;
   }
   //==========função menu interativo admin==========
   static void menuAdmin() {
       int opcaoAdmin;
       do {
           System.out.println("\n==============================");
           System.out.println("AREA ADMINISTRATIVA");
           System.out.println("==============================");
           System.out.println("1 - Cadastrar Produto");
           System.out.println("2 - Listar Produtos");
           System.out.println("3 - Buscar Produto");
           System.out.println("4 - Atualizar Estoque");
           System.out.println("5 - Editar Produto");
           System.out.println("6 - Excluir Produto");
           System.out.println("7 - Logout");
           System.out.print("\nEscolha uma opcao: ");
           try {
               opcaoAdmin = Integer.parseInt(entrada.nextLine().trim());
           } catch (NumberFormatException e) {
               opcaoAdmin = 0;
           }
           switch (opcaoAdmin) {
               case 1:
                   if (vetor == null) {
                       capacidade = lerInteiro("\nDigite a capacidade inicial do estoque: ");
                       while (capacidade <= 0) {
                           capacidade = lerInteiro("\nDigite um valor maior que zero: ");
                       }
                       try {
                           vetor = new Produto[capacidade];
                       } catch (OutOfMemoryError e) {
                           System.out.println("\nErro ao alocar memoria.\n");
                           capacidade = 0;
                           return;
                       }
                   }
                   cadastrar();
                   break;
               case 2:
                   listar();
                   break;
               case 3:
                   busca();
                   break;
               case 4:
                   if (vetor == null) {
                       System.out.println("\nNenhum produto cadastrado.\n");
                       break;
                   }
                   atualizarEstoque();
                   break;
               case 5:
                   if (vetor == null) {
                       System.out.println("\nNenhum produto cadastrado.\n");
                       break;
                   }
                   editarProduto();
                   break;
               case 6:
                   if (vetor == null) {
                       System.out.println("\nNenhum produto cadastrado.\n");
                       break;
                   }
                   excluirProduto();
                   break;
               case 7:
                   System.out.println("\nLogout realizado com sucesso.\n");
                   break;
               default:
                   System.out.println("\nOpcao invalida!\n");
           }
       } while (opcaoAdmin != 7);
   }
   //==========função para cadastrar produtos==========
   static void cadastrar() {
       int i;
       int idExiste;
       // verifica se a capacidade está cheia
       if (quantidade == capacidade) {
           int novacapacidade;
           // Pede um novo tamanho para o cadastro
           novacapacidade = lerInteiro("\nCapacidade maxima de produtos foi atingida, digite a nova capacidade: ");
           // garante que a nova capacidade seja maior que a anterior
           while (novacapacidade <= capacidade) {
               novacapacidade = lerInteiro("\nValor digitado menor ou igual ao anterior, digite um valor maior que o anterior: ");
           }
           // Aumenta o tamanho do vetor usando Arrays.copyOf, equivalente ao realloc neste contexto.
           // temp é utilizado para, caso de erro, não alterar o vetor original.
           Produto[] temp;
           try {
               temp = Arrays.copyOf(vetor, novacapacidade);
           } catch (OutOfMemoryError e) {
               System.out.println("Erro ao alocar memoria.\n");
               return;
           }
           // Atualiza o vetor original
           vetor = temp;
           // Atualiza a capacidade do cadastro
           capacidade = novacapacidade;
       }
       vetor[quantidade] = new Produto();
       vetor[quantidade].nome = lerTexto("\nDigite o nome do produto: ", 99);
       // verificacao se ja existe produto com esse id
       do {
           idExiste = 0;
           vetor[quantidade].id = lerInteiro("\nDigite o ID do produto: ");
           if (vetor[quantidade].id <= 0) {
               System.out.println("\nID invalido! Digite um valor maior que zero.\n");
               continue;
           }
           for (i = 0; i < quantidade; i++) {
               if (vetor[i].id == vetor[quantidade].id) {
                   idExiste = 1;
                   System.out.println("\nEsse ID ja esta cadastrado. Digite outro.\n");
                   break;
               }
           }
       } while (vetor[quantidade].id <= 0 || idExiste == 1);
       vetor[quantidade].categoria = lerTexto("\nDigite a categoria: ", 39);
       vetor[quantidade].quantidade = lerInteiro("\nDigite a quantidade do produto: ");
       while (vetor[quantidade].quantidade < 0) {
           vetor[quantidade].quantidade = lerInteiro("Valor para quantidade invalido, digite novamente um valor valido: ");
       }
       vetor[quantidade].preco = lerFloat("\nDigite o preço do produto: ");
       while (vetor[quantidade].preco < 0) {
           vetor[quantidade].preco = lerFloat("Valor para preço invalido, digite novamente um valor valido: ");
       }
       vetor[quantidade].descricao = lerTexto("\nDigite a descricao do produto: ", 99);
       // aumenta a quantidade de produtos cadastrados
       quantidade++;
       System.out.println("\nProduto cadastrado com sucesso!\n");
   }
   //==========Função para listar todos os produtos==========
   static void listar() {
       int i;
       if (vetor == null || quantidade == 0) {
           System.out.println("\nNenhum produto cadastrado.\n");
           return;
       }
       // percorre todos os produtos cadastrados
       for (i = 0; i < quantidade; i++) {
           System.out.println("\n====================");
           System.out.println("Nome: " + vetor[i].nome);
           System.out.println("ID: " + vetor[i].id);
           System.out.println("Categoria: " + vetor[i].categoria);
           System.out.printf("Preco: %.2f%n", vetor[i].preco);
           System.out.println("Quantidade: " + vetor[i].quantidade);
           System.out.println("Descricao: " + vetor[i].descricao);
       }
   }
   // ADICIONADO: a função busca() era chamada no código C, mas sua implementação não constava no documento.
   // Para permitir a compilação e manter a opção do menu funcional, a busca foi implementada pelo ID do produto.
   static void busca() {
       if (vetor == null || quantidade == 0) {
           System.out.println("\nNenhum produto cadastrado.\n");
           return;
       }
       int idBusca = lerInteiro("\nDigite o ID do produto que deseja buscar: ");
       for (int i = 0; i < quantidade; i++) {
           if (vetor[i].id == idBusca) {
               System.out.println("\n====================");
               System.out.println("Nome: " + vetor[i].nome);
               System.out.println("ID: " + vetor[i].id);
               System.out.println("Categoria: " + vetor[i].categoria);
               System.out.printf("Preco: %.2f%n", vetor[i].preco);
               System.out.println("Quantidade: " + vetor[i].quantidade);
               System.out.println("Descricao: " + vetor[i].descricao);
               return;
           }
       }
       System.out.println("\nProduto nao encontrado.\n");
   }
   //==========função de atualizar estoque percorrendo vetor==========
   static void atualizarEstoque() {
       int idBusca;
       int alteracao;
       int i;
       int encontrado = 0;
       if (vetor == null || quantidade == 0) {
           System.out.println("\nNenhum produto cadastrado.\n");
           return;
       }
       idBusca = lerInteiro("\nDigite o ID do produto: ");
       for (i = 0; i < quantidade; i++) {
           if (vetor[i].id == idBusca) {
               encontrado = 1;
               System.out.println("\nProduto: " + vetor[i].nome);
               System.out.println("Quantidade atual: " + vetor[i].quantidade);
               alteracao = lerInteiro("\nDigite a quantidade para alterar (+ ou -): ");
               if (vetor[i].quantidade + alteracao < 0) {
                   System.out.println("\nErro: estoque nao pode ficar negativo.\n");
                   return;
               }
               vetor[i].quantidade += alteracao;
               System.out.println("\nEstoque atualizado com sucesso!");
               System.out.println("Nova quantidade: " + vetor[i].quantidade + "\n");
               return;
           }
       }
       if (encontrado == 0) {
           System.out.println("\nProduto nao encontrado.\n");
       }
   }
   //==========funcao de editar produto buscando percorrendo vetor e atualizando o ponteiro, com validação de id==========
   static void editarProduto() {
       int idBusca;
       int i;
       int encontrado = 0;
       if (vetor == null || quantidade == 0) {
           System.out.println("\nNenhum produto cadastrado.\n");
           return;
       }
       idBusca = lerInteiro("\nDigite o ID do produto que deseja editar: ");
       for (i = 0; i < quantidade; i++) {
           if (vetor[i].id == idBusca) {
               int novoId;
               int idExiste;
               int j;
               encontrado = 1;
               System.out.println("\nProduto encontrado!\n");
               do {
                   idExiste = 0;
                   novoId = lerInteiro("\nNovo ID: ");
                   if (novoId <= 0) {
                       System.out.println("\nID invalido! Digite um valor maior que zero.\n");
                       continue;
                   }
                   for (j = 0; j < quantidade; j++) {
                       if (j != i && vetor[j].id == novoId) {
                           idExiste = 1;
                           System.out.println("\nEsse ID ja esta cadastrado. Digite outro.\n");
                           break;
                       }
                   }
               } while (novoId <= 0 || idExiste == 1);
               vetor[i].id = novoId;
               vetor[i].nome = lerTexto("\nNovo nome: ", 99);
               vetor[i].categoria = lerTexto("\nNova categoria: ", 39);
               vetor[i].preco = lerFloat("\nNovo preco: ");
               while (vetor[i].preco < 0) {
                   vetor[i].preco = lerFloat("\nPreco invalido. Digite novamente: ");
               }
               vetor[i].quantidade = lerInteiro("\nNova quantidade: ");
               while (vetor[i].quantidade < 0) {
                   vetor[i].quantidade = lerInteiro("\nQuantidade invalida. Digite novamente: ");
               }
               vetor[i].descricao = lerTexto("\nNova descricao: ", 99);
               System.out.println("\nProduto editado com sucesso!\n");
               return;
           }
       }
       if (encontrado == 0) {
           System.out.println("\nProduto nao encontrado.\n");
       }
   }
   //==========funcao de excluir produto percorrendo vetor e excluindo movendo para esquerda==========
   static void excluirProduto() {
       int idBusca;
       int i;
       int j;
       int encontrado = 0;
       if (vetor == null || quantidade == 0) {
           System.out.println("\nNenhum produto cadastrado.\n");
           return;
       }
       idBusca = lerInteiro("\nDigite o ID do produto que deseja excluir: ");
       for (i = 0; i < quantidade; i++) {
           if (vetor[i].id == idBusca) {
               encontrado = 1;
               System.out.printf("\nProduto %s (ID %d) excluido com sucesso!%n", vetor[i].nome, vetor[i].id);
               // desloca os produtos para a esquerda
               for (j = i; j < quantidade - 1; j++) {
                   vetor[j] = vetor[j + 1];
               }
               quantidade--;
               return;
           }
       }
       if (encontrado == 0) {
           System.out.println("\nProduto nao encontrado.\n");
       }
   }
   //====================função de contato====================
   static void contato() {
       System.out.println("\n===== CONTATO =====");
       System.out.println("Soares Recuperadora de Farois");
       System.out.println("Telefone: ");
       System.out.println("Email: ");
       System.out.println("Endereco: ");
   }
   //====================função sobre a empresa====================
   static void sobreEmpresa() {
       System.out.println("\n===== SOBRE A EMPRESA =====");
       System.out.println("A Soares Recuperadora de Farois atua na recuperacao restauracao e polimento de farois automotivos oferecendo servicos de qualidade para seus clientes");
   }
   //=============================main================================
   public static void main(String[] args) {
       int opcao;
       System.out.println("Bem vindo a Soares Recuperadora De Farois!\n");
       do {
           System.out.println("\n=================================");
           System.out.println("SOARES RECUPERADORA DE FAROIS");
           System.out.println("=================================");
           System.out.println("1 - Buscar Produto");
           System.out.println("2 - Listar Produtos");
           System.out.println("3 - Login Administrativo");
           System.out.println("4 - Contatos");
           System.out.println("5 - Sobre a empresa");
           System.out.println("6 - Sair");
           System.out.print("\nEscolha uma opcao: ");
           try {
               opcao = Integer.parseInt(entrada.nextLine().trim());
           } catch (NumberFormatException e) {
               opcao = 0;
           }
           switch (opcao) {
               case 1:
                   busca();
                   break;
               case 2:
                   listar();
                   break;
               case 3:
                   if (loginAdmin() == 1) {
                       menuAdmin();
                   }
                   break;
               case 4:
                   contato();
                   break;
               case 5:
                   sobreEmpresa();
                   break;
               case 6:
                   System.out.println("\nEncerrando programa...\n");
                   break;
               default:
                   System.out.println("\nOpcao invalida!\n");
           }
       } while (opcao != 6);
       // Em Java, não é necessário chamar free(): a memória é gerenciada pela coleta de lixo.
       entrada.close();
   }
   static int lerInteiro(String mensagem) {
       while (true) {
           System.out.print(mensagem);
           try {
               return Integer.parseInt(entrada.nextLine().trim());
           } catch (NumberFormatException e) {
               System.out.println("Entrada invalida. Digite um numero inteiro.");
           }
       }
   }
   static float lerFloat(String mensagem) {
       while (true) {
           System.out.print(mensagem);
           try {
               return Float.parseFloat(entrada.nextLine().trim().replace(',', '.'));
           } catch (NumberFormatException e) {
               System.out.println("Entrada invalida. Digite um numero valido.");
           }
       }
   }
   static String lerTexto(String mensagem, int limite) {
       System.out.print(mensagem);
       String texto = entrada.nextLine();
       // Mantém os limites aproximados dos campos char[] do código C.
       if (texto.length() > limite) {
           texto = texto.substring(0, limite);
       }
       return texto;
   }
}
