/**
 * PMXCrossover.java
 * Class representing a partially matched (PMX) crossover operator
 * @author Antonio J. Nebro
 * @version 1.0
 */

package jmetal.base.operator.crossover;

import java.util.Properties;
import jmetal.base.Solution;
import jmetal.base.variable.Permutation;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 * This class allows to apply a PMX crossover operator using two parent
 * solutions. NOTE: the operator is applied to the first variable of the
 * solutions, and the type of those variables must be VariableType_.Permutation.
 */
public class PMXCrossover extends Crossover
{
	private static Class<?> PERMUTATION_SOLUTION;

	/**
	 * Constructor
	 */
	public PMXCrossover()
	{
		try
		{
			PERMUTATION_SOLUTION = Class.forName("jmetal.base.solutionType.PermutationSolutionType");
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // catch
	} // PMXCrossover

	/**
	 * Constructor
	 */
	public PMXCrossover(Properties properties)
	{
		this();
	} // PMXCrossover

	/**
	 * Perform the crossover operation
	 * 
	 * @param probability Crossover probability
	 * @param parent1 The first parent
	 * @param parent2 The second parent
	 * @return An array containig the two offsprings
	 * @throws JMException
	 */
	public Solution[] doCrossover(double probability, Solution parent1, Solution parent2) throws JMException
	{

		Solution[] offspring = new Solution[2];

		offspring[0] = new Solution(parent1);
		offspring[1] = new Solution(parent2);

		if ((parent1.getType().getClass() == PERMUTATION_SOLUTION) && (parent2.getType().getClass() == PERMUTATION_SOLUTION))
		{

			int permutationLength;

			permutationLength = ((Permutation) parent1.getDecisionVariables()[0]).getLength();

			int parent1Vector[] = ((Permutation) parent1.getDecisionVariables()[0]).vector_;
			int parent2Vector[] = ((Permutation) parent2.getDecisionVariables()[0]).vector_;
			int offspring1Vector[] = ((Permutation) offspring[0].getDecisionVariables()[0]).vector_;
			int offspring2Vector[] = ((Permutation) offspring[1].getDecisionVariables()[0]).vector_;

			if (PseudoRandom.randDouble() < probability)
			{
				int cuttingPoint1;
				int cuttingPoint2;

				// STEP 1: Get two cutting points
				cuttingPoint1 = PseudoRandom.randInt(0, permutationLength - 1);
				cuttingPoint2 = PseudoRandom.randInt(0, permutationLength - 1);
				while (cuttingPoint2 == cuttingPoint1)
					cuttingPoint2 = PseudoRandom.randInt(0, permutationLength - 1);

				if (cuttingPoint1 > cuttingPoint2)
				{
					int swap;
					swap = cuttingPoint1;
					cuttingPoint1 = cuttingPoint2;
					cuttingPoint2 = swap;
				} // if
				// STEP 2: Get the subchains to interchange
				int replacement1[] = new int[permutationLength];
				int replacement2[] = new int[permutationLength];
				for (int i = 0; i < permutationLength; i++)
					replacement1[i] = replacement2[i] = -1;

				// STEP 3: Interchange
				for (int i = cuttingPoint1; i <= cuttingPoint2; i++)
				{
					offspring1Vector[i] = parent2Vector[i];
					offspring2Vector[i] = parent1Vector[i];

					replacement1[parent2Vector[i]] = parent1Vector[i];
					replacement2[parent1Vector[i]] = parent2Vector[i];
				} // for

				// STEP 4: Repair offsprings
				for (int i = 0; i < permutationLength; i++)
				{
					if ((i >= cuttingPoint1) && (i <= cuttingPoint2))
						continue;

					int n1 = parent1Vector[i];
					int m1 = replacement1[n1];

					int n2 = parent2Vector[i];
					int m2 = replacement2[n2];

					while (m1 != -1)
					{
						n1 = m1;
						m1 = replacement1[m1];
					} // while
					while (m2 != -1)
					{
						n2 = m2;
						m2 = replacement2[m2];
					} // while
					offspring1Vector[i] = n1;
					offspring2Vector[i] = n2;
				} // for
			} // if
		} // if
		else
		{
			Configuration.logger_.severe("PMXCrossover.doCrossover: invalid type");
			Class<?> cls = String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".doCrossover()");
		} // else
		return offspring;
	} // doCrossover

	/**
	 * Executes the operation
	 * 
	 * @param object An object containing an array of two solutions
	 * @throws JMException
	 */
	public Object execute(Object object) throws JMException
	{
		Solution[] parents = (Solution[]) object;
		Double crossoverProbability = null;

		if ((parents[0].getType().getClass() != PERMUTATION_SOLUTION) || (parents[1].getType().getClass() != PERMUTATION_SOLUTION))
		{

			Configuration.logger_.severe("PMCCrossover.execute: the solutions " + "are not of the right type. The type should be 'Permutation', but " + parents[0].getType() + " and " + parents[1].getType() + " are obtained");
		}

		// crossoverProbability = (Double)parameters_.get("probability");
		crossoverProbability = (Double) getParameter("probability");

		if (parents.length < 2)
		{
			Configuration.logger_.severe("PMXCrossover.execute: operator needs two " + "parents");
			Class<?> cls = String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		}
		else if (crossoverProbability == null)
		{
			Configuration.logger_.severe("PMXCrossover.execute: probability not " + "specified");
			Class<?> cls = String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		}

		Solution[] offspring = doCrossover(crossoverProbability.doubleValue(), parents[0], parents[1]);

		return offspring;
	} // execute
} // PMXCrossover
