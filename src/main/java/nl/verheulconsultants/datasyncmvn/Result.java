package nl.verheulconsultants.datasyncmvn;

/**
 * Cumulative result record for the processing of an Opdrachtregel or an Opdracht
 * dummy change to check Mercurial
 */
class Result {
  //stops in order of precedence
  static final int NOT_STOPPED = 0;
  static final int STOP_IMMEDIATELY = 1;
  static final int STOP_AFTER_FILE = 2;
  static final int STOP_AFTER_DIRECTORY = 3;
  static final int STOP_AFTER_SOURCE = 4;

  int fileCount = 0;
  int fileCountSourceNewer = 0;
  //synchronisation succeeded
  int fileCountSourceNewerSynct = 0;
  int fileCountDestNewer = 0;
  int fileCountSameDateStamp = 0;
  int fileCountNewFile = 0;
  //synchronisation succeeded
  int fileCountNewFileSynct = 0;
  int sourceFilesToBeDeleted = 0;
  //actually deleted
  int sourceFilesDeleted = 0; 
  int targetFilesToBeDeleted = 0;
  //actually deleted
  int targetFilesDeleted = 0;
  //bytes counted
  long size = 0;
  int stopByUser = NOT_STOPPED;
  int stopByProgram = NOT_STOPPED;
  //last succesfull processed source (subBron)
  String stopLocation= null;
  int errorCount = 0;
  int reportLinesCount = 0;
  //for short term use only (one Bestemming) then reset
  boolean readOnlyWasReset = false;
}