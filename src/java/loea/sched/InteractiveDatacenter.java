/**
 * 
 */
package loea.sched;

import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * An interactive Datacenter that response to the info request from
 * DatacenterBroker
 * 
 * @author ian
 *
 */
public class InteractiveDatacenter extends Datacenter {

	/**
	 * @param name
	 * @param characteristics
	 * @param vmAllocationPolicy
	 * @param storageList
	 * @param schedulingInterval
	 * @throws Exception
	 */
	public InteractiveDatacenter(String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList,
				schedulingInterval);
	}

	@Override
	protected void processOtherEvent(SimEvent ev) {
		switch (ev.getTag()) {
		case CloudSimTags.EXPERIMENT:
			CompSchedEvent csEv = new CompSchedEvent(ev);
			processCompSchedEvent(csEv);
			break;
		default:
			super.processOtherEvent(ev);
			break;
		}
	}

	protected void processCompSchedEvent(CompSchedEvent ev) {

		switch (ev.getTag()) {
		case PERIODIC_TASK_SCHEDULING:
			send(ev.getEntSrc(), 0, CompSchedTag.SUBMIT_NEW_SUBTASKS,
					getHostList());
			break;
		case SUBMIT_NEW_SUBTASKS:
			send(ev.getEntSrc(), 0, CompSchedTag.SUBMIT_NEW_SUBTASKS,
					getHostList());
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}

	protected void send(int entityId, double delay, CompSchedTag _tags,
			Object _data) {
		Object data = CompSchedEvent.createTagDataPair(_tags, _data);
		send(entityId, delay, CloudSimTags.EXPERIMENT, data);
	}

}
