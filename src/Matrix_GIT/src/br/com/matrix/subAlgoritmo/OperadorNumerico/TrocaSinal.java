package br.com.matrix.subAlgoritmo.OperadorNumerico;

import java.util.ArrayList;
import java.util.List;

import br.com.matrix.subAlgoritmo.MetaInfo.MetaInfo;
import br.com.matrix.subAlgoritmo.MetaInfo.MetaInfoAssinatura;
import br.com.matrix.subAlgoritmo.MetaInfo.Quantidade;
import br.com.matrix.subAlgoritmo.MetaInfo.Tipo;

public class TrocaSinal extends OperadorNumerico {

    public TrocaSinal() {
	super(getParam());	
    }

    @Override
    public void executar() {
	paramEntrada.get(0).executar();
	result = ((Double)paramEntrada.get(0).retornar())*-1;
    }


    private static List<MetaInfoAssinatura> getParam() {
	List<MetaInfoAssinatura> l = new ArrayList<MetaInfoAssinatura>(1);
	l.add(MetaInfo.fabricarAssinatura(Tipo.TP_NUMERICO, Quantidade.fabricarQt(1, 1)));
	return l;
    }
    
    @Override
    public String toString() {
	return "(-1 * "+paramEntrada.get(0).toString()+")";
    }
}
