package verkehrschaos;


/**
* verkehrschaos/TTruckListHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from verkehrschaos.idl
* Sonntag, 12. April 2015 13:36 Uhr MESZ
*/

abstract public class TTruckListHelper
{
  private static String  _id = "IDL:verkehrschaos/TTruckList:1.0";

  public static void insert (org.omg.CORBA.Any a, verkehrschaos.Truck[] that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static verkehrschaos.Truck[] extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = verkehrschaos.TruckHelper.type ();
      __typeCode = org.omg.CORBA.ORB.init ().create_sequence_tc (0, __typeCode);
      __typeCode = org.omg.CORBA.ORB.init ().create_alias_tc (verkehrschaos.TTruckListHelper.id (), "TTruckList", __typeCode);
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static verkehrschaos.Truck[] read (org.omg.CORBA.portable.InputStream istream)
  {
    verkehrschaos.Truck value[] = null;
    int _len0 = istream.read_long ();
    value = new verkehrschaos.Truck[_len0];
    for (int _o1 = 0;_o1 < value.length; ++_o1)
      value[_o1] = verkehrschaos.TruckHelper.read (istream);
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, verkehrschaos.Truck[] value)
  {
    ostream.write_long (value.length);
    for (int _i0 = 0;_i0 < value.length; ++_i0)
      verkehrschaos.TruckHelper.write (ostream, value[_i0]);
  }

}