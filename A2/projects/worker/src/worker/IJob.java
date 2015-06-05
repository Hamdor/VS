package worker;

public interface IJob {
  /**
   * Returns a string representing the sender of this message
   * @returns a String with senders name
   */
  String sender();
}
