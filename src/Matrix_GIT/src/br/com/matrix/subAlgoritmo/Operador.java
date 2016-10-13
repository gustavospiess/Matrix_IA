package br.com.matrix.subAlgoritmo;

import java.util.ArrayList;
import java.util.List;

import br.com.matrix.subAlgoritmo.MetaInfo.MetaInfo;
import br.com.matrix.subAlgoritmo.MetaInfo.MetaInfoAssinatura;
import br.com.matrix.subAlgoritmo.MetaInfo.MetaInfoExec;
import br.com.matrix.subAlgoritmo.MetaInfo.Tipo;

/**
 * Implementa��o abstrata de um operador de retorno Tp. <br>
 * Aplica-se � condi��es logicas, opera��es matem�ticas, etc...
 * 
 * @author GustavoHenrique
 *
 * @param <Tp>
 *            - Tipagem do retorno
 */
public abstract class Operador<Tp extends Object> implements SubAlgoritmo<Tp> {
    /**
     * Par�metros recebidos no preparo da execu��o;
     */
    protected List<SubAlgoritmo<?>> paramEntrada;

    /**
     * Par�metros requeridos, definidos nas classes que extendem essa ou na
     * constru��o de uma inst�ncia.
     */
    protected List<MetaInfoAssinatura> paramReq;

    /**
     * Vari�vel de controle para o resultado.
     */
    protected Tp result = null;

    /**
     * Vari�vel de controle para tipagem do retorno
     */
    protected Class<Tp> tpResult;

    /**
     * Contrutor
     * 
     * @param param
     *            - Lista de par�metro requeridos.
     * @param tp
     *            - Tipagem do retorno.
     */
    public Operador(List<MetaInfoAssinatura> param, Class<Tp> tp) {
	this.paramReq = param;
	this.tpResult = tp;
    }

    /**
     * Verifica��o de se os par�metros est�o de aconrto com o que foi requerido
     * <br>
     * e de se est�o todos devidamente preparados.
     */
    @Override
    public void preparar(List<SubAlgoritmo<?>> l) {

	if (!MetaInfoAssinatura.compararListaMetaInfoSubAlg(paramReq, l))
	    throw new IllegalArgumentException("Faltam par�metros");

	for (SubAlgoritmo<?> sa : l)
	    if (!sa.isPreparado())
		throw new IllegalArgumentException(sa.toString() + " - SubAlgoritmo n�o preparado.");

	this.paramEntrada = new ArrayList<>();
	paramEntrada.addAll(l);
    }

    /**
     * Retorna a vari�vel de controle <code>result</code>
     */
    @Override
    public Tp retornar() {
	return result;
    }

    /**
     * Verifica se os par�metros est�o de acordo com o que foi solicitado.
     */
    @Override
    public boolean isPreparado() {
	return paramEntrada != null && !paramEntrada.isEmpty()
		&& MetaInfoAssinatura.compararListaMetaInfoSubAlg(paramReq, paramEntrada);
    }

    /**
     * Constru��o e retorno da meta info com base nas vari�veis de controle
     * <code>tpResult</code> e <code>paramReq</code>.
     */
    @Override
    public MetaInfoExec getMetaInfo() {
	return MetaInfo.fabricarExec(new Tipo(tpResult), paramReq);
    }

    /**
     * Verifica se a vari�vel de controle referente ao resultado � n�o nula.
     */
    @Override
    public boolean isExecutado() {
	return result != null;
    }

    @Override
    public boolean isChamado(SubAlgoritmo<?> sa) {
	boolean r = false;

	for (SubAlgoritmo<?> saLocal : paramEntrada) {
	    if (saLocal.equals(sa) || saLocal.isChamado(sa)) {
		r = true;
		break;
	    }
	}

	return r;
    }

    @Override
    public void resetExecutado() {
	result = null;
	
    }

}
