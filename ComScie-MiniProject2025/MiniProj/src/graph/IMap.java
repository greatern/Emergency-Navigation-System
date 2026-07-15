/**
 * @author K Maremela 221030105
 * @author KS Mothoagae 220022690
 * @author NF Maluleke 221063322
 * @author TG Nenwali 222001364
 */
package graph;
public interface IMap<K,V> {
	public int size();
	public V get(K key);
	public V put(K key,V value);
	public V remove(K key);
	
}
