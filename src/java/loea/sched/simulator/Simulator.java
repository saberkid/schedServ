package loea.sched.simulator;


import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import loea.sched.scheduler.SubtaskScheduler;
import loea.sched.scheduler.TaskBrokerCAEFT;
import loea.sched.task.Subtask;
import loea.sched.task.Task;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class Simulator {

	/** The Task list. */
	private static List<Task> taskList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example.
	 *
	 * @param args
	 *            the args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {

		Log.printLine("Starting CloudSimExample1...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = true; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			Datacenter datacenter0 = createDatacenter("Datacenter_0");

			// Third step: Create Broker
			TaskBrokerCAEFT broker = createBroker();
			int brokerId = broker.getId();

			// Fourth step: Create one virtual machine
			vmlist = new ArrayList<Vm>();

			// VM description
			int vmid = 0;
			int mips = 1000;
			long size = 10000; // image size (MB)
			int ram = 512; // vm memory (MB)
			long bw = 1000;
			int pesNumber = 1; // number of cpus
			String vmm = "Xen"; // VMM name

			// create VM and add the VM to the vmList
			vmlist.add(new Vm(vmid++, brokerId, mips, pesNumber, ram, bw, size,
					vmm, new SubtaskScheduler()));
			vmlist.add(new Vm(vmid++, brokerId, mips, pesNumber, ram, bw, size,
					vmm, new SubtaskScheduler()));
			vmlist.add(new Vm(vmid++, brokerId, mips, pesNumber, ram, bw, size,
					vmm, new SubtaskScheduler()));
			vmlist.add(new Vm(vmid++, brokerId, mips, pesNumber, ram, bw, size,
					vmm, new SubtaskScheduler()));

			// submit vm list to the broker
			broker.submitVmList(vmlist);

			// Fifth step: Create an empty task list
			taskList = new ArrayList<Task>();

			Task t = new Task(0, 0, "configs/example2.txt");
			t.setUserId(brokerId);

			// add the task to the list
			taskList.add(t);

			// submit task list to the broker
			broker.submitTaskList(taskList);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);
			System.out.println();
			printGantt(newList);
			System.out.println();

			Log.printLine("CloudSimExample1 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	/**
	 * Creates the datacenter.
	 *
	 * @param name
	 *            the name
	 *
	 * @return the datacenter
	 */
	private static Datacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 1000;
		int peId = 0;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(peId++, new PeProvisionerSimple(mips))); // need to
																	// store
																	// Pe id and
																	// MIPS
																	// Rating
		peList.add(new Pe(peId++, new PeProvisionerSimple(mips)));
		peList.add(new Pe(peId++, new PeProvisionerSimple(mips)));
		peList.add(new Pe(peId++, new PeProvisionerSimple(mips)));

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int hostId = 0;
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;

		hostList.add(new Host(hostId, new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw), storage, peList,
				new VmSchedulerSpaceShared(peList))); // This is our machine

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are
																		// not
																		// adding
																		// SAN
		// devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics,
					new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	/**
	 * Creates the broker.
	 *
	 * @return the datacenter broker
	 */
	private static TaskBrokerCAEFT createBroker() {
		TaskBrokerCAEFT broker = null;
		try {
			broker = new TaskBrokerCAEFT("Broker"
					);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list
	 *            list of Cloudlets
	 */
	public static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}

	private static void printGantt(List<Cloudlet> list) {
		GanttChart chart = new GanttChart();
		for (Cloudlet cl : list) {
			chart.add((Subtask) cl);
		}
		System.out.println(chart.print());
	}

}

class GanttChart {
	Map<Integer, List<Subtask>> map = new TreeMap<Integer, List<Subtask>>();
	final static int lineWidth = 80;
	final static String fill = "-";
	final static String whitespace = " ";
	final static int vmIdWidth = 8;
	final static int taskIdWidth = 8;
	double allTimeStart = Double.POSITIVE_INFINITY;
	double allTimeEnd = Double.NEGATIVE_INFINITY;
	double totalTimeSpan = allTimeEnd - allTimeStart;
	double widthPerTime = getWidthPerTime(totalTimeSpan);

	void add(Subtask st) {
		if (!map.containsKey(st.getVmId())) {
			map.put(st.getVmId(), new ArrayList<Subtask>());
		}
		map.get(st.getVmId()).add(st);
		if (allTimeStart > st.getExecStartTime()) {
			allTimeStart = st.getExecStartTime();
		}
		if (allTimeEnd < st.getFinishTime()) {
			allTimeEnd = st.getFinishTime();
		}
		totalTimeSpan = allTimeEnd - allTimeStart;
		widthPerTime = getWidthPerTime(totalTimeSpan);
	}

	double getWidthPerTime(double span) {
		return (lineWidth - vmIdWidth - taskIdWidth) / span;
	}

	String print() {
		if (map.isEmpty()) {
			return "GanttChart cannot be printed because it is empty!";
		}
		String out = printTitle();

		Iterator<Entry<Integer, List<Subtask>>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, List<Subtask>> entry = it.next();
			int vmId = entry.getKey();
			List<Subtask> subtasks = entry.getValue();
			out += printVMLine(vmId);
			for (Subtask subtask : subtasks) {
				out += printSubtaskLine(subtask);
			}
		}
		return out;
	}

	String printTitle() {
		String line = String.format("%-" + vmIdWidth + "s%-" + taskIdWidth
				+ "s%s", "VMID", "TaskID", "Time Span");
		line += "\n";
		return line;
	}

	String printVMLine(int vmId) {
		String line = String.format("%-" + vmIdWidth + "d", vmId);
		line += "\n";
		return line;
	}

	String printSubtaskLine(Subtask st) {

		int startPosition = (int) (widthPerTime * st.getExecStartTime());
		int periodWidth = (int) (widthPerTime * st.getActualCPUTime());

		String line = new String(new char[vmIdWidth]).replace("\0", whitespace);
		String taskId = String.format("%d.%d", st.getParent().getId(),
				st.getCloudletId());
		line += String.format("%-" + taskIdWidth + "s", taskId);
		String period = new String(new char[periodWidth]).replace("\0", fill);
		line += String
				.format("%" + (startPosition + periodWidth) + "s", period);
		line += "\n";
		return line;
	}

}