package loea.sched.simulator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import loea.sched.ProviderImporter;
import loea.sched.VMImporter;
import loea.sched.scheduler.TaskBrokerGA;
import loea.sched.task.Subtask;
import loea.sched.task.Task;

public class ScalableSimulatorGA {

	private final String provider;
	private final List<Task> taskList;
	private final String vms;

	private final Set<Integer> vmSet = new HashSet<Integer>();
	private final Set<Integer> subtaskSet = new HashSet<Integer>();

	public ScalableSimulatorGA(String _provider, String _tasksFile, String _vms) {
		provider = _provider;
		vms = _vms;
		taskList = Task.XMLImporter(_tasksFile);
		List<Vm> vmList = VMImporter.XMLImporter(_vms, 0);

		for (Vm vm : vmList) {
			vmSet.add(vm.getId());
		}
		for (Task t : taskList) {
			for (Subtask st : t) {
				subtaskSet.add(st.getRef());
			}
		}

		double timeCost = 0;

		Map<Integer, Integer> map;

		Date start = new Date();
		for (int i = 0; i < 100; i++) {
			map = randomMap(subtaskSet, vmSet);
			timeCost = eval(map);
			System.out.println(timeCost);
		}
		Date end = new Date();
		System.out.println((end.getTime() - start.getTime()) / 1000.0);

	}

	private Map<Integer, Integer> randomMap(Set<Integer> _subtaskSet, Set<Integer> _vmSet) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		List<Integer> _vmList = new ArrayList<Integer>(_vmSet);
		Random rand = new Random();
		for (int st : _subtaskSet) {
			int vm = _vmList.get(rand.nextInt(_vmList.size()));
			map.put(st, vm);
		}
		return map;
	}

	private static void help(Options _opt) {

		// This prints out some help
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Main", _opt);
		System.exit(0);

	}

	private double eval(Map<Integer, Integer> _map) {
		return eval(_map, provider, vms, taskList);
	}

	private double eval(Map<Integer, Integer> _map, String _provider, String _vms, List<Task> taskList) {
		double timeCost = 0;

		try {
			// Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = true; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			@SuppressWarnings("unused")
			List<Datacenter> centerList = ProviderImporter.XMLImporter(_provider);

			TaskBrokerGA broker = new TaskBrokerGA("TaskBroker");

			// VM XMLImporter
			List<Vm> _vmList = VMImporter.XMLImporter(_vms, broker.getId());
			// submit VM list to broker
			broker.submitVmList(_vmList);

			for (Task t : taskList) {
				t.reset();
				for (Subtask st : t) {
					st.setUserId(broker.getId());
				}
			}
			broker.submitTaskList(taskList);

			broker.setMap(_map);

			// Starts the simulation
			timeCost = CloudSim.startSimulation();

			CloudSim.stopSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			Simulator.printCloudletList(newList);

			Log.printLine("Scalable Simulator finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}

		return timeCost;

	}

	public static void main(String[] args) {

		String providerFile = "";
		String tasksFile = "";
		String VMsFile = "";

		Options options = new Options();

		{
			options.addOption("h", "help", false, "show help.");
			Option option;
			option = Option.builder("p").longOpt("provider").required().hasArg()
					.desc("specifies the file defining the cloud provider.").build();
			options.addOption(option);
			option = Option.builder("t").longOpt("task").required().hasArg()
					.desc("specifies the file describing the tasks.").build();
			options.addOption(option);
			option = Option.builder("v").longOpt("vm").required().hasArg()
					.desc("specifies the file describing the virtual machines.").build();
			options.addOption(option);
		}

		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("h"))
				help(options);
			providerFile = cmd.getOptionValue("p");
			tasksFile = cmd.getOptionValue("t");
			VMsFile = cmd.getOptionValue("v");

		} catch (ParseException e) {
			help(options);
		}

		System.out.println("Starting GA Scheduling...");
		Log.setDisabled(true);

		ScalableSimulatorGA simulator = new ScalableSimulatorGA(providerFile, tasksFile, VMsFile);
	}

}
