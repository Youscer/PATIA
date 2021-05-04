package config;

import java.io.IOException;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.parser.ErrorManager;
import fr.uga.pddl4j.planners.ProblemFactory;
import fr.uga.pddl4j.planners.statespace.AbstractStateSpacePlanner;
import fr.uga.pddl4j.planners.statespace.hsp.HSP;
import fr.uga.pddl4j.util.IntExp;
import fr.uga.pddl4j.util.Plan;
import satplan.SAT;

public class Main {

	//Test
	public static final String domain = "pddl/simple3/domain.pddl";
	public static final String problem = "pddl/simple3/p01.pddl";

	public static void main(String[] args) {
		final ProblemFactory factory = ProblemFactory.getInstance();
		ErrorManager errorManager = null;
		try {
			errorManager = factory.parse(domain, problem);
		} catch (IOException e) {
			System.out.println("Unexpected error when parsing the PDDL planning problem description :");
			System.out.println(e.getMessage());
			System.exit(0);
		}

		if (!errorManager.isEmpty()) {
			errorManager.printAll();
			System.exit(0);
		} else {
			System.out.println("Parsing domain file and problem file done successfully !");
		}

		final CodedProblem pb = factory.encode();
		System.out.println("Encoding problem done successfully (" + pb.getOperators().size() + " ops, "
				+ pb.getRelevantFacts().size() + " facts).");

		if (!pb.isSolvable()) {
			System.out.println("Goal can be simplified to FALSE. No search will solve it.");
			System.exit(0);
		}
		
		final AbstractStateSpacePlanner planner = new SAT();
//		final AbstractStateSpacePlanner planner = new HSP();

		final Plan plan = planner.search(pb);
		if (plan != null) {
		    System.out.println("Found plan as follows:");
		    System.out.println(pb.toString(plan));
		} else {
		    System.out.println("No plan found.");
		}

	}
	
	private static void showIntExp(IntExp exp) {
		System.out.println( "Predicate: " + exp.getPredicate() );
		System.out.println( "Type: " + exp.getType() );
		System.out.println( "Value: " + exp.getValue() );
		System.out.println( "Variable: " + exp.getVariable() );
		System.out.println();
	}

}
