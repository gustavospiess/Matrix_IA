package br.com.matrix.banco.tabelas;

import br.com.matrix.banco.tabelas.classesAbstratas.ATabela;
import br.com.matrix.banco.tabelas.propTabelas.GenColuna;

public class Sugestores extends ATabela {

	private Sugestores() {
		super("Sugestores", "su");

		getColunas().add(new GenColuna("id", this));
		getColunas().add(new GenColuna("qt_uso", this));
		getColunas().add(new GenColuna("qt_acerto", this));
		getColunas().add(new GenColuna("dt", this));
		getColunas().add(new GenColuna("ie_ativo", this));

	}

	/**
	 * refer�ncia da tabela
	 */
	private static Sugestores ref;

	public static Sugestores get() {
		return (ref == null) ? ref = new Sugestores() : ref;
	}

}
