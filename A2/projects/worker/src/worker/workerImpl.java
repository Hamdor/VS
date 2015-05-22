package worker;

import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;

public class workerImpl extends WorkerPOA {
  private String m_name;
  private Thread m_thread;
  private SynchronousQueue<Job> m_jobs;
  private volatile boolean run = true;
  private Semaphore m_sema;

  private String  m_left_name = "";
  private String  m_right_name = "";
  private boolean m_left = false;
  private boolean m_right = false;
  private String  m_snapshot_sender = "";
  private String  m_monitor_name = "";

  public workerImpl(final String name) {
    m_name = name;
    m_jobs = new SynchronousQueue<Job>();
    m_thread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (run) {
          try {
            if (m_left && m_right) {
              // TODO: Send our marker result back to m_snapshot_sender
            }
            Job current_job = m_jobs.take();
            if (current_job.marker()) {
              m_left = false;
              m_right = false;
              // TODO:
              //       1. Get reference to left and right worker
              //       2. Call snapshot on them
              //       3. Set boolean values for left and right
            }
            if (current_job.value() > 0) {
              // TODO:
              //       1. Do calculation...
              //       2. Share result with left and right
            }
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
    m_left_name  = left;
    m_right_name = right;
    try {
      m_jobs.put(new Job(value, false));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    m_monitor_name = monitor;
  }

  @Override
  public void shareResult(String sender, int value) {
    // TODO: Checkme! Is it correct to put this number into our work queue?
    try {
      m_jobs.put(new Job(value, false));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void kill() {
    // TODO: What has to be done here for a clean shutdown?
    run = false;
    m_sema.release();
  }

  @Override
  public void snapshot(String sender) {
    m_snapshot_sender = sender;
    m_left = false;
    m_right = false;
  }

}
