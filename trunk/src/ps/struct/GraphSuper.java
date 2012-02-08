package ps.struct;

import java.util.Date;

import ps.enumerators.AssociationCaseEnum;

/**
 * A superset of the default graph struct which contains additional information for the graph struct
 */
public class GraphSuper {

	private Graph graph;
	private AssociationCaseEnum associationCase;
	private Date periodFrom;
	private Date periodTo;
	private GraphSuper intersGraphSuper;

	public boolean hasIntersection() {
		return this.intersGraphSuper == null;
	}

	public GraphSuper(Graph graph, AssociationCaseEnum associationCase, Date periodFrom, Date periodTo) {
		super();
		this.graph = graph;
		this.associationCase = associationCase;
		this.periodFrom = periodFrom;
		this.periodTo = periodTo;
	}

	public void intersect(GraphSuper gs) {
		this.intersGraphSuper = gs;
		gs.setIntersGraphSuper(this);
	}

	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	public AssociationCaseEnum getAssociationCase() {
		return associationCase;
	}

	public void setAssociationCase(AssociationCaseEnum associationCase) {
		this.associationCase = associationCase;
	}

	public Date getPeriodFrom() {
		return periodFrom;
	}

	public void setPeriodFrom(Date periodFrom) {
		this.periodFrom = periodFrom;
	}

	public Date getPeriodTo() {
		return periodTo;
	}

	public void setPeriodTo(Date periodTo) {
		this.periodTo = periodTo;
	}

	public GraphSuper getIntersGraphSuper() {
		return intersGraphSuper;
	}

	public void setIntersGraphSuper(GraphSuper intersGraphSuper) {
		this.intersGraphSuper = intersGraphSuper;
	}

}
