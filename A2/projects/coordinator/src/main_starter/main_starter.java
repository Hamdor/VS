package main_starter;

import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import coordinator.coordinatorImpl;

public class main_starter {
  static final long sleep_time = 500; // Sleep for 500ms before ORB.shutdown()

  ORB m_orb = null;
  POA m_rootpoa = null;
  NamingContextExt m_nameingcontext = null;

  private static void print_help_message() {
    StringBuilder str = new StringBuilder();
    str.append("Usage: java -cp . client [Options...]\n");
    str.append("Arguments:\n");
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
      final String coordinator) {
    boolean init = true;
    m_orb = ORB.init(args, props);
    try {
      m_rootpoa = POAHelper.narrow(m_orb.resolve_initial_references("RootPOA"));
      m_rootpoa.the_POAManager().activate();
      m_nameingcontext = NamingContextExtHelper.narrow(m_orb
          .resolve_initial_references("NameService"));
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
    String coordinator_name = "";
    for (int i = 0; i < args.length; ++i) {
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
    if (coordinator_name.isEmpty()) {
      print_help_message();
      System.exit(-1);
    }
    final main_starter instance = new main_starter();
    if (!instance.initCorba(props, args, coordinator_name)) {
      System.out.println("Error initializing CORBA...");
      System.exit(-2);
    }
    coordinatorImpl coord = new coordinatorImpl(coordinator_name);
    coord.run();
  }

  private void shutdown() {
    try {
      Thread.sleep(sleep_time);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    m_orb.shutdown(true);
  }
}
