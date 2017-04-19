package loea.sched.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import loea.sched.CompSchedEvent;
import loea.sched.CompSchedTag;
import loea.sched.task.Subtask;
import loea.sched.task.Task;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * @author ian
 *
 */
public class TaskBrokerCAEFT extends DatacenterBroker {

	private final static double SCHEDULING_PERIOD = 1.0;

	private final List<Task> taskExecList = new ArrayList<Task>();
	private final List<Task> taskPausedList = new ArrayList<Task>();
	private final List<Task> taskFinishedList = new ArrayList<Task>();
	private final List<Task> futureTask = new ArrayList<Task>();

	private final List<SchedulingRequest> schedulingRequests = new ArrayList<SchedulingRequest>();
	
//	private int vmId = 0;

	public TaskBrokerCAEFT(String name) throws Exception {
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
		send(getId(), SCHEDULING_PERIOD, CompSchedTag.PERIODIC_TASK_SCHEDULING, null);
	}

	private void schedule(SchedulingRequest sq) {
		switch (sq.getTag()) {
		case SUBMIT_NEW_SUBTASKS:
			for (int datacenterId : datacenterIdsList) {
				send(datacenterId, 0, CompSchedTag.SUBMIT_NEW_SUBTASKS, sq.getData());
			}
			break;
		case PERIODIC_TASK_SCHEDULING:
			for (int datacenterId : datacenterIdsList) {
				send(datacenterId, 0, CompSchedTag.PERIODIC_TASK_SCHEDULING, sq.getData());
			}
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * map subtasks to the VMs
	 * 
	 * @param cloudlets
	 */
	private Map<Subtask, Vm> mapSubtasks2VMs(List<Subtask> subtasks, List<Host> hostList) {

		class VMComparator implements Comparator<Vm> {

			final Subtask st;
			final Map<Subtask, Vm> map;
			final Map<Vm, List<Double>> mips;

			public VMComparator(Subtask _st, Map<Subtask, Vm> _map, Map<Vm, List<Double>> _mips) {
				st = _st;
				map = _map;
				mips = _mips;
			}

			private double estimateFinishedTime(Vm vm, Subtask st, Set<Subtask> allosts, List<Double> vmMipsPower) {

				SubtaskScheduler scheduler = (SubtaskScheduler) vm.getCloudletScheduler();

				long totalWorkload = 0;
				for (ResCloudlet rcl : scheduler.getCloudletExecList()) {
					totalWorkload += rcl.getRemainingCloudletLength();
				}
				for (Subtask allost : allosts) {
					totalWorkload += allost.getCloudletLength();
				}
				totalWorkload += st.getCloudletLength();

				double power = 0;
				for (double p : vmMipsPower) {
					power += p;
				}

				return totalWorkload / power;
			}

			private Set<Subtask> filter(Map<Subtask, Vm> _map, Vm vm) {
				Set<Subtask> subtasks = new HashSet<Subtask>();
				for (Entry<Subtask, Vm> entry : _map.entrySet()) {
					if (entry.getValue() == vm) {
						subtasks.add(entry.getKey());
					}
				}
				return subtasks;
			}

			@Override
			public int compare(Vm arg0, Vm arg1) {

				double arg0time = estimateFinishedTime(arg0, st, filter(map, arg0), mips.get(arg0));
				double arg1time = estimateFinishedTime(arg1, st, filter(map, arg1), mips.get(arg1));

				if (arg0time < arg1time) {
					return -1;
				} else if (arg0time == arg1time) {
					return 0;
				} else {
					return 1;
				}
			}
		}

		Map<Subtask, Vm> map = new HashMap<Subtask, Vm>();

		Collections.sort(subtasks, Collections.reverseOrder(Task.criticalPathComparator));

		List<Vm> _vmList = new ArrayList<Vm>();
		_vmList.addAll(vmList);

		Map<Vm, List<Double>> mips = new HashMap<Vm, List<Double>>();
		for (Host host : hostList) {
			for (Vm vm : host.getVmList()) {
				mips.put(vm, host.getAllocatedMipsForVm(vm));
			}
		}

		for (Subtask st : subtasks) {
			VMComparator comp = new VMComparator(st, map, mips);
			Collections.sort(_vmList, comp);
			map.put(st, _vmList.get(0));
		}

//		 int subtaskId = 0;
//		 Map<Subtask, Vm> map = new HashMap<Subtask, Vm>();
//		
//		 while (map.size() < subtasks.size()) {
//		 map.put(subtasks.get(subtaskId++), vmList.get(vmId++));
//		 if (vmId >= vmList.size()) {
//		 vmId = 0;
//		 }
//		 }

		return map;
	}

	/**
	 * Submit subtasks to the created VMs
	 * 
	 * @param cloudlets
	 */
	protected void submitCloudlets(Map<Subtask, Vm> map) {
		Iterator<Entry<Subtask, Vm>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Subtask, Vm> entry = it.next();
			submitCloudlet(entry.getKey(), entry.getValue());
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
		sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, subtask);
	}

	/**
	 * Destroy the virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (Vm vm : getVmsCreatedList()) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
		}

		getVmsCreatedList().clear();
	}

	protected void send(int entityId, double delay, CompSchedTag _tags, Object _data) {
		Object data = CompSchedEvent.createTagDataPair(_tags, _data);
		send(entityId, delay, CloudSimTags.EXPERIMENT, data);
	}

	protected void periodicScheduling(CompSchedEvent ev) {
		if (!isComplete()) {

			List<SchedulingRequest> tmpRequests = new ArrayList<SchedulingRequest>();
			for (SchedulingRequest sq : schedulingRequests) {
				if (sq.getTag() == CompSchedTag.PERIODIC_TASK_SCHEDULING) {
					// Log.printLine("Datacenter " + ev.getEntSrc() + " said: "
					// + ev.getData());
					tmpRequests.add(sq);
				}
			}
			schedulingRequests.removeAll(tmpRequests);

			SchedulingRequest sq = new SchedulingRequest(CompSchedTag.PERIODIC_TASK_SCHEDULING, CloudSim.clock(),
					"Periodic Scheduling requests info");
			schedulingRequests.add(sq);
			schedule(sq);
			send(getId(), SCHEDULING_PERIOD, CompSchedTag.PERIODIC_TASK_SCHEDULING, null);
		}
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

		if (isComplete()) { // all subtasks executed
			Log.printLine(CloudSim.clock() + ": " + getName() + ": All tasks executed. Idle...");
			// clearDatacenters();
			// finishExecution();
		} else {

			SchedulingRequest sq = new SchedulingRequest(CompSchedTag.SUBMIT_NEW_SUBTASKS, CloudSim.clock(),
					"Cloudlet " + cloudlet.getCloudletId() + " finished, request info for subsequent subtasks.");
			schedulingRequests.add(sq);
			schedule(sq);
		}
	}

	protected void processCompSchedEvent(CompSchedEvent ev) {

		switch (ev.getTag()) {

		case TASK_INCOMING:
			processIncomingTask(ev);
			break;
		case SUBMIT_NEW_SUBTASKS:
			processSubmitNewSubtasks(ev);
			break;
		case PERIODIC_TASK_SCHEDULING:
			periodicScheduling(ev);
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
		SchedulingRequest sq = new SchedulingRequest(CompSchedTag.SUBMIT_NEW_SUBTASKS, CloudSim.clock(), null);
		schedulingRequests.add(sq);
		schedule(sq);
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

	protected void processSubmitNewSubtasks(CompSchedEvent ev) {

		List<SchedulingRequest> tmpRequests = new ArrayList<SchedulingRequest>();
		for (SchedulingRequest sq : schedulingRequests) {
			if (sq.getTag() == CompSchedTag.SUBMIT_NEW_SUBTASKS) {
				tmpRequests.add(sq);
			}
		}
		schedulingRequests.removeAll(tmpRequests);

		List<Subtask> runnableCloudlets = new ArrayList<Subtask>();
		for (Task t : taskExecList) {
			List<Subtask> list = t.getRunnableSubtasks();
			runnableCloudlets.addAll(list);
		}
		Map<Subtask, Vm> map = mapSubtasks2VMs(runnableCloudlets, (List<Host>) ev.getData());
		submitCloudlets(map);
	}

	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			Vm vm = VmList.getById(getVmList(), vmId);
			getVmsCreatedList().add(vm);
			Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId + " has been created in Datacenter #"
					+ datacenterId + ", Host #" + VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
		} else {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId + " failed in Datacenter #"
					+ datacenterId);
		}

		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
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
			return true;
		} else {
			return false;
		}
	}
}

class SchedulingRequest {
	private final CompSchedTag tag;
	private final double time;
	private final Object data;
	private List<Object> infoList;

	/**
	 * @param tag
	 * @param time
	 * @param data
	 */
	SchedulingRequest(CompSchedTag tag, double time, Object data) {
		super();
		this.tag = tag;
		this.time = time;
		this.data = data;
		this.infoList = new ArrayList<Object>();
	}

	List<Object> getInfo() {
		return infoList;
	}

	void setInfo(List<Object> _infoList) {
		this.infoList = _infoList;
	}

	void addInfo(Object _info) {
		infoList.add(_info);
	}

	CompSchedTag getTag() {
		return tag;
	}

	double getTime() {
		return time;
	}

	Object getData() {
		return data;
	}

}