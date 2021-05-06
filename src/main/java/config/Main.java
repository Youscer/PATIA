package config;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.logging.log4j.core.Logger;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.heuristics.relaxation.Heuristic;
import fr.uga.pddl4j.parser.ErrorManager;
import fr.uga.pddl4j.planners.Planner;
import fr.uga.pddl4j.planners.ProblemFactory;
import fr.uga.pddl4j.planners.statespace.AbstractStateSpacePlanner;
import fr.uga.pddl4j.planners.statespace.StateSpacePlanner;
import fr.uga.pddl4j.planners.statespace.hsp.HSP;
import fr.uga.pddl4j.planners.statespace.search.strategy.AStar;
import fr.uga.pddl4j.util.Plan;
import fr.utils.LogType;
import fr.utils.Logs;
import planner.hsp.Astar;
import planner.sat.SAT;

public class Main {
	// Test
//	private static String domain = "pddl/simple/domain.pddl";
//	private static String problem = "pddl/simple/p01.pddl";
	private static ProblemFactory factory;

	private static final String TIMEOUT = "-t";
	private static final String PLANNER = "-p";
	private static final String LOG = "-l";
	private static final String DEFINE = "-d";

	
	public static void main(String[] args) {
		factory = ProblemFactory.getInstance();

		String domain = "";
		String problem = "";
//		String output = "";
		Set<LogType> logtypes = new HashSet<LogType>();
		Set<String> possibleLogTypes = new HashSet<String>();
		possibleLogTypes.add(LogType.TIMINGS.toString());
		possibleLogTypes.add(LogType.PLAN.toString());
		possibleLogTypes.add(LogType.STEPS.toString());
		possibleLogTypes.add(LogType.CLAUSES.toString());
		possibleLogTypes.add(LogType.INFO.toString());
		possibleLogTypes.add(LogType.ERROR.toString());

		Set<String> possibleSolvertypes = new HashSet<String>();
		possibleSolvertypes.add("sat");
		possibleSolvertypes.add("astar");

		int timeout = 120;

		String solverType = "";

		int argSize = args.length;
//		System.out.println(argSize + "args");
		for (int i = 0; i < argSize; i++) {
			String arg = args[i];
//			System.out.println("parsing " + arg);
			if (!arg.startsWith("-")) {
				System.out.println("must start with -");
				System.exit(0);
			}
			String mainArg = arg;
			boolean isInarg = i < argSize - 1 && !args[i + 1].startsWith("-");
			;
			List<String> inArgs = new ArrayList<String>();
			while (isInarg) {
				i++;
				inArgs.add(args[i]);
//				System.out.println("Added " + args[i]);
				isInarg = i < argSize - 1 && !args[i + 1].startsWith("-");
				;
			}
			if (mainArg.equalsIgnoreCase(PLANNER)) {
				if (inArgs.size() != 1) {
					System.out.println("Only one solver type is allowed");
					System.exit(0);
				}
				String param = inArgs.get(0);
				if (possibleSolvertypes.contains(param)) {
					solverType = param;
				} else {
					System.out.println("solver type : " + param + " non supporté");
					System.exit(0);
				}
			} else if (mainArg.equalsIgnoreCase(LOG)) {
				for (String param : inArgs) {
					if (possibleLogTypes.contains(param.toUpperCase())) {
						logtypes.add(LogType.valueOf(param.toUpperCase()));
					} else {
						System.out.println("logtype : " + param + " non supporté");
					}
					Logs.init(logtypes);
				}
			} else if (mainArg.equalsIgnoreCase(DEFINE)) {
				if (inArgs.size() < 2) {
					System.out.println("Arg " + mainArg + " Must have 2 parameter");
					System.exit(0);
				}
				domain = "pddl/" + inArgs.get(0) + "/domain.pddl";
				problem = "pddl/" + inArgs.get(0) + "/" + inArgs.get(1);
			} else if (mainArg.equalsIgnoreCase(TIMEOUT)) {
				if (inArgs.size() < 1) {
					System.out.println("Arg " + mainArg + " Must have 1 parameter");
					System.exit(0);
				}
				try {
					timeout = Integer.parseInt(inArgs.get(0));
				} catch (NumberFormatException e) {
					System.out.println("Timeout Must be an integer");
				}
			} else {
				System.out.println("Argument inconnu");
				System.exit(0);
			}
		}

		CodedProblem pb = initProblem(domain, problem);
		AbstractStateSpacePlanner planner = getPlanner(solverType, timeout);
		Plan plan = solve(planner, pb);

		if (plan != null) {
			Logs.log(LogType.PLAN, pb.toString(plan));

			Date time = new Date(planner.getStatistics().getTimeToEncode() + planner.getStatistics().getTimeToSearch());
			DateFormat formatter = new SimpleDateFormat("s.S ");
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

			Logs.log(LogType.TIMINGS, formatter.format(time));
		} else {
			Logs.log(LogType.ERROR, "Le planner n'a pas pu résoudre");
			Logs.log(LogType.TIMINGS, ""+-1);
		}
	}

	private static AbstractStateSpacePlanner getPlanner(String solverType, int timeout) {
		AbstractStateSpacePlanner planner = null;
		if (solverType.equalsIgnoreCase("sat")) {
			planner = new SAT(timeout, Heuristic.Type.FAST_FORWARD, 0, false, 0);
		} else if (solverType.equalsIgnoreCase("astar")) {
			planner = new Astar();
//			planner = new Astar(timeout, StateSpacePlanner.DEFAULT_HEURISTIC, StateSpacePlanner.DEFAULT_WEIGHT, false, StateSpacePlanner.DEFAULT_TRACE_LEVEL);
		}
		return planner;
	}

	public static Plan solve(AbstractStateSpacePlanner planner, CodedProblem pb) {
		final Plan plan = planner.search(pb);
		return plan;
	}

	public static CodedProblem initProblem(String domain, String problem) {
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
			Logs.log(LogType.INFO, "Parsing domain file and problem file done successfully !");
		}
		final CodedProblem pb = factory.encode();
		Logs.log(LogType.INFO, "Encoding problem done successfully (" + pb.getOperators().size() + " ops, "
				+ pb.getRelevantFacts().size() + " facts).");

		if (!pb.isSolvable()) {
			System.out.println("Goal can be simplified to FALSE. No search will solve it.");
			System.exit(0);
		}
		return pb;
	}

}
