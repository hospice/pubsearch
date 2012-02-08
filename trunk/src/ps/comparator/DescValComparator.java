package ps.comparator;

import java.util.Comparator;
import java.util.Map;

/**
 * Comparator implementation for descending value order.
 */
@SuppressWarnings("rawtypes")
public class DescValComparator implements Comparator {
	Map base;

	@Override
	public int compare(Object a, Object b) {
		if ((Double) base.get(a) < (Double) base.get(b)) {
			return 1;
		}
		if ((Double) base.get(a) == (Double) base.get(b)) {
			return 0;
		}
		return -1;
	}

	public DescValComparator(Map base) {
		this.base = base;
	}

}
