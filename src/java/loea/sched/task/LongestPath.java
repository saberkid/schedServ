package loea.sched.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

/**
 * An implementation of
 * <a href="https://en.wikipedia.org/wiki/Longest_path_problem">Longest path
 * algorithm</a> based on <code>DijkstraShortestPath</code> and
 * <code>ClosestFirstIterator</code> implementations in jgrapht.
 *
 * @author Tian Huang
 * @since May 21, 2016
 */

public class LongestPath<V, E> {

	final DirectedAcyclicGraph<V, E> graph;
	/**
	 * Map of vertices and their longest path to the exit of a graph.
	 */
	protected Map<V, QueueEntry<E>> map = new HashMap<V, QueueEntry<E>>();

	/**
	 * Creates and executes a new LongestPath algorithm instance. An instance is
	 * only good for a single search; after construction, it can be accessed to
	 * retrieve information about the path found.
	 *
	 * @param graph
	 *            the graph to be searched
	 */
	public LongestPath(DirectedAcyclicGraph<V, E> _graph) {

		graph = _graph;

		if (graph.vertexSet().isEmpty()) {
			throw new IllegalArgumentException("graph must contain at least one vertex");
		}

		DAGEdgeIterator<V, E> iter = new DAGEdgeIterator<V, E>(graph);

		while (iter.hasNext()) {
			E e = iter.next();
			V v = graph.getEdgeSource(e);

			if (map.containsKey(v)) {
				encounterVertexAgain(v, e);
			} else {
				encounterVertex(v, e);
			}

		}
	}

	protected void encounterVertex(V v, E e) {

		QueueEntry<E> dstQe = map.get(graph.getEdgeTarget(e));
		double preLen = dstQe == null ? 0 : dstQe.length;

		QueueEntry<E> curQe = new QueueEntry<E>(e, graph.getEdgeWeight(e) + preLen);
		map.put(v, curQe);
	}

	protected void encounterVertexAgain(V v, E e) {

		QueueEntry<E> dstQe = map.get(graph.getEdgeTarget(e));
		double preLen = dstQe == null ? 0 : dstQe.length;

		if (map.get(v).length < preLen + graph.getEdgeWeight(e)) {
			QueueEntry<E> curQe = new QueueEntry<E>(e, graph.getEdgeWeight(e) + preLen);
			map.put(v, curQe);
		}
	}

	/**
	 * Return the edges making up the path found.
	 *
	 * @return List of Edges, or null if no path exists
	 */
	public List<E> getPathEdgeList(V v) {
		List<E> edges = new ArrayList<E>();
		while (map.containsKey(v)) {
			QueueEntry<E> qe = map.get(v);
			edges.add(qe.spanningTreeEdge);
			v = graph.getEdgeTarget(qe.spanningTreeEdge);
		}

		return edges;
	}

	/**
	 * Return the weighted length of the path found.
	 *
	 * @return path length, or Double.POSITIVE_INFINITY if no path exists
	 */
	public double getPathLength(V v) {
		if (map.containsKey(v)) {
			return map.get(v).length;
		} else {
			return Double.NaN;
		}
	}
}

class DAGEdgeIterator<V, E> implements Iterator<E> {

	final DirectedAcyclicGraph<V, E> graph;
	List<E> unvisited = new ArrayList<E>();
	Set<E> visited = new HashSet<E>();

	public DAGEdgeIterator(DirectedAcyclicGraph<V, E> _graph) {

		graph = _graph;

		if (graph.vertexSet().isEmpty()) {
			throw new IllegalArgumentException("graph must contain at least one vertex.");
		}

		for (V v : graph.vertexSet()) {
			if (graph.outDegreeOf(v) == 0) {
				unvisited.addAll(graph.incomingEdgesOf(v));
			}
		}
	}

	@Override
	public boolean hasNext() {
		if (unvisited.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public E next() {
		E e = unvisited.get(0);
		visited.add(e);

		V v = graph.getEdgeSource(e);
		unvisited.addAll(graph.incomingEdgesOf(v));
		unvisited.removeAll(visited);
//		System.out.println(visited.size() + "/" + graph.edgeSet().size());
		return e;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}

class QueueEntry<E> {
	/**
	 * Best spanning tree edge to vertex seen so far.
	 */
	E spanningTreeEdge;

	/**
	 * The length of the longest path so far.
	 */
	double length;

	public QueueEntry(E _e, double _len) {
		spanningTreeEdge = _e;
		length = _len;
	}

}
