package loea.sched.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import loea.sched.CompSchedEvent;
import loea.sched.CompSchedTag;
import loea.sched.task.Subtask;
import loea.sched.task.Task;

public class TaskBrokerRR extends DatacenterBroker {

	private final List<Task> taskExecList = new ArrayList<Task>();
	private final List<Task> taskPausedList = new ArrayList<Task>();
	private final List<Task> taskFinishedList = new ArrayList<Task>();
	private final List<Task> futureTask = new ArrayList<Task>();

	private Map<Integer, Integer> map = new HashMap<Integer, Integer>();

	private int vmId = 0;

	public TaskBrokerRR(String name) throws Exception {
		super(name);
	}

	public void submitTaskList(List<Task> list) {
		for (Task t : list) {
			if (t.getArrivalTime() <= CloudSim.clock()) {
				taskExecList.add(t);
			} else {
				futureTask.add(t);
			}
		}
	}

	/**
	 * submitVmList and submitTask must be called before this method
	 * 
	 * @param map
	 *            the map to set
	 */
	public void setMap(Map<Integer, Integer> _map) {
		map.clear();
		map.putAll(_map);
	}

	private Map<Subtask, Vm> scheduling() {

		List<Subtask> runnableCloudlets = new ArrayList<Subtask>();
		for (Task t : taskExecList) {
			List<Subtask> list = t.getRunnableSubtasks();
			runnableCloudlets.addAll(list);
		}
		Map<Subtask, Vm> _map = mapSubtasks2VMs(runnableCloudlets);
		return _map;
	}

	/**
	 * map subtasks to the VMs
	 * 
	 * @param cloudlets
	 */
	private Map<Subtask, Vm> mapSubtasks2VMs(List<Subtask> subtasks) {

		Map<Subtask, Vm> _map = new HashMap<Subtask, Vm>();

		for (Subtask st : subtasks) {
			_map.put(st, vmList.get(vmId++));
			if (vmId >= vmList.size()) {
				vmId = 0;
			}
		}

		return _map;

	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {

		Map<Subtask, Vm> _map = scheduling();
		if (!_map.isEmpty()) {
			submitCloudlets(_map);
		}

	}

	/**
	 * Submit subtasks to the created VMs
	 * 
	 * @param cloudlets
	 */
	protected void submitCloudlets(Map<Subtask, Vm> map) {
		for (Subtask st : map.keySet()) {
			Vm vm = map.get(st);
			submitCloudlet(st, vm);
		}
	}

	/**
	 * Submit a Cloudlet to the created VMs
	 * 
	 * @param cloudlets
	 */
	protected void submitCloudlet(Subtask subtask, Vm vm) {
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet " + subtask.getCloudletId() + " to VM #"
				+ vm.getId());
		subtask.setVmId(vm.getId());
		getCloudletList().add(subtask);
		subtask.issued();
		cloudletsSubmitted++;
		sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, subtask);
	}

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId() + " received");
		completed((Subtask) cloudlet);
		cloudletsSubmitted--;
		// stCnt++;
		submitCloudlets();

		if (isComplete()) { // all subtasks executed
			Log.printLine(CloudSim.clock() + ": " + getName() + ": All tasks executed. Idle...");
			clearDatacenters();
			finishExecution();
		} else {
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				createVmsInDatacenter(0);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cloudbus.cloudsim.DatacenterBroker#processOtherEvent(org.cloudbus
	 * .cloudsim.core.SimEvent)
	 */
	@Override
	protected void processOtherEvent(SimEvent ev) {
		switch (ev.getTag()) {
		case CloudSimTags.EXPERIMENT:
			CompSchedEvent csEv = new CompSchedEvent(ev);
			processCompSchedEvent(csEv);
			break;
		// other unknown tags are processed by this method
		default:
			super.processOtherEvent(ev);

		}
	}

	protected void processCompSchedEvent(CompSchedEvent ev) {

		switch (ev.getTag()) {

		case TASK_INCOMING:
			processIncomingTask(ev);
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * 
	 * @param ev
	 */

	protected void processIncomingTask(CompSchedEvent ev) {
		Task t = (Task) ev.getData();
		taskExecList.add(t);
		futureTask.remove(t);
		submitCloudlets();
	}

	private void completed(Subtask st) {
		st.getParent().completed(st);
		if (st.getParent().isCompleted()) {
			taskExecList.remove(st.getParent());
			taskFinishedList.add(st.getParent());
		}
	}

	private boolean isComplete() {
		if (taskExecList.isEmpty() && taskPausedList.isEmpty()) {
			// System.out.print(stCnt);
			return true;
		} else {
			return false;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
		for (Task t : futureTask) {
			send(getId(), t.getArrivalTime() - CloudSim.clock(), CompSchedTag.TASK_INCOMING, t);
		}
	}

	protected void send(int entityId, double delay, CompSchedTag _tags, Object _data) {
		Object data = CompSchedEvent.createTagDataPair(_tags, _data);
		send(entityId, delay, CloudSimTags.EXPERIMENT, data);
	}

}
