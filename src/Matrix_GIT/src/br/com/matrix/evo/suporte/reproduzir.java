package br.com.matrix.evo.suporte;

import java.util.function.BiFunction;

/**
 * 
 * @author GustavoHenrique
 * 
 *         Interface de reprodu��o para entidadePadrao
 *
 * @paramReq <G>
 *            - Tipagem do codigo gen�tico
 * @paramReq <R>
 *            - Tipagem do retorno das entidades
 * @paramReq <P>
 *            - Parametro de execucao
 */
public interface reproduzir<G, R, P>
		extends BiFunction<GrupoEntidadesEvo<G, R, P>, Integer, GrupoEntidadesEvo<G, R, P>> {

}
