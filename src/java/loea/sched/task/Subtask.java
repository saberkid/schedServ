package loea.sched.task;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.UtilizationModelNull;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

public class Subtask extends Cloudlet {

	private static final long DEFAULTCLOUDLETLENGTH = 200;
	private static final int DEFAULTPESNUMBER = 1;
	private static final long DEFAULTFILESIZE = 0;
	private static final UtilizationModel DEFAULTUTILIZATIONMODEL = new UtilizationModelFull();
	private static final boolean DEFAULTRECORD = true;

	private static final String CLOUDLETLENGTH = "cloudletLength";
	private static final String PESNUMBER = "pesNumber";
	private static final String CLOUDLETFILESIZE = "cloudletFileSize";
	private static final String CLOUDLETOUTPUTSIZE = "cloudletOutputSize";
	private static final String UTILIZATIONMODELCPU = "utilizationModelCpu";
	private static final String UTILIZATIONMODELRAM = "utilizationModelRam";
	private static final String UTILIZATIONMODELBW = "utilizationModelBw";
	private static final String RECORD = "record";
	private static final String FILELIST = "fileList";

	private Task parent;
	private int height = 0;
	private static int ID_COUNT = 0;
	private final int ref;
	private long criticalPathToExit = Long.MIN_VALUE;

	public Subtask(long cloudletLength) {
		this(Integer.MIN_VALUE, cloudletLength, DEFAULTPESNUMBER, DEFAULTFILESIZE, DEFAULTFILESIZE,
				DEFAULTUTILIZATIONMODEL, DEFAULTUTILIZATIONMODEL, DEFAULTUTILIZATIONMODEL, DEFAULTRECORD, null);
	}

	public Subtask() {
		this(0);
	}

	Subtask(int _ref, long cloudletLength, int pesNumber, long cloudletFileSize, long cloudletOutputSize,
			UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw, boolean _record, List<String> _fileList) {
		super(ID_COUNT++, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
				utilizationModelRam, utilizationModelBw, _record, _fileList);
		ref = _ref;
	}

	static Subtask XMLImporter(Element subtEle) throws DataConversionException {

		int refe = Integer.MIN_VALUE;
		long leng = DEFAULTCLOUDLETLENGTH;
		int pesN = DEFAULTPESNUMBER;
		long fiSz = DEFAULTFILESIZE;
		long opSz = DEFAULTFILESIZE;
		UtilizationModel uCPU = DEFAULTUTILIZATIONMODEL;
		UtilizationModel uRAM = DEFAULTUTILIZATIONMODEL;
		UtilizationModel uBw = DEFAULTUTILIZATIONMODEL;
		boolean recd = DEFAULTRECORD;
		List<String> fLst = new ArrayList<String>();

		String refeAttr = subtEle.getAttributeValue("ref");
		if (refeAttr != null && !refeAttr.isEmpty()) {
			refe = Integer.parseInt(refeAttr);
		}

		String lengs = subtEle.getChildTextNormalize(CLOUDLETLENGTH);
		if (lengs != null && !lengs.isEmpty()) {
			leng = Long.parseLong(lengs);
		}

		String pesNs = subtEle.getChildTextNormalize(PESNUMBER);
		if (pesNs != null && !pesNs.isEmpty()) {
			pesN = Integer.parseInt(pesNs);
		}

		String fiSzs = subtEle.getChildTextNormalize(CLOUDLETFILESIZE);
		if (fiSzs != null && !fiSzs.isEmpty()) {
			fiSz = Long.parseLong(fiSzs);
		}

		String opSzs = subtEle.getChildTextNormalize(CLOUDLETOUTPUTSIZE);
		if (opSzs != null && !opSzs.isEmpty()) {
			opSz = Long.parseLong(opSzs);
		}

		String uCPUs = subtEle.getChildTextNormalize(UTILIZATIONMODELCPU);
		if (uCPUs != null && !uCPUs.isEmpty()) {
			uCPU = parseUtilizationModel(uCPUs);
		}

		String uRAMs = subtEle.getChildTextNormalize(UTILIZATIONMODELRAM);
		if (uRAMs != null && !uRAMs.isEmpty()) {
			uRAM = parseUtilizationModel(uRAMs);
		}

		String uBws = subtEle.getChildTextNormalize(UTILIZATIONMODELBW);
		if (uBws != null && !uBws.isEmpty()) {
			uBw = parseUtilizationModel(uBws);
		}

		String recds = subtEle.getChildTextNormalize(RECORD);
		if (recds != null && !recds.isEmpty()) {
			recd = Boolean.parseBoolean(recds);
		}

		String fLsts = subtEle.getChildTextNormalize(FILELIST);
		if (fLsts != null && !fLsts.isEmpty()) {

		}

		Subtask subtask = new Subtask(refe, leng, pesN, fiSz, opSz, uCPU, uRAM, uBw, recd, fLst);

		return subtask;
	}

	protected static UtilizationModel parseUtilizationModel(String s) {
		if (s.compareTo("UtilizationModelFull") == 0) {
			return new UtilizationModelFull();
		} else if (s.compareTo("UtilizationModelNull") == 0) {
			return new UtilizationModelNull();
		} else if (s.compareTo("UtilizationModelStochastic") == 0) {
			return new UtilizationModelStochastic();
		} else {
			return new UtilizationModelFull();
		}
	}

	public Task getParent() {
		return parent;
	}

	public void issued() {
		getParent().issued(this);
	}

	public void setParent(Task parent) {
		this.parent = parent;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getRef() {
		return ref;
	}

	public long getCriticalPathToExit() {
		return criticalPathToExit;
	}

	protected void setCriticalPathToExit(long cplen2exit) {
		this.criticalPathToExit = cplen2exit;
	}

	public Subtask reset() {

		return new Subtask(ref, getCloudletLength(), getNumberOfPes(), getCloudletFileSize(), getCloudletOutputSize(),
				getUtilizationModelCpu(), getUtilizationModelRam(), getUtilizationModelBw(), false, getRequiredFiles());

	}

}
