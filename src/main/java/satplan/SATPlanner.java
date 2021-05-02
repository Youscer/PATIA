package satplan;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.planners.statespace.AbstractStateSpacePlanner;
import fr.uga.pddl4j.util.Plan;

public class SATPlanner extends AbstractStateSpacePlanner {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5901410094129347200L;

	public Plan search(CodedProblem problem) {
		
		final int MAXVAR = 10;
		final int NBCLAUSES = 1;

		ISolver satSolver = SolverFactory.newDefault();

		// prepare the solver to accept MAXVAR variables. MANDATORY for MAXSAT solving
		satSolver.newVar(MAXVAR);
		satSolver.setExpectedNumberOfClauses(NBCLAUSES);

		// Feed the solver using Dimacs format, using arrays of int
		// (best option to avoid dependencies on SAT4J IVecInt)
		for (int i = 0; i < NBCLAUSES; i++) {
			// get the clause from somewhere
			// the clause should not contain a 0, only integer (positive or negative)
			// with absolute values less or equal to MAXVAR
			// e.g. int [] clause = {1, -3, 7}; is fine
			// while int [] clause = {1, -3, 7, 0}; is not fine
			int[] clause = new int[3];
			clause[0] = 1;
			clause[1] = -3;
			clause[2] = 7;
			
			// adapt Array to IVecInt
			VecInt vecint = new VecInt(clause);
			try {
				satSolver.addClause(vecint);
			} catch (ContradictionException e) {
				System.out.println("Unsatisfiable (trivial)!");
			}
		}
		
		IProblem satProblem = satSolver;
		try {
			if (satProblem.isSatisfiable()) {
			   System.out.println("Satisfiable");
			} else {
				System.out.println("Unsatisfiable");
			}
		} catch (TimeoutException e) {
			System.out.println("Timout when solving SAT4J");
		}

		return null;
	}

}
