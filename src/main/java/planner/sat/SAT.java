package planner.sat;

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
import fr.utils.LogType;
import fr.utils.Logs;
import fr.utils.Utils;

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
	int timeout = 120;
	Heuristic.Type heuristicType;

	/**
	 * Creates a new planner with default parameters.
	 */
	public SAT() {
		this.timeout = 120;
		this.heuristicType = Heuristic.Type.FAST_FORWARD;
	}

	/**
	 * Creates a new planner with default parameters for search strategy.
	 *
	 * @param statisticState the statistics generation value.
	 * @param traceLevel     the trace level of the planner.
	 */
	public SAT(final boolean statisticState, final int traceLevel) {
		super(statisticState, traceLevel);
		this.timeout = 120;
		this.heuristicType = Heuristic.Type.FAST_FORWARD;
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
		this.timeout = timeout;
		this.heuristicType = heuristicType;
	}

	public Plan search(CodedProblem problem) {
		final Logger logger = this.getLogger();
		Objects.requireNonNull(problem);

		// On récupere l'heuristique pour sauter les etapes du solveur non necessaire
		final Heuristic heuristic = HeuristicToolKit.createHeuristic(this.heuristicType, problem);
		int MIN_STEP = heuristic.estimate(new BitState(problem.getInit()), problem.getGoal());

		final int etapesMax = 500;
		final int MAXVAR = 1000;
		int etape = 1;

		Logs.log(LogType.INFO, "* starting SAT\n");

		long startedTime = System.currentTimeMillis();

		ISolver satSolver = SolverFactory.newDefault();
		IProblem satProblem = satSolver;
		List<int[]> clauses = null;

		long timeSearch = 0;
		long timeSearchStep;

		long timeEncode = 0;
		long timeEncodeStep = System.currentTimeMillis();
		EncoderToSAT4J encoder = new EncoderToSAT4J(problem);
		clauses = encoder.init();
		timeEncode += System.currentTimeMillis() - timeEncodeStep;

		boolean isSatisfy = false;
		boolean isTimeouted = false;

		// On cherche une solution a l'etape t+1 tant que le solver en trouver pas a
		// l'etape t
		// On arrete si le timeout du solver est depasse a une etape t
		// On arrete si on a depasse l'etape maximal autorise
		// On arrete si on a trouve une solution au probleme
		try {
			while (!isSatisfy && etape < etapesMax && !isTimeouted) {

				// Recuperation de la prochaine etape
				timeEncodeStep = System.currentTimeMillis();
				clauses = encoder.getNext();
				timeEncode += System.currentTimeMillis() - timeEncodeStep;

				Logs.log(LogType.STEPS, "Etape : " + etape + " clauses : " + clauses.size());
//				Utils.printClauses4(clauses);

				// On essaye de trouver une solution aux clauses recuperer grace au solveur
				// SAT4J
				timeSearchStep = System.currentTimeMillis();
				if (etape > MIN_STEP) {
					boolean encodedToSat4J = encodeToSAT4J(satSolver, MAXVAR, clauses, timeout);
					if (encodedToSat4J) {
						isSatisfy = isSatisfiable(satProblem);
					}
				}
				timeSearch += System.currentTimeMillis() - timeSearchStep;

				if ((System.currentTimeMillis() - startedTime) / 2000 > timeout)
					isTimeouted = true;
				// On passe a la prochaine etape
				etape++;
			}
		} catch (TimeoutException e) {
			Logs.log(LogType.ERROR, "SAT4J Timeout !");
			return null;
		}

		// Recuperation des statistiques de timing
		Utils.showTime("Total time : ", timeEncode + timeSearch);
		super.getStatistics().setTimeToEncode(timeEncode);
		Utils.showTime("Encoded in : ", timeEncode);
		super.getStatistics().setTimeToSearch(timeSearch);
		Utils.showTime("Searched in : ", timeSearch);

		// Echec si on a depasse les etapes autorise
		if (etape >= etapesMax) {
			Logs.log(LogType.ERROR, "Nombre maximal d'étapes dépassé ");
			return null;
		}

		// Echec si timeout global depasse
		if (isTimeouted) {
			Logs.log(LogType.ERROR, "Timeout global dépassé ");
			return null;
		}

		// On decode le plan trouve
		final Plan plan = DecoderFromSAT4J.decodePlanFromSatResult(problem, satProblem);

		Logs.log(LogType.INFO, "\n* SAT succeeded");
		return plan;
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
	 * Verification de la satisfaisabilite du probleme avec SAT4J
	 * 
	 * @param satProblem
	 * @return True si oui, False si non ou si Timeout
	 */
	private boolean isSatisfiable(IProblem satProblem) throws TimeoutException {
		if (satProblem.isSatisfiable()) {
			Logs.log(LogType.INFO, "\nSatisfiable\n");
			return true;
		} else {
//			System.out.println("Unsatisfiable");
		}
		return false;
	}

}
