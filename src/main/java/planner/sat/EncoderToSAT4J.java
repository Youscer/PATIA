package planner.sat;

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

/**
 * Encodeur permettant de passer d'un CodedProblem de PDDL4J a une liste de clauses
 * 
 * @author youcef
 *
 */
public class EncoderToSAT4J {

	private List<int[]> clauses = new ArrayList<int[]>();

	private BitExp initState;
	private List<IntExp> relevantfacts;
	private BitExp goalState;
	private int nbgoal = 0;

	private int etape = 1;

	private CodedProblem problem;

	/**
	 * Constructeur de l'encodeur
	 * 
	 * @param problem Probleme a encoder
	 */
	public EncoderToSAT4J(CodedProblem problem) {
		this.initState = problem.getInit();
		this.relevantfacts = problem.getRelevantFacts();
		this.goalState = problem.getGoal();
		this.problem = problem;
	}

	/**
	 * Initialise l'encodeur en ajoutant l'etat initial
	 * 
	 * @return l'etat des clauses actuel
	 */
	public List<int[]> init() {
		addInit();
		return clauses;
	}

	/**
	 * Effectue une etape d'encodage
	 * Ajoute les nouvelles clauses a l'etat actuel de l'encodeur
	 * 
	 * @return Les clauses de la prochaine etape
	 */
	public List<int[]> getNext() {
		
		// Retirer les goals de la liste pour les ajouter a l'etape i+1 ensuite
		if (nbgoal > 0)
			clauses = clauses.subList(0, clauses.size() - nbgoal);
		
		// Ajout des actions et de leurs impliquations
		addActions();
		
		// Ajout des goals a l'etape i+1
		addGoal();
		etape++;
		return clauses;
	}
	
	/**
	 * Ajout des actions et de leurs impliquations
	 * 
	 */
	private void addActions() {
		int actionSize = problem.getOperators().size();
		Map<Integer, List<Integer>> posAxiomsMap = new HashMap<Integer, List<Integer>>(relevantfacts.size());
		Map<Integer, List<Integer>> negAxiomsMap = new HashMap<Integer, List<Integer>>(relevantfacts.size());
		
		// Pour toutes les actions
		for (int actionIndex = 0; actionIndex < actionSize; actionIndex++) {
			int actionID = actionIndex + relevantfacts.size();

			final BitOp action = problem.getOperators().get(actionIndex);
			final BitVector posPreconds = action.getPreconditions().getPositive();
			final BitVector negPreconds = action.getPreconditions().getNegative();
			final BitVector posEffects = action.getUnconditionalEffects().getPositive();
			final BitVector negEffects = action.getUnconditionalEffects().getNegative();

			// Implication to CNF memento : A => (x & y & !z) == (x | !A) & (y | !A) & (!z |
			// !A)
			// Pour toutes les faits 
			for (int factIndex = 0; factIndex < relevantfacts.size(); factIndex++) {
				// Ajouts des clauses de precondition si l'action en une sur ce predicat
				if (posPreconds.get(factIndex)) {
            		addImply(Utils.couple(actionID, etape), Utils.couple(factIndex, etape));            		
				}
				if (negPreconds.get(factIndex)) {
            		addImply(Utils.couple(actionID, etape), -Utils.couple(factIndex, etape));               		
				}
				// Ajouts des clauses d'effets si l'action en une sur ce predicat
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
		// Ajoute les implications de changement d'etat
		addAxioms(posAxiomsMap);
		addAxioms(negAxiomsMap);
	}

	/**
	 * Ajoute qu'il peut y avoir une seul action d'effectuer à une étape t
	 * 
	 * @param actionID 
	 */
	private void addOneActionAtTime(int actionIndex, int actionSize) {
		int factSize = relevantfacts.size();
		// Pour chaque action, forme un couple (!action1 OU !action2) et l'ajoute aux clauses
		for (int actionIndex2 = actionIndex + 1; actionIndex2 < actionSize; actionIndex2++) {
			int[] coupleAction = new int[2];
			coupleAction[0] = -Utils.couple((actionIndex + factSize), etape);
			coupleAction[1] = -Utils.couple((actionIndex2 + factSize), etape);
			clauses.add(coupleAction);
		}
	}

	/**
	 * Ajoute les clauses d'axiom.
	 * Ceux-ci sont stocke dans une HashMap pour optimiser l'encodage
	 * S'execute une fois pour les effets positifs et une autre pour les negatifs
	 * 
	 * @param axiomsMap
	 */
	private void addAxioms(Map<Integer, List<Integer>> axiomsMap) {
		// Pour chaque fait, recupere sa liste dans la map et l'ajoute aux clauses
		for (int factIndex = 0; factIndex < relevantfacts.size(); factIndex++) {
			// Si ce fait n'a aucun lien avec aucune action il n'apparait pas dans la map
			if(axiomsMap.containsKey(factIndex)) {
				int[] array = axiomsMap.get(factIndex).stream().mapToInt(i -> i).toArray();
				clauses.add(array);
			}
		}
	}

	/**
	 * Ajoutes les clauses indiquant qu'un changement d'état est la conséquence d'une action à une étape t
	 * Et qu'une action à une étape t est la cause d'un changement d'état
	 * 
	 * @param actionIndex L'action qui s'applique
	 * @param axiomsMap   La map permettant de retrouver ulterieurement les axioms
	 * @param factIndex   Le fait qu'il implique
	 * @param pos         1 si effet positif, moins 1 si action negatif
	 */
	private void putAxiom(int actionIndex, Map<Integer, List<Integer>> axiomsMap, int factIndex, int pos) {
		// Si ce fait n'a pas encore eu de lien action/fait, on cree sa liste
		if(!axiomsMap.containsKey(factIndex)) {
			// Ajout des premier litteraux 
			// Fait1_1 => Fait1_2 pour negatif
			// -Fait1_1 => -Fait1_2 pour positif
			axiomsMap.put(factIndex, 
					new ArrayList<Integer>(Arrays.asList(pos*Utils.couple(factIndex, etape), -pos*Utils.couple(factIndex, etape+1))));
		}
		// Ajout de l'action dans la clause
		axiomsMap.get(factIndex).add(Utils.couple(actionIndex, etape));
	}

	// A => (.&.&...)
	/**
	 * Cree une implication entre un identifiant et une liste d'identifiant
	 * Et l'ajoute aux clauses
	 * 
	 * @param impliquant Literal impliquant
	 * @param implieds   Litteraux impliques
	 */
	private void addImply(int impliquant, int...implieds) {
		for(int implied : implieds) {
			int[] clause = new int[2];
			clause[0] = -impliquant;
			clause[1] = implied;
			clauses.add(clause);
		}
	}

	/**
	 * Ajoute les etats initiaux a la liste des clauses
	 * 
	 */
	private void addInit() {
		for (int i = 0; i < relevantfacts.size(); i++) {
			boolean predicateState = initState.getPositive().get(i);
			int id = (predicateState ? 1 : -1) * Utils.couple(i, etape);
			clauses.add(new int[] { id });
		}
	}

	/**
	 * Ajoute les buts a l'etape t+1
	 * 
	 */
	private void addGoal() {
		// On memorise le nombre de buts afin de pouvoir les retirer de maniere efficace a la demande de la prochaine etape
		nbgoal = 0;
		for (int i = 0; i < relevantfacts.size(); i++) {
			int[] clause = new int[1];
			if (goalState.getPositive().get(i)) {
				clause[0] = Utils.couple(i, etape + 1);
				clauses.add(clause);
				nbgoal++;
			} else if (goalState.getNegative().get(i)) {
				clause[0] = -Utils.couple(i, etape + 1);
				clauses.add(clause);
				nbgoal++;
			}
		}
	}

}
