package verkehrschaos;


/**
* verkehrschaos/TLocationListHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from verkehrschaos.idl
* Sonntag, 12. April 2015 13:36 Uhr MESZ
*/

public final class TLocationListHolder implements org.omg.CORBA.portable.Streamable
{
  public String value[] = null;

  public TLocationListHolder ()
  {
  }

  public TLocationListHolder (String[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = verkehrschaos.TLocationListHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    verkehrschaos.TLocationListHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return verkehrschaos.TLocationListHelper.type ();
  }

}
