/**
 * @author K Maremela 221030105
 * @author KS Mothoagae 220022690
 * @author NF Maluleke 221063322
 * @author TG Nenwali 222001364
 */
/**
 * Represents a connection between two nodes with a movement cost.
 */
package graph;
public class Edge {
    public final Node from;
    public final Node to;
    public final int cost;

    public Edge(Node from, Node to, int cost) {
        this.from = from;
        this.to = to;
        this.cost = cost;
    }

    @Override
    public String toString() {
        return from + " -> " + to + " (cost: " + cost + ")";
    }
}