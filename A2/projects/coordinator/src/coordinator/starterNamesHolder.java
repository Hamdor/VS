package coordinator;


/**
* coordinator/starterNamesHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from coordinator.idl
* Samstag, 16. Mai 2015 21:52 Uhr MESZ
*/

public final class starterNamesHolder implements org.omg.CORBA.portable.Streamable
{
  public String value[] = null;

  public starterNamesHolder ()
  {
  }

  public starterNamesHolder (String[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = coordinator.starterNamesHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    coordinator.starterNamesHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return coordinator.starterNamesHelper.type ();
  }

}
