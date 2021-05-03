package config;

import java.io.IOException;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.parser.ErrorManager;
import fr.uga.pddl4j.planners.ProblemFactory;
import fr.uga.pddl4j.util.Plan;
import satplan.SAT;

public class Main {

	//Test
	public static final String domain = "pddl/gripper/domain.pddl";
	public static final String problem = "pddl/gripper/p01.pddl";

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
		
		final SAT planner = new SAT();

		final Plan plan = planner.search(pb);
		if (plan != null) {
		    System.out.println("Found plan as follows:");
		    System.out.println(pb.toString(plan));
		} else {
		    System.out.println("No plan found.");
		}

	}

}
