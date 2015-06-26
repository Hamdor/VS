package hawsensor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import sensorproxy.AnyURIArray;
import sensorproxy.ObjectFactory;
import sensorproxy.Sensor;
import sensorproxy.SensorService;
import hawmeterproxy.HAWMeteringWebservice;
import hawmeterproxy.HAWMeteringWebserviceService;
import hawmeterproxy.WebColor;

/**
 * TODO: + Permissions müssen korrekt gesetzt werden (im moment wird auf allen
 * views geschrieben) DONE TESTED + Testen obs mit mehreren geht DONE +
 * Wahlverfahren testen SO far if coordi dead all dead (+ Code aufräumen...) +
 * ranges setzen + sensorbelegungsmanagement anpassen
 **/

@WebService
@SOAPBinding(style = Style.RPC)
public class sensor {

  private boolean[] m_viewlock;
  private URL[] m_others;
  private URL m_url;
  private URL m_coordinator;
  private URL[] m_used_views;
  private HAWMeteringWebservice[] m_views;
  private volatile boolean running;
  private Timer m_timer_timeout;
  private Timer m_timer_coordinator;
  public boolean m_is_coordinator;
  private int m_currentValue; // last Value Computed by the Sensor

  private boolean m_got_tick = false;
  private boolean m_vote_running = false;

  private ObjectFactory m_factory;

  private final long TICK_RATE_MSEC = 1500;
  private final long TICK_RATE_COORDI_MSEC = 1000;

  private HAWMeteringWebservice get_view_ref(String url)
      throws MalformedURLException {
    // http://localhost:9999/hawmetering/no\?WSDL
    HAWMeteringWebserviceService service = new HAWMeteringWebserviceService(
        new URL(HAWMeteringWebserviceService.class.getResource("."), url),
        new QName("http://hawmetering/", "HAWMeteringWebserviceService"));
    return service.getHAWMeteringWebservicePort();
  }

  public sensor(final URL sensorUrl, boolean displays[], boolean isCoordinator) {
    m_url = sensorUrl;
    m_used_views = new URL[4];
    m_others = new URL[4];
    m_coordinator = isCoordinator ? m_url : null;
    // System.out.println("created sensor at:" + m_url);
    m_views = new HAWMeteringWebservice[4];
    try {
      for (int k = 0; k < m_views.length; k++) {// initialize array
        m_views[k] = null;
      }
      // fill if appropriate
      if (displays[0])
        m_views[0] = get_view_ref("http://localhost:9999/hawmetering/nw");
      if (displays[1])
        m_views[1] = get_view_ref("http://localhost:9999/hawmetering/no");
      if (displays[2])
        m_views[2] = get_view_ref("http://localhost:9999/hawmetering/sw");
      if (displays[3])
        m_views[3] = get_view_ref("http://localhost:9999/hawmetering/so");

    } catch (Exception err) {
      System.err.println("Invalid URL for view..." + err.getMessage());
      System.exit(-5);
    }

    m_timer_timeout = new Timer();
    m_timer_timeout.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        if (running) {
          if (!m_got_tick && !m_vote_running) {
            vote(m_url);
          }
          m_got_tick = false;
        }
      }
    }, TICK_RATE_MSEC, TICK_RATE_MSEC);
    m_timer_coordinator = new Timer();
    m_timer_coordinator.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        if (running) {
          // //System.out.println("m_timer_coordinator run()");
          if (m_is_coordinator) {
            // //System.out.println("m_timer_coordinator m_is_coordinator");
            boolean refresh_data = false;
            for (int i = 0; i < m_used_views.length; ++i) {
              try {
                if (m_used_views[i] != null) {
                  resolve_sensor(m_used_views[i]).signalUpdate();
                }
              } catch (Exception err) {
                System.out.println(err.getMessage());
                refresh_data = true;
                m_used_views[i] = null;
              }
            }
            if (refresh_data) {
              doSendDataUpdate();
            }
          }
        }
      }
    }, (2000 - new Date().getTime() % 2000), TICK_RATE_COORDI_MSEC); // TODO:
                                                                     // Start
                                                                     // only at
    // full second...
    m_is_coordinator = false;
    m_currentValue = 0;
    m_factory = new ObjectFactory();
    // Publish web service
    // //System.out.println(m_url);
    Endpoint.publish(m_url.toString(), this);
  }

  private boolean initialized = false;

  public void initial_view_setup() {
    if (!initialized) {
      for (int i = 0; i < m_views.length; i++) {// & default shit maybe do
                                                // something fancy
        if (m_views[i] != null) {
          m_views[i].clearIntervals();// green
          WebColor color = new WebColor();
          color.setRed(0);
          color.setGreen(255);
          color.setBlue(0);
          color.setAlpha(100);
          m_views[i].setIntervals("", 0, 50, color);// green
          color.setRed(255);
          color.setGreen(255);
          color.setBlue(0);
          color.setAlpha(100);
          m_views[i].setIntervals("", 50, 75, color);// yellow
          color.setRed(255);
          color.setGreen(0);
          color.setBlue(0);
          color.setAlpha(100);
          m_views[i].setIntervals("", 75, 100, color);// red
        }
      }
      initialized = true;
    }
  }

  Sensor resolve_sensor(URL url) {
    SensorService service = new SensorService(url, new QName(
        "http://hawsensor/", "sensorService"));
    return service.getSensorPort();
  }

  void run() {
    running = true;
    while (running) {
      // TODO: Check if this is correct...
      long lTicks = new Date().getTime();
      m_currentValue = ((int) (lTicks % 20000)) / 100;
      if (m_currentValue > 100) {
        m_currentValue = 200 - m_currentValue;
      }
    }
  }

  /**
   * This function is invoked from a new created sensor to get a reference to
   * the current coordinator.
   * 
   * @returns a URL to the current coordinator
   */
  @WebMethod
  public URL getCoordinator() {
    return m_coordinator;
  }

  /**
   * This function is invoked from the coordinator on all slave sensors and
   * signal them to update their view datas.
   */
  @WebMethod
  public void signalUpdate() {
    // //System.out.println("signalUpdate()");
    // //System.out.println(m_url);
    Runnable task = new Runnable() {

      @Override
      public void run() {
        // TODO Auto-generated method stub
        m_got_tick = true;
        for (int i = 0; i < m_views.length; ++i) {
          if (m_views[i] != null) {
            m_views[i].setValue(m_currentValue);
          }
        }
      }
    };
task.run();
  }

  private void doSendDataUpdate() {
    // Send updates to all other views
    // System.out.println("dataupdate");

    String[] to_send = new String[4];
    for (int i = 0; i < to_send.length; i++) {
      if (m_used_views[i] == null) {
        to_send[i] = "";
      } else {
        to_send[i] = m_used_views[i].toString();
      }
    }
    // Send update to us first
    sendDataUpdate(to_send[0], to_send[1], to_send[2], to_send[3],
        m_coordinator);
    for (URL url : m_others) {
      if (url == null || url == m_url) {
        continue;
      }
      resolve_sensor(url).sendDataUpdate(to_send[0], to_send[1], to_send[2],
          to_send[3], m_coordinator.toString());
    }
  }

  /**
   * This function is used to send updates to other sensors. These updates
   * include informations about which sensors are known and which displays are
   * assigned to whom.
   * 
   * @param known_sensors
   *          is an array over all known sensors the size of this array should
   *          be 4. The mapping is array[0] = North array[1] = East array[2] =
   *          South array[3] = West
   * @param coordinator_url
   *          URL of the current coordinator
   */
  @WebMethod
  public void sendDataUpdate(@WebParam(name = "north") String north,
      @WebParam(name = "east") String east,
      @WebParam(name = "south") String south,
      @WebParam(name = "west") String west,
      @WebParam(name = "coordinator_url") URL coordinator) {

    m_coordinator = coordinator;
    int others = 0;

    try {

      if (!north.equals(""))
        m_used_views[0] = new URL(north);
      if (!east.equals(""))
        m_used_views[1] = new URL(east);
      if (!south.equals(""))
        m_used_views[2] = new URL(south);
      if (!west.equals(""))
        m_used_views[3] = new URL(west);
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    m_is_coordinator = m_coordinator.equals(m_url);
    for (int k = 0; k < m_others.length; k++) {
      m_others[k] = null;
    }
    for (int j = 0; j < m_used_views.length; j++) {
      boolean contains = false;
      for (int z = 0; z < m_others.length; z++) {
        if (m_others[z] != null) {
          if (m_others[z].equals(m_used_views[j])) {
            contains = true;
          }
        }
      }
      if (!contains) {
        m_others[others] = m_used_views[j];
        others++;
      }
    }

    m_got_tick = true;
    m_vote_running = false;
  }

  /**
   * This function is called from a sensor on the coordinator to register
   * itself.
   * 
   * @param myself
   *          specifies the URL of the caller
   * @param displays
   *          is an array over the requested views to write on
   * @returns an array over booleans which represents the allowed views to write
   *          on. The following mapping is used array[0] = North array[1] = East
   *          array[2] = South array[3] = West `TRUE` allows the sensor to send
   *          in the specified slot `FALSE` prohibit the sensor to send in the
   *          specified slot This function can also return `NULL` if the caller
   *          is not the coordinator If the array contains `FALSE` on all slots
   *          the register request is revoked by the coordinator.
   */
  // return array is not really needed imho
  @WebMethod
  public boolean register(@WebParam(name = "myself") URL myself,
      @WebParam(name = "displays") boolean[] displays) {
    if (!m_is_coordinator) {
      return false;
    }
    // dont know whether this is needed
    // Find free slot for sensor
    int free_idx = -1;
    for (int i = 0; i < m_used_views.length; ++i) {
      if (m_used_views[i] == null) {
        free_idx = i;
        break;
      }
    }
    // //System.out.println("register 0");
    if (free_idx == -1) {
      return false;
    }
    // //System.out.println("register1");
    // Check if requested displays are free
    for (int i = 0; i < m_used_views.length; ++i) {
      if (m_used_views[i] != null && displays[i] == true) {
        return false;
      }
    }
    // set real values
    // //System.out.println("register 2");
    for (int i = 0; i < m_used_views.length; ++i) {
      if (m_used_views[i] == null && displays[i] == true) {
        // view is free and requested
        m_used_views[i] = myself;
      }
    }
    // Send updates to all other views
    // //System.out.println("register complete");
    doSendDataUpdate();
    return true;
  }

  /**
   * This function is invoked by another sensor and presents a vote from the
   * caller for the callee. the callee will invoke the same function on an
   * sensor he has to vote. when this method is completed a new coordinator has
   * been elected @ return returns true if our URL is stronger than the URL of
   * the called one returns false if our URL is weaker than the URL of the
   * called one
   */
  @WebMethod
  public boolean vote(@WebParam(name = "value") URL value) {

    if (this.m_is_coordinator || m_vote_running) {// we are coordinator dont
                                                  // vote
      // System.out.println("coordinator pinged for vote");
      return false;
    }

    m_vote_running = true;
    if (m_url.toString().compareTo(value.toString()) >= 0) {// incoming URL is
                                                            // weaker
      // System.out.println("vote initialized at: " + m_url);
      // System.out.println("vote requested by: " + value);
      boolean winner = true;

      for (int i = 0; i < m_others.length; i++) {
        if (m_others[i] != null) {
          if (m_url.toString().compareTo(m_others[i].toString()) < 0) {
            try {
              // System.out.println("vote requested at:" + m_others[i]);
              resolve_sensor(m_others[i]).vote(m_url.toString());
              winner = false;
            } catch (Exception e) {
              // System.out.println("sensor at:" + m_others[i] +
              // "is unreachable");
            }
          }
        }
      }
      if (winner) {
        // System.out.println("vote won by: " + m_url);
        this.m_coordinator = m_url;

        // reach all other sensors and refresh the list
        for (int i = 0; i < m_others.length; i++) {
          try {
            resolve_sensor(m_used_views[i]).getCoordinator(); // ping others to
                                                              // see
            // System.out.println("pinged " + m_others[i]); // if they're still
            // available
          } catch (Exception e3) {// other member is not reachable --> is dead
            // System.out.println("tried to reach :" + m_used_views[i]
            // + "without success");
            m_used_views[i] = null;
            continue;
          }
        }
        doSendDataUpdate();
        m_is_coordinator = true;
        // System.out.println("sent update");
      } else {
        // System.out.println("vote lost by: " + m_url);
      }
      return true;
    } else {// incoming url is stronger
      // should not happen
      // System.out.println("stronger sensor requested vote at me");
      return false;
    }

  }
}
