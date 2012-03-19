package ps.util;

import java.text.DecimalFormat;

public class FormatterUtils {

	/**
	 * Returns a two-decimal string representation of the specific double.
	 */
	public static String getTwoDecimalDouble(Double d) {
		DecimalFormat df = new DecimalFormat("#.00");
		return df.format(d);
	}

}
