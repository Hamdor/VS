package starter;


/**
* starter/_StarterStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from starter.idl
* Samstag, 9. Mai 2015 17:50 Uhr MESZ
*/

public class _StarterStub extends org.omg.CORBA.portable.ObjectImpl implements starter.Starter
{


  /**
       * Start the expected number of worker prozesses (ggT)
       * @param number  Number of prozesses to start
       */
  public void startWorker (int number)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("startWorker", true);
                $out.write_long (number);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                startWorker (number        );
            } finally {
                _releaseReply ($in);
            }
  } // startWorker


  /**
       * Kills the starter and all of its worker processes
       */
  public void kill ()
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("kill", true);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                kill (        );
            } finally {
                _releaseReply ($in);
            }
  } // kill

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:starter/Starter:1.0"};

  public String[] _ids ()
  {
    return (String[])__ids.clone ();
  }

  private void readObject (java.io.ObjectInputStream s) throws java.io.IOException
  {
     String str = s.readUTF ();
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     org.omg.CORBA.Object obj = orb.string_to_object (str);
     org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate ();
     _set_delegate (delegate);
   } finally {
     orb.destroy() ;
   }
  }

  private void writeObject (java.io.ObjectOutputStream s) throws java.io.IOException
  {
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     String str = orb.object_to_string (this);
     s.writeUTF (str);
   } finally {
     orb.destroy() ;
   }
  }
} // class _StarterStub
