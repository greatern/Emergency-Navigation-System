/**
 * @author K Maremela 221030105
 * @author KS Mothoagae 220022690
 * @author NF Maluleke 221063322
 * @author TG Nenwali 222001364
 */
package graph;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the graph, holding its position and connected edges.
 */
public class Node {
    public final Point position;
    public final List<Edge> edges;

    public Node(Point position) {
        this.position = position;
        this.edges = new ArrayList<>();
    }
    

    @Override
    public String toString() {
        return "Node(" + position.x + "," + position.y + ")";
    }
}