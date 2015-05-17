package starter;

import java.util.concurrent.Semaphore;

public class starterImpl extends StarterPOA {

  private String m_name;
  private Semaphore m_sema;

  public starterImpl(final String name) {
    m_name = name;
    m_sema = new Semaphore(0);
  }

  public void run() {
    try {
      m_sema.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void startWorker(int number) {
    while(number-- > 0) {
      // TODO: Fork into new jvms with correct arguments
      //       1. Generate Global name for worker
      //          ==> Maybe machine id + starter name + number
      //       2. Fork program
      //       3. done => worker should do the rest...
    }
  }

  @Override
  public void kill() {
    // TODO: Kill all workers
    m_sema.release();
  }
}
