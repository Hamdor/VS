package worker;


/**
* worker/_WorkerStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from idl_files/worker.idl
* Tuesday, 12 May 2015 10:55:40 o'clock CEST
*/

public class _WorkerStub extends org.omg.CORBA.portable.ObjectImpl implements worker.Worker
{


  /**
       * Initialize the worker with values
       * @param left  Identifier for left worker
       * @param right Identifier for right worker
       * @param value Start value for caluclation
       * @param delay Maximum delay 14:27
       * @param Identifier for monitor
       */
  public void init (String left, String right, int value, int delay, String monitor)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("init", true);
                $out.write_string (left);
                $out.write_string (right);
                $out.write_long (value);
                $out.write_long (delay);
                $out.write_string (monitor);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                init (left, right, value, delay, monitor        );
            } finally {
                _releaseReply ($in);
            }
  } // init


  /**
       * Called from another worker to share its result with left and right worker
       * @param sender Identifier of worker who send this request
       * @param value  Value of result to share
       */
  public void shareResult (String sender, int value)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("shareResult", true);
                $out.write_string (sender);
                $out.write_long (value);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                shareResult (sender, value        );
            } finally {
                _releaseReply ($in);
            }
  } // shareResult


  /**
       * Send a snapshot request to worker
       * @param sender The sender of the marker
       */
  public void snapshot (String sender)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("snapshot", true);
                $out.write_string (sender);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                snapshot (sender        );
            } finally {
                _releaseReply ($in);
            }
  } // snapshot


  /**
       * Kill the ggT Prozess (Worker)
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
    "IDL:worker/Worker:1.0"};

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
} // class _WorkerStub
