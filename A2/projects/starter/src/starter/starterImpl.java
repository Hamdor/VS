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
    // TODO Auto-generated method stub

  }

  @Override
  public void kill() {
    // TODO: Kill all workers
    m_sema.release();
  }
}
