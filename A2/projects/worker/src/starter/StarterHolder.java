package starter;

/**
* starter/StarterHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from starter.idl
* Samstag, 16. Mai 2015 21:52 Uhr MESZ
*/

public final class StarterHolder implements org.omg.CORBA.portable.Streamable
{
  public starter.Starter value = null;

  public StarterHolder ()
  {
  }

  public StarterHolder (starter.Starter initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = starter.StarterHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    starter.StarterHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return starter.StarterHelper.type ();
  }

}
