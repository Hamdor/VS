package monitor;


/**
* monitor/StartzahlenHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from monitor.idl
* Tuesday, 9 June 2015 10:05:16 o'clock CEST
*/

public final class StartzahlenHolder implements org.omg.CORBA.portable.Streamable
{
  public int value[] = null;

  public StartzahlenHolder ()
  {
  }

  public StartzahlenHolder (int[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = monitor.StartzahlenHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    monitor.StartzahlenHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return monitor.StartzahlenHelper.type ();
  }

}
