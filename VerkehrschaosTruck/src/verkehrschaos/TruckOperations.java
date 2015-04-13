package verkehrschaos;


/**
* verkehrschaos/TruckOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from verkehrschaos.idl
* Sonntag, 12. April 2015 13:36 Uhr MESZ
*/

public interface TruckOperations 
{

  /* Gibt den Namen des LKWs. */
  String getName ();

  /* Gibt die zugeordnete Spedition */
  verkehrschaos.TruckCompany getCompany ();

  /* Neue Zuordnung einer Spedition.
       * Wird von der zustaendigen Spedition aufgerufen. 
       * Zu Debug-Zwecken soll der Name der neuen Spedition auf der Konsole ausgegeben werden. 
       */
  void setCompany (verkehrschaos.TruckCompany company);

  /* Informiert den LKW waehrend einer Fahrt ueber die aktuelle Position.
       * Position kann auf der Konsole ausgegeben werden.
       * Wird von Streets aufgerufen.
       */
  void setCoordinate (double x, double y);

  /*
       * Stilllegung des LKWs (LKW Anwendung wird beendet).
       * Wird z. B. von zugeordneter Spedition aufgerufen, wenn diese still gelegt wird
       * oder von der Steueranwendung (Client).
       * Beenden der Anwendung durch Aufruf von orb.shutdown(true).
       * Nach orb.shutdown kleine Pause einlegen (0.5 sec) um Exception zu vermeiden.
       */
  void putOutOfService ();
} // interface TruckOperations
