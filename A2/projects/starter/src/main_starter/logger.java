package main_starter;

public class logger {
  private static logger s_instance = new logger();

  public static logger get_instance() {
    return s_instance;
  }

  public synchronized void log(final log_level level,
                               final String class_name,
                               final String fun_name,
                               final String opt_text) {
    StringBuilder output = new StringBuilder();
    if (level == log_level.INFO) {
      output.append("[INFO] ");
    } else if (level == log_level.WARNING) {
      output.append("[WARNING] ");
    } else if (level == log_level.ERROR) {
      output.append("[ERROR] ");
    }
    output.append(class_name);
    output.append("::");
    output.append(fun_name);
    if (!opt_text.isEmpty()) {
      output.append(" Desc: ");
      output.append(opt_text);
    } else {
      output.append(" (TRACE)");
    }
    System.out.println(output);
  }
}
