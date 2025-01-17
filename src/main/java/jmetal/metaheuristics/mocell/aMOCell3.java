/**
 * aMOCell3b.java
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.metaheuristics.mocell;

import java.util.Comparator;
import jmetal.base.Algorithm;
import jmetal.base.Operator;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.operator.comparator.CrowdingComparator;
import jmetal.base.operator.comparator.DominanceComparator;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.Neighborhood;
import jmetal.util.PseudoRandom;
import jmetal.util.Ranking;
import jmetal.util.archive.CrowdingArchive;

/**
 * This class represents an asynchronous version of MOCell algorithm. It is
 * based on aMOCell1 but replacing the worst neighbor.
 */
public class aMOCell3 extends Algorithm
{

	private Problem problem_; // The problem to solve

	public aMOCell3(Problem problem)
	{
		problem_ = problem;
	}

	/**
	 * Execute the algorithm
	 * 
	 * @throws JMException
	 */
	public SolutionSet execute() throws JMException, ClassNotFoundException
	{
		int populationSize, archiveSize, maxEvaluations, evaluations, feedBack;
		Operator mutationOperator, crossoverOperator, selectionOperator;
		SolutionSet currentPopulation;
		CrowdingArchive archive;
		SolutionSet[] neighbors;
		Neighborhood neighborhood;
		Comparator<Solution> dominance = new DominanceComparator();
		Comparator<Solution> crowdingComparator = new CrowdingComparator();
		Distance distance = new Distance();

		// Read the parameters
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		archiveSize = ((Integer) getInputParameter("archiveSize")).intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
		feedBack = ((Integer) getInputParameter("feedBack")).intValue();

		// Read the operators
		mutationOperator = operators_.get("mutation");
		crossoverOperator = operators_.get("crossover");
		selectionOperator = operators_.get("selection");

		// Initialize the variables
		// Initialize the population and the archive
		currentPopulation = new SolutionSet(populationSize);
		archive = new CrowdingArchive(archiveSize, problem_.getNumberOfObjectives());
		evaluations = 0;
		neighborhood = new Neighborhood(populationSize);
		neighbors = new SolutionSet[populationSize];

		// Create the comparator for check dominance
		dominance = new DominanceComparator();

		// Create the initial population
		for (int i = 0; i < populationSize; i++)
		{
			Solution individual = new Solution(problem_);
			problem_.evaluate(individual);
			problem_.evaluateConstraints(individual);
			currentPopulation.add(individual);
			individual.setLocation(i);
			evaluations++;
		}

		while (evaluations < maxEvaluations)
		{
			for (int ind = 0; ind < currentPopulation.size(); ind++)
			{
				Solution individual = new Solution(currentPopulation.get(ind));

				Solution[] parents = new Solution[2];
				Solution[] offSpring;

				// neighbors[ind] =
				// neighborhood.getFourNeighbors(currentPopulation,ind);
				neighbors[ind] = neighborhood.getEightNeighbors(currentPopulation, ind);
				neighbors[ind].add(individual);

				// parents
				parents[0] = (Solution) selectionOperator.execute(neighbors[ind]);
				parents[1] = (Solution) selectionOperator.execute(neighbors[ind]);

				// Create a new individual, using genetic operators mutation and
				// crossover
				offSpring = (Solution[]) crossoverOperator.execute(parents);
				mutationOperator.execute(offSpring[0]);

				// ->Evaluate individual an his constraints
				problem_.evaluate(offSpring[0]);
				problem_.evaluateConstraints(offSpring[0]);
				evaluations++;
				// <-Individual evaluated

				int flag = dominance.compare(individual, offSpring[0]);

				if (flag == 1)
				{ // The new individuals dominate
					offSpring[0].setLocation(individual.getLocation());
					currentPopulation.replace(offSpring[0].getLocation(), offSpring[0]);
					archive.add(new Solution(offSpring[0]));
				}
				else if (flag == 0)
				{// The individuals are non-dominates
					neighbors[ind].add(offSpring[0]);
					offSpring[0].setLocation(-1);
					Ranking rank = new Ranking(neighbors[ind]);
					for (int j = 0; j < rank.getNumberOfSubfronts(); j++)
					{
						distance.crowdingDistanceAssignment(rank.getSubfront(j), problem_.getNumberOfObjectives());
					}
					neighbors[ind].sort(crowdingComparator);
					Solution worst = neighbors[ind].get(neighbors[ind].size() - 1);

					if (worst.getLocation() == -1)
					{// The worst is the offspring
						archive.add(new Solution(offSpring[0]));
					}
					else
					{
						offSpring[0].setLocation(worst.getLocation());
						currentPopulation.replace(offSpring[0].getLocation(), offSpring[0]);
						archive.add(new Solution(offSpring[0]));
					}
				}
			}

			// Store a portion of the archive into the population
			distance.crowdingDistanceAssignment(archive, problem_.getNumberOfObjectives());
			for (int j = 0; j < feedBack; j++)
			{
				if (archive.size() > j)
				{
					int r = PseudoRandom.randInt(0, currentPopulation.size() - 1);
					if (r < currentPopulation.size())
					{
						Solution individual = archive.get(j);
						individual.setLocation(r);
						currentPopulation.replace(r, new Solution(individual));
					}
				}
			}
		}

		return archive;
	}
} // aMOCell3

