package coordinator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class coordinatorImpl extends CoordinatorPOA {

  private String m_name;
  private HashMap<String, ArrayList<String>> m_registry;
  private Semaphore m_sema;

  public coordinatorImpl(final String name) {
    m_name = name;
    m_registry = new HashMap<String, ArrayList<String>>();
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
  public void register(String whom, String owner) {
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
  }

  @Override
  public void register_starter(String whom) {
    synchronized (m_registry) {
      if (m_registry.get(whom) == null) {
        m_registry.put(whom, new ArrayList<String>());
      }
    }
  }

  @Override
  public void inform(String whom, int seqNr, boolean finished, int current) {
    // TODO:  I think this has to be send to the monitor
    //        called from worker who got marker message (after worker got all results)
  }

  @Override
  public String[] getStarter() {
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
    // TODO: 1. Start workers
    //       2. Wait for workers
    //       3. Build ring of workers
    //       4. Kick off calculation
  }

  @Override
  public void kill(String whom) {
    // TODO: Kill a starter
    //       1. Get reference to starter
    //       2. Call kill function on starter
    //           ==> Starter will terminate its workers?
  }

  @Override
  public void terminate() {
    // Kill all starters
    for (String s : getStarter()) {
      kill(s);
    }
    m_sema.release();
  }
}
