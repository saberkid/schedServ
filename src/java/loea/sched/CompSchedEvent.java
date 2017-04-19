package loea.sched;

import org.cloudbus.cloudsim.core.SimEvent;

public class CompSchedEvent {

	private final int etype;
	private final double time;
	private final int entSrc;
	private final int entDst;
	private final CompSchedTag tag;
	private final Object data;

	public int getEtype() {
		return etype;
	}

	public double getTime() {
		return time;
	}

	public int getEntSrc() {
		return entSrc;
	}

	public int getEntDst() {
		return entDst;
	}

	public CompSchedTag getTag() {
		return tag;
	}

	public Object getData() {
		return data;
	}

	public CompSchedEvent(SimEvent ev) {
		etype = ev.getType();
		time = ev.eventTime();
		entSrc = ev.getSource();
		entDst = ev.getDestination();

		tag = ((TagDataPair) ev.getData()).getTag();
		data = ((TagDataPair) ev.getData()).getData();
	}

	public static Object createTagDataPair(CompSchedTag _tags, Object _data) {
		TagDataPair pair = new TagDataPair(_tags, _data);
		return (Object) pair;
	}
}

class TagDataPair {

	CompSchedTag tag;
	Object data;

	public TagDataPair(CompSchedTag _tag, Object _data) {
		tag = _tag;
		data = _data;
	}

	CompSchedTag getTag() {
		return tag;
	}

	void setTag(CompSchedTag tag) {
		this.tag = tag;
	}

	Object getData() {
		return data;
	}

	void setData(Object data) {
		this.data = data;
	}

}
