package coordinator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import starter.Starter;
import starter.StarterHelper;
import worker.Worker;
import worker.WorkerHelper;

public class coordinatorImpl extends CoordinatorPOA {

  private String m_name;
  private HashMap<String, ArrayList<String>> m_registry;
  private Semaphore m_sema;
  private Semaphore m_wait = null;
  private ArrayList<String> m_newworker;
  private Worker m_running_workers[];

  private int m_seq_counter = 0;

  private Timer m_timer = null;

  public coordinatorImpl(final String name) {
    m_name = name;
    //               Key: StarterName, Value: Worker List
    m_registry = new HashMap<String, ArrayList<String>>();
    m_sema = new Semaphore(0);
    m_newworker = new ArrayList<String>();
  }

  public void run() {
    try {
      m_sema.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void register(String whom, String owner) {
    main_starter.logger.get_instance().log(main_starter.log_level.INFO,
                                           "coordinatorImpl", "register", "");
    synchronized (m_registry) {
      ArrayList<String> workerList = m_registry.get(owner);
      if (workerList == null) {
        // if this happens the starter didn't register itself to
        // the coordinator. This should never happen...
        System.out
            .println("[WARNING]: Register Worker, but Starter was not known before...");
        workerList = new ArrayList<String>();
        m_registry.put(owner, workerList);
      }
      workerList.add(whom);
    }
    synchronized(m_newworker) {
      m_newworker.add(whom);
    }
    m_wait.release(); // add +1 to our fence...
  }

  @Override
  public void register_starter(String whom) {
    main_starter.logger.get_instance().log(main_starter.log_level.INFO,
                                           "coordinatorImpl", "register_starter", "");
    synchronized (m_registry) {
      if (m_registry.get(whom) == null) {
        m_registry.put(whom, new ArrayList<String>());
      }
    }
  }
  
  @Override
  public synchronized void inform(String whom, int seqNr, boolean finished,
                                  int current) {
    main_starter.logger.get_instance().log(main_starter.log_level.INFO,
                                           "coordinatorImpl", "inform", "");
    if (!finished || seqNr != m_seq_counter) {
      return;
    }
    // if we got this message, this means the worker has finished...
    for (int i = 0; i < m_running_workers.length; ++i) {
      if (m_running_workers[i] != null
          && m_running_workers[i].getName().equals(whom)) {
        m_running_workers[i] = null;
      }
    }
    int count = m_running_workers.length;
    for (Worker w : m_running_workers) {
      if (w == null) {
        count--;
      }
    }
    if (count == 0) {
      NamingContextExt nc = main_starter.main_starter.get_naming_context();
      m_timer.cancel();
      m_timer.purge();
      // Kill all workers
      Iterator<ArrayList<String>> worker_names = null;
      synchronized(m_registry) {
        worker_names = m_registry.values().iterator();
        while(worker_names.hasNext()) {
          ArrayList<String> list = worker_names.next();
          for (String worker : list) {
            try {
              org.omg.CORBA.Object obj = nc.resolve_str(worker);
              Worker wobj = WorkerHelper.narrow(obj);
              wobj.kill();
            } catch (NotFound | CannotProceed | InvalidName e) {
              //e.printStackTrace();
            }
          }
          list.clear();
        }
      }
    }
  }

  @Override
  public String[] getStarter() {
    main_starter.logger.get_instance().log(main_starter.log_level.INFO,
                                           "coordinatorImpl", "getStarter", "");
    synchronized (m_registry) {
      Set<String> keys = m_registry.keySet();
      String[] result = new String[keys.size()];
      int idx = 0;
      for (String s : keys) {
        result[idx++] = s;
      }
      return result;
    }
  }

  @Override
  public void calculate(String monitor, int ggTLower, int ggTUpper,
      int delayLower, int delayUpper, int period, int expectedggT) {
    main_starter.logger.get_instance().log(main_starter.log_level.INFO,
        "coordinatorImpl", "calculate", "");
    final NamingContextExt nc = main_starter.main_starter.get_naming_context();
    synchronized(m_registry) {
      final int num = randInt(ggTLower, ggTUpper);
      m_wait = new Semaphore(-(num * m_registry.keySet().size())+1);
      for (String starter_name : m_registry.keySet()) {
        try {
          org.omg.CORBA.Object obj = nc.resolve_str(starter_name);
          Starter starterObj = StarterHelper.narrow(obj);
          starterObj.startWorker(num);
        } catch (NotFound | CannotProceed | InvalidName e) {
          e.printStackTrace();
        }
      }
    }
    // Block until all
    main_starter.logger.get_instance().log(main_starter.log_level.INFO,
        "coordinatorImpl", "calculate", "Before m_wait.aquire() (TRACE)");
    try {
      m_wait.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    main_starter.logger.get_instance().log(main_starter.log_level.INFO,
        "coordinatorImpl", "calculate", "After m_wait.aquire() (TRACE)"); 
    // Call ring on monitor
    String[] s = null;
    synchronized(m_newworker) {
      s = new String[m_newworker.size()];
      m_newworker.toArray(s);
    }
    main_starter.main_starter.get_monitor().ring(s);
    // Get reference to all workers and store them into array
    // later we can build the ring, based on this array
    m_running_workers = null;
    synchronized(m_newworker) {
      m_running_workers = new Worker[m_newworker.size()];
      int idx = 0;
      for (String wid : m_newworker) {
        try {
          org.omg.CORBA.Object obj = nc.resolve_str(wid);
          m_running_workers[idx++] = WorkerHelper.narrow(obj);
        } catch (NotFound | CannotProceed | InvalidName e) {
          e.printStackTrace();
        }
      }
      m_newworker.clear();
    }
    
    final int[] randomValues = new int[m_running_workers.length];
    for (int i = 0; i < randomValues.length; ++i) {
      randomValues[i] = expectedggT * randInt(1, 100) * randInt(1, 100);
    }
    Arrays.sort(randomValues);
    
    // Actually build ring, based on reference array
    int[] start_values = new int[m_running_workers.length];
    if (m_running_workers.length >= 3) {
      main_starter.logger.get_instance().log(main_starter.log_level.INFO,
          "coordinatorImpl", "calculate", "Build ring... (mod 3 OK) (TRACE)");
      int idx_left   = m_running_workers.length-1;
      int idx_middle = 0;
      int idx_right  = 1;
      Worker left_obj  = null;
      Worker right_obj = null;
      String monitor_name = main_starter.main_starter.get_monitor_string();
      for (; idx_middle < m_running_workers.length; ++idx_middle
          , idx_left  = ++idx_left % m_running_workers.length
          , idx_right = ++idx_right % m_running_workers.length) {
        left_obj  = m_running_workers[idx_left];
        right_obj = m_running_workers[idx_right];
        start_values[idx_middle] = randomValues[idx_middle];
        int delay = randInt(delayLower, delayUpper);
        m_running_workers[idx_middle].init(left_obj.getName(), right_obj.getName(),
            randomValues[idx_middle], delay, monitor_name, (idx_middle < 3 ? true : false));
      }
    } else {
      if (m_running_workers.length == 2) {
        // Just start 2 workers...
        main_starter.logger.get_instance().log(main_starter.log_level.WARNING,
            "coordinatorImpl", "calculate", "Build ring... (ONLY 2 WORKER)");
        int random_start_val = expectedggT * randInt(1, 100) * randInt(1, 100);
        start_values[0] = random_start_val;
        int delay = randInt(delayLower, delayUpper);
        m_running_workers[0].init(m_running_workers[0].getName(),
            m_running_workers[1].getName(),
            random_start_val, delay, main_starter.main_starter.get_monitor_string(), true);
        random_start_val = expectedggT * randInt(1, 100) * randInt(1, 100);
        start_values[1] = random_start_val;
        delay = randInt(delayLower, delayUpper);
        m_running_workers[1].init(m_running_workers[1].getName(), m_running_workers[0].getName(),
            random_start_val, delay, main_starter.main_starter.get_monitor_string(), true);
      } else {
        // only 1 worker...
        main_starter.logger.get_instance().log(main_starter.log_level.ERROR,
            "coordinatorImpl", "calculate", "Build ring... (ONLY 1 WORKER) ==> return");
        return;
      }
    }
    // Call `startzahlen` on monitor
    main_starter.main_starter.get_monitor().startzahlen(start_values);
    // Start underlying threads at worker process
    for (Worker w : m_running_workers) {
      w.start();
    }
    // Start snapshot timer...
    m_timer = new Timer();
    m_timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        synchronized (m_registry) {
          if (m_registry.isEmpty()) {
            return;
          }
          String kickoff_at = m_registry.get(
              m_registry.keySet().iterator().next()).get(0);
          try {
            org.omg.CORBA.Object obj = main_starter.main_starter
                .get_naming_context().resolve_str(kickoff_at);
            Worker w = WorkerHelper.narrow(obj);
            m_seq_counter++;
            w.snapshot(m_name, m_seq_counter);
          } catch (NotFound | CannotProceed | InvalidName e) {
            e.printStackTrace();
          }
        }
      }
    }, period, period);
  }

  /**
   * Returns a pseudo-random number between min and max, inclusive.
   * The difference between min and max can be at most
   * <code>Integer.MAX_VALUE - 1</code>.
   *
   * @param min Minimum value
   * @param max Maximum value.  Must be greater than min.
   * @return Integer between min and max, inclusive.
   * @see java.util.Random#nextInt(int)
   * @see http://stackoverflow.com/questions/363681/generating-random-integers-in-a-range-with-java
   */
  public static int randInt(int min, int max) {

      // NOTE: Usually this should be a field rather than a method
      // variable so that it is not re-seeded every call.
      Random rand = new Random();

      // nextInt is normally exclusive of the top value,
      // so add 1 to make it inclusive
      int randomNum = rand.nextInt((max - min) + 1) + min;

      return randomNum;
  }

  @Override
  public void kill(String whom) {
    main_starter.logger.get_instance().log(main_starter.log_level.INFO,
        "coordinatorImpl", "kill", "");
    try {
      org.omg.CORBA.Object ref = main_starter.main_starter.get_naming_context().resolve_str(whom);
      Starter sel = StarterHelper.narrow(ref);
      sel.kill();
    } catch (NotFound | CannotProceed | InvalidName e) {
      e.printStackTrace();
    }
    synchronized(m_registry) {
      m_registry.remove(whom);
    }
  }

  @Override
  public void terminate() {
    main_starter.logger.get_instance().log(main_starter.log_level.INFO,
        "coordinatorImpl", "terminate", "");
    // Kill all starters
    for (String s : getStarter()) {
      kill(s);
    }
    m_sema.release();
  }
}
