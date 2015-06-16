package hawsensor;

import java.net.MalformedURLException;
import java.net.URL;

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
    boolean aquire_north = false;
    boolean aquire_east = false;
    boolean aquire_south = false;
    boolean aquire_west = false;
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
        aquire_north = true;
      }
      if (args[i].contains("--east")) {
        aquire_east = true;
      }
      if (args[i].contains("--south")) {
        aquire_south = true;
      }
      if (args[i].contains("--west")) {
        aquire_west = true;
      }
      if (args[i].contains("--help")) {
        print_help_message();
        System.exit(0);
      }
    }
    // Check input
    if (own_url.isEmpty() || own_port.isEmpty() || other_url.isEmpty()) {
      print_help_message();
      System.exit(-1);
    }
    if (!aquire_north && !aquire_east && !aquire_south && !aquire_west) {
      // No view specified... exit...
      print_help_message();
      System.out.println("No view specified...");
      System.exit(-2);
    }
    try {
      sensor instance = new sensor(new URL(own_url + ":" + own_port + "/"));
      instance.run();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }
}
