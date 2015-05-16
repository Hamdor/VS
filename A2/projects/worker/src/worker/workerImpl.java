package worker;

import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;

public class workerImpl extends WorkerPOA {
  private String m_name;
  private Thread m_thread;
  private SynchronousQueue<Job> m_jobs;
  private boolean run = true;
  private Semaphore m_sema;

  public workerImpl(final String name) {
    m_name = name;
    m_jobs = new SynchronousQueue<Job>();
    m_thread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (run) {
          try {
            Job current_job = m_jobs.take();
            // TODO
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    });
    m_sema = new Semaphore(0);
    m_thread.start();
  }

  public void run() {
    try {
      m_sema.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void init(String left, String right, int value, int delay,
      String monitor) {
    // TODO Auto-generated method stub

  }

  @Override
  public void shareResult(String sender, int value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void kill() {
    // TODO: What has to be done here for a clean shutdown?
    m_sema.release();
  }

  @Override
  public void snapshot(String sender) {
    // TODO Auto-generated method stub

  }

}
