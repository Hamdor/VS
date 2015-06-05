package worker;

public class Marker implements IJob {
  private String m_sender;
  private int    m_seq;

  public Marker(final String sender, final int seq) {
    m_sender = sender;
    m_seq    = seq;
  }

  /**
   * Getter of `m_seq`
   * @returns a integer representing the sequence number of this marker
   * message
   */
  public int seq() {
    return m_seq;
  }

  /**
   * Getter of `m_sender`
   * @returns a String representing the sender of this marker message
   */
  @Override
  public String sender() {
    return m_sender;
  }
}
