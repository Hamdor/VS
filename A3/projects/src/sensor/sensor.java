package sensor;

import hawmeterproxy.HAWMeteringWebservice;

import java.net.URL;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.Semaphore;

public class Sensor extends Thread implements Sensor_I {

  private URL m_coordinator;
  private boolean isCoord;
  private URL m_URL;
  private Sensor_I[] m_others;
  private HAWMeteringWebservice m_view;
  private Timer m_timer;
  private Semaphore m_sema;
  private long m_power; // contains the power of this Sensor used in the voting
                        // algorithm
  private double m_currentValue; // last Value Computed by the Sensor
  private Queue<Job> m_todos;

  public Sensor(URL sensorUrl) {

  }

  public void run() {
    // TODO Auto-generated method stub
    // STARTUP PHASE

    // RUNNING PHASE
    while (true) {// Something

    }
    // SHUTDOWN PHASE (if needet)

  }

  public URL getCoordinator() {
    return m_coordinator;
  }

  public void signalUpdate() {

    // TODO send Update to the metering webservide
    m_todos.add(new Job("update"));
  }

  public void sendDataUpdate() { // ??
    m_view.setValue(m_currentValue);
  }

  /*
   * if i am coordinator i check if i can include this one into the list of
   * active sensorswhat is String[] displays for ?
   */
  public boolean register(Sensor_I myself, String[] displays,
      boolean[] canSendTo) {

    if (this.isCoord) {// i am coordinator
      for (int i = 0; i < m_others.length; i++) {
        if (m_others[i] == null) {// an empty slot
          if (canSendTo[i]) { // this sensor can deliver data to this slot
            m_others[i] = myself; // set up list with the new sensor
            for (int j = 0; j < m_others.length; j++) {
              m_others[j].refreshOthersList(m_others);
            }
            // TODO register and set up connection to sensor
            return true;
          }
        }
      }
    }

    return false;
  }

  public void removePermission(String display) {
    // TODO Auto-generated method stub

  }

  public boolean vote(URL url) {
    //TODO maybe change job system?
    if (m_URL.toString().compareTo(url.toString()) > 0) {
      m_todos.add(new Job("vote"));
      return true;
    } else {
      return false;
    }

  }

  public void refreshOthersList(Sensor_I[] newList) {
    m_others = newList;
  }

  

}
