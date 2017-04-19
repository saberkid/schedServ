package loea.sched.simulator;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import loea.sched.ProviderImporter;
import loea.sched.VMImporter;
import loea.sched.scheduler.TaskBrokerCAEFT;
import loea.sched.task.Subtask;
import loea.sched.task.Task;

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

public class ScalableSimulatorCAEFT {

/*	public static void main(String[] args) {

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

		Log.printLine("Starting Scalable Simulator...");

		try {
			// Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = true; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			@SuppressWarnings("unused")
			List<Datacenter> centerList = ProviderImporter.XMLImporter(providerFile);

			TaskBrokerCAEFT broker = new TaskBrokerCAEFT("TaskBroker");

			// VM XMLImporter
			List<Vm> vmList = VMImporter.XMLImporter(VMsFile, broker.getId());
			// submit VM list to broker
			broker.submitVmList(vmList);

			List<Task> taskList = Task.XMLImporter(tasksFile);
			for (Task t : taskList) {
				for (Subtask st : t) {
					st.setUserId(broker.getId());
				}
			}
			broker.submitTaskList(taskList);

			Date start = new Date();
			// Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();
			Date end = new Date();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			Simulator.printCloudletList(newList);

			Log.printLine("Scalable Simulator finished!");
			Log.printLine("Time elapsed: "+((end.getTime()-start.getTime())/1000.0));
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}

	}*/
	public static HashMap<String, String> runScheduler(String providerFile , String tasksFile, String VMsFile){
        providerFile = "C:\\xampp\\tomcat\\webapps\\schedServ\\web\\WEB-INF\\prov_h4.xml";
        tasksFile = "C:\\xampp\\tomcat\\webapps\\schedServ\\web\\WEB-INF\\task_s01_t1_st1000_e10000.xml";
        VMsFile = "C:\\xampp\\tomcat\\webapps\\schedServ\\web\\WEB-INF\\vm_v4.xml";
		Log.printLine("Starting Scalable Simulator...");
        HashMap <String, String> simuResult = new HashMap<String, String>();
        simuResult.put("success","false");
        simuResult.put("time","0");
		try {
			// Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = true; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			@SuppressWarnings("unused")
			List<Datacenter> centerList = ProviderImporter.XMLImporter(providerFile);

			TaskBrokerCAEFT broker = new TaskBrokerCAEFT("TaskBroker");

			// VM XMLImporter
			List<Vm> vmList = VMImporter.XMLImporter(VMsFile, broker.getId());
			// submit VM list to broker
			broker.submitVmList(vmList);

			List<Task> taskList = Task.XMLImporter(tasksFile);
			for (Task t : taskList) {
				for (Subtask st : t) {
					st.setUserId(broker.getId());
				}
			}
			broker.submitTaskList(taskList);

			Date start = new Date();
			// Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();
			Date end = new Date();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			Simulator.printCloudletList(newList);
            simuResult.put("success","true");
            simuResult.put("time", String.valueOf((end.getTime()-start.getTime())/1000.0));
			Log.printLine("Scalable Simulator finished!");
			Log.printLine("Time elapsed: "+((end.getTime()-start.getTime())/1000.0));
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}

		return simuResult;
	}
	private static void help(Options _opt) {

		// This prints out some help
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Main", _opt);
		System.exit(0);

	}
}
