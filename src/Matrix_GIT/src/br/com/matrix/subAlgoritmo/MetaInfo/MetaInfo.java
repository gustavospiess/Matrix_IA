package br.com.matrix.subAlgoritmo.MetaInfo;

import java.util.Comparator;
import java.util.List;

/**
 * Interface pai para MetaInfo
 * 
 * @author GustavoHenrique
 *
 */
public interface MetaInfo {

    /**
     * 
     * @return A tipagem do retorno do subAlgoritmo ao qual essa inst�ncia de
     *         MetaInfo se refere.
     */
    public Tipo getReturnTp();

    /**
     * Verifica se a tipagem � a mesma, se s�o instancias de MetaInfoAssinatura
     * ou de MetaInfoExec
     * 
     * @param m1
     *            - primeiro MetaInfo
     * @param m2
     *            - segundo MetaInfo
     * @return true se a tipagem for a mesma e forem ambos ou Assinatura ou
     *         execu��o, nesse segundo caso � feita a verifica��o nas classes
     *         respectivas tamb�m.
     */
    public static boolean equals(MetaInfo m1, MetaInfo m2) {

	if (!tpEquals(m1, m2))
	    return false;

	if ((m1 instanceof MetaInfoAssinatura) && (m2 instanceof MetaInfoAssinatura))
	    return MetaInfoAssinatura.equals((MetaInfoAssinatura) m1, (MetaInfoAssinatura) m2);

	if ((m1 instanceof MetaInfoExec) && (m2 instanceof MetaInfoExec))
	    return MetaInfoExec.equals((MetaInfoExec) m1, (MetaInfoExec) m2);

	return true;
    }

    /**
     * 
     * @param m1
     *            - primeiro MetaInfo
     * @param m2
     *            - segundo MetaInfo
     * @return true se a tipagem for equivalente.
     */
    public static boolean tpEquals(MetaInfo m1, MetaInfo m2) {
	return m1.getReturnTp().equals(m2.getReturnTp());
    }

    /**
     * 
     * 
     * @param t
     *            tipagem do retorno requerido.
     * @param quantidade
     *            - quantidade requerida.
     * @return uma nova inst�ncia de MetaInfoAssinatura com as caracter�sticas
     *         passadas por par�metro.
     */
    public static MetaInfoAssinatura fabricarAssinatura(Tipo t, Quantidade quantidade) {
	return MetaInfoAssinatura.fabricar(t, quantidade);
    }

    /**
     * 
     * @param t
     *            Tipagem do retorno do subAlgoritmo referete.
     * @param l
     *            Par�metros para a prepara��o do subAlgoritmo.
     * @return - Uma nova inst�ncia de MetaInfoExec com as caracter�sticas
     *         passadas por par�metro.
     */
    public static MetaInfoExec fabricarExec(Tipo t, List<MetaInfoAssinatura> l) {
	return MetaInfoExec.fabricar(t, l);
    }

    /**
     * Comparator que utiliza a classifica��o do tipo de retorno.
     */
    public static final Comparator<MetaInfo> tpComparator = new Comparator<MetaInfo>() {

	@Override
	public int compare(MetaInfo o1, MetaInfo o2) {
	    int r = o1.getReturnTp().getSimpleName().compareTo(o2.getReturnTp().getSimpleName());
	    return r;
	}
    };
}
