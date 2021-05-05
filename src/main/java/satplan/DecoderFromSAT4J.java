package satplan;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.sat4j.specs.IProblem;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.util.Plan;
import fr.uga.pddl4j.util.SequentialPlan;
import fr.utils.Utils;

public class DecoderFromSAT4J {
	public static Plan decodePlanFromSatResult(CodedProblem problem, IProblem satProblem) {
		Plan plan = new SequentialPlan();
		List<Integer[]> solutionActions = new ArrayList<Integer[]>();
		
		for (int id : satProblem.model()) {
			int[] decoupled = Utils.decouple(id);
			//Decodage de l'index correspondant à l'operation dans le CodedProblem
			int index = Math.abs(decoupled[0]-1) - problem.getRelevantFacts().size();
			//Decodage de l'étape à laquel s'applique l'opération
			int step = decoupled[1];
			if(id>=0 && index>=0) {
				solutionActions.add(new Integer[] {index, step});
			}
        }
		solutionActions = solutionActions.stream().sorted((o1, o2)->o1[1].
                compareTo(o2[1])).
                collect(Collectors.toList());
		
		for(Integer[] index : solutionActions) {
			plan.add(index[1]-1, problem.getOperators().get(index[0]));
		}
		return plan;
	}
	
	public static boolean isSatisfied(CodedProblem problem, IProblem satProblem) {
		for (int id : satProblem.model()) {
			int[] decoupled = Utils.decouple(id);
			int index = Math.abs(decoupled[0]-1) - problem.getRelevantFacts().size();
			if(id>0 && index>=0) {
				return true;
			}
        }
		return false;
	}
}
