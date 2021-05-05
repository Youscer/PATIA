package satplan;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.logging.log4j.Logger;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.heuristics.relaxation.Heuristic;
import fr.uga.pddl4j.heuristics.relaxation.HeuristicToolKit;
import fr.uga.pddl4j.planners.statespace.AbstractStateSpacePlanner;
import fr.uga.pddl4j.util.BitState;
import fr.uga.pddl4j.util.Plan;

/**
 * Classe de planification utilisant SAT4J Certaines parties basique du code
 * sont basé sur le wiki du projet pddl4j ainsi que de la classe HSP de D.
 * Pellier
 * 
 * @author Youcef K.
 *
 */
public class SAT extends AbstractStateSpacePlanner {

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
	public SAT(final int timeout, final Heuristic.Type heuristicType, final double weight, final boolean statisticState,
			final int traceLevel) {
		super(statisticState, traceLevel);
	}

	public Plan search(CodedProblem problem) {
		final Logger logger = this.getLogger();
		Objects.requireNonNull(problem);
//		System.out.println(problem.toString(problem.getInit()));
		final Heuristic heuristic = HeuristicToolKit.createHeuristic(Heuristic.Type.FAST_FORWARD, problem);
		int MIN_STEP = heuristic.estimate(new BitState(problem.getInit()), problem.getGoal());
		logger.trace("* starting SAT\n");

		final int timeout = 200;
		final int etapesMax = 100000;
		final int MAXVAR = 1000;
		int etape = 1;

		// initialisation des clauses avec SAT4J
		ISolver satSolver = SolverFactory.newDefault();
		IProblem satProblem = satSolver;
		List<int[]> clauses = null;
		
		// Vérification de la satisfaisabilité avec SAT4J
		long timeSearch = 0;
		long timeSearchStep = System.currentTimeMillis();
		long timeEncode = 0;
		long timeEncodeStep = System.currentTimeMillis();
		EncoderToSAT4J encoder = new EncoderToSAT4J(problem);
		clauses = encoder.init();
		timeEncode += System.currentTimeMillis() - timeEncodeStep;
		boolean isSatisfy = false;
		try {
			while (!isSatisfy && etape < etapesMax) {

				timeEncodeStep = System.currentTimeMillis();
				clauses = encoder.getNext();
				timeEncode += System.currentTimeMillis() - timeEncodeStep;
				System.out.println("Etape : " + etape + " clauses : " + clauses.size());
//				Utils.printClauses4(clauses);
				timeSearchStep = System.currentTimeMillis();
				if(etape>MIN_STEP) {
					boolean encodedToSat4J = encodeToSAT4J(satSolver, MAXVAR, clauses, timeout);
					if (encodedToSat4J) {
						isSatisfy = isSatisfiable(satProblem);
					}
				}
				timeSearch += System.currentTimeMillis() - timeSearchStep;
				etape++;
			}
		} catch (TimeoutException e) {
			System.out.println("TimeOut !");
		}
		
		showTime("Total time : ", timeEncode+timeSearch);
		super.getStatistics().setTimeToEncode(timeEncode);
		showTime("Encoded in : ", timeEncode);
		super.getStatistics().setTimeToSearch(timeSearch);
		showTime("Searched in : ", timeSearch);
		

		System.out.println("étapes: " + etape);
		if (etape == etapesMax) { System.out.println("Nombre maximum d'étape dépassé"); return null; }

		final Plan plan = DecoderFromSAT4J.decodePlanFromSatResult(problem, satProblem);
		
		logger.trace("* SAT succeeded\n");
		return plan;
	}

	private void showTime(String prefix, long time) {
		Date date = new Date(time);
		DateFormat formatter = new SimpleDateFormat("mm:ss.SSS ");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		System.out.println( prefix + formatter.format(date) );
		
	}

	/**
	 * Initialise les clauses avec SAT4J
	 * 
	 * @param satSolver
	 * @param MAXVAR
	 * @param clauses
	 * @return False si le problème est trivialement insatisfaisable(contradiction),
	 *         true sinon
	 */
	private boolean encodeToSAT4J(ISolver satSolver, final int MAXVAR, List<int[]> clauses, int timeout) {
		final int NBCLAUSES = clauses.size();

		// prepare the solver to accept MAXVAR variables. MANDATORY for MAXSAT solving
		satSolver.reset();
		satSolver.setTimeout(timeout);
		satSolver.newVar(1000000);
		satSolver.setExpectedNumberOfClauses(clauses.size());

		// Feed the solver using Dimacs format, using arrays of int
		// (best option to avoid dependencies on SAT4J IVecInt)
		for (int i = 0; i < NBCLAUSES; i++) {
			// get the clause from somewhere
			// the clause should not contain a 0, only integer (positive or negative)
			// with absolute values less or equal to MAXVAR
			// e.g. int [] clause = {1, -3, 7}; is fine
			// while int [] clause = {1, -3, 7, 0}; is not fine
			VecInt vecint = new VecInt(clauses.get(i));
			try {
				satSolver.addClause(vecint);
			} catch (ContradictionException e) {
//				System.out.println("Unsatisfiable (trivial)!");
				return false;
			}
		}
		return true;
	}

	/**
	 * Vérification de la satisfaisabilité du problème avec SAT4J
	 * 
	 * @param satProblem
	 * @return True si oui, False si non ou si Timeout
	 */
	private boolean isSatisfiable(IProblem satProblem) throws TimeoutException {
		if (satProblem.isSatisfiable()) {
			System.out.println("Satisfiable");
			return true;
		} else {
//			System.out.println("Unsatisfiable");
		}
		return false;
	}

}
