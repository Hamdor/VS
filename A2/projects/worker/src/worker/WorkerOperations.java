package worker;


/**
* worker/WorkerOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from idl_files/worker.idl
* Tuesday, 12 May 2015 10:55:40 o'clock CEST
*/

public interface WorkerOperations 
{

  /**
       * Initialize the worker with values
       * @param left  Identifier for left worker
       * @param right Identifier for right worker
       * @param value Start value for caluclation
       * @param delay Maximum delay 14:27
       * @param Identifier for monitor
       */
  void init (String left, String right, int value, int delay, String monitor);

  /**
       * Called from another worker to share its result with left and right worker
       * @param sender Identifier of worker who send this request
       * @param value  Value of result to share
       */
  void shareResult (String sender, int value);

  /**
       * Send a snapshot request to worker
       * @param sender The sender of the marker
       */
  void snapshot (String sender);

  /**
       * Kill the ggT Prozess (Worker)
       */
  void kill ();
} // interface WorkerOperations
