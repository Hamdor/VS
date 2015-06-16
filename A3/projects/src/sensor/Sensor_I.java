package sensor;

import java.net.URL;

import javax.jws.WebMethod;
import javax.jws.WebService;

// TODO Implement proper interface for WebService calls...
@WebService
public interface Sensor_I {

  public void run();
  @WebMethod public URL getCoordinator();
  @WebMethod public void signalUpdate();
  @WebMethod public void sendDataUpdate();
  @WebMethod public boolean register(Sensor_I myself, String[] displays, boolean[] canSendTo);
  @WebMethod public void removePermission(String display);
  @WebMethod public boolean vote(URL url);
  @WebMethod public void refreshOthersList(Sensor_I[] newList);

}
