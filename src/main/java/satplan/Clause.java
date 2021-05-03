package satplan;

public class Clause {
	private int[] litteraux;
	
	public Clause(int...litteraux) {
		this.litteraux = litteraux;
	}
	
	public final int[] getLitteralsArray() {
		return litteraux;
	}
}
