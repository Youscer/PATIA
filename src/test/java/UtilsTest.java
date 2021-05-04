import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.utils.Utils;

public class UtilsTest {
	
	@Test
	public void t_bijection_onefulltest() {
		int x =30; int y = 1;
		int id1 = Utils.couple(30, 1);
		assertEquals(true, id1>0);
		assertEquals(x+"_"+y, Utils.lToS(id1));
		assertEquals(x+"_"+y, Utils.lToS(-id1));
		int id2 = Utils.couple(-x, y);
		assertEquals(-id1, id2);
		assertEquals(x+"_"+y, Utils.lToS(id2));
		assertEquals(x+"_"+y, Utils.lToS(-id2));
	}
	
	@Test
	public void t_bijection_positif() {
		for(int i=1; i<30; i++) {
			for(int j=1; j<30; j++) {
				int n = Utils.couple(i, j);
				int[] actual = Utils.decouple(n);
				assertEquals(i, actual[0]);
				assertEquals(j, actual[1]);			
			}
		}
	}
	
	@Test
	public void t_bijection_neg_pos() {
		for(int i=-30; i<0; i++) {
			for(int j=1; j<30; j++) {
				int n = Utils.couple(i, j);
				int[] actual = Utils.decouple(n);
				assertEquals(-i, actual[0]);
				assertEquals(j, actual[1]);
			}
		}
	}
}
