package worker;

import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;

import coordinator.Coordinator;
import coordinator.CoordinatorHelper;
import starter.NamingContextExt;

public class workerImpl extends WorkerPOA {
  private String m_name;
  private Thread m_thread;
  private SynchronousQueue<Job> m_jobs;
  private volatile boolean run = true;
  private Semaphore m_sema;

  private int m_currentValue; // stores the last result calculated by this worker
  private String  m_left_name = "";
  private String  m_right_name = "";
  private boolean m_left = false;
  private boolean m_right = false;
  private String  m_snapshot_sender = "";
  private String  m_monitor_name = ""; // this is not the monitor but the coordinator 
  private Worker m_leftneighbor = null; // reference to our neighbors
  private Worker m_rightneighbor = null;
  
  /**
   * WICHTIG 
   * der shit funzt bei mir nicht richtig irgendwelche libs fehlen deswegen ist der gesamte code bei mir rot kann also nicht checken ob irgendwas nicht ordentlich ist
   * ausserdem sind mir einige design flaws aufgefallen die ich gerne nochmal ansprechen wollte
   */
  
  
  
  
  
  public workerImpl(final String name) {
    m_name = name;
    m_jobs = new SynchronousQueue<Job>();
    m_thread = new Thread(new Runnable() {
      @Override
      public void run() {
        
        NamingContextExt nc = main_starter.main_starter.get_naming_context(); // can i do this ?
         //       1. Get reference to left and right worker
          // worker relations are unlikely to change during computation 
          // whats the name service called ?
          org.omg.CORBA.Object obj = nc.resolve_str(m_left_name);
          m_leftneighbor = WorkerHelper.narrow(obj);
          
          org.omg.CORBA.Object obj2 = nc.resolve_str(m_right_name);
          m_rightneighbor = WorkerHelper.narrow(obj2);
        
        while (run) {
         
          
          try {
            if (m_left && m_right) {
              // TODO: Send our marker result back to the coordinator
              org.omg.CORBA.Object obj3 = nc.resolve_str(m_monitor_name);
              Coordinator coord = CoordinatorHelper.narrow(obj3);
              int seqnum = 0;//TODO find out where to get that from 
              boolean finished = false; // isnt the coordinateor supposed to find out whether the computation is finished ?
              coord.inform(m_name,seqnum,finished,m_currentValue);
            }
            Job current_job = m_jobs.take();
            if (current_job.marker()) {
             
                if(current_job.sender()==m_monitor_name){// coordinator commands to start a snapshot
                 m_left = false;
              m_right = false;
              m_leftneighbor.snapshot(m_name);
              m_rightneighbor.snapshot(m_name);
              }else if(current_job.sender()==m_left_name){
                m_left = true;
              }else if(current_job.sender()==m_right_name){
                m_right = true;
              }else{
                // something awful happened here 
                // dont know what to do 
                // best ignore it
              }
                  
              
            }else
            if (current_job.value() > 0) {
                if(m_currentValue == 0){
                  m_currentValue = current_job.value(); // getting the first value
                                                        // doing it this way means we can only ever use a worker for one run of calculations
                  // send the first round of messages to the neighbors
                  m_leftneighbor.shareResult(m_name, m_currentValue);
                  m_rightneighbor.shareResult(m_name, m_currentValue);
                
                }else{
              
                  if(current_job.value() < m_currentValue){
                    
                    m_currentValue = ((m_currentValue-1)%current_job.value())+1;
                    m_leftneighbor.shareResult(m_name, m_currentValue);
                    m_rightneighbor.shareResult(m_name, m_currentValue);
                  }
              // TODO:
              // do calculation ggt(current value, current_job.value); 
              //       1. Do calculation...
              //       2. Share result with left and right
                }
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

  
  // how to handle the delay ?
  @Override
  public void init(String left, String right, int value, int delay,
      String monitor) {
    m_left_name  = left;
    m_right_name = right;
    try {
      m_jobs.put(new Job(value, false,monitor));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    m_monitor_name = monitor;
  }

  
  @Override
  public void shareResult(String sender, int value) {
//we dont put it in our work queue this is called by the neightbors
    try {
      m_jobs.put(new Job(value, false,sender));
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
//      m_snapshot_sender = sender; // dont know whether this is needed anymore
//    m_left = false; 
//    m_right = false; 
    
    try {
      m_jobs.put(new Job(0, true, sender));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
  }

}
