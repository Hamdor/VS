package verkehrschaosTruckCompany;

import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import verkehrschaos.TruckCompany;
import verkehrschaos.TruckCompanyHelper;

public class companyStarter {
	public static void main(String[] args) {
		Properties props = new Properties();
		props.put("org.omg.CORBA.ORBInitialPort", "20000");
		props.put("org.omg.CORBA.ORBInitialHost", "localhost");
		// Get input for company name and comapany location
		String company_name = "";
		String company_pos  = "";
		for (int i = 0; i < args.length; ++i) {
			if (args[i].contains("--name=")) {
				String[] splitted = args[i].split("=");
				company_name = splitted.length == 2 ? splitted[1] : "";
			}
			if (args[i].contains("--location=")) {
				String[] splitted = args[i].split("=");
				company_pos = splitted.length == 2 ? splitted[1] : "";
			}
			if (args[i].contains("--help")) {
				System.out.println("Usage: java -cp . verkehrschaosTruckCompany [OPTIONS...]");
				System.out.println("Arguments:");
				System.out.println("--name=arg      Set the company name");
				System.out.println("--location=arg  Set the location of the company");
				System.out.println("                Valid locations: nord, ost, sued, west");
				System.out.println("--help          Print this help message");
				System.out.println("ORB Arguments are passed to CORBA Framework");
				System.exit(-1);
			}
		}
		// Check input
		if (company_name.isEmpty()) {
			System.out.println("No company name given! Set copmany name with `--name=...`");
			System.exit(-2);
		}
		System.out.println(company_pos);
		if (!company_pos.equals("nord") && !company_pos.equals("sued")
				&& !company_pos.equals("west") && !company_pos.equals("ost")) {
			System.out.println("No valid location specified! Set location with `--location=...`");
			System.out.println("Valid locations are: nord, sued, west, ost");
			System.exit(-3);
		}
		// Init ORB
		final ORB orb = ORB.init(args, props);
		try {
			// Get root POA
			final POA rootPoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootPoa.the_POAManager().activate();
			// Init our object
			final TruckCompanyImpl company = new TruckCompanyImpl(company_name);
			// Get reference to our servant
			org.omg.CORBA.Object ref = rootPoa.servant_to_reference(company);
		    final TruckCompany href = TruckCompanyHelper.narrow(ref);
		    company.setObj(href);
		    // Get name service 
		    final NamingContextExt nc = NamingContextExtHelper.narrow(
		    		orb.resolve_initial_references("NameService"));
		    // bind our object ref to a name
		    final NameComponent path[] = nc.to_name(company_name);
		    nc.rebind(path, href);
		    // Register for location and start our duty...
		    // This call is blocking until program has to exit
		    company.run(nc, company_pos);
		    // unbind object from name service
		    nc.unbind(path);
		} catch (InvalidName | AdapterInactive | ServantNotActive |
				WrongPolicy | NotFound | CannotProceed |
				org.omg.CosNaming.NamingContextPackage.InvalidName e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} finally {
		    // Shutdown ORB
			orb.shutdown(true);
		}
	}
}
