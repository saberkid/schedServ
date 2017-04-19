package loea.sched;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import loea.sched.scheduler.SubtaskScheduler;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Vm;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class VMImporter {

	private static final XPathFactory xFactory = XPathFactory.instance();
	private static final SAXBuilder builder = new SAXBuilder();

	private static final XPathExpression<Element> vmExpr = xFactory.compile(
			"/Customer/VM[@ref]", Filters.element());

	private static final String SIZE = "size";
	private static final String RAM = "ram";
	private static final String MIPS = "mips";
	private static final String BW = "bw";
	private static final String PESN = "pesNumber";
	private static final String VMM = "vmm";
	private static final String CLSCHED = "CloudletScheduler";

	public static List<Vm> XMLImporter(String file, int userId) {

		List<Vm> vmList = new ArrayList<Vm>();

		try {

			Document doc = (Document) builder.build(file);
			doc.getRootElement();

			for (Element vmEle : vmExpr.evaluate(doc)) {

				// VM Parameters
				long size = 10000; // image size (MB)
				int ram = 512; // vm memory (MB)
				int mips = 1000;
				long bw = 1000;
				int pesNumber = 1; // number of cpus
				String vmm = "Xen"; // VMM name
				CloudletScheduler sched = new CloudletSchedulerTimeShared();

				String SIZEs = vmEle.getChildTextNormalize(SIZE);
				if (SIZEs != null && !SIZEs.isEmpty()) {
					size = Long.parseLong(SIZEs);
				}
				String RAMs = vmEle.getChildTextNormalize(RAM);
				if (RAMs != null && !RAMs.isEmpty()) {
					ram = Integer.parseInt(RAMs);
				}
				String MIPSs = vmEle.getChildTextNormalize(MIPS);
				if (MIPSs != null && !MIPSs.isEmpty()) {
					mips = Integer.parseInt(MIPSs);
				}
				String BWs = vmEle.getChildTextNormalize(BW);
				if (BWs != null && !BWs.isEmpty()) {
					bw = Long.parseLong(BWs);
				}
				String PESNs = vmEle.getChildTextNormalize(PESN);
				if (PESNs != null && !PESNs.isEmpty()) {
					pesNumber = Integer.parseInt(PESNs);
				}
				String VMMs = vmEle.getChildTextNormalize(VMM);
				if (VMMs != null && !VMMs.isEmpty()) {
					vmm = VMMs;
				}
				String CLSCHEDs = vmEle.getChildTextNormalize(CLSCHED);
				if (CLSCHEDs != null && !CLSCHEDs.isEmpty()) {
					sched = parseCloudletScheduler(CLSCHEDs);
				}

				Vm vm = new Vm(vmList.size(), userId, mips, pesNumber, ram, bw,
						size, vmm, sched);

				vmList.add(vm);
			}

		} catch (IOException io) {
			io.printStackTrace();
		} catch (JDOMException jdomex) {
			jdomex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return vmList;
	}

	private static CloudletScheduler parseCloudletScheduler(String s) {
		if (s.compareTo("CloudletSchedulerSpaceShared") == 0) {
			return new CloudletSchedulerSpaceShared();
		} else if (s.compareTo("CloudletSchedulerTimeShared") == 0) {
			return new CloudletSchedulerTimeShared();
		} else if (s.compareTo("SubtaskScheduler") == 0) {
			return new SubtaskScheduler();
		} else {
			return new SubtaskScheduler();
		}
	}
}
