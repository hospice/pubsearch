package ps.app2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Structure that contains all heuristic levels.
 */
public class MultiLevelBuckets {

	private int currentLevel;

	private Map<Double, Map<Double, Map<Double, List<Integer>>>> map; // contains the 3-level buckets

	public int getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}

	public MultiLevelBuckets() {
		this.currentLevel = 0;
		this.map = new HashMap<Double, Map<Double, Map<Double, List<Integer>>>>();
	}

	public Map<Double, Map<Double, Map<Double, List<Integer>>>> getM() {
		return map;
	}

	public void setM(Map<Double, Map<Double, Map<Double, List<Integer>>>> m) {
		this.map = m;
	}
		
	public void saveMapForLevel(int level){
		
	}
}
