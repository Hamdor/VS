package worker;

public class Calculation implements IJob {
  private String m_sender;
  private int    m_value;

  public Calculation(final String sender, final int value) {
    m_sender = sender;
    m_value  = value;
  }
  
  /**
   * Returns the calculation value to be shared with this message
   * @returns integer with calculation value of neighbor
   */
  public int value() {
    return m_value;
  }

  /**
   * Getter of `m_sender`
   * @returns a String representing the sender of this calculation message
   */
  @Override
  public String sender() {
    return m_sender;
  }
}
