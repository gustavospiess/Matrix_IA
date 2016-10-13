package br.com.matrix.subAlgEvo;

import java.util.List;

import br.com.matrix.evo.EntidadeEvo;
import br.com.matrix.evo.padrao.EntidadePadrao;
import br.com.matrix.evo.padrao.GerarFitness;
import br.com.matrix.evo.suporte.Executar;
import br.com.matrix.evo.suporte.Fabricar;
import br.com.matrix.subAlgoritmo.SubAlgoritmo;

public class EntidadeSubAlgEvo<R, P> extends EntidadePadrao<SubAlgoritmo<?>, R, P> {

    public EntidadeSubAlgEvo(Executar<SubAlgoritmo<?>, P, R> exe,
	    GerarFitness<EntidadePadrao<SubAlgoritmo<?>, R, P>, SubAlgoritmo<?>, R, P> ftn, List<SubAlgoritmo<?>> gen,
	    List<SubAlgoritmo<?>> genPool, int qtGenes) {
	super(exe, EntidadePadrao.getMutPadraoTrocaComplexa(),
		EntidadePadrao.getRepOrdenada(getFabricar(exe, ftn, gen, genPool)), ftn,
		(new SubAlgEvoPadronizarCG(genPool)), gen, qtGenes);
	padronizaCG();
    }

    public EntidadeSubAlgEvo(Executar<SubAlgoritmo<?>, P, R> exe,
	    GerarFitness<EntidadePadrao<SubAlgoritmo<?>, R, P>,SubAlgoritmo<?>, R, P> ftn, 
	    List<SubAlgoritmo<?>> gen,
	    List<SubAlgoritmo<?>> genPool) {
	super(exe, EntidadePadrao.getMutPadraoTrocaComplexa(),
		EntidadePadrao.getRepOrdenada(getFabricar(exe, ftn, gen, genPool)), ftn,
		(new SubAlgEvoPadronizarCG(genPool)));
    }

    private static <R, P> Fabricar<SubAlgoritmo<?>, R, P> getFabricar(Executar<SubAlgoritmo<?>, P, R> exe,
	    GerarFitness<EntidadePadrao<SubAlgoritmo<?>, R, P>, SubAlgoritmo<?>, R, P> ftn, List<SubAlgoritmo<?>> gen,
	    List<SubAlgoritmo<?>> genPool) {
	return new Fabricar<SubAlgoritmo<?>, R, P>() {

	    @Override
	    public EntidadeEvo<SubAlgoritmo<?>, R, P> get() {
		return new EntidadeSubAlgEvo<>(exe, ftn, gen, genPool);
	    }
	};
    }
    
    public void resetExecutado(){
	for (SubAlgoritmo<?> sa : getCG()) {
	    sa.resetExecutado();
	}
    }

}
