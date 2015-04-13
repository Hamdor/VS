package verkehrschaosTruck;

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

import verkehrschaos.Truck;
import verkehrschaos.TruckHelper;

public class main {
	public static void main(String[] args) {
		Properties props = new Properties();
		props.put("org.omg.CORBA.ORBInitialPort", "20000");
		props.put("org.omg.CORBA.ORBInitialHost", "localhost");
		// Get input for company name and comapany location
		String truck_name    = "";
		String company_name  = "";
		for (int i = 0; i < args.length; ++i) {
			if (args[i].contains("--name=")) {
				String[] splitted = args[i].split("=");
				truck_name = splitted.length == 2 ? splitted[1] : "";
			}
			if (args[i].contains("--company=")) {
				String[] splitted = args[i].split("=");
				company_name = splitted.length == 2 ? splitted[1] : "";
			}
			if (args[i].contains("--help")) {
				System.out.println("Usage: java -cp . verkehrschaosTruck [OPTIONS...]");
				System.out.println("Arguments:");
				System.out.println("--name=arg      Set the truck name");
				System.out.println("--company=arg   Set the truck company");
				System.out.println("--help          Print this help message");
				System.exit(-1);
			}
		}
		// Check input
		if (truck_name.isEmpty()) {
			System.out.println("No truck name given! Set truck name with `--name=...`");
			System.exit(-2);
		}
		if (company_name.isEmpty()) {
			System.out.println("No company name given! Set company name with `--company=...");
			System.exit(-3);
		}
		// Init ORB
		final ORB orb = ORB.init(args, props);
		try {
			// Get root POA
			final POA rootPoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootPoa.the_POAManager().activate();
			// Init our object
			final TruckImpl truck = new TruckImpl(truck_name);
			// Get reference to our servant
			org.omg.CORBA.Object ref = rootPoa.servant_to_reference(truck);
		    final Truck href = TruckHelper.narrow(ref);
		    truck.setTruck(href);
		    // Get name service 
		    final NamingContextExt nc = NamingContextExtHelper.narrow(
		    		orb.resolve_initial_references("NameService"));
		    // bind our object ref to a name
		    final NameComponent path[] = nc.to_name(truck_name);
		    nc.rebind(path, href);
		    // Register for location and start our duty...
		    // This call is blocking until program has to exit
		    truck.run(nc, company_name);
		    // unbind object from name service
		    nc.unbind(path);
		    // SHutdown ORB
		    orb.shutdown(true);
		} catch (InvalidName e) {
			e.printStackTrace();
		} catch (AdapterInactive e) {
			e.printStackTrace();
		} catch (ServantNotActive e) {
			e.printStackTrace();
		} catch (WrongPolicy e) {
			e.printStackTrace();
		} catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
			e.printStackTrace();
		} catch (NotFound e) {
			e.printStackTrace();
		} catch (CannotProceed e) {
			e.printStackTrace();
		}
	}
}
