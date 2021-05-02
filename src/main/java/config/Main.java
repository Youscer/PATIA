package config;
import java.io.IOException;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.parser.ErrorManager;
import fr.uga.pddl4j.planners.ProblemFactory;

public class Main {

	public static final String domain = "";
	public static final String problem = "";

	public static void main(String[] args) {
		final ProblemFactory factory = ProblemFactory.getInstance();
		ErrorManager errorManager = null;
		try {
			errorManager = factory.parse(domain, problem);
		} catch (IOException e) {
			System.out.println("Unexpected error when parsing the PDDL planning problem description.");
			System.exit(0);
		}

		if (!errorManager.isEmpty()) {
			errorManager.printAll();
			System.exit(0);
		} else {
			System.out.println("Parsing domain file and problem file done successfully");
		}
		
		final CodedProblem pb = factory.encode();
		
	}

}
