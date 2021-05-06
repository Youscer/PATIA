import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.parser.ErrorManager;
import fr.uga.pddl4j.planners.ProblemFactory;
import fr.uga.pddl4j.planners.statespace.AbstractStateSpacePlanner;
import fr.uga.pddl4j.planners.statespace.hsp.HSP;
import fr.uga.pddl4j.util.Plan;
import fr.utils.Utils;
import planner.sat.SAT;

public class SatTest {
	public static final String dSimple = "pddl/simple/domain.pddl";
	public static final String pSimple = "pddl/simple/p01.pddl";
	
	public static final String dSimple2 = "pddl/simple2/domain.pddl";
	public static final String pSimple2 = "pddl/simple2/p01.pddl";
	
	public static final String dSimple3 = "pddl/simple3/domain.pddl";
	public static final String pSimple3 = "pddl/simple3/p01.pddl";
	
	private CodedProblem probSimple;
	private CodedProblem probSimple2;
	private CodedProblem probSimple3;
	
	
	
	@Before
	public void init() {
		probSimple = factory(dSimple, pSimple);
		probSimple2 = factory(dSimple2, pSimple2);
		probSimple3 = factory(dSimple3, pSimple3);

	}

	@Test
	public void t_simple() {
		final AbstractStateSpacePlanner plannerExpected = new HSP();
		final AbstractStateSpacePlanner plannerActual = new SAT();

		final Plan planExpected = plannerExpected.search(probSimple);
		final Plan planActual = plannerActual.search(probSimple);
		assertNotNull(planActual);
		assertEquals(probSimple.toString(planExpected), probSimple.toString(planActual));
	}
	
	@Test
	public void t_simple2() {
		final AbstractStateSpacePlanner plannerExpected = new HSP();
		final AbstractStateSpacePlanner plannerActual = new SAT();

		final Plan planExpected = plannerExpected.search(probSimple2);
		final Plan planActual = plannerActual.search(probSimple2);
		assertNotNull(planActual);
		assertEquals(probSimple2.toString(planExpected), probSimple2.toString(planActual));
	}
	
	@Test
	public void t_simple3() {
		final AbstractStateSpacePlanner plannerExpected = new HSP();
		final AbstractStateSpacePlanner plannerActual = new SAT();

		final Plan planExpected = plannerExpected.search(probSimple3);
		final Plan planActual = plannerActual.search(probSimple3);
		assertNotNull(planActual);
		assertEquals(probSimple3.toString(planExpected), probSimple3.toString(planActual));
	}
	
	public CodedProblem factory(String dom, String prob) {
		final ProblemFactory factory = ProblemFactory.getInstance();
		ErrorManager errorManager = null;
		try {
			errorManager = factory.parse(dom, prob);
		} catch (IOException e) {
			System.out.println("Unexpected error when parsing the PDDL planning problem description :");
			System.out.println(e.getMessage());
		}
		if (!errorManager.isEmpty()) {
			errorManager.printAll();
		} else {
			System.out.println("Parsing domain file and problem file done successfully !");
		}

		final CodedProblem pb = factory.encode();
		
		if (!pb.isSolvable()) {
			System.out.println("Goal can be simplified to FALSE. No search will solve it.");
		}
		return pb;
	}
}
