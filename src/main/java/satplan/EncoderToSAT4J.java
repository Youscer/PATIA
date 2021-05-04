package satplan;

import java.util.ArrayList;
import java.util.List;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.util.BitOp;
import fr.uga.pddl4j.util.BitState;
import fr.uga.pddl4j.util.BitVector;
import fr.uga.pddl4j.util.IntExp;
import fr.utils.Utils;

public class EncoderToSAT4J {

	private List<int[]> clauses = new ArrayList<int[]>();
	
	private BitState initState;
	private List<IntExp> relevantfacts;
	private BitState goalState;
	
	private int etape = 1;

	private CodedProblem problem;


	public EncoderToSAT4J(CodedProblem problem) {
		this.initState = new BitState(problem.getInit());
		System.out.println("INIT");
		System.out.println(problem.toString(initState));
		System.out.println("--INIT--");
		
		this.relevantfacts = problem.getRelevantFacts();
		System.out.println("FACT");
		for(IntExp fact : relevantfacts) {
			problem.toString(fact);
		}
		System.out.println("--FACT--");
		
		System.out.println("ACTION");
		int actionSize = problem.getOperators().size();
		for(int actionIndex = 0; actionIndex < actionSize; actionIndex++) {
			problem.toString(problem.getOperators().get(actionIndex));
		}
		System.out.println("--ACTION--");

		
		this.goalState = new BitState(problem.getGoal());
		System.out.println("GOAL");
		System.out.println(problem.toString(goalState));
		System.out.println("--GOAL--");
		
		System.out.println("");

		this.problem = problem;
	}
	
	public List<int[]> init() {
		addInit();
		return clauses;
	}
	
	public List<int[]> getNext() {
		addActions();
		List<int[]> newclauses = Utils.deepCopyListInt(clauses); 
		addGoal(newclauses);
		etape++;
		return newclauses;
	}


	private void addActions() {
		int actionSize = problem.getOperators().size();
		int[] clauseActionSame = new int[actionSize];
		int[] clauseActionDiff = new int[actionSize];
        for (int actionIndex = 0; actionIndex < actionSize; actionIndex++) {
        	int actionID = actionIndex + relevantfacts.size();
        	
            final BitOp action = problem.getOperators().get(actionIndex);
            final BitVector posPreconds = action.getPreconditions().getPositive();
//            final BitVector negPreconds = action.getPreconditions().getNegative();
            final BitVector posEffects = action.getUnconditionalEffects().getPositive();
//            final BitVector negEffects = action.getUnconditionalEffects().getNegative();
            
//            System.out.println("Action: " + actionIndex);
            // Implication to CNF memento : A => (x & y & !z) == (x | !A) & (y | !A) & (!z | !A)
            //Ajout preconditions et effets de l'action
            for (int i = 0; i < relevantfacts.size(); i++) {
            	//Preconditions
                int[] precond = new int[2];
                int indexPrecond = posPreconds.get(i)? i: -i;
                precond[0] = Utils.couple(indexPrecond, etape);
                precond[1] = Utils.couple(-actionID, etape);
                clauses.add(precond);
                //Effects
                int[] effect = new int[2];
                int indexEffect = posEffects.get(i)? i: -i;
                effect[0] = Utils.couple(indexEffect, etape+1);
                effect[1] = Utils.couple(-actionID, etape);
                clauses.add(effect);
                //FrameAxioms
                
            }
            
    		//Ajoute que deux actions ne peuvent pas être faites en même temps
            clauseActionSame[actionIndex] = Utils.couple(-actionID, etape);
    		//Ajoute qu'il ne peut pas y avoir aucune action d'effectué
            clauseActionDiff[actionIndex] = Utils.couple(actionID, etape);
            
        }
        clauses.add(clauseActionSame);
        clauses.add(clauseActionDiff);

	}

	private void addInit() {
		for (int i = 0; i < relevantfacts.size(); i++) {
			boolean predicateState = initState.get(i);
			addClause( predicateState ? i : -i );
		}
	}

	private void addClause(int i) {
		clauses.add(new int[]{Utils.couple(i, etape)});
	}


	private List<int[]> addGoal(List<int[]> clauses) {
        for (int i = 0; i < relevantfacts.size(); i++) {
            int[] clause = new int[1];
            if (goalState.get(i)) {
                clause[0] = Utils.couple(i, etape + 1);
                clauses.add(clause);
            }
        }
        return clauses;
	}
	

}
