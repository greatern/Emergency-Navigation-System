/**
 * @author K Maremela 221030105
 * @author KS Mothoagae 220022690
 * @author NF Maluleke 221063322
 * @author TG Nenwali 222001364
 */
package graph;
import java.util.List;
public interface IGraph<T> {

	public void addVertex(T vertex);
	public void addEdge(T source, T destination);
	public void addEdge(T source, T destination, int weight);
	public void removeVertex(T vertex);
	public void removeEdge(T source, T destination);
	public boolean hasVertex(T vertex);
	public boolean hasEdge(T source, T destination);
	public int getVertexCount();
	public int getEdgeCount();
	public List<T> getNeighbors(T vertex);
	public List<T> getVertices();
}
