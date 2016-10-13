package br.com.matrix.subAlgoritmo;

import java.util.List;

import br.com.matrix.subAlgoritmo.MetaInfo.MetaInfoExec;

/**
 * Fra��o de algoritmo, tal como: <br>
 * <code>if(...){}</code><br>
 * <code>for(...){}</code><br>
 * <code>a = b;</code>
 * 
 * @author GustavoHenrique
 *
 * @param <Tp>
 *            - Tipagem do retorno
 */
public interface SubAlgoritmo<Tp extends Object> {

    /**
     * 
     * @return A MetaInfoExec, um emcapsulador de tipo de retorno, e par�metros
     *         requeridos.
     */
    public MetaInfoExec getMetaInfo();

    /**
     * 
     * @param l
     *            - Lista de par�mtros.
     * @throws IllegalArgumentException
     *             - Se a lista de par�metros passados n�o for condisente com os
     *             requeridos.
     */
    public void preparar(List<SubAlgoritmo<?>> l) throws IllegalArgumentException;

    /**
     * 
     * @return true se est� preparado
     */
    public boolean isPreparado();

    /**
     * Prepara para retornar e/ou executa um comando espec�fico.
     */
    public void executar();

    /**
     * 
     * @return true se foi executado pelo menos uma vez.
     */
    public boolean isExecutado();

    /**
     * 
     * @return O Retorno da ope��o executada.
     */
    public Tp retornar();
    
    /**
     * 
     * @param sa - subAlgoritmo � se verificar se � chamado direta ou indiretamente por esse.
     * @return true se o algoritmo sa for chamado.
     */
    public boolean isChamado(SubAlgoritmo<?> sa);
    
    /**
     * Atribui false ao isExecutado.
     */
    public void resetExecutado();
}
