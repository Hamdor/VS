package coordinator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Semaphore;

import main_starter.log_level;
import monitor.Monitor;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
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

  public coordinatorImpl(final String name) {
    m_name = name;
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
  public void inform(String whom, int seqNr, boolean finished, int current) {
    main_starter.logger.get_instance().log(main_starter.log_level.INFO,
                                           "coordinatorImpl", "inform", "");
    // TODO:  I think this has to be send to the monitor
    //        called from worker who got marker message (after worker got all results)
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
    // TODO: 1. Start workers
    //       2. Wait for workers
    //       2. Call `ring` on monitor
    //       4. Build ring of workers
    //       5. Get random start values for calculation
    //       6. Call `berechnen` on worker
    //       7. Kick off calculation
    // ------------------------------
    final int num = 3;            // TODO: 1. Roll number of workers to start * known starters
    // TODO: we have to start the same amount of workers on very starter
    m_wait = new Semaphore(-(num * m_registry.keySet().size())+1); // 2. Wait for workers

    final NamingContextExt nc = main_starter.main_starter.get_naming_context();
    for (String starter_name : m_registry.keySet()) {
      try {
        org.omg.CORBA.Object obj = nc.resolve_str(starter_name);
        Starter starterObj = StarterHelper.narrow(obj);
        starterObj.startWorker(num);
      } catch (NotFound | CannotProceed | InvalidName e) {
        e.printStackTrace();
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
    String[] s;
    synchronized(m_newworker) {
      s = new String[m_newworker.size()];
      m_newworker.toArray(s);
    }
    main_starter.main_starter.get_monitor().ring(s);
    // Get reference to all workers and store them into array
    // later we can build the ring, based on this array
    Worker[] workers = null;
    synchronized(m_newworker) {
      workers = new Worker[m_newworker.size()];
      int idx = 0;
      for (String wid : m_newworker) {
        try {
          org.omg.CORBA.Object obj = nc.resolve_str(wid);
          workers[idx++] = WorkerHelper.narrow(obj);

        } catch (NotFound | CannotProceed | InvalidName e) {
          e.printStackTrace();
        }
      }
      m_newworker.clear();
    }
    // Actually build ring, based on reference array
    // TODO: Better logic to support also 1 or 2 worker rings...
    int[] start_values = new int[workers.length];
    if (workers.length % 3 == 0) {
      main_starter.logger.get_instance().log(main_starter.log_level.INFO,
          "coordinatorImpl", "calculate", "Build ring... (mod 3 OK) (TRACE)");
      int idx_left   = workers.length-1;
      int idx_middle = 0;
      int idx_right  = 1;
      Worker left_obj  = null;
      Worker right_obj = null;
      String monitor_name = main_starter.main_starter.get_monitor_string();
      for (; idx_middle < workers.length; ++idx_middle
          , idx_left  = ++idx_left % workers.length
          , idx_right = ++idx_right % workers.length) {
        left_obj  = workers[idx_left];
        right_obj = workers[idx_right];
        final int random_start_val = expectedggT * randInt(1, 100) * randInt(1, 100);
        start_values[idx_middle] = random_start_val;
        int delay = 400; // TODO: ... What has to be done here?
        workers[idx_middle].init(left_obj.getName(), right_obj.getName(),
            random_start_val, delay, monitor_name);
      }
    } else {
      main_starter.logger.get_instance().log(main_starter.log_level.ERROR,
          "coordinatorImpl", "calculate", "Invalid count of workers (mod 3 NOT OK)");
    }
    // Call `startzahlen` on monitor
    main_starter.main_starter.get_monitor().startzahlen(start_values);
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
