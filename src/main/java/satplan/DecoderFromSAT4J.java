package satplan;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.sat4j.specs.IProblem;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.util.Plan;
import fr.uga.pddl4j.util.SequentialPlan;
import fr.utils.Utils;

/**
 * Decodeur permettant de passer du resultat de SAT4J a un Plan pour PDDL4J 
 * 
 * @author youcef
 *
 */
public class DecoderFromSAT4J {
	
	/**
	 * Decode une solution en provenance de SAT4J vers un plan pour PDDL4J
	 * 
	 * @param problem    CodedProblem de PDDL4J sur lequel le plan sera defini
	 * @param satProblem Solution de SAT4J a decoder
	 * @return           Le plan resultant de la solution
	 */
	public static Plan decodePlanFromSatResult(CodedProblem problem, IProblem satProblem) {
		Plan plan = new SequentialPlan();
		List<Integer[]> solutionActions = new ArrayList<Integer[]>();
		
		// Lecture de la solution du model de SAT4J 
		for (int id : satProblem.model()) {
			int[] decoupled = Utils.decouple(id);
			// Decodage de l'index correspondant à l'operation dans le CodedProblem
			int index = Math.abs(decoupled[0]-1) - problem.getRelevantFacts().size();
			// Decodage de l'étape à laquel s'applique l'opération
			int step = decoupled[1];
			
			// Si index>=0 : C'est une action. Et Si id>=0 : L'action est évalué a vrai, il code donc une etape a ajouter dans le plan.
			if(id>=0 && index>=0) {
				solutionActions.add(new Integer[] {index, step});
			}
        }
		
		// On trie la liste pour l'avoir dans l'ordre des etapes afin d'etablir le plan dans le bon ordre
		solutionActions = solutionActions.stream().sorted((o1, o2)->o1[1].
                compareTo(o2[1])).
                collect(Collectors.toList());
		
		// Ajout de chaque action au Plan PDDL4J
		for(Integer[] index : solutionActions) {
			plan.add(index[1]-1, problem.getOperators().get(index[0]));
		}
		return plan;
	}
}
