package ps.struct;

import java.util.List;

/**
 * Defines an experiment structure.
 */
public class Experiment {
	private List<Session> sessions;

	public List<Session> getSessions() {
		return sessions;
	}

	public void setSessions(List<Session> sessions) {
		this.sessions = sessions;
	}
}
