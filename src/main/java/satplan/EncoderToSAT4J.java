package satplan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.util.BitExp;
import fr.uga.pddl4j.util.BitOp;
import fr.uga.pddl4j.util.BitVector;
import fr.uga.pddl4j.util.IntExp;
import fr.utils.Utils;

public class EncoderToSAT4J {

	private List<int[]> clauses = new ArrayList<int[]>();

	private BitExp initState;
	private List<IntExp> relevantfacts;
	private BitExp goalState;
	private int nbgoal = 0;

	private int etape = 1;

	private CodedProblem problem;

	public EncoderToSAT4J(CodedProblem problem) {
		this.initState = problem.getInit();
		this.relevantfacts = problem.getRelevantFacts();
		this.goalState = problem.getGoal();
		this.problem = problem;
	}

	public List<int[]> init() {
		addInit();
		return clauses;
	}

	public List<int[]> getNext() {
		if (nbgoal > 0)
			clauses = clauses.subList(0, clauses.size() - nbgoal);
		addActions();
		addGoal();
		etape++;
		return clauses;
	}
	
	private void addActions() {
		int actionSize = problem.getOperators().size();
		Map<Integer, List<Integer>> posAxiomsMap = new HashMap<Integer, List<Integer>>(relevantfacts.size());
		Map<Integer, List<Integer>> negAxiomsMap = new HashMap<Integer, List<Integer>>(relevantfacts.size());
		
		for (int actionIndex = 0; actionIndex < actionSize; actionIndex++) {
			int actionID = actionIndex + relevantfacts.size();

			final BitOp action = problem.getOperators().get(actionIndex);
			final BitVector posPreconds = action.getPreconditions().getPositive();
			final BitVector negPreconds = action.getPreconditions().getNegative();
			final BitVector posEffects = action.getUnconditionalEffects().getPositive();
			final BitVector negEffects = action.getUnconditionalEffects().getNegative();

//            System.out.println("Action: " + actionIndex);
			// Implication to CNF memento : A => (x & y & !z) == (x | !A) & (y | !A) & (!z |
			// !A)
			// Ajout preconditions et effets de l'action
			for (int factIndex = 0; factIndex < relevantfacts.size(); factIndex++) {
				// Preconditions
				if (posPreconds.get(factIndex)) {
            		addImply(Utils.couple(actionID, etape), Utils.couple(factIndex, etape));            		
				}
				if (negPreconds.get(factIndex)) {
            		addImply(Utils.couple(actionID, etape), -Utils.couple(factIndex, etape));               		
				}
				// Effects
				if (posEffects.get(factIndex)) {
            		addImply(Utils.couple(actionID, etape), Utils.couple(factIndex, etape+1));
            		putAxiom(actionID, posAxiomsMap, factIndex, 1);
				}
				if (negEffects.get(factIndex)) {
            		addImply(Utils.couple(actionID, etape), -Utils.couple(factIndex, etape+1));
            		putAxiom(actionID, negAxiomsMap, factIndex, -1);
				}
			}
			// Ajoute qu'il peut y avoir une unique action d'effectué
			addOneActionAtTime(actionIndex, actionSize);
		}
		addAxioms(posAxiomsMap);
		addAxioms(negAxiomsMap);
	}

	/**
	 * Ajoute qu'il peut y avoir une seul action d'effectuer à une étape t
	 * @param actionID 
	 */
	private void addOneActionAtTime(int actionIndex, int actionSize) {
		int factSize = relevantfacts.size();
		for (int actionIndex2 = actionIndex + 1; actionIndex2 < actionSize; actionIndex2++) {
			int[] coupleAction = new int[2];
			coupleAction[0] = -Utils.couple((actionIndex + factSize), etape);
			coupleAction[1] = -Utils.couple((actionIndex2 + factSize), etape);
			clauses.add(coupleAction);
		}
	}

	private void addAxioms(Map<Integer, List<Integer>> axiomsMap) {
		for (int factIndex = 0; factIndex < relevantfacts.size(); factIndex++) {
			if(axiomsMap.containsKey(factIndex)) {
				int[] array = axiomsMap.get(factIndex).stream().mapToInt(i -> i).toArray();
				clauses.add(array);
			}
		}
	}
	/**
	 * Ajoutes les clauses indiquant qu'un changement d'état est la conséquence d'une action à une étape t
	 * Et qu'une action à une étape t est la cause d'un changement d'état 
	 * @param posAxiomsMap 
	 * @param factIndex, 
	 */
	private void putAxiom(int actionIndex, Map<Integer, List<Integer>> axiomsMap, int factIndex, int pos) {
		if(!axiomsMap.containsKey(factIndex)) {
			axiomsMap.put(factIndex, 
					new ArrayList<Integer>(Arrays.asList(pos*Utils.couple(factIndex, etape), -pos*Utils.couple(factIndex, etape+1))));
		}
		axiomsMap.get(factIndex).add(Utils.couple(actionIndex, etape));
	}

	// A => (.&.&...)
	private void addImply(int impliquant, int...implieds) {
		for(int implied : implieds) {
			int[] clause = new int[2];
			clause[0] = -impliquant;
			clause[1] = implied;
			clauses.add(clause);
		}
	}

	private void addInit() {
		for (int i = 0; i < relevantfacts.size(); i++) {
			boolean predicateState = initState.getPositive().get(i);
			int id = (predicateState ? 1 : -1) * Utils.couple(i, etape);
//			System.out.println("AddInit: " + Utils.ids(id));
			clauses.add(new int[] { id });
		}
	}

	private void addGoal() {
		nbgoal = 0;
		for (int i = 0; i < relevantfacts.size(); i++) {
			int[] clause = new int[1];
			if (goalState.getPositive().get(i)) {
				clause[0] = Utils.couple(i, etape + 1);
//				System.out.println("addGoal+:" + Utils.ids(clause[0]));
				clauses.add(clause);
				nbgoal++;
			} else if (goalState.getNegative().get(i)) {
				clause[0] = -Utils.couple(i, etape + 1);
//				System.out.println("addGoal-:" + Utils.ids(clause[0]));
				clauses.add(clause);
				nbgoal++;
			}
		}
	}

}
