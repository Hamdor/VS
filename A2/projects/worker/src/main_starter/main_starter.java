package main_starter;

import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import coordinator.Coordinator;
import coordinator.CoordinatorHelper;
import worker.Worker;
import worker.WorkerHelper;
import worker.workerImpl;

public class main_starter {
  private static final long sleep_time = 500; // Sleep for 500ms before ORB.shutdown()

  private static NamingContextExt s_nameingcontext = null;

  /**
   * Can be used from other parts of the program to get the
   * NamingContextExt
   */
  public static final NamingContextExt get_naming_context() {
    return s_nameingcontext;
  }

  private static Coordinator s_coordinator = null;

  /**
   * Can be used from other parts of the program to get the
   * Coordinator reference.
   */
  public static final Coordinator get_coordinator() {
    return s_coordinator;
  }

  private ORB m_orb = null;
  private POA m_rootpoa = null;
  private workerImpl m_obj = null;
  private NameComponent[] m_path = null;

  public static String m_name = "";

  private static void print_help_message() {
    StringBuilder str = new StringBuilder();
    str.append("Usage: java -cp . starter [Options...]\n");
    str.append("Arguments:\n");
    str.append("--name=arg          Set the worker name\n");
    str.append("--starter=arg       Set the starter name\n");
    str.append("--coordinator=arg   Set the coordinator name\n");
    str.append("--build-in-props    Use the build in ORB arguments\n");
    str.append("--help              Print this help message\n");
    str.append("ORB Arguments are passed to CORBA Framework");
    System.out.println(str);
  }

  private static String read_argument(final String line) {
    String[] splitted = line.split("=");
    return splitted.length == 2 ? splitted[1] : "";
  }

  private boolean initCorba(final Properties props, final String[] args,
      final String coordinator_name, final String starter_name,
      final String worker_name) {
    boolean init = true;
    m_orb = ORB.init(args, props);
    try {
      m_rootpoa = POAHelper.narrow(m_orb.resolve_initial_references("RootPOA"));
      m_rootpoa.the_POAManager().activate();
      s_nameingcontext = NamingContextExtHelper.narrow(m_orb
          .resolve_initial_references("NameService"));
      // Get reference to coordinator
      org.omg.CORBA.Object obj = s_nameingcontext.resolve_str(coordinator_name);
      s_coordinator = CoordinatorHelper.narrow(obj);
      // Create worker object
      m_obj = new workerImpl(worker_name);
      // Register Object for CORBA
      org.omg.CORBA.Object ref = m_rootpoa.servant_to_reference(m_obj);
      Worker href = WorkerHelper.narrow(ref);
      m_path = s_nameingcontext.to_name(worker_name);
      s_nameingcontext.rebind(m_path, href);
      // Register at coordinator
      s_coordinator.register(worker_name, starter_name);
      m_name = worker_name;
    } catch (Exception e) {
      e.printStackTrace();
      init = false;
    } finally {
      if (init) {
        // Add shutdown hook if CORBA was initialized successfully
        Runtime.getRuntime().addShutdownHook(new Thread() {
          public void run() {
            shutdown();
          }
        });
      }
    }
    return init;
  }

  public static void main(String[] args) {
    Properties props = null;
    String worker_name = "";
    String starter_name = "";
    String coordinator_name = "";
    for (int i = 0; i < args.length; ++i) {
      if (args[i].contains("--name=")) {
        worker_name = read_argument(args[i]);
      }
      if (args[i].contains("--starter=")) {
        starter_name = read_argument(args[i]);
      }
      if (args[i].contains("--coordinator=")) {
        coordinator_name = read_argument(args[i]);
      }
      if (args[i].contains("--build-in-props")) {
        props = new Properties();
        props.put("org.omg.CORBA.ORBInitialPort", "20000");
        props.put("org.omg.CORBA.ORBInitialHost", "localhost");
      }
      if (args[i].contains("--help")) {
        print_help_message();
        System.exit(0);
      }
    }
    // Check input
    if (starter_name.isEmpty() || coordinator_name.isEmpty()
        || worker_name.isEmpty()) {
      print_help_message();
      System.exit(-1);
    }
    final main_starter instance = new main_starter();
    if (!instance.initCorba(props, args, coordinator_name, starter_name, worker_name)) {
      System.out.println("Error initializing CORBA...");
      System.exit(-2);
    }
    instance.m_obj.run();
  }

  private void shutdown() {
    try {
      s_nameingcontext.unbind(m_path);
      Thread.sleep(sleep_time);
    } catch (InterruptedException | NotFound | CannotProceed | InvalidName e) {
      e.printStackTrace();
    }
    m_orb.shutdown(true);
  }
}
