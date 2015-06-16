package hawsensor;

import java.net.URL;
import java.util.Timer;
import java.util.concurrent.Semaphore;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import hawmeterproxy.HAWMeteringWebservice;

@WebService
@SOAPBinding(style = Style.RPC)
public class sensor {
  private URL                     m_url;
  // TODO: We should store this as URL first
  //       when we have a working version we can change this
  //       to arrays over sensorProxys.
  private URL[]                   m_others;
  private URL[]                   m_assigned_views;
  private URL                     m_coordinator;
  private HAWMeteringWebservice[] m_views; // TODO: is this correct? Or should this be the proxy?
  private Timer                   m_timer;
  private Semaphore               m_sema;
  private boolean                 m_is_coordinator;
  private double                  m_currentValue; // last Value Computed by the Sensor

  public sensor(final URL sensorUrl) {
    m_url = sensorUrl;
    m_others = new URL[4];
    m_assigned_views = new URL[4];
    m_coordinator = null;
    m_views = new HAWMeteringWebservice[4];
    m_timer = new Timer(); // TODO: Initialize with correct functions
    m_sema  = new Semaphore(0);
    m_is_coordinator = false;
    m_currentValue = 0;
    // Publish web service
    Endpoint.publish(m_url.toString(), this);
  }

  void run() {
    try {
      m_sema.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * This function is invoked from a new created sensor to get a
   * reference to the current coordinator.
   * @returns  a URL to the current coordinator
   */
  @WebMethod
  public URL getCoordinator() {
    return m_coordinator;
  }

  /**
   * This function is invoked from the coordinator on all slave sensors
   * and signal them to update their view datas.
   */
  @WebMethod
  public void signalUpdate() {
    // TODO: Send update to HAWMeteringWebservice aka `m_view`
  }

  /**
   * This function is used to send updates to other sensors.
   * These updates include informations about which sensors
   * are known and which displays are assigned to whom.
   * @param known_sensors    is an array over all known sensors
   *                         the size of this array should be 4.
   * @param assigned_views   array over assigned views, the mapping is
   *                         array[0] = North
   *                         array[1] = East
   *                         array[2] = South
   *                         array[3] = West
   * @param coordinator_url  URL of the current coordinator
   */
  @WebMethod
  public void sendDataUpdate(
      @WebParam(name = "known_sensors") URL[] known_sensors,
      @WebParam(name = "assigned_views") URL[] assigned_views,
      @WebParam(name = "coordinator_url") URL coordinator) {
    // TODO: Assign parameters
  }

  /**
   * This function is called from a sensor on the coordinator
   * to register itself.
   * @param   myself   specifies the URL of the caller
   * @param   displays is an array over the requested views to write on
   * @returns an array over booleans which represents the allowed
   *          views to write on. The following mapping is used
   *          array[0] = North
   *          array[1] = East
   *          array[2] = South
   *          array[3] = West
   *          `TRUE` allows the sensor to send in the specified slot
   *          `FALS` prohibit the sensor to send in the specified slot
   *          This function can also return `NULL` if the caller is
   *          not the coordinator
   *          If the array contains `FALSE` on all slots the register
   *          request is revoked by the coordinator.
   */
  @WebMethod
  public boolean[] register(
      @WebParam(name = "myself") URL myself,
      @WebParam(name = "displays") boolean[] displays) {
    if (!m_is_coordinator) {
      return null;
    }
    // Find free slot for sensor
    int free_idx = -1;
    for (int i = 0; i < m_others.length; ++i) {
      if (m_others[i] == null) {
        free_idx = i;
        break;
      }
    }
    if (free_idx == -1) {
      // TODO: Check if there are some sensors with multiple views
      // if so, revoke write permissions for  them and assign write
      // permission to the new sensor.
      // No free slots, revoke request
      return new boolean[] { false, false, false, false };
    }
    // Check if requested displays are free
    boolean[] result = new boolean[4];
    for (int i = 0; i < m_assigned_views.length; ++i) {
      if (m_assigned_views[i] == null && displays[i] == true) {
        // view is free and requested
        result[i] = true;
      }
    }
    // Send updates to all other views
    for (URL url : m_others) {
      // TODO: Get callable proxy for url stored in `url`
      //sendDataUpdate(m_others, m_assigned_views, m_url);
    }
    // TODO: Call update function on new registred sensor
    // sendDataUpdate(...)
    return result;
  }

  /**
   * This function is invoked by the coordinator on a slave sensor
   * to revoke its write permissions on a view. This is used if this
   * sensor had multiple write permissions on views.
   * @param display specifies the view. The following mapping applies
   *        1 = North, 2 = East, 3 = South, 4 = West
   */
  @WebMethod
  public void removePermission(
      @WebParam(name = "display") int display) {
    // TODO: Remove permissions to write on display (view)
  }

  /**
   * This function is invoked by another sensor and presents
   * a vote from the caller for the callee.
   * the callee will invoke the same function on an sensor he
   * has to vote.
   */
  @WebMethod
  public boolean vote(
      @WebParam(name = "value") long value) {
    // TODO: I'm not sure what this long value is for.
    /*if (m_URL.toString().compareTo(url.toString()) > 0) {
      m_todos.add(new Job("vote"));
      return true;
    } else {
      return false;
    }*/
    return false;
  }
}
