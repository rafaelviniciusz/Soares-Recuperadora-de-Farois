import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.Test;

public class RecuperadoraFaroisTest {

    @Test
    public void testarRegraDoEstoque() {
        assertTrue(RecuperadoraFarois.estoqueValido(5, 2));
        assertTrue(RecuperadoraFarois.estoqueValido(5, -5));
        assertFalse(RecuperadoraFarois.estoqueValido(5, -6));
    }

    @Test
    public void testarCadastroAtualizacaoEdicaoEExclusao() {
        if (System.getenv("PGUSER") == null || System.getenv("PGPASSWORD") == null) {
            System.out.println("Teste de integracao ignorado: credenciais nao configuradas.");
            Assume.assumeTrue(false);
        }
        System.out.println("Executando teste de integracao com PostgreSQL.");
        int id = 1_000_000_000
                + (int) (System.currentTimeMillis() % 1_000_000_000);

        RecuperadoraFarois.Produto produto =
                new RecuperadoraFarois.Produto();

        produto.id = id;
        produto.nome = "Produto de teste";
        produto.categoria = "Categoria de teste " + id;
        produto.preco = 12.50f;
        produto.quantidade = 4;
        produto.descricao = "Produto criado pelo teste";

        assertTrue(RecuperadoraFarois.inserirProdutoBanco(produto));
        assertTrue(RecuperadoraFarois.atualizarQuantidadeBanco(id, 7));

        produto.id = id + 1;
        produto.nome = "Produto editado";
        produto.quantidade = 7;

        assertTrue(RecuperadoraFarois.atualizarProdutoBanco(produto, id));
        assertTrue(RecuperadoraFarois.excluirProdutoBanco(id + 1));
    }
}
