package worker;

public class Job {
  public Job(int value, boolean marker) {
    m_value = value;
    m_marker = marker;
  }

  private int m_value = 0;
  private boolean m_marker = false;

  public int value() {
    return m_value;
  }

  public boolean marker() {
    return m_marker;
  }
}
