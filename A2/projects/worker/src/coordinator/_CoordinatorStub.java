package coordinator;


/**
* coordinator/_CoordinatorStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from coordinator.idl
* Tuesday, 9 June 2015 10:05:16 o'clock CEST
*/

public class _CoordinatorStub extends org.omg.CORBA.portable.ObjectImpl implements coordinator.Coordinator
{


  /**
       * Register a ggT Prozess (Worker) at coordinator side
       * @param whom    Identifier of ggT Prozess (Worker)
       * @param owner   Identifier of the starter
       */
  public void register (String whom, String owner)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("register", true);
                $out.write_string (whom);
                $out.write_string (owner);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                register (whom, owner        );
            } finally {
                _releaseReply ($in);
            }
  } // register


  /**
       * Register a new Starter at coordinator side
       * @param whom    Identifier of starter
       */
  public void register_starter (String whom)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("register_starter", true);
                $out.write_string (whom);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                register_starter (whom        );
            } finally {
                _releaseReply ($in);
            }
  } // register_starter


  /**
       * Called from a ggT prozess to inform about current state
       * @param whom      Identifier of ggT Prozess (Worker)
       * @param seqNr     Sequenz Number
       * @param finished  TRUE  when ggT is finished
       *                  FALSE when ggT is not finished
       * @param current   Last calculated ggT, if finished is TRUE this value
       *                  represent the result
       */
  public void inform (String whom, int seqNr, boolean finished, int current)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("inform", true);
                $out.write_string (whom);
                $out.write_long (seqNr);
                $out.write_boolean (finished);
                $out.write_long (current);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                inform (whom, seqNr, finished, current        );
            } finally {
                _releaseReply ($in);
            }
  } // inform


  /**
       * Returns a list of all starters
       * @returns the names of available starts
       *          this is a list over strings
       */
  public String[] getStarter ()
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("getStarter", true);
                $in = _invoke ($out);
                String $result[] = coordinator.starterNamesHelper.read ($in);
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return getStarter (        );
            } finally {
                _releaseReply ($in);
            }
  } // getStarter


  /**
       * Kick off a calculation
       * @param monitor     Name of monitor
       * @param ggTLower    Lower interval for ggT prozess count (Workers)
       * @param ggTUpper    Upper interval for ggT Prozess count (Workers)
       * @param delayLower  Lower interval for delay (is this really needed?)
       * @param delayUpper  Upper interval for delay
       * @param period      Period for system state
       * @param expectedggt Expected ggT
       */
  public void calculate (String monitor, int ggTLower, int ggTUpper, int delayLower, int delayUpper, int period, int expectedggT)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("calculate", true);
                $out.write_string (monitor);
                $out.write_long (ggTLower);
                $out.write_long (ggTUpper);
                $out.write_long (delayLower);
                $out.write_long (delayUpper);
                $out.write_long (period);
                $out.write_long (expectedggT);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                calculate (monitor, ggTLower, ggTUpper, delayLower, delayUpper, period, expectedggT        );
            } finally {
                _releaseReply ($in);
            }
  } // calculate


  /**
       * Kills a starter, the starter has to terminate all of its child prozesses
       * @param whom Identifier of starter
       */
  public void kill (String whom)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("kill", true);
                $out.write_string (whom);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                kill (whom        );
            } finally {
                _releaseReply ($in);
            }
  } // kill


  /**
       * This call terminates the coordinator and all of its starters
       */
  public void terminate ()
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("terminate", true);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                terminate (        );
            } finally {
                _releaseReply ($in);
            }
  } // terminate

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:coordinator/Coordinator:1.0"};

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
} // class _CoordinatorStub
