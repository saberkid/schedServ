package loea.sched.task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.jgrapht.Graph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.ext.EdgeProvider;
import org.jgrapht.ext.DOTImporter;
import org.jgrapht.ext.ImportException;
import org.jgrapht.ext.VertexProvider;
import org.jgrapht.graph.DefaultEdge;

/**
 * @author ian
 *
 */
public class Task implements Iterable<Subtask> {

	private Map<Integer, Subtask> subtasks = new HashMap<Integer, Subtask>();
	private Map<Integer, Long> longestPathes = new HashMap<Integer, Long>();
	private DirectedAcyclicGraph<Integer, DefaultEdge> graph = new DirectedAcyclicGraph<Integer, DefaultEdge>(
			DefaultEdge.class);

	private double arrivalTime = 0;
	private double deadline = Double.POSITIVE_INFINITY;

	private int ref;

	private int priority = 0; // -20~19, -20 is the highest priority

	private static int ID_COUNT = 0;
	public final int id;

	public static final long DEFAULTFILESIZE = 100;
	public static final int DEFAULTPESNUMBER = 1;
	public static final UtilizationModel DEFAULTUTILIZATIONMODEL = new UtilizationModelFull();
	public static final int MAXIMUMLENGTH = (int) 1e6;
	public static final int MINIMUMLENGTH = (int) 1e2;
	public static final String CLOUDLETLENGTH = "Length";

	protected final Set<Integer> completedSubtasks;
	protected final Set<Integer> issuedSubtasks;

	public static final Comparator<Task> arrivalTimeComparator = new Comparator<Task>() {
		@Override
		public int compare(Task arg0, Task arg1) {
			if (arg0.getArrivalTime() < arg1.getArrivalTime()) {
				return -1;
			} else if (arg0.getArrivalTime() == arg1.getArrivalTime()) {
				return 0;
			} else {
				return 1;
			}
		}
	};

	public static final Comparator<Task> deadlineComparator = new Comparator<Task>() {
		@Override
		public int compare(Task arg0, Task arg1) {
			if (arg0.getDeadline() < arg1.getDeadline()) {
				return -1;
			} else if (arg0.getDeadline() == arg1.getDeadline()) {
				return 0;
			} else {
				return 1;
			}
		}
	};

	public static final Comparator<Subtask> criticalPathComparator = new Comparator<Subtask>() {
		@Override
		public int compare(Subtask arg0, Subtask arg1) {
			if (arg0.getCriticalPathToExit() < arg1.getCriticalPathToExit()) {
				return -1;
			} else if (arg0.getCriticalPathToExit() == arg1.getCriticalPathToExit()) {
				return 0;
			} else {
				return 1;
			}
		}
	};

	/**
	 * 
	 */
	public Task() {
		this(0, 0);
	}

	public Task(int tArriving, int prio, String graphFile) {

		this(tArriving, prio);

		DOTImporter<Integer, DefaultEdge> importer = new DOTImporter<Integer, DefaultEdge>(
				new VertexProvider<Integer>() {
					@Override
					public Integer buildVertex(String label, Map<String, String> attributes) {
						long length = Long.parseLong(attributes.get(Task.CLOUDLETLENGTH));
						Subtask st = new Subtask(length);
						subtasks.put(st.getRef(), st);
						return st.getRef();
					}
				}, new EdgeProvider<Integer, DefaultEdge>() {
					@Override
					public DefaultEdge buildEdge(Integer from, Integer to, String label,
							Map<String, String> attributes) {
						return new DefaultEdge();
					}
				}, null);

		try {
			byte[] encoded = Files.readAllBytes(Paths.get(graphFile));
			importer.read(new String(encoded), graph);
		} catch (ImportException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Subtask st : this) {
			st.setParent(this);
			st.setHeight(0);
		}

		calcHeight();
	}

	private Task(int tArriving, int prio) {
		arrivalTime = tArriving;
		priority = prio;
		completedSubtasks = new HashSet<Integer>();
		issuedSubtasks = new HashSet<Integer>();
		id = ID_COUNT++;
	}

	public boolean isCompleted() {
		if (completedSubtasks != null) {
			if (completedSubtasks.size() == graph.vertexSet().size()) {
				return true;
			}
		}
		return false;
	}

	public void setUserId(int _id) {
		for (Subtask st : subtasks.values())
			st.setUserId(_id);
	}

	public void addSubtask(Subtask st) {
		graph.addVertex(st.getRef());
		subtasks.put(st.getRef(), st);
		st.setParent(this);
		st.setHeight(0);

	}

	public boolean addDependency(Subtask src, Subtask dst) {
		try {
			graph.addDagEdge(src.getRef(), dst.getRef());
			return true;
		} catch (CycleFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean addDependency(int src, int dst) {
		try {
			graph.addDagEdge(src, dst);
			return true;
		} catch (CycleFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	// calculate the height of all subtasks
	public void calcHeight() {
		for (Subtask st : this) {
			for (DefaultEdge edge : graph.incomingEdgesOf(st.getRef())) {
				Subtask srcSt = subtasks.get(graph.getEdgeSource(edge));
				int srcHeight = srcSt.getHeight();
				if (st.getHeight() < srcHeight + 1) {
					st.setHeight(srcHeight + 1);
				}
			}
		}
	}

	/**
	 * Calculate the critical path length from every vertex to the exit of the
	 * graph
	 */
	public void calcCriticalPathLength() {

		Map<Integer, Subtask> tempTasks = new HashMap<Integer, Subtask>();

		DirectedAcyclicGraph<Integer, DefaultEdge> tempGraph = new DirectedAcyclicGraph<Integer, DefaultEdge>(
				DefaultEdge.class) {
			/**
			* 
			*/
			private static final long serialVersionUID = 5689924817978034026L;

			/**
			 * @see Graph#getEdgeWeight(Object)
			 */
			@Override
			public double getEdgeWeight(DefaultEdge e) {
				Subtask srcSt = tempTasks.get(getEdgeSource(e));
				return srcSt.getCloudletLength();
			};
		};
		// duplicate graph
		for (int st : graph.vertexSet()) {
			tempGraph.addVertex(st);
		}
		for (DefaultEdge e : graph.edgeSet()) {
			int src = graph.getEdgeSource(e);
			int dst = graph.getEdgeTarget(e);
			try {
				tempGraph.addDagEdge(src, dst);
			} catch (CycleFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		tempTasks.putAll(subtasks);

		// add dummy exit
		List<Integer> realExitList = new ArrayList<Integer>();
		for (int st : tempGraph.vertexSet()) {
			if (tempGraph.outDegreeOf(st) == 0) {
				realExitList.add(st);
			}
		}
		Subtask dummyExit = new Subtask(0);
		tempGraph.addVertex(dummyExit.getRef());
		for (int realExit : realExitList) {
			try {
				tempGraph.addDagEdge(realExit, dummyExit.getRef());
			} catch (CycleFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		LongestPath<Integer, DefaultEdge> lp = new LongestPath<Integer, DefaultEdge>(tempGraph);

		for (int st : graph.vertexSet()) {
			long cplen = (long) lp.getPathLength(st);
			longestPathes.put(st, cplen);
		}
	}

	public boolean completed(Subtask st) {
		if (!graph.containsVertex(st.getRef())) {
			return false;
		}
		if (completedSubtasks.contains(st.getRef())) {
			return false;
		}
		completedSubtasks.add(st.getRef());
		issuedSubtasks.remove(Integer.valueOf(st.getRef()));
		return true;
	}

	protected boolean issued(Subtask st) {
		if (st.getParent() != this) {
			return false;
		} else {
			issuedSubtasks.add(st.getRef());
			return true;
		}
	}

	public List<Subtask> getRunnableSubtasks() {

		List<Subtask> runnableSubtasks = new ArrayList<Subtask>();

		for (Subtask st : subtasks.values()) {
			if (isReady(st)) {
				runnableSubtasks.add(st);
			}
		}
		return runnableSubtasks;
	}

	protected boolean isReady(Subtask st) {
		if (!graph.containsVertex(st.getRef())) {
			return false;
		}
		if (completedSubtasks.contains(st.getRef()) || issuedSubtasks.contains(st.getRef())) {
			return false;
		}
		Set<DefaultEdge> edges = graph.incomingEdgesOf(st.getRef());
		boolean allPrecedentComplete = true;
		for (DefaultEdge e : edges) {
			Subtask src = subtasks.get(graph.getEdgeSource(e));
			if (!completedSubtasks.contains(src.getRef())) {
				allPrecedentComplete = false;
				break;
			}
		}
		if (allPrecedentComplete) {
			return true;
		} else {
			return false;
		}
	}

	public boolean contains(Subtask v) {
		return subtasks.containsValue(v);
	}

	public boolean contains(Cloudlet v) {
		return subtasks.containsValue(v);
	}

	public static List<Task> XMLImporter(String file) {

		final XPathFactory xFactory = XPathFactory.instance();
		final SAXBuilder builder = new SAXBuilder();
		final XPathExpression<Element> taskExpr = xFactory.compile("/Customer/Task[@ref]", Filters.element());
		final String ARRIVALTIME = "arrivalTime";
		final String DEADLINE = "deadline";
		final XPathExpression<Element> subtExpr = xFactory.compile("Subtask[@ref]", Filters.element());
		final XPathExpression<Element> depeExpr = xFactory.compile("Dependency[@src and @dst]", Filters.element());

		List<Task> taskList = new ArrayList<Task>();

		try {

			Document doc = (Document) builder.build(file);
			doc.getRootElement();

			for (Element taskEle : taskExpr.evaluate(doc)) {

				Task task = new Task();

				String _ref = taskEle.getAttributeValue("ref");
				if (_ref != null && !_ref.isEmpty()) {
					task.setRef(Integer.parseInt(_ref));
				}

				String arri = taskEle.getChildTextNormalize(ARRIVALTIME);
				if (arri != null && !arri.isEmpty()) {
					task.setArrivalTime(Double.parseDouble(arri));
				}

				String dead = taskEle.getChildTextNormalize(DEADLINE);
				if (dead != null && !dead.isEmpty()) {
					task.setDeadline(Double.parseDouble(dead));
				}

				for (Element subtEle : subtExpr.evaluate(taskEle)) {
					Subtask subtask = Subtask.XMLImporter(subtEle);
					task.addSubtask(subtask);
				}

				for (Element depeEle : depeExpr.evaluate(taskEle)) {
					String srcAtt = depeEle.getAttributeValue("src");
					String dstAtt = depeEle.getAttributeValue("dst");
					if (srcAtt != null && dstAtt != null) {
						int src = Integer.parseInt(srcAtt);
						int dst = Integer.parseInt(dstAtt);
						task.addDependency(src, dst);
					}
				}

				task.calcCriticalPathLength();
				task.calcHeight();

				taskList.add(task);
			}

		} catch (IOException io) {
			System.out.println(io.getMessage());
		} catch (JDOMException jdomex) {
			System.out.println(jdomex.getMessage());
		}

		return taskList;
	}

	@Override
	public Iterator<Subtask> iterator() {
		return new SubtaskIterator(graph.iterator());
	}

	class SubtaskIterator implements Iterator<Subtask> {
		final Iterator<Integer> it;

		public SubtaskIterator(Iterator<Integer> _it) {
			it = _it;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public Subtask next() {
			return subtasks.get(it.next());
		}
	}

	public double getArrivalTime() {
		return arrivalTime;
	}

	void setArrivalTime(double _arrivalTime) {
		arrivalTime = _arrivalTime;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int _priority) {
		priority = _priority;
	}

	public double getDeadline() {
		return deadline;
	}

	public void setDeadline(double _deadline) {
		deadline = _deadline;
	}

	public int getId() {
		return id;
	}

	public int getRef() {
		return ref;
	}

	public void setRef(int _ref) {
		ref = _ref;
	}

	public void reset() {
		completedSubtasks.clear();
		issuedSubtasks.clear();

		Map<Integer, Subtask> resetedSubtasks = new HashMap<Integer, Subtask>();
		for (Integer id : subtasks.keySet()) {
			Subtask st = subtasks.get(id).reset();
			st.setParent(this);
			resetedSubtasks.put(id, st);
		}
		subtasks = resetedSubtasks;
	}
}
