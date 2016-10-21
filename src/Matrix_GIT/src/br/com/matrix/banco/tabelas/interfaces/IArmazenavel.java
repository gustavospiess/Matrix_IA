/**
 * 
 */
package br.com.matrix.banco.tabelas.interfaces;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author LucasGabrielDaCosta
 *
 */
public interface IArmazenavel {

	public default ICampo getCampoPorNm(String nm) {
		ICampo r = null;

		for (Iterator<IColuna> iterator = getValoresCampo().keySet().iterator(); iterator.hasNext();) {
			IColuna c = (IColuna) iterator.next();
			if (c.getNm().equalsIgnoreCase(nm)) {
				r = getValoresCampo().get(c);
				break;
			}
		}

		return r;
	}

	/**
	 * 
	 * @return ITabela que contem o c�digo identificador do objeto armazen�vel.
	 */
	public ITabela getTb();

	/**
	 * 
	 * @return A lista de armazenaveis dependentes, isso �, referenciados aqui.
	 */
	public List<IArmazenavel> getDependencias();

	/**
	 * 
	 * @return C�digo identificador do objeto armazen�vel.
	 */
	public default int getId() {
		return Integer.valueOf(getCampoPorNm("id").getValor().toString());
	}

	/**
	 * 
	 * @return A lista de valores dos campos da classa que implementa
	 *         armazenavel.
	 */
	public default HashMap<IColuna, Object> getValores() {
		HashMap<IColuna, Object> r = new HashMap<IColuna, Object>();

		for (IColuna c : getValoresCampo().keySet())
			r.put(c, getValoresCampo().get(c).getValor());

		return r;
	}

	/**
	 * 
	 * @return A lista de valores dos campos (tipados como Campo) da classa que
	 *         implementa armazenavel.
	 */
	public HashMap<IColuna, ICampo> getValoresCampo();

	/**
	 * 
	 * @paramReq a
	 * @return metodo de compara��o especifico para armazenavel
	 */
	public default boolean equals(IArmazenavel a) {
		if (this == a)
			return true;
		if (a == null)
			return false;
		if (this.getId() != a.getId())
			return false;
		return ((getTb() == null) ? (a.getTb() == null) : getTb().equals(a.getTb()) && (getId() == a.getId()));
	}

}
