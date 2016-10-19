package br.com.matrix.banco.tabelas;

import br.com.matrix.banco.tabelas.classesAbstratas.ATabela;
import br.com.matrix.banco.tabelas.propTabelas.GenColuna;

public class Textos extends ATabela {

	private Textos() {
		super("Textos", "tx");

		getColunas().add(new GenColuna("id", this));

	}

	/**
	 * refer�ncia da tabela
	 */
	private static Textos ref;

	public static Textos get() {
		return (ref == null) ? ref = new Textos() : ref;
	}

}
