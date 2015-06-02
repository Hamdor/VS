package starter;


/**
* starter/StarterOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from starter.idl
* Dienstag, 2. Juni 2015 17:45 Uhr MESZ
*/

public interface StarterOperations 
{

  /**
       * Start the expected number of worker prozesses (ggT)
       * @param number  Number of prozesses to start
       */
  void startWorker (int number);

  /**
       * Kills the starter and all of its worker processes
       */
  void kill ();
} // interface StarterOperations
