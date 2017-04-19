package loea.sched;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class ProviderImporter {

	private static final XPathFactory xFactory = XPathFactory.instance();
	private static final SAXBuilder builder = new SAXBuilder();

	private static final XPathExpression<Element> dataExpr = xFactory.compile(
			"/Provider/Datacenter[@ref]", Filters.element());

	private static final String ARCH = "arch";
	private static final String OS = "os";
	private static final String VMM = "vmm";
	private static final String TIME_ZONE = "time_zone";
	private static final String COST = "cost";
	private static final String COSTPERMEM = "costPerMem";
	private static final String COSTPERSTORAGE = "costPerStorage";
	private static final String COSTPERBW = "costPerBw";
	private static final String STORAGELIST = "storageList";

	private static final XPathExpression<Element> hostExpr = xFactory.compile(
			"Host[@ref]", Filters.element());

	private static final String PE = "PE";
	private static final String RAMPROVISIONER = "RamProvisioner";
	private static final String BWPROVISIONER = "BwProvisioner";
	private static final String STORAGE = "storage";
	private static final String VMSCHEDULER = "VmScheduler";

	public static List<Datacenter> XMLImporter(String file) {

		List<Datacenter> centerList = new ArrayList<Datacenter>();

		try {

			Document doc = (Document) builder.build(file);
			doc.getRootElement();

			for (Element dataEle : dataExpr.evaluate(doc)) {

				List<Host> hostList = new ArrayList<Host>();

				for (Element hostEle : hostExpr.evaluate(dataEle)) {

					List<Pe> peList = new ArrayList<Pe>();
					peList.add(new Pe(0, new PeProvisionerSimple(1000)));
					peList.add(new Pe(1, new PeProvisionerSimple(1000)));
					RamProvisioner ramp = new RamProvisionerSimple(2048);
					BwProvisioner bwp = new BwProvisionerSimple(10000);
					long storage = 1000000;
					VmScheduler vmsc = new VmSchedulerTimeShared(peList);

					// Create PE list
					String PEs = hostEle.getChildTextNormalize(PE);
					if (PEs != null && !PEs.isEmpty()) {
						String[] pesplit = PEs.split(",");
						peList.clear();
						for (String pe : pesplit) {
							int mips = Integer.parseInt(pe);
							peList.add(new Pe(peList.size(),
									new PeProvisionerSimple(mips)));
						}
					}

					String RAMPROVISIONERs = hostEle
							.getChildTextNormalize(RAMPROVISIONER);
					if (RAMPROVISIONERs != null && !RAMPROVISIONERs.isEmpty()) {
						int rampi = Integer.parseInt(RAMPROVISIONERs);
						ramp = new RamProvisionerSimple(rampi);
					}

					String BWPROVISIONERs = hostEle
							.getChildTextNormalize(BWPROVISIONER);
					if (BWPROVISIONERs != null && !BWPROVISIONERs.isEmpty()) {
						int bwpi = Integer.parseInt(BWPROVISIONERs);
						bwp = new BwProvisionerSimple(bwpi);
					}

					String STORAGEs = hostEle.getChildTextNormalize(STORAGE);
					if (STORAGEs != null && !STORAGEs.isEmpty()) {
						storage = Long.parseLong(STORAGEs);
					}

					String VMSCHEDULERs = hostEle
							.getChildTextNormalize(VMSCHEDULER);
					if (VMSCHEDULERs != null && !VMSCHEDULERs.isEmpty()) {
						vmsc = parseVmScheduler(VMSCHEDULERs, peList);
					}

					hostList.add(new Host(hostList.size(), ramp, bwp, storage,
							peList, vmsc));
				}

				// system architecture
				String arch = "x86";
				// operating system
				String os = "Linux";
				String vmm = "Xen";
				// time zone this resource located
				double time_zone = 10.0;
				// the cost of using processing in this resource
				double cost = 3.0;
				// the cost of using memory in this resource
				double costPerMem = 0.05;
				// the cost of using storage in this resource
				double costPerStorage = 0.1;
				// the cost of using bw in this resource
				double costPerBw = 0.1;
				// we are not adding SAN devices by now
				List<Storage> storageList = new ArrayList<Storage>();

				String ARCHs = dataEle.getChildTextNormalize(ARCH);
				if (ARCHs != null && !ARCHs.isEmpty()) {
					arch = ARCHs;
				}
				String OSs = dataEle.getChildTextNormalize(OS);
				if (OSs != null && !OSs.isEmpty()) {
					os = OSs;
				}
				String VMMs = dataEle.getChildTextNormalize(VMM);
				if (VMMs != null && !VMMs.isEmpty()) {
					vmm = VMMs;
				}
				String TIME_ZONEs = dataEle.getChildTextNormalize(TIME_ZONE);
				if (TIME_ZONEs != null && !TIME_ZONEs.isEmpty()) {
					time_zone = Double.parseDouble(TIME_ZONEs);
				}
				String COSTs = dataEle.getChildTextNormalize(COST);
				if (COSTs != null && !COSTs.isEmpty()) {
					cost = Double.parseDouble(COSTs);
				}
				String COSTPERMEMs = dataEle.getChildTextNormalize(COSTPERMEM);
				if (COSTPERMEMs != null && !COSTPERMEMs.isEmpty()) {
					costPerMem = Double.parseDouble(COSTPERMEMs);
				}
				String COSTPERSTORAGEs = dataEle
						.getChildTextNormalize(COSTPERSTORAGE);
				if (COSTPERSTORAGEs != null && !COSTPERSTORAGEs.isEmpty()) {
					costPerStorage = Double.parseDouble(COSTPERSTORAGEs);
				}
				String COSTPERBWs = dataEle.getChildTextNormalize(COSTPERBW);
				if (COSTPERBWs != null && !COSTPERBWs.isEmpty()) {
					costPerBw = Double.parseDouble(COSTPERBWs);
				}
				String STORAGELISTs = dataEle
						.getChildTextNormalize(STORAGELIST);
				if (STORAGELISTs != null && !STORAGELISTs.isEmpty()) {

				}

				DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
						arch, os, vmm, hostList, time_zone, cost, costPerMem,
						costPerStorage, costPerBw);

				InteractiveDatacenter datacenter = new InteractiveDatacenter("Datacenter_"
						+ centerList.size(), characteristics,
						new VmAllocationPolicySimple(hostList), storageList, 0);

				centerList.add(datacenter);

			}

		} catch (IOException io) {
			io.printStackTrace();
		} catch (JDOMException jdomex) {
			jdomex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return centerList;

	}

	protected static VmScheduler parseVmScheduler(String s, List<Pe> _peList) {
		if (s.compareTo("VmSchedulerSpaceShared") == 0) {
			return new VmSchedulerSpaceShared(_peList);
		} else if (s.compareTo("VmSchedulerTimeShared") == 0) {
			return new VmSchedulerTimeShared(_peList);
		} else if (s.compareTo("VmSchedulerTimeSharedOverSubscription") == 0) {
			return new VmSchedulerTimeSharedOverSubscription(_peList);
		} else {
			return new VmSchedulerTimeShared(_peList);
		}
	}
}
