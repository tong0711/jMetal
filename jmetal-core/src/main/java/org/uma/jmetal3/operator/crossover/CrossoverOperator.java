package org.uma.jmetal3.operator.crossover;

import org.uma.jmetal3.core.Operator;
import org.uma.jmetal3.core.Solution;

import java.util.List;

/**
 * Created by Antonio J. Nebro on 04/09/14.
 */
public interface CrossoverOperator<Source extends List<? extends Solution>, Result extends List<? extends Solution>> extends Operator<Source,Result> {
}
