//  SPEA2Runner.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.runner;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.spea2.SPEA2;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.Kursawe;
import jmetal.problems.ProblemFactory;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Configuration;
import jmetal.util.JMetalException;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * Class for configuring and running the SPEA2 algorithm
 */
public class SPEA2Runner {
  public static Logger logger_;      
  public static FileHandler fileHandler_; 

  /**
   * @param args Command line arguments. The first (optional) argument specifies
   *             the problem to solve.
   * @throws jmetal.util.JMetalException
   * @throws IOException
   * @throws SecurityException Usage: three options
   *                           - jmetal.runner.SPEA2_main
   *                           - jmetal.runner.SPEA2_main problemName
   *                           - jmetal.runner.SPEA2_main problemName ParetoFrontFile
   */
  public static void main(String[] args) throws JMetalException, IOException, ClassNotFoundException {
    Problem problem;
    Algorithm algorithm;
    Operator crossover;
    Operator mutation;
    Operator selection;

    QualityIndicator indicators;

    // Logger object and file to store log messages
    logger_ = Configuration.logger_;
    fileHandler_ = new FileHandler("SPEA2.log");
    logger_.addHandler(fileHandler_);

    indicators = null;
    if (args.length == 1) {
      Object[] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0], params);
    } else if (args.length == 2) {
      Object[] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0], params);
      indicators = new QualityIndicator(problem, args[1]);
    } else {
      problem = new Kursawe("Real", 3);
      //problem = new Water("Real");
      //problem = new ZDT1("ArrayReal", 1000);
      //problem = new ZDT4("BinaryReal");
      //problem = new WFG1("Real");
      //problem = new DTLZ1("Real");
      //problem = new OKA2("Real") ;
    }

    algorithm = new SPEA2();
    algorithm.setProblem(problem);

    // Algorithm parameters
    algorithm.setInputParameter("populationSize", 100);
    algorithm.setInputParameter("archiveSize", 100);
    algorithm.setInputParameter("maxEvaluations", 25000);

    // Mutation and Crossover for Real codification 
    HashMap<String, Object> crossoverParameters = new HashMap<String, Object>();
    crossoverParameters.put("probability", 0.9);
    crossoverParameters.put("distributionIndex", 20.0);
    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", crossoverParameters);

    HashMap<String, Object> mutationParameters = new HashMap<String, Object>();
    mutationParameters.put("probability", 1.0 / problem.getNumberOfVariables());
    mutationParameters.put("distributionIndex", 20.0);
    mutation = MutationFactory.getMutationOperator("PolynomialMutation", mutationParameters);

    // Selection operator 
    HashMap<String, Object> selectionParameters = null; // FIXME: why we are passing null?
    selection = SelectionFactory.getSelectionOperator("BinaryTournament", selectionParameters);

    // Add the operators to the algorithm
    algorithm.addOperator("crossover", crossover);
    algorithm.addOperator("mutation", mutation);
    algorithm.addOperator("selection", selection);

    // Execute the algorithm
    long initTime = System.currentTimeMillis();
    SolutionSet population = algorithm.execute();
    long estimatedTime = System.currentTimeMillis() - initTime;

    // Result messages 
    logger_.info("Total execution time: " + estimatedTime + "ms");
    logger_.info("Objectives values have been writen to file FUN");
    population.printObjectivesToFile("FUN");
    logger_.info("Variables values have been writen to file VAR");
    population.printVariablesToFile("VAR");

    if (indicators != null) {
      logger_.info("Quality indicators");
      logger_.info("Hypervolume: " + indicators.getHypervolume(population));
      logger_.info("GD         : " + indicators.getGD(population));
      logger_.info("IGD        : " + indicators.getIGD(population));
      logger_.info("Spread     : " + indicators.getSpread(population));
      logger_.info("Epsilon    : " + indicators.getEpsilon(population));
    }
  }
}
