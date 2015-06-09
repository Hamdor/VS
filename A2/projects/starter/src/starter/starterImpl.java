package starter;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.Semaphore;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import worker.Worker;
import worker.WorkerHelper;

public class starterImpl extends StarterPOA {

  private String m_name;
  private Semaphore m_sema;

  private ArrayList<String> m_worker_names;

  private static final String fullcp = "/home/students/abl424/gitrepos/VS/A2/projects/worker/bin/";
  private static final String cmdworker = "java -cp " + fullcp + ":. main_starter.main_starter";

  public starterImpl(final String name) {
    m_name = name;
    m_sema = new Semaphore(0);
    m_worker_names = new ArrayList<String>();
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
    main_starter.logger.get_instance().log(main_starter.log_level.INFO,
                                           "starterImpl", "startWorker",
                                           "number: " + number + " (TRACE)");
    Runtime r = Runtime.getRuntime();
    // Start workers...
    while(number-- > 0) {
      try {
        String unique_name = m_name + "-" + number;
        String arguments = "--name=" + unique_name
            + " --starter=" + m_name
            + " --coordinator=" + main_starter.main_starter.get_coordinator_name()
            + " -ORBInitialPort " + main_starter.main_starter.get_ORBInitialPort()
            + " -ORBInitialHost " + main_starter.main_starter.get_ORBInitialHost();
        r.exec(cmdworker + " " + arguments);
        main_starter.logger.get_instance().log(main_starter.log_level.INFO,
                                               "starterImpl", "startWorker",
                                               "started: " + number + " (TRACE)");
        m_worker_names.add(unique_name);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void kill() {
    main_starter.logger.get_instance().log(main_starter.log_level.INFO,
                                           "coordinatorImpl", "kill", "");
    // Kill all our workers...
    NamingContextExt nc = main_starter.main_starter.get_naming_context();
    org.omg.CORBA.Object obj = null;
    Worker worker = null;
    for (String s : m_worker_names) {
      try {
        obj = nc.resolve_str(s);
      } catch (NotFound | CannotProceed | InvalidName e) {
        // Dont print exception
        // If worker already has terminated its no longer valid...
        continue;
      }
      worker = WorkerHelper.narrow(obj);
      worker.kill(); // kill worker
    }
    m_sema.release();
  }
}
