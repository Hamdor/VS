package hawsensor;

import java.net.MalformedURLException;
import java.net.URL;

import sensorproxy.BooleanArray;
import sensorproxy.ObjectFactory;

public class main_starter {
  private static void print_help_message() {
    StringBuilder str = new StringBuilder();
    str.append("Usage: java -cp . sensor [Options...]\n");
    str.append("Arguments:\n");
    str.append("--url=arg     Set own url to listen on\n");
    str.append("--port=arg    Set own port to listen on\n");
    str.append("--other=arg   This sensor will be asked for the coordinator");
    str.append("--north       Try to aquire north\n");
    str.append("--east        Try to aquire east\n");
    str.append("--south       Try to aquire south\n");
    str.append("--west        Try to aquire west");
    str.append("--help        Print this help message\n");
    System.out.println(str);
  }

  private static String read_argument(final String line) {
    String[] splitted = line.split("=");
    return splitted.length == 2 ? splitted[1] : "";
  }

  public static void main(String[] args) {
    String own_url = "";
    String own_port = "";
    String other_url = "";
    boolean displays[] = new boolean[4];
    for (int i = 0; i < args.length; ++i) {
      if (args[i].contains("--url=")) {
        own_url = read_argument(args[i]);
      }
      if (args[i].contains("--port=")) {
        own_port = read_argument(args[i]);
      }
      if (args[i].contains("--other=")) {
        other_url = read_argument(args[i]);
      }
      if (args[i].contains("--north")) {
        displays[0] = true;
      }
      if (args[i].contains("--east")) {
        displays[1] = true;
      }
      if (args[i].contains("--south")) {
        displays[2] = true;
      }
      if (args[i].contains("--west")) {
        displays[3] = true;
      }
      if (args[i].contains("--help")) {
        print_help_message();
        System.exit(0);
      }
    }
    // Check input //other URL can be empty if there is no one there yet
    // in that case the started sensor is the first sensor and therefore the coordinator 
    if (own_url.isEmpty() || own_port.isEmpty()) {
      print_help_message();
      System.exit(-1);
    }
    if (!displays[0] && !displays[1] && !displays[2] && !displays[3]) {
      // No view specified... exit...
      print_help_message();
      System.out.println("No view specified...");
      System.exit(-2);
    }
    try {
      URL url = new URL(own_url + ":" + own_port + "/");
      sensor instance = new sensor(url);
      if (other_url.isEmpty()) {
        // We are the coordinator
        instance.m_is_coordinator = true; // :-)
        instance.register(url, displays);
      } else {
        // Get the other coordinator
        ObjectFactory factory = new ObjectFactory();
        BooleanArray display_array = factory.createBooleanArray();
        int try_ = 0;
        for (; try_ < 5; ++try_) {
          try {
            String coordinator_str =
                instance.resolve_sensor(new URL(other_url)).getCoordinator();
            for (int i = 0; i < displays.length; ++i) {
              display_array.getItem().add(displays[i]);
            }
            if (instance.resolve_sensor(
                new URL(coordinator_str)).register(url.toString(), display_array)) {
              break; // YAY
            } else {
              System.out.println("No free slots... Exit...");
              System.exit(-3);
            }
          } catch (Exception err) {
            // Nop...
            try {
              Thread.sleep(500);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        }
        if (try_ == 5) {
          System.out.println("Request timeout (REGISTER) ... Exit...");
          System.exit(-4);
        }
      }
      instance.initial_view_setup();
      instance.run();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }
}
