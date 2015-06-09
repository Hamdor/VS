package starter;


/**
* starter/StarterPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from starter.idl
* Tuesday, 9 June 2015 10:05:16 o'clock CEST
*/

public abstract class StarterPOA extends org.omg.PortableServer.Servant
 implements starter.StarterOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("startWorker", new java.lang.Integer (0));
    _methods.put ("kill", new java.lang.Integer (1));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {

  /**
       * Start the expected number of worker prozesses (ggT)
       * @param number  Number of prozesses to start
       */
       case 0:  // starter/Starter/startWorker
       {
         int number = in.read_long ();
         this.startWorker (number);
         out = $rh.createReply();
         break;
       }


  /**
       * Kills the starter and all of its worker processes
       */
       case 1:  // starter/Starter/kill
       {
         this.kill ();
         out = $rh.createReply();
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:starter/Starter:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public Starter _this() 
  {
    return StarterHelper.narrow(
    super._this_object());
  }

  public Starter _this(org.omg.CORBA.ORB orb) 
  {
    return StarterHelper.narrow(
    super._this_object(orb));
  }


} // class StarterPOA
