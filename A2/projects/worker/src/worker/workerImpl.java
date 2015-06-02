package worker;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class workerImpl extends WorkerPOA {
  private String m_name;
  private Thread m_thread;
  private LinkedBlockingQueue<Job> m_jobs;
  private volatile boolean run = true;
  private Semaphore m_sema;

  private int     m_currentValue; // stores the last result calculated by this worker
  private String  m_left_name = "";
  private String  m_right_name = "";
  private boolean m_left = false;
  private boolean m_right = false;
  private String  m_snapshot_sender = "";
  private String  m_monitor_name = "";
  private Worker  m_leftneighbor = null; // reference to our neighbors
  private Worker  m_rightneighbor = null;

  public workerImpl(final String name) {
    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "workerImpl",
        "name: " + name + " (TRACE)");
    m_name = name;
    m_jobs = new LinkedBlockingQueue<Job>();
    m_thread = new Thread(new Runnable() {
      @Override
      public void run() {
        NamingContextExt nc = main_starter.main_starter.get_naming_context();
        org.omg.CORBA.Object obj;
        try {
          obj = nc.resolve_str(m_left_name);
          m_leftneighbor = WorkerHelper.narrow(obj);
          obj = nc.resolve_str(m_right_name);
          m_rightneighbor = WorkerHelper.narrow(obj);
        } catch (NotFound | CannotProceed | InvalidName e1) {
          e1.printStackTrace();
        }
        while (run) {
          if (m_left && m_right) {
            try {
              obj = nc.resolve_str(m_snapshot_sender);
              Worker sender = WorkerHelper.narrow(obj);
              //int seqnum = 0; // TODO: find out where to get that from
              sender.snapshot(m_name);
            } catch (NotFound | CannotProceed | InvalidName e) {
              e.printStackTrace();
            }
          }
          Job current_job = null;
          try {
            current_job = m_jobs.take();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          if (current_job.marker()) {
            if (current_job.sender() == m_left_name) {
              m_left = true;
            } else if (current_job.sender() == m_right_name) {
              m_right = true;
            } else {
              System.out.println("[ERROR]: Unexpected name in marker message: " + current_job.sender());
            }
          } else {
            if (current_job.value() > 0) {
              if (m_currentValue == 0) {
                m_currentValue = current_job.value(); // getting the first value
                                                      // doing it this way means we can only ever use a worker for one run of calculations
                // send the first round of messages to the neighbors
                m_leftneighbor.shareResult(m_name, m_currentValue);
                m_rightneighbor.shareResult(m_name, m_currentValue);
              } else {
                if (current_job.value() < m_currentValue) {
                  m_currentValue = ((m_currentValue - 1) % current_job.value()) + 1;
                  m_leftneighbor.shareResult(m_name, m_currentValue);
                  m_rightneighbor.shareResult(m_name, m_currentValue);
                }
              }
            }
          }
        }
      }
    });
    m_sema = new Semaphore(0);
    m_thread.start();
  }

  public void run() {
    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "run", "");
    try {
      m_sema.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "run",
        "exit function... (TRACE)");
  }

  // TODO: how to handle the delay ?
  @Override
  public void init(String left, String right, int value, int delay,
      String monitor) {
    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "init",
        "left: " + left + " right: " + right + " value: " + value + " delay: " +
        delay + " monitor: " + monitor + " (TRACE)");
    m_left_name  = left;
    m_right_name = right;
    try {
      m_jobs.put(new Job(value, false,monitor));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    m_monitor_name = monitor;
    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "init",
        "exit function... (TRACE)");
  }

  @Override
  public void shareResult(String sender, int value) {
    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "shareResult",
        "sender: " + sender + " value: " + value + " (TRACE)");
    try {
      m_jobs.put(new Job(value, false, sender));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "shareResult",
        "exit function... (TRACE)");
  }

  @Override
  public void kill() {
    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "kill", "");
    run = false;
    m_sema.release();
  }

  @Override
  public void snapshot(String sender) {
    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "snapshot", "sender: " + sender + " (TRACE)");
    // TODO: Identifie if sender is coordinator
    //       if the sender is the coordinator,
    //       the worker has to send the maker
    //       back to the coordinator...
    m_snapshot_sender = sender;
    m_left = false;
    m_right = false;
    try {
      m_jobs.put(new Job(0, true, sender));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "snapshot", "exit function... (TRACE)");
  }

  @Override
  public String getName() {
    return m_name;
  }
}
