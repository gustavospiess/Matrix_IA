package br.com.matrix.banco.tabelas;

import br.com.matrix.banco.tabelas.classesAbstratas.ATabela;
import br.com.matrix.banco.tabelas.propTabelas.GenColuna;
import br.com.matrix.banco.tabelas.propTabelas.ColunaFk;

public class Textos_has_Frases extends ATabela {

	private Textos_has_Frases() {
		super("Textos_has_Frases", "thf");

		getDependecias().add(new ColunaFk(Textos.get().getId(), "id_t", this));
		getDependecias().add(new ColunaFk(Frases.get().getId(), "id_f", this));

		getColunas().add(new GenColuna("id", this));
		getColunas().addAll(getDependecias());
	}

	/**
	 * referÍncia da tabela
	 */
	private static Textos_has_Frases ref;

	public static Textos_has_Frases get() {
		return (ref == null) ? ref = new Textos_has_Frases() : ref;
	}

}
