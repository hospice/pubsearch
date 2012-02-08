package ps.comparator;


import java.util.Comparator;
import java.util.Map;

/**
 * Comparator implementation for ascending order.
 */
@SuppressWarnings("rawtypes")
public class AscValComparator implements Comparator {
	Map base;

	@Override
	public int compare(Object a, Object b) {
		if ((Double) base.get(a) > (Double) base.get(b)) {
			return 1;
		}
		if ((Double) base.get(a) < (Double) base.get(b)) {
			return -1;
		}
		return 0;
	}

	public AscValComparator(Map base) {
		this.base = base;
	}

}
