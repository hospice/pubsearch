package ps.tests;

public class RecursionTest {
	
	private final static int LENGTH = 3;
	private final static String[] chars = { "a", "b", "c" };

	public static void main(String[] args) {
		String s = "";
		RecursionTest.doNothing(s);
	}
	
	public static void doNothing(String s) {
		int count = 0;
		if (s.length() == LENGTH) {
			return;
		} else {
			for (int i = 0; i < chars.length; i++) {
				System.out.println(count + ". " + s);
				count++;
				//doNothing(s + chars[i]);
			}
		}
	}
	
}
