package worker;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import monitor.Monitor;
import monitor.MonitorHelper;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import coordinator.Coordinator;

public class workerImpl extends WorkerPOA {
  private String m_name;
  private Thread m_thread;
  private LinkedBlockingQueue<IJob> m_jobs;
  private volatile boolean run = true;
  private Semaphore m_sema;

  private int     m_currentValue; // stores the last result calculated by this worker
  private String  m_left_name = "";
  private String  m_right_name = "";
  private boolean m_left = false;
  private boolean m_right = false;
  private int     m_old_seq = -1;
  private boolean m_terminate = false;
  private int     m_delay = 0;
  private Monitor m_monitor = null;
  private Worker  m_leftneighbor = null; // reference to our neighbors
  private Worker  m_rightneighbor = null;

  private Runnable worker_runnable = new Runnable() {
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
      while (run || !m_jobs.isEmpty()) {
        main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
            "workerImpl", "run",
            "while(run) loop (TRACE)");
        IJob cur_job = null;
        try {
          cur_job = m_jobs.take();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (cur_job instanceof Marker) {
          // Marker
          Marker cur_marker = (Marker)cur_job;
          if (m_old_seq < cur_marker.seq()) {
            // new snapshot run, reset internal values...
            m_old_seq = cur_marker.seq();
            m_left = false;
            m_right = false;
            m_terminate = true;
            // Send marker to neigbors
            m_leftneighbor.snapshot(m_name, cur_marker.seq());
            m_rightneighbor.snapshot(m_name, cur_marker.seq());
          }
          if (cur_marker.seq() == m_old_seq) {
            // Got marker with same seq number
            // this means it is a response to our marker
            if (cur_marker.sender().equals(m_left_name)) {
              m_left = true;
            }
            if (cur_marker.sender().equals(m_right_name)) {
              m_right = true;
            }
            if (m_left && m_right) {
              // Got response from both sides...
              Coordinator coord = main_starter.main_starter.get_coordinator();
              coord.inform(m_name, m_old_seq, m_terminate, m_currentValue);
            }
          }
          m_monitor.terminieren(m_name, cur_marker.sender(), m_terminate);
          if (m_terminate) {
            main_starter.main_starter.get_coordinator().inform(m_name, 0, m_terminate, 0);
          }
        } else {
          // Calculation
          Calculation cur_calc = (Calculation)cur_job;
          if (m_currentValue == 0) {
            // Get first value...
            m_currentValue = cur_calc.value();
            m_leftneighbor.shareResult(m_name, m_currentValue);
            m_rightneighbor.shareResult(m_name, m_currentValue);
          }
          if (cur_calc.value() < m_currentValue) {
            // Simulate some calculation time :-)
            try {
              Thread.sleep(m_delay);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            m_currentValue = ((m_currentValue - 1) % cur_calc.value()) + 1;
            m_terminate = false;
            m_leftneighbor.shareResult(m_name, m_currentValue);
            m_rightneighbor.shareResult(m_name, m_currentValue);
          }
          m_monitor.rechnen(m_name, cur_calc.sender(), cur_calc.value());
        }
      }
    }
  };

  public workerImpl(final String name) {
    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "workerImpl",
        "name: " + name + " (TRACE)");
    m_name = name;
    m_jobs = new LinkedBlockingQueue<IJob>();
    m_thread = null;
    m_sema = new Semaphore(0);
  }

  public void run() {
    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "run", "");
    try {
      m_sema.acquire();
      m_thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "run",
        "exit function... (TRACE)");
  }

  @Override
  public void init(String left, String right, int value, int delay,
      String monitor) {
    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "init",
        "left: " + left + " right: " + right + " value: " + value + " delay: " +
        delay + " monitor: " + monitor + " (TRACE)");
    m_left_name  = left;
    m_right_name = right;
    m_delay = delay;
    try {
      m_jobs.put(new Calculation("", value));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // Get reference to monitor
    try {
      org.omg.CORBA.Object obj = main_starter.main_starter.get_naming_context()
          .resolve_str(monitor);
      m_monitor = MonitorHelper.narrow(obj);
    } catch (NotFound | CannotProceed | InvalidName e) {
      e.printStackTrace();
    }
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
      m_jobs.put(new Calculation(sender, value));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    m_monitor.rechnen(m_name, sender, value);
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
  public String getName() {
    return m_name;
  }

  @Override
  public void start() {
    if (m_thread == null) {
      m_thread = new Thread(worker_runnable);
      m_thread.start();
    }
  }

  @Override
  public void snapshot(String sender, int seq) {
    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "snapshot", "sender: " + sender + " (TRACE)");
    try {
      m_jobs.put(new Marker(sender, seq));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    main_starter.io_logger.get_instance().log(main_starter.log_level.INFO,
        "workerImpl", "snapshot", "exit function... (TRACE)");
  }
}
