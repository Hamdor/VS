package main_starter;

import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

import coordinator.Coordinator;
import coordinator.CoordinatorHelper;

public class main_starter {
  static final long sleep_time = 500; // Sleep for 500ms before ORB.shutdown()

  ORB m_orb = null;
  POA m_rootpoa = null;
  NamingContextExt m_nameingcontext = null;
  Coordinator m_coordinator = null;

  private static void print_help_message() {
    StringBuilder str = new StringBuilder();
    str.append("Usage: java -cp . client [Options...]\n");
    str.append("Arguments:\n");
    str.append("--coordinator=arg   Set the coordinator name\n");
    str.append("--build-in-props    Use the build in ORB arguments\n");
    str.append("--help              Print this help message\n");
    str.append("ORB Arguments are passed to CORBA Framework");
    System.out.print(str);
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
      m_rootpoa = POAHelper.narrow(
          m_orb.resolve_initial_references("RootPOA"));
      m_rootpoa.the_POAManager().activate();
      m_nameingcontext = NamingContextExtHelper.narrow(
          m_orb.resolve_initial_references("NameService"));
      m_coordinator = CoordinatorHelper.narrow(
          m_nameingcontext.resolve_str(coordinator));
    } catch (InvalidName | AdapterInactive | NotFound | CannotProceed
        | org.omg.CosNaming.NamingContextPackage.InvalidName e) {
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
    if (coordinator_name.isEmpty()) {
      print_help_message();
      System.exit(-1);
    }
    final main_starter instance = new main_starter();
    if (!instance.initCorba(props, args, coordinator_name)) {
      System.out.println("Error initializing CORBA...");
    }
    instance.run();
  }

  private void shutdown() {
    try {
      Thread.sleep(sleep_time);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    m_orb.shutdown(true);
  }

  enum console_code {
    HELP,
    KILL,
    KILLS,
    CALCULATE,
    EXIT,
    UNKNOWN
  };

  class console_input {
    public console_input(final console_code code) {
      m_code = code;
      m_opt  = null;
    }
    public console_code m_code;
    public String[]     m_opt;
  }

  final String[] inputs = {
    "help", "kill", "kills", "calculate", "exit"
  };

  private console_input parse(final String input) {
    console_code code = console_code.UNKNOWN;
    for (String s : inputs) {
      if (input.toLowerCase().contains(s)) {
        switch(s) {
        case "help":
          code = console_code.HELP;
          break;
        case "kill":
          code = console_code.KILL;
          break;
        case "kills":
          code = console_code.KILLS;
          // TODO: Parse arguments
          break;
        case "calculate":
          code = console_code.CALCULATE;
          // TODO: Parse arguments
          break;
        case "exit":
          code = console_code.EXIT;
          break;
        }
      }
    }
    return new console_input(code);
  }

final String shell_header = " ____    __              ___    ___      \n" +
  "/\\  _`\\ /\\ \\            /\\_ \\  /\\_ \\     \n" +
  "\\ \\,\\L\\_\\ \\ \\___      __\\//\\ \\ \\//\\ \\    \n" +
  " \\/_\\__ \\\\ \\  _ `\\  /'__`\\\\ \\ \\  \\ \\ \\   \n" +
  "   /\\ \\L\\ \\ \\ \\ \\ \\/\\  __/ \\_\\ \\_ \\_\\ \\_ \n" +
  "   \\ `\\____\\ \\_\\ \\_\\ \\____\\/\\____\\/\\____\\\n" +
  "    \\/_____/\\/_/\\/_/\\/____/\\/____/\\/____/\n";

  private void run() {
    System.out.println(shell_header);
    while(true) {
      System.out.print("$ ");
      console_input input = parse(System.console().readLine());
      if (input.m_code == console_code.HELP) {
        // Print commands
      } else if(input.m_code == console_code.KILL) {
        // Kill coordinator
      } else if(input.m_code == console_code.KILLS) {
        // Kill starter
      } else if(input.m_code == console_code.CALCULATE) {
        // Start new calculation
      } else if(input.m_code == console_code.EXIT) {
        // Exit while loop
        break;
      } else {
        // Unknown
      }
    }
  }
}
