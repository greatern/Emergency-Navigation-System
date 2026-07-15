/**
 * @author K Maremela 221030105
 * @author KS Mothoagae 220022690
 * @author NF Maluleke 221063322
 * @author TG Nenwali 222001364
 */
package graph;

public class AStarNode implements Comparable<AStarNode> {

	final Node node;
	final int fScore;

	AStarNode(Node node, int fScore) {
		this.node = node;
		this.fScore = fScore;
	}

	@Override
	public int compareTo(AStarNode other) {
		return Integer.compare(this.fScore, other.fScore);
	}
}
