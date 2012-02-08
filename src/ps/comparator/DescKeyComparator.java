package ps.comparator;

import java.util.Comparator;
import java.util.Map;

/**
 * Comparator implementation for descending key order.
 */
@SuppressWarnings("rawtypes")
public class DescKeyComparator implements Comparator {
	Map base;

	@Override
	public int compare(Object a, Object b) {
		if ((Double) a < (Double) b) {
			return 1;
		}
		if ((Double) a == (Double) b) {
			return 0;
		}
		return -1;
	}

	public DescKeyComparator(Map base) {
		this.base = base;
	}

}
