package coordinator;

/**
* coordinator/CoordinatorHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from coordinator.idl
* Dienstag, 2. Juni 2015 17:45 Uhr MESZ
*/

public final class CoordinatorHolder implements org.omg.CORBA.portable.Streamable
{
  public coordinator.Coordinator value = null;

  public CoordinatorHolder ()
  {
  }

  public CoordinatorHolder (coordinator.Coordinator initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = coordinator.CoordinatorHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    coordinator.CoordinatorHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return coordinator.CoordinatorHelper.type ();
  }

}
