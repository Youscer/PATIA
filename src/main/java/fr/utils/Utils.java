package fr.utils;

import java.util.List;

public class Utils {

    public static int couple(int n1, int n2) {
    	boolean neg = n1<0;
    	if(neg) n1*=-1; 
        int z = (int) (0.5F * (n1+n2) * (n1+n2+1) + n2) + 1;
        return (neg)?-z:z;
    }
    
    public static int[] decouple(int n) {
    	int[] decouple;
    	boolean neg = n<0;
    	if(neg) n*=-1;
    	n-=1;
        int t = (int) (Math.floor((Math.sqrt(8 * n + 1) - 1) / 2));
        decouple = new int[]{(t * (t+3) / 2 - n)+1, (n - t * (t+1) / 2)};
    	return decouple;
    }

	public static void printClauses(List<int[]> clauses) {
		String toprint = "Clauses : \n";
		toprint += "[ ";
		for (int[] clause : clauses) {
			toprint += "[ ";
			for (int lit : clause) {
				toprint += lit + " ";
			}
			toprint += "] ";
		}
		toprint += "]";
		System.out.println(toprint);
	}

	public static void printClauses2(List<int[]> clauses) {
		String toprint = "Clauses : \n";
		toprint += "[ ";
		for (int[] clause : clauses) {
			toprint += "[ ";
			for (int lit : clause) {
				toprint += lToS(lit) + " ";
			}
			toprint += "] ";
		}
		toprint += "]";
		System.out.println(toprint);
	}
	
	public static void printClauses3(List<int[]> clauses) {
		String toprint = "Clauses : \n";
		toprint += "[ ";
		for (int[] clause : clauses) {
			toprint += "[ ";
			for (int lit : clause) {
				toprint += ids(lit) + " ";
			}
			toprint += "] ";
		}
		toprint += "]";
		System.out.println(toprint);
	}
	
	public static void printClauses4(List<int[]> clauses) {
		String toprint = "Clauses : "+clauses.size()+" \n";
		toprint += "[ ";
		for (int[] clause : clauses) {
			toprint += "[ ";
			for (int lit : clause) {
				toprint += ((lit<0)?"--":"") + lToS(lit) + " ";
			}
			toprint += "] ";
		}
		toprint += "]";
		System.out.println(toprint);
	}

	public static String lToS(int lit) {
		return "" + (Utils.decouple(lit)[0]-1) + "_" + Utils.decouple(lit)[1];
	}
	
	public static String ids(int lit) {
		return "" + lit + "|" + lToS(lit);
	}

}
