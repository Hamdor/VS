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

/**
 * TODO:
 * + Permissions müssen korrekt gesetzt werden (im moment wird auf allen views geschrieben)
 * + Testen obs mit mehreren geht
 * + Wahlverfahren testen...
 * (+ Code aufräumen...) 
 **/

@WebService
@SOAPBinding(style = Style.RPC)
public class sensor {
  private URL m_url;
  private URL m_coordinator;
  private URL[] m_others;
  private HAWMeteringWebservice[] m_views; // TODO: is this correct? Or should
  private volatile boolean running;
  private Timer m_timer_timeout;
  private Timer m_timer_coordinator;
  public boolean m_is_coordinator;
  private int m_currentValue; // last Value Computed by the Sensor
  
  private boolean m_got_tick;

  private ObjectFactory m_factory;
  
  private final long TICK_RATE_MSEC = 1000;
  private final long TICK_RATE_COORDI_MSEC = 250;

  private HAWMeteringWebservice get_view_ref(String url) throws MalformedURLException {
    // http://localhost:9999/hawmetering/no\?WSDL
    HAWMeteringWebserviceService service = new HAWMeteringWebserviceService(new URL(HAWMeteringWebserviceService.class.getResource("."),url), new QName(
        "http://hawmetering/", "HAWMeteringWebserviceService"));
    return service.getHAWMeteringWebservicePort();
  }
  
  public sensor(final URL sensorUrl) {
    m_url = sensorUrl;
    m_others = new URL[4];
    m_coordinator = null;
    m_views = new HAWMeteringWebservice[4];
    try {
      m_views[0] = get_view_ref("http://localhost:9999/hawmetering/nw");
      m_views[1] = get_view_ref("http://localhost:9999/hawmetering/no");
      m_views[2] = get_view_ref("http://localhost:9999/hawmetering/sw");
      m_views[3] = get_view_ref("http://localhost:9999/hawmetering/so");
    } catch(Exception err) {
      System.err.println("Invalid URL for view..." + err.getMessage());
      System.exit(-5);
    }
    
    m_timer_timeout = new Timer();
    m_timer_timeout.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        if (!m_got_tick) {
          vote(m_url);
        }
        m_got_tick = false;
      }
    }, TICK_RATE_MSEC, TICK_RATE_MSEC);
    m_timer_coordinator = new Timer();
    m_timer_coordinator.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        System.out.println("m_timer_coordinator run()");
        if (m_is_coordinator) {
          System.out.println("m_timer_coordinator m_is_coordinator");
          boolean refresh_data = false;
          for (int i = 0; i < m_others.length; ++i) {
            try {
              if (m_others[i] != null) {
                resolve_sensor(m_others[i]).signalUpdate();
              }
            } catch (Exception err) {
              System.out.println(err.getMessage());
              refresh_data = true;
              m_others[i]= null;
            }
          }
          if (refresh_data) {
            doSendDataUpdate();
          }
        }
      }
    }, TICK_RATE_COORDI_MSEC, TICK_RATE_COORDI_MSEC); // TODO: Start only at full second...
    m_is_coordinator = false;
    m_currentValue = 0;
    m_factory = new ObjectFactory();
    // Publish web service
    Endpoint.publish(m_url.toString(), this);
  }
  
  private boolean initialized = false;
  public void initial_view_setup() {
    if (!initialized) {
      // TODO: Initialize view values (interval...)...
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
    System.out.println("signalUpdate()");
    System.out.println(m_url);
    m_got_tick = true;
    for (int i = 0; i < m_views.length; ++i) {
      if (m_views[i] != null) {
        m_views[i].setValue(m_currentValue);
      }
    }
  }

  private void doSendDataUpdate() {
    // Send updates to all other views
    AnyURIArray other_urls = m_factory.createAnyURIArray();
    for (URL url : m_others) {
      if (url == null) { continue; }
      other_urls.getItem().add(url.toString());
    }
    for (URL url : m_others) {
      if (url == null || url == m_url) { continue; }
      resolve_sensor(url).sendDataUpdate(other_urls, m_coordinator.toString());
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
  public void sendDataUpdate(
      @WebParam(name = "known_sensors") URL[] known_sensors,
      @WebParam(name = "coordinator_url") URL coordinator) {
    m_coordinator = coordinator;
    for (int i = 0; i < m_others.length; i++) {
      m_others[i] = known_sensors[i];
    }
    m_is_coordinator = (m_coordinator == m_url);
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
    for (int i = 0; i < m_others.length; ++i) {
      if (m_others[i] == null) {
        free_idx = i;
        break;
      }
    }
    if (free_idx == -1) {
      return false;
    }
    // Check if requested displays are free
    for (int i = 0; i < m_others.length; ++i) {
      if (m_others[i] != null && displays[i] == true) {
        return false;
      }
    }
    // set real values
    for (int i = 0; i < m_others.length; ++i) {
      if (m_others[i] == null && displays[i] == true) {
        // view is free and requested
        m_others[i] = myself;
      }
    }
    // Send updates to all other views
    doSendDataUpdate();
    return true;
  }

  /**
   * This function is invoked by another sensor and presents a vote from the
   * caller for the callee. the callee will invoke the same function on an
   * sensor he has to vote.
   * when this method is completed a new coordinator has been elected
   * @ return returns true if our URL is stronger than the URL of the called one
   *          returns false if our URL is weaker than the URL of the called one
   */
  @WebMethod
  public boolean vote(@WebParam(name = "value") URL value) {
    if (m_url.toString().compareTo(value.toString()) >= 0) {// our URL is more
                                                           // Powerful --> we
                                                           // win
      boolean winner = true;
      // we won --> we have to start an election with everybody else to see
      // whether we are the strongest
      for (int i = 0; i < m_others.length; i++) {
        try {
          if (m_others[i] != m_url) {
            if (resolve_sensor(m_others[i]).vote(m_url.toString())) {
              winner = false;
            }
          }
        } catch (Exception e) {
          continue;
        }
      }
      if (winner) {// we are new coordinator --> update all data
        this.m_coordinator = m_url;
        m_is_coordinator = true;
        // reach all other sensors and refresh the list
        for (int i = 0; i < m_others.length; i++) {
          try {
            resolve_sensor(m_others[i]).getCoordinator(); // ping others to see
                                                          // if theyre still
                                                          // available
          } catch (Exception e3) {// other member is not reachable --> is dead
            m_others[i] = null;
            continue;
          }
        }
        doSendDataUpdate();
      }
      return true;
    } else {// our URL is less Powerful --> we lose
      return false;
    }
  }
}
