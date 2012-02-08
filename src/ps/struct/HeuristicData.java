package ps.struct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Structure that encapsulates all experiment data. Created: 31 Oct. 2011
 */
public class HeuristicData {

	// GENERAL DATA:
	private int level; // the current heuristic level

	// INPUT/OUTPUT DATA:
	private List<Integer> initialList; // the initial list (first level)
	private Map<Double, List<Integer>> firstLevelOutMap; // the first level output (second level input)
	private Map<Double, Map<Double, List<Integer>>> secondLevelOutMap; // the second level output (third level input)
	private List<Integer> finalList; // the third level output (final rank)

	// PARAMETERS:
	private double tfBucketRange;
	private double dccBucketRange;
	private double cliquesRange;

	public HeuristicData(List<Integer> initialList, double tfBucketRange, double dccBucketRange, double cliquesRange) {
		this.level = 1;
		this.initialList = initialList;
		this.firstLevelOutMap = new HashMap<Double, List<Integer>>();
		this.secondLevelOutMap = new HashMap<Double, Map<Double, List<Integer>>>();
		this.finalList = new ArrayList<Integer>();
		this.tfBucketRange = tfBucketRange;
		this.dccBucketRange = dccBucketRange;
		this.cliquesRange = cliquesRange;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public List<Integer> getInitialList() {
		return initialList;
	}

	public void setInitialList(List<Integer> initialList) {
		this.initialList = initialList;
	}

	public Map<Double, List<Integer>> getFirstLevelOutMap() {
		return firstLevelOutMap;
	}

	public void setFirstLevelOutMap(Map<Double, List<Integer>> firstLevelOutMap) {
		this.firstLevelOutMap = firstLevelOutMap;
	}

	public Map<Double, Map<Double, List<Integer>>> getSecondLevelOutMap() {
		return secondLevelOutMap;
	}

	public void setSecondLevelOutMap(Map<Double, Map<Double, List<Integer>>> secondLevelOutMap) {
		this.secondLevelOutMap = secondLevelOutMap;
	}

	public List<Integer> getFinalList() {
		return finalList;
	}

	public void setFinalList(List<Integer> finalList) {
		this.finalList = finalList;
	}

	public double getTfBucketRange() {
		return tfBucketRange;
	}

	public void setTfBucketRange(double tfBucketRange) {
		this.tfBucketRange = tfBucketRange;
	}

	public double getDccBucketRange() {
		return dccBucketRange;
	}

	public void setDccBucketRange(double dccBucketRange) {
		this.dccBucketRange = dccBucketRange;
	}

	public double getCliquesRange() {
		return cliquesRange;
	}

	public void setCliquesRange(double cliquesRange) {
		this.cliquesRange = cliquesRange;
	}
}
