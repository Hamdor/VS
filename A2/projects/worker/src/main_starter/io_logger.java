package main_starter;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class io_logger {
  private static final boolean ENABLED = false;
  private io_logger() {
    // nop
  }
  private static io_logger s_instance = new io_logger();
  public static final io_logger get_instance() { return s_instance; }

  private OutputStreamWriter m_osw = null;
  private StringBuilder m_output = new StringBuilder();

  public synchronized void log(final log_level level,
                               final String class_name,
                               final String fun_name,
                               final String opt_text) {
    if (!ENABLED) {
      // Logger is disabled...
      return; 
    }
    if (level == log_level.INFO) {
      m_output.append("[INFO] ");
    } else if (level == log_level.WARNING) {
      m_output.append("[WARNING] ");
    } else if (level == log_level.ERROR) {
      m_output.append("[ERROR] ");
    }
    m_output.append(class_name);
    m_output.append("::");
    m_output.append(fun_name);
    if (!opt_text.isEmpty()) {
      m_output.append(" Desc: ");
      m_output.append(opt_text);
    } else {
      m_output.append(" (TRACE)");
    }
    m_output.append("\n");
    if (flush(m_output)) {
      m_output = new StringBuilder();
    } else {
      // File could not be written yet, keep input
      // it will be stored later...
    }
  }

  private boolean flush(final StringBuilder sb) {
    if (m_osw == null) {
      if (main_starter.m_name == "") { return false; }
      try {
        m_osw = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(main_starter.m_name + ".log")));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        return false;
      }
    }
    if (m_osw != null) {
      try {
        m_osw.write(sb.toString());
        m_osw.flush();
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
    }
    return m_osw != null;
  }
}
