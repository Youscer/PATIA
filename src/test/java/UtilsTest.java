import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.utils.Utils;

public class UtilsTest {
	
	@Test
	public void t_bijection_onefulltest() {
		int x =1; int y = 1;
		int id1 = Utils.couple(x, y);
//		assertEquals(true, id1>0);
		System.out.println(Utils.ids(id1));
//		assertEquals(id1+"|"+x+"_"+y, Utils.ids(id1));
//		assertEquals(id1+"|"+x+"_"+y, Utils.ids(-id1));
		int id2 = -Utils.couple(x, y);
//		assertEquals(-id1, id2);
		System.out.println(Utils.ids(id2));
//		assertEquals(id2+"|"+x+"_"+y, Utils.ids(id2));
//		assertEquals(id2+"|"+x+"_"+y, Utils.ids(-id2));
	}
	
	@Test
	public void t_bijection_positif() {
		for(int i=0; i<500; i++) {
			for(int j=1; j<500; j++) {
				int n = Utils.couple(i, j);
				int[] actual = Utils.decouple(n);
				assertEquals(i, actual[0]-1);
				assertEquals(j, actual[1]);			
			}
		}
	}
	
	@Test
	public void t_bijection_neg_pos() {
		for(int i=-5; i<5; i++) {
			for(int j=1; j<5; j++) {
				int n = Utils.couple(i, j);
//				System.out.println("n:"+n);
				int[] actual = Utils.decouple(n);
//				System.out.println("i:" + i + " j:" + j);
//				System.out.println("x:" + (actual[0]-1) + " y:" + actual[1]);
//				System.out.println("");
				assertEquals(Math.abs(i), actual[0]-1);
				assertEquals(j, actual[1]);
			}
		}
	}
}
