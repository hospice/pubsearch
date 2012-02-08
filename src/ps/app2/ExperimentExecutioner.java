package ps.app2;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ps.constants.ExperimentParameters;
import ps.persistence.PersistenceController;
import ps.struct.Experiment;
import ps.struct.HeuristicData;
import ps.struct.Item;
import ps.struct.Param;
import ps.struct.Session;
import ps.util.ExperimentIOUtils;
import ps.util.HeuristicUtils;
import ps.util.PropertyUtils;

/**
 * Provides experiment related utilities.
 */
public class ExperimentExecutioner {

	// TF parameters
	private static Double tfBucketRange = 0d;
	private static Double tfBucketRangeMax = null;
	private static Double tfBucketRangeMin = null;
	private static Double tfBucketRangeStep = null;

	// DCC parameters
	private static Double dccBucketRange = 0d;
	private static Double dccBucketRangeMax = null;
	private static Double dccBucketRangeMin = null;
	private static Double dccBucketRangeStep = null;

	// clique parameters
	private static Double cliqueBucketRange = 0d;
	private static Double cliqueBucketRangeMax = null;
	private static Double cliqueBucketRangeMin = null;
	private static Double cliqueBucketRangeStep = null;
	
	
	/**
	 * Runs the experiment for all specified queries (queries.txt) with the specified configuration (config.xml).
	 */
	public static void execute() throws Exception {
		// fetches all query ids to be included in the current experiment
		List<Integer> queries = ExperimentIOUtils.fetchAllQueryIds();
		// fetches all maximal weighted cliques
		Map<Integer, List<Integer>> cliqueTopicsMap = PersistenceController.getAllTopicsPerClique();
		// reads the experiment configuration
		Experiment experiment = ExperimentIOUtils.readConfiguration(PropertyUtils.readProperty(
				ps.constants.GeneralConstants.CONFIG_FILE_PATH,
				ps.constants.GeneralConstants.EXPERIMENT_CONFIG_FILE_LOCATION));
		// runs the experiment for all specified queries
		for (Integer queryId : queries) {
			List<Integer> initialList = PersistenceController.fetchQueryResultsDefault(queryId, 10);
			runExperiment(experiment, initialList, queryId, cliqueTopicsMap);
		}
	}
	
	/**
	 * Runs the experiment with the specified configuration for a specific query.
	 */
	public static void runExperiment(Experiment experiment, List<Integer> initialList, int queryId,
			Map<Integer, List<Integer>> cliqueTopicsMap) throws Exception {

		for (Session currSession : experiment.getSessions()) {
			Map<Integer, String> heuristicHierarchyMap = new LinkedHashMap<Integer, String>();
			for (Item item : currSession.getItems()) {
				String heurName = item.getName();
				int heurOrder = item.getOrder();
				heuristicHierarchyMap.put(heurOrder, heurName);
				initializeParams(item.getParameters());
			}
			initializeUninitializedParams();
			execExperimentForParameterRanges(queryId, initialList, heuristicHierarchyMap, cliqueTopicsMap);
		}
	}
	
	/**
	 * Initializes all specified parameters.
	 */
	private static void initializeParams(List<Param> parameters){
		for (Param param : parameters) {
			String paramName = param.getName();
			String paramValue = param.getValue();
			String paramMinValue = param.getMinValue();
			String paramMaxValue = param.getMaxValue();
			String paramStep = param.getStep();
			if (ExperimentParameters.TF_BUCKET_RANGE.equals(paramName)) {
				if (paramValue != null) {
					tfBucketRange = Double.parseDouble(paramValue);
				} else if (paramMinValue != null) {
					tfBucketRangeMin = Double.parseDouble(paramMinValue);
					tfBucketRangeMax = Double.parseDouble(paramMaxValue);
					tfBucketRangeStep = Double.parseDouble(paramStep);
				} else if (paramMaxValue != null) {
					tfBucketRangeMax = Double.parseDouble(paramMaxValue);
					tfBucketRangeMin = Double.parseDouble(paramMinValue);
					tfBucketRangeStep = Double.parseDouble(paramStep);
				}
			} else if (ExperimentParameters.DCC_BUCKET_RANGE.equals(paramName)) {
				if (paramValue != null) {
					dccBucketRange = Double.parseDouble(paramValue);
				} else if (paramMinValue != null) {
					dccBucketRangeMin = Double.parseDouble(paramMinValue);
					dccBucketRangeMax = Double.parseDouble(paramMaxValue);
					dccBucketRangeStep = Double.parseDouble(paramStep);
				} else if (paramMaxValue != null) {
					dccBucketRangeMax = Double.parseDouble(paramMaxValue);
					dccBucketRangeMin = Double.parseDouble(paramMinValue);
					dccBucketRangeStep = Double.parseDouble(paramStep);
				}
			} else if (ExperimentParameters.CLIQUE_BUCKET_RANGE.equals(paramName)) {
				if (paramValue != null) {
					cliqueBucketRange = Double.parseDouble(paramValue);
				} else if (paramMinValue != null) {
					cliqueBucketRangeMin = Double.parseDouble(paramMinValue);
					cliqueBucketRangeMax = Double.parseDouble(paramMaxValue);
					cliqueBucketRangeStep = Double.parseDouble(paramStep);
				} else if (paramMaxValue != null) {
					cliqueBucketRangeMax = Double.parseDouble(paramMaxValue);
					cliqueBucketRangeMin = Double.parseDouble(paramMinValue);
					cliqueBucketRangeStep = Double.parseDouble(paramStep);
				}
			}
		}
	}
	
	/**
	 * Initializes all uninitialized parameters.
	 */
	private static void initializeUninitializedParams() {
		if (tfBucketRangeMax == null) {
			tfBucketRangeMax = tfBucketRange;
		}
		if (tfBucketRangeMin == null) {
			tfBucketRangeMin = tfBucketRange;
		}
		if (tfBucketRangeStep == null) {
			tfBucketRangeStep = 1d;
		}
		if (dccBucketRangeMax == null) {
			dccBucketRangeMax = dccBucketRange;
		}
		if (dccBucketRangeMin == null) {
			dccBucketRangeMin = dccBucketRange;
		}
		if (dccBucketRangeStep == null) {
			dccBucketRangeStep = 1d;
		}
		if (cliqueBucketRangeMax == null) {
			cliqueBucketRangeMax = cliqueBucketRange;
		}
		if (cliqueBucketRangeMin == null) {
			cliqueBucketRangeMin = cliqueBucketRange;
		}
		if (cliqueBucketRangeStep == null) {
			cliqueBucketRangeStep = 1d;
		}
	}

	/**
	 * Executes the experiment for all different parameter ranges.
	 */
	private static void execExperimentForParameterRanges(int queryId, List<Integer> initialList,
			Map<Integer, String> heurExecOrder, Map<Integer, List<Integer>> cliqueTopicsMap) throws Exception {
		for (double tfBucketCurrRange = tfBucketRangeMin; tfBucketCurrRange < tfBucketRangeMax; tfBucketCurrRange += tfBucketRangeStep) {
			for (double dccBucketCurrRange = dccBucketRangeMin; dccBucketCurrRange < dccBucketRangeMax; dccBucketCurrRange += dccBucketRangeStep) {
				for (double cliqueBucketCurrRange = cliqueBucketRangeMin; cliqueBucketRangeMin < cliqueBucketRangeMax; cliqueBucketCurrRange += cliqueBucketRangeStep) {
					HeuristicData heuristicData = new HeuristicData(initialList, tfBucketCurrRange, dccBucketCurrRange,
							cliqueBucketCurrRange);
					Iterator<Integer> it = heurExecOrder.keySet().iterator();
					while (it.hasNext()) {
						Integer order = it.next();
						String heurName = heurExecOrder.get(order);
						// i. Term Frequency Heuristic
						if (heurName.equals(ExperimentParameters.TF_HEUR)) {
							HeuristicUtils.applyTf(heuristicData, queryId);
						}
						// ii. Depreciated Citation Count (DCC) Heuristic
						else if (heurName.equals(ExperimentParameters.DCC_HEUR)) {
							HeuristicUtils.applyDcc(heuristicData, queryId);
						}
						// iii. Max. Weighted Clique Heuristic
						else if (heurName.equals(ExperimentParameters.CLIQUE_HEUR)) {
							HeuristicUtils.applyCliques(heuristicData, queryId, cliqueTopicsMap);
						}
					}
				}
			}
		}
	}	
	
	
//	// ************************************************************* //
//	// ********************* T  E  S  T  S ************************* //
//	// ************************************************************* //
//	
//	/**
//	 * TESTS PARAMETERS CONFIGURATION
//	 */
//	private static void testParametersConfigurationTest() throws Exception {
//		Experiment exp = ExperimentIOUtils.readConfiguration(PropertyUtils.readProperty(
//				pub.search.constants.GeneralConstants.CONFIG_FILE_PATH,
//				pub.search.constants.GeneralConstants.EXPERIMENT_CONFIG_FILE_LOCATION));
//		Map<Integer, List<Integer>> cliqueTopicsMap = new HashMap<Integer, List<Integer>>();
//		List<Integer> initialList = new ArrayList<Integer>();
//		int queryId = 1;
//		runExperiment(exp, initialList, queryId, cliqueTopicsMap);
//	}
	
}
