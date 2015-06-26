
package sensorproxy;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebService(name = "sensor", targetNamespace = "http://hawsensor/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@XmlSeeAlso({
    ObjectFactory.class
})
public interface Sensor {


    /**
     * 
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(partName = "return")
    @Action(input = "http://hawsensor/sensor/getCoordinatorRequest", output = "http://hawsensor/sensor/getCoordinatorResponse")
    public String getCoordinator();

    /**
     * 
     */
    @WebMethod
    @Action(input = "http://hawsensor/sensor/signalUpdateRequest", output = "http://hawsensor/sensor/signalUpdateResponse")
    public void signalUpdate();

    /**
     * 
     * @param knownSensors
     * @param coordinatorUrl
     */
    @WebMethod
    @Action(input = "http://hawsensor/sensor/sendDataUpdateRequest", output = "http://hawsensor/sensor/sendDataUpdateResponse")
    public void sendDataUpdate(
        @WebParam(name = "north", partName = "north") String north,
        @WebParam(name = "east", partName = "east")String east,
        @WebParam(name = "south", partName = "south") String south,
        @WebParam(name = "west", partName = "west") String west,
        @WebParam(name = "coordinator_url", partName = "coordinator_url")
        String coordinatorUrl);

    /**
     * 
     * @param value
     * @return
     *     returns boolean
     */
    @WebMethod
    @WebResult(partName = "return")
    @Action(input = "http://hawsensor/sensor/voteRequest", output = "http://hawsensor/sensor/voteResponse")
    public boolean vote(
        @WebParam(name = "value", partName = "value")
        String value);

    /**
     * 
     * @param myself
     * @param displays
     * @return
     *     returns boolean
     */
    @WebMethod
    @WebResult(partName = "return")
    @Action(input = "http://hawsensor/sensor/registerRequest", output = "http://hawsensor/sensor/registerResponse")
    public boolean register(
        @WebParam(name = "myself", partName = "myself")
        String myself,
        @WebParam(name = "displays", partName = "displays")
        BooleanArray displays);

}