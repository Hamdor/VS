package worker;

public class Job {
  public Job(int value, boolean marker, String sender) {
    m_value = value;
    m_marker = marker;
    m_sender = sender;
  }

  private String m_sender = "";
  private int m_value = 0;
  private boolean m_marker = false;

  public int value() {
    return m_value;
  }

  public boolean marker() {
    return m_marker;
  }
  
  public String sender() {
    return sender;
  }
}
