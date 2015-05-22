package main_starter;

import java.util.Properties;

import monitor.Monitor;
import monitor.MonitorHelper;

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
import coordinator.coordinatorImpl;

public class main_starter {
  private static final long sleep_time = 500; // Sleep for 500ms before ORB.shutdown()

  private static NamingContextExt s_namingcontext = null;
  private static Monitor s_monitor = null;

  /**
   * Can be used from other parts of the program to get the
   * NamingContextExt
   */
  public static final NamingContextExt get_naming_context() {
    return s_namingcontext;
  }

  /**
   * Can be used from other parts of the program to get a
   * reference to the monitor
   */
  public static final Monitor get_monitor() {
    return s_monitor;
  }

  private ORB m_orb = null;
  private POA m_rootpoa = null;
  private coordinatorImpl m_obj = null;
  private NameComponent[] m_path = null;

  private static void print_help_message() {
    StringBuilder str = new StringBuilder();
    str.append("Usage: java -cp . coordinator [Options...]\n");
    str.append("Arguments:\n");
    str.append("--monitor=arg       Set name of monitor\n");
    str.append("--name=arg          Set the coordinator name\n");
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
      final String coordinator, final String monitor) {
    boolean init = true;
    m_orb = ORB.init(args, props);
    try {
      m_rootpoa = POAHelper.narrow(m_orb.resolve_initial_references("RootPOA"));
      m_rootpoa.the_POAManager().activate();
      s_namingcontext = NamingContextExtHelper.narrow(m_orb
          .resolve_initial_references("NameService"));
      m_obj = new coordinatorImpl(coordinator);
      // Register Object for CORBA
      org.omg.CORBA.Object ref = m_rootpoa.servant_to_reference(m_obj);
      Coordinator href = CoordinatorHelper.narrow(ref);
      m_path = s_namingcontext.to_name(coordinator);
      s_namingcontext.rebind(m_path, href);
      // Get reference to monitor
      org.omg.CORBA.Object obj = s_namingcontext.resolve_str(monitor);
      s_monitor = MonitorHelper.narrow(obj);
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
    String monitor_name = "";
    String coordinator_name = "";
    for (int i = 0; i < args.length; ++i) {
      if (args[i].contains("--monitor=")) {
        monitor_name = read_argument(args[i]);
      }
      if (args[i].contains("--name=")) {
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
    if (coordinator_name.isEmpty() || monitor_name.isEmpty()) {
      print_help_message();
      System.exit(-1);
    }
    final main_starter instance = new main_starter();
    if (!instance.initCorba(props, args, coordinator_name, monitor_name)) {
      System.out.println("Error initializing CORBA...");
      System.exit(-2);
    }
    instance.m_obj.run();
  }

  private void shutdown() {
    try {
      s_namingcontext.unbind(m_path);
      Thread.sleep(sleep_time);
    } catch (InterruptedException | NotFound | CannotProceed | InvalidName e) {
      e.printStackTrace();
    }
    m_orb.shutdown(true);
  }
}
