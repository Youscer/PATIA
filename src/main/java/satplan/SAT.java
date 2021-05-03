package satplan;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.Logger;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.heuristics.relaxation.Heuristic;
import fr.uga.pddl4j.planners.statespace.AbstractStateSpacePlanner;
import fr.uga.pddl4j.util.BitState;
import fr.uga.pddl4j.util.Plan;

/**
 * Classe de planification utilisant SAT4J
 * Certaines parties basique du code sont basé sur le wiki du projet pddl4j ainsi que de la classe HSP de D. Pellier 
 * 
 * @author Youcef K.
 *
 */
public class SAT extends AbstractStateSpacePlanner {

	private List<Clause> clauses = new ArrayList<Clause>();
	/**
	 * 
	 */
	private static final long serialVersionUID = -5901410094129347200L;
	
    /**
     * Creates a new planner with default parameters.
     */
    public SAT() {
    }
	
    /**
     * Creates a new planner with default parameters for search strategy.
     *
     * @param statisticState the statistics generation value.
     * @param traceLevel     the trace level of the planner.
     */
    public SAT(final boolean statisticState, final int traceLevel) {
        super(statisticState, traceLevel);
    }
    
    /**
     * Creates a new planner.
     *
     * @param timeout        the time out of the planner.
     * @param heuristicType  the heuristicType to use to solve the planning problem.
     * @param weight         the weight set to the heuristic.
     * @param statisticState the statistics generation value.
     * @param traceLevel     the trace level of the planner.
     */
    public SAT(final int timeout, final Heuristic.Type heuristicType, final double weight,
               final boolean statisticState, final int traceLevel) {
        super(statisticState, traceLevel);
    }

	public Plan search(CodedProblem problem) {
		final Logger logger = this.getLogger();
		Objects.requireNonNull(problem);
		
		logger.trace("* starting SAT\n");
		
        // We get the initial state from the planning problem
        final BitState init = new BitState(problem.getInit());
        
        final int MAXVAR = 100;
//        clauses.add(new Clause(1));
        
        //initialisation des clauses avec SAT4J
        ISolver satSolver = SolverFactory.newDefault();       
		boolean encodedToSat4J = encodeToSAT4J(satSolver, MAXVAR, clauses);
		if( !encodedToSat4J) { return null; }
		
		IProblem satProblem = satSolver;
		
		//Vérification de la satisfaisabilité avec SAT4J
		boolean isSatisfy = isSatisfiable(satProblem);
		if( !isSatisfy ) { return null; }
		
		//Récupération du résultat avec SAT4J
        for (int etat : satProblem.model()) {
            System.out.println("Var: " + etat);
        }

		return null;
	}
	
	/**
	 * Initialise les clauses avec SAT4J
	 * @param satSolver
	 * @param MAXVAR
	 * @param clauses
	 * @return False si le problème est trivialement insatisfaisable(contradiction), true sinon
	 */
	private boolean encodeToSAT4J(ISolver satSolver, final int MAXVAR, List<Clause> clauses) {
        final int NBCLAUSES = clauses.size();
		
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
			VecInt vecint = new VecInt(clauses.get(i).getLitteralsArray());
			try {
				satSolver.addClause(vecint);
			} catch (ContradictionException e) {
				System.out.println("Unsatisfiable (trivial)!");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Vérification de la satisfaisabilité du problème avec SAT4J
	 * @param satProblem
	 * @return True si oui, False si non ou si Timeout
	 */
	private boolean isSatisfiable(IProblem satProblem) {
		try {
			if (satProblem.isSatisfiable()) {
			   System.out.println("Satisfiable");
			   return true;
			} else {
				System.out.println("Unsatisfiable");
			}
		} catch (TimeoutException e) {
			System.out.println("Timeout when solving SAT4J");
		}
		return false;
	}

}
