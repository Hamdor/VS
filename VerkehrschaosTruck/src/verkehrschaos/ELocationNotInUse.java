package verkehrschaos;


/**
* verkehrschaos/ELocationNotInUse.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from verkehrschaos.idl
* Sonntag, 12. April 2015 13:36 Uhr MESZ
*/

public final class ELocationNotInUse extends org.omg.CORBA.UserException
{
  public String msg = null;

  public ELocationNotInUse ()
  {
    super(ELocationNotInUseHelper.id());
  } // ctor

  public ELocationNotInUse (String _msg)
  {
    super(ELocationNotInUseHelper.id());
    msg = _msg;
  } // ctor


  public ELocationNotInUse (String $reason, String _msg)
  {
    super(ELocationNotInUseHelper.id() + "  " + $reason);
    msg = _msg;
  } // ctor

} // class ELocationNotInUse
