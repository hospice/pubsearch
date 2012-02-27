package ps.util;

/**
 * Provides a set of time-related functionality.
 */
public class TimeUtils {

	/**
	 * Sleeps up to 30 seconds.
	 */
	public static void randomSleep() throws InterruptedException {
		Long delay = 3 * (1 + ((Double) (Math.random() * 10)).longValue());
		System.out.print("sleeping for " + delay + " sec ");
		int count = 0;
		while (count < delay) {
			System.out.print(".");
			Thread.sleep(1000);
			count++;
		}
		System.out.println(" done");
	}

	/**
	 * Sleeps for the specified number of seconds.
	 */
	public static void sleepForSecs(int secs) {
		try {
			System.out.print("Waking from sleep in (secs): ");
			for (int sec = 0; sec < secs; sec++) {
				System.out.print((secs - sec));
				Thread.sleep(500);
				if (sec+1 < secs) {
					System.out.print(".");
				}
				Thread.sleep(500);
			}
			System.out.println();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		;
	}

}
