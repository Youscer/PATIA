package fr.utils;

import java.util.ArrayList;
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
    	n--;
        int t = (int) (Math.floor((Math.sqrt(8 * n + 1) - 1) / 2));
        decouple = new int[]{(t * (t+3) / 2 - n), (n - t * (t+1) / 2)};
    	return decouple;
    }

//	public static int couple(int n1, int n2) {
//		int toRet = ((n1) << 16) + n2;
//		return toRet;
//	}
//
//	public static int[] decouple(int n) {
//		int[] decouple;
//		decouple = new int[]{n / (1<<16),n % (1<<16)};
//		return decouple;
//	}

//	/**
//	 * Fonction bijective de ZxN dans N
//	 * 
//	 * @param n1 premier élement
//	 * @param n2 deuxième élement
//	 * @return Résultat de la bijection dans N
//	 */
//	public static int couple(int x, int y) {
//		int xx = x >= 0 ? x * 2 : x * -2 - 1;
//		int yy = y >= 0 ? y * 2 : y * -2 - 1;
//		int z = (xx >= yy) ? (xx * xx + xx + yy) : (yy * yy + xx);
//		return x>=0? z : -z-1;
//	}
//
//	/**
//	 * Fonction bijective de N dans ZxN
//	 *
//	 * @param n élément
//	 * @return Résultat de la bijection dans NxN
//	 */
//	public static int[] decouple(int z) {
//		int[] decouple = new int[2];
//		z *= z>=0? 1 : -1;
//		int sqrtz = (int) Math.floor(Math.sqrt(z));
//		int sqz = sqrtz * sqrtz;
//		int[] result1 = ((z - sqz) >= sqrtz) ? new int[] { sqrtz, z - sqz - sqrtz } : new int[] { z - sqz, sqrtz };
//		decouple[0] = Math.abs(result1[0] % 2 == 0 ? result1[0] / 2 : (result1[0] + 1) / -2);
//		decouple[1] = Math.abs(result1[1] % 2 == 0 ? result1[1] / 2 : (result1[1] + 1) / -2);
//		return decouple;
//	}

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

	public static String lToS(int lit) {
		return "" + Utils.decouple(lit)[0] + "_" + Utils.decouple(lit)[1];
	}
	
	public static String ids(int lit) {
		return "" + lit + "|" + lToS(lit);
	}

	public static List<int[]> deepCopyListInt(List<int[]> list) {
		List<int[]> copy = new ArrayList<int[]>(list.size());
		for(int[] clause : list) {
			copy.add( clause.clone() );
		}
		return copy;
	}

}
