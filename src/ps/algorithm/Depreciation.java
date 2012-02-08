package ps.algorithm;

/**
 * Provides all the functionality for calculating the time depreciation
 */
public class Depreciation {

	private static final int CURRENT_YEAR = 2011;

	/**
	 * Calculates the depreciation for the specified year
	 */
	public static Double calculate(int year) {
		double x = CURRENT_YEAR - year;
		double tanh = Math.tanh((x - 10) / 4);
		return 1 - (1 + tanh) / 2;
	}

}
