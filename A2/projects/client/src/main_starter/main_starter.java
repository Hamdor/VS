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
      System.exit(-2);
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
    UNKNOWN,
    INVALID_ARGS
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

  final String[] descr = {
   "Show commands", "kill the coordinator {arg: coordinator name}",
   "kill a starter {arg: starter name}",
   "start a new calculation {args: monitor, ggT lower, ggT upper,"
   + " delay lower, delay upper, period, expected ggT", "exit this shell"
  };

  private console_input parse(final String input) {
    console_input result = new console_input(console_code.UNKNOWN);
    String[] splitted;
    for (String s : inputs) {
      if (input.toLowerCase().contains(s)) {
        switch(s) {
        case "help":
          result.m_code = console_code.HELP;
          break;
        case "kill":
          result.m_code = console_code.KILL;
          splitted = input.split(" ");
          if (splitted.length == 2) {
        	  result.m_opt    = new String[1];
        	  result.m_opt[0] = splitted[1];
          } else {
            result.m_code = console_code.INVALID_ARGS;
          }
          break;
        case "kills":
          result.m_code = console_code.KILLS;
          splitted = input.split(" ");
          if (splitted.length == 2) {
        	  result.m_opt    = new String[1];
        	  result.m_opt[0] = splitted[1];
          } else {
            result.m_code = console_code.INVALID_ARGS;
          }
          break;
        case "calculate":
          result.m_code = console_code.CALCULATE;
          splitted = input.split(" ");
          if (splitted.length == 8) {
            result.m_opt = new String[7];
            for (int i = 0; i < result.m_opt.length; ++i) {
              result.m_opt[i] = splitted[i+1];
            }
          } else {
            result.m_code = console_code.INVALID_ARGS;
          }
          break;
        case "exit":
          result.m_code = console_code.EXIT;
          break;
        }
      }
    }
    return result;
  }

  final String shell_header = " ____    __              ___    ___      \n" +
    "/\\  _`\\ /\\ \\            /\\_ \\  /\\_ \\     \n" +
    "\\ \\,\\L\\_\\ \\ \\___      __\\//\\ \\ \\//\\ \\    \n" +
    " \\/_\\__ \\\\ \\  _ `\\  /'__`\\\\ \\ \\  \\ \\ \\   \n" +
    "   /\\ \\L\\ \\ \\ \\ \\ \\/\\  __/ \\_\\ \\_ \\_\\ \\_ \n" +
    "   \\ `\\____\\ \\_\\ \\_\\ \\____\\/\\____\\/\\____\\\n" +
    "    \\/_____/\\/_/\\/_/\\/____/\\/____/\\/____/\n";

  private void run() {
    if (System.console() == null) {
      System.out.println("Please start this program from your terminal");
      return;
    }
    System.out.println(shell_header);
    while(true) {
      System.out.print("€ ");
      console_input input = parse(System.console().readLine());
      if (input.m_code == console_code.HELP) {
        StringBuilder str = new StringBuilder();
        str.append("Commands:\n");
        for (int i = 0; i < descr.length; ++i) {
          str.append(inputs[i] + " -- ");
          str.append(descr[i] + "\n");
        }
        System.out.println(str);
      } else if(input.m_code == console_code.KILL) {
    	  m_coordinator.terminate();
      } else if(input.m_code == console_code.KILLS) {
    	  m_coordinator.kill(input.m_opt[0]); // Kill starter
      } else if(input.m_code == console_code.CALCULATE) {
        // Start new calculation
        m_coordinator.calculate(input.m_opt[0],   // Monitor
             Integer.valueOf(input.m_opt[1]),  // ggT Lower
             Integer.valueOf(input.m_opt[2]),  // ggT Upper
             Integer.valueOf(input.m_opt[3]),  // delay Lower
             Integer.valueOf(input.m_opt[4]),  // delay Upper
             Integer.valueOf(input.m_opt[5]),  // perid
             Integer.valueOf(input.m_opt[6])); // expected ggT
      } else if(input.m_code == console_code.EXIT) {
        // Exit while loop
        break;
      } else if(input.m_code == console_code.INVALID_ARGS) {
        System.out.println("Invalid arguments...");
      } else {
        System.out.println("Unknown command... (type `help` to show commands)");
      }
    }
  }
}
