package coordinator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Semaphore;

import main_starter.log_level;
import monitor.Monitor;

import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import starter.Starter;
import starter.StarterHelper;

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
    //       2. Wait for workers (how...?)
    //       3. Build ring of workers
    //       4. Call `ring` on monitor
    //       5. Get random start values for calculation
    //       6. Call `berechnen` on worker
    //       7. Kick off calculation
    // ------------------------------
    final int num = 5;            // TODO: 1. Roll number of workers to start
    m_wait = new Semaphore(-num); // 2. Wait for workers
    // TODO: 3. Build ring of workers (We need the new ids...)
    // Call ring on monitor
    String[] s;
    synchronized(m_newworker) {
      s = new String[m_newworker.size()];
      m_newworker.toArray(s);
    }
    main_starter.main_starter.get_monitor().ring(s);
    // TODO: 5. Generate random start values
    // TODO: 6. Call berechnen on monitor
    // TOOD: 7. Kick off calculation
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
