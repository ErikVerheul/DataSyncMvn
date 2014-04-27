package nl.verheulconsultants.datasyncmvn;

import java.io.*;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import static nl.verheulconsultants.datasyncmvn.DataSync.loggerFileHandler;

/**
 * One synchronization command corresponding to one line in the mapping table.
 */
class TaskLine implements Comparable {

    private static final Logger LOGGER = Logger.getLogger(TaskLine.class.getName());
    private static final int DEBUG_INFO = 1;
    private static final int INFO = 2;
    private static final int WARNING = 3;
    private static final int SEVERE = 4;
    private static final int LOG_ONLY = 5;
    private static final int STATUSWINDOW_ONLY = 6;
    private static final int DO_SYNC = 1;
    private static final int DO_COMPARE = 2;
    private static final int MAX_REPORT_LINES = 1000;
    private boolean maxReportLinesCountReached = false;
    private final Mapping map;
    private final FilterSpec includeFilter;
    private final Set<FilterSpec> excludeFiles = new TreeSet<>();
    //the result record for this instance
    private Result r;

    /**
     * Create this task line from a Mapping
     *
     * @param task the ancestor
     * @param map the mapping
     * @param includeFilter the filter (can be empty)
     */
    TaskLine(Mapping map, FilterSpec includeFilter) {
        // remove all handlers that will be replaced
        // TODO: find out why this is necessary
        Handler[] handlers = LOGGER.getHandlers();
        for (Handler handler : handlers) {
            LOGGER.removeHandler(handler);
        }
        // Send logger output to our FileHandler.
        LOGGER.addHandler(loggerFileHandler);
        // Request that every detail gets logged.
        LOGGER.setLevel(Level.ALL);
        this.map = map;
        this.includeFilter = includeFilter;
    }

    /**
     * Get the Bron of this TaskLine
     *
     * @return the Bron
     */
    String getBron() {
        return map.bron;
    }

    /**
     * Get the includeFilter from this TaskLine
     *
     * @return includeFilter the filter
     */
    FilterSpec getIncludeFilter() {
        return includeFilter;
    }

    /**
     * Check if this TaskLine is a file filter
     *
     * @return true if so
     */
    boolean isFilter() {
        return includeFilter.isSet();
    }

    /**
     * Check if this TaskLine is a file filter and subdirectories of Bron must
     * be filtered also
     *
     * @return true if so
     */
    boolean continueWithSubdirectories() {
        return includeFilter.isSet() && includeFilter.continueWithSubdirectories;
    }

    /**
     * Get the Bestemming of this TaskLine
     *
     * @return the Bestemming
     */
    String getBestemming() {
        return map.bestemming;
    }

    /**
     * Get the Mapping of this TaskLine
     *
     * @return the Mapping
     */
    Mapping getMap() {
        return map;
    }

    /**
     * Add an exception to this Bron-Bestemming mapping Store the Bron in the
     * TreeSet uitzonderingen
     *
     * @param fileFilter the fileFilter for the files to exclude from copying
     * @return true if successful (not already entered)
     */
    public boolean addFilterSpec(FilterSpec fileFilter) {
        return excludeFiles.add(fileFilter);
    }

    private void limitReportLength(JTextArea progress) {
        if (!maxReportLinesCountReached && r.reportLinesCount > MAX_REPORT_LINES) {
            progress.append("Stop met weergave info-regels na " + MAX_REPORT_LINES + " regels voor deze Bron, zie verder de log\n");
            maxReportLinesCountReached = true;
        }
    }

    /**
     * Convenience method to report messages both to the thread status window
     * and the logFile. 'debuginfo' messages are not reported (to file only)
     * unless the user sets the debug mode flag. if maxReportLinesCount was
     * reached for a 'opdrachtregel' no more 'info' messages are send to the
     * progress display.
     *
     * @param s the message
     * @param belang the SEVEREness
     * @param progress the thread status window
     */
    private void report(String s, int belang, JTextArea progress) {
        if (MainFrame.debugMode && belang == DEBUG_INFO) {
            LOGGER.info(s);
        } else if (belang == INFO) {
            LOGGER.info(s);
            if (!maxReportLinesCountReached) {
                progress.append(s);
                r.reportLinesCount++;
            }
        } else if (belang == WARNING) {
            LOGGER.warning(s);
            progress.append(s);
            r.reportLinesCount++;
        } else if (belang == SEVERE) {
            LOGGER.severe(s);
            progress.append(s);
            r.reportLinesCount++;
        } else if (belang == LOG_ONLY) {
            LOGGER.info(s);
        } else if (belang == STATUSWINDOW_ONLY) {
            progress.append(s);
            r.reportLinesCount++;
        }
        limitReportLength(progress);
    }

    /**
     * Check if a copy of this file will pass the set data limit for this
     * TaskLine Report (warning in the logFile and on screen) if the limit is
     * passed Set the flag to stop processing this TaskLine
     *
     * @param file the file to copy
     * @param bron the Bron name
     * @param dataLimit the data limit set for this TaskLine
     * @param progress the thread status window
     * @return true if limit is not passed
     */
    private boolean checkDataLimit(File file, String bron, long dataLimit, JTextArea progress) {
        if (r.size + file.length() > dataLimit) {
            report("STOP met bron " + bron + ". De data limiet van " + Routines.makeVolumeStr(dataLimit)
                    + " dreigt te worden overschreden met file " + file.getPath(), WARNING, progress);
            report("Data volume tot nu toe " + Routines.makeVolumeStr(r.size)
                    + ", file grootte " + file.length() + " bytes", WARNING, progress);
            r.stopByProgram = Result.STOP_IMMEDIATELY;
            r.errorCount++;
            return false;
        }
        return true;
    }

    /**
     * Check if a Bestemming directory exists. If not, try to create the
     * directory. Report success or failure. Use this method only when
     * synchronizing (not when comparing)
     *
     * @param dir the directory to check
     * @param progress the thread statusbar
     * @return true if the directory exists or a new one could be created, false
     * if unable to create
     */
    private boolean canAccessOrMakeDir(File dir, JTextArea progress) {
        if (dir.exists()) {
            return Routines.canWriteInDir(dir, "Bestemming");
        }

        if (dir.mkdirs()) {
            report("Directory " + dir.getPath() + " is gecre�erd", DEBUG_INFO, progress);
            return true;
        } else {
            report("Fout: Kan directory " + dir.getPath() + " niet cre�ren", SEVERE, progress);
            r.errorCount++;
            return false;
        }
    }

    private boolean copyFile(File in, File out, long maxBandWidth, JTextArea progress) {
        if (Routines.doFastCopy(in, out, maxBandWidth)) {
            long size = in.length();
            if (!MainFrame.stopOnmiddellijk) {
                report("File " + in.getPath() + " (" + Routines.makeVolumeStr(size)
                        + ") is gekopieerd naar " + out.getPath(), LOG_ONLY, progress);
            } else {
                report("File " + in.getPath() + " (" + Routines.makeVolumeStr(size)
                        + ") is NIET gekopieerd maar verwijderd van de bestemming " + out.getPath()
                        + " vanwege een STOP-onmiddellijk opdracht.", WARNING, progress);
            }
            r.size = r.size + size;
            return true;
        } else {
            progress.append("Fout bij kopieren " + in.getPath() + ", zie de log");
            r.errorCount++;
            return false;
        }
    }

    /**
     * Check if the file is writable (actie == do_sync) or assume it is (actie =
     * do_vergelijk) Copy the file (actie == do_sync) or add the file size to
     * the size to be copied (actie == do_vergelijk)
     *
     * @param in input file
     * @param out output file
     * @param actie what to do
     * @param maxBandWidth the maximum average throughput for the copy routine
     * in MB/s.
     * @param verwijderBron mark Bron for deletion if true
     * @param deleteSet Set of files to be deleted
     * @param progress the thread status window
     * @return true is successful
     */
    private boolean processFile(File in, File out, int actie, long maxBandWidth, JTextArea progress) {
        if (actie == DO_SYNC) {
            if (canAccessOrMakeDir(out.getParentFile(), progress) && Routines.canWriteToFile(out, r, MainFrame.overschrijfReadOnly)) {
                return copyFile(in, out, maxBandWidth, progress);
            } else {
                progress.append("Fout: Geen schrijf-toegang tot subBestemmingFile " + out.getPath());
                return false;
            }
        } else {
            //actie = vergelijk
            report("File " + in.getPath() + " (" + Routines.makeVolumeStr(in.length())
                    + ") moet gekopieerd worden naar " + out.getPath(), LOG_ONLY, progress);
            //assume the file can be copied
            r.size = r.size + in.length();
            return true;
        }
    }

    private String stripFirstComponent(String path) {
        int pos = path.indexOf('\\');
        if (pos > -1) {
            return path.substring(0, pos);
        } else {
            return "";
        }
    }

    private class MyFilter implements FileFilter {

        TaskLine or;

        MyFilter(TaskLine or) {
            this.or = or;
        }

        @Override
        public boolean accept(File file) {
            //filter against exclusions
            for (FilterSpec excludeFilter : excludeFiles) {
                if (excludeFilter.match(file.getPath())) {
                    return false;
                }
            }
            //filter on what files to include:
            //accept directories when continueWithSubdirectories is set
            //accept files and directories which match file.getPath
            //accept files when filterDirectories is set and match the path component before the file name
            return file.isDirectory() && or.continueWithSubdirectories()
                    || includeFilter.match(file.getPath())
                    || file.isFile() && includeFilter.filterDirectories && includeFilter.match(stripFirstComponent(file.getPath()));

        }
    }

    /**
     * Delete a file if the action is 'DO_SYNC' If the action is 'vergelijk'
     * just count the potential deletes.
     *
     * @param file
     * @param actie
     */
    private void deleteFile(File file, int actie) {
        r.sourceFilesToBeDeleted++;
        if (actie == DO_SYNC && file.delete()) {
            r.sourceFilesDeleted++;
        } else {
            LOGGER.log(Level.WARNING, "Fout: Kan bron file {0} niet verwijderen.", file);
            r.errorCount++;
        }
    }

    private void checkStops(String bron, String subBron) {
        if (r.stopByProgram == Result.STOP_IMMEDIATELY
                || r.stopByProgram == Result.STOP_AFTER_FILE
                || r.stopByProgram == Result.STOP_AFTER_DIRECTORY
                || r.stopByUser == Result.STOP_IMMEDIATELY
                || r.stopByUser == Result.STOP_AFTER_FILE) {
            r.stopLocation = subBron;
            return;
        }
        if (MainFrame.stopNaDirectory) {
            r.stopByUser = Result.STOP_AFTER_DIRECTORY;
            r.stopLocation = subBron;
            return;
        }
        if (MainFrame.stopNaBron) {
            r.stopByUser = Result.STOP_AFTER_SOURCE;
            r.stopLocation = bron;
        }
    }

    private void noDestinationExists(File fileArrayItem, Mapping m, int actie, String subBestemming, JTextArea progress) {
        String bron = m.bron;
        File subBestemmingFile = new File(subBestemming);
        report("Bestemming " + subBestemming + " bestaat niet", DEBUG_INFO, progress);
        r.fileCountNewFile++;
        if (!checkDataLimit(fileArrayItem, bron, m.dataLimit, progress)) {
            return;
        }
        if (processFile(fileArrayItem, subBestemmingFile, actie, m.maxBandWidth, progress)) {
            r.fileCountNewFileSynct++;
            if (m.verwijderBron) {
                deleteFile(fileArrayItem, actie);
            }
        }
    }

    // Note: file.lastModified() returns a long value representing the time the file was last modified, 
    // measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970), or 0L if the file does not exist or if an I/O error occurs.
    // A higher value means more recent.
    // As Samba has a accuracy of 1 second use a 2 second window for determining 'equal' time stamps
    private void destinationExists(File fileArrayItem, Mapping m, int actie, String subBron, String subBestemming, JTextArea progress) {
        String bron = m.bron;
        File subBestemmingFile = new File(subBestemming);
        if (Routines.canAccessDirOrReadFromFile(subBestemmingFile, "Bestemming")) {
            if (fileArrayItem.lastModified() - subBestemmingFile.lastModified() > 1000L) {
                //Bron minimaal 1000 mS nieuwer dan Bestemming
                report("Bron " + subBron + " is NIEUWER dan Bestemming " + subBestemming, DEBUG_INFO, progress);
                r.fileCountSourceNewer++;
                if (!checkDataLimit(fileArrayItem, bron, m.dataLimit, progress)) {
                    return;
                }
                if (processFile(fileArrayItem, subBestemmingFile, actie, m.maxBandWidth, progress)) {
                    r.fileCountSourceNewerSynct++;
                    if (m.verwijderBron) {
                        deleteFile(fileArrayItem, actie);
                    }
                }
            } else if (subBestemmingFile.lastModified() - fileArrayItem.lastModified() > 1000L) {
                //Bron minimaal 1000 mS ouder dan bestemming
                report("Bron " + subBron + " is " + (subBestemmingFile.lastModified() - fileArrayItem.lastModified())
                        + " miliseconden OUDER dan Bestemming " + subBestemming, WARNING, progress);
                r.fileCountDestNewer++;
                if (m.verwijderBron) {
                    deleteFile(fileArrayItem, actie);
                }
            } else {
                //Bron en Bestemming even oud binnen een window van 2000 mS
                report("Bron " + subBron + " is even oud als Bestemming " + subBestemming, DEBUG_INFO, progress);
                r.fileCountSameDateStamp++;
                if (m.verwijderBron) {
                    deleteFile(fileArrayItem, actie);
                }
            }
            if (r.readOnlyWasReset) {
                Routines.reapplyReadOnly(subBestemmingFile);
                r.readOnlyWasReset = false;
            }
        } else {
            report("Fout: Geen lees-toegang tot subBestemmingFile " + subBestemmingFile, SEVERE, progress);
            r.errorCount++;
        }
    }

    private void processFileOrDirectory(File fileArrayItem, Mapping m, int actie, String subBron, String subBestemming, JTextArea progress) {
        String bron = m.bron;
        File subBestemmingFile = new File(subBestemming);
        if (fileArrayItem.isFile()) {
            r.fileCount++;
            if (!subBestemmingFile.exists()) {
                noDestinationExists(fileArrayItem, m, actie, subBestemming, progress);
            } else {
                destinationExists(fileArrayItem, m, actie, subBron, subBestemming, progress);
            }
        }
        //new Bron subdirectory
        if (fileArrayItem.isDirectory()) {
            //recursion here
            runRecursief(new Mapping(subBron, subBestemming, m), actie, progress);
            checkStops(bron, subBron);
        }
    }

    private void loopSelectedFiles(File[] fileArray, Mapping m, int actie, JTextArea progress) {
        String bron = m.bron;
        String bestemming = m.bestemming;
        for (File fileArrayItem : fileArray) {
            //process current Bron directory
            if (r.errorCount >= m.maxErrors) {
                report("STOP met Bron " + bron + ". De error limiet van " + m.maxErrors
                        + " is bereikt of overschreden", SEVERE, progress);
                r.stopByProgram = Result.STOP_IMMEDIATELY;
                return;
            }
            r.readOnlyWasReset = false;
            if (MainFrame.stopOnmiddellijk) {
                r.stopByUser = Result.STOP_IMMEDIATELY;
                return;
            }
            if (MainFrame.stopNaFile) {
                r.stopByUser = Result.STOP_AFTER_FILE;
                return;
            }
            String subBron = fileArrayItem.getPath();
            //calculate the name of the corresponding Bestemming
            String subBestemming;
            if (!m.dumpFiles) {
                subBestemming = bestemming + subBron.substring(bron.length());
            } else {
                subBestemming = map.bestemming + "\\" + fileArrayItem.getName();
            }

            if (Routines.canAccessDirOrReadFromFile(fileArrayItem, "Bron")) {
                processFileOrDirectory(fileArrayItem, m, actie, subBron, subBestemming, progress);
            } else {
                report("Fout: Geen lees-toegang tot subBron " + subBron, SEVERE, progress);
                //cannot read subBron
                r.errorCount++;
                if (subBron.length() > 255) {
                    report("Skip verdere verwerking van deze directory; bronnamen zijn te lang", SEVERE, progress);
                    return;
                }
            }
        }
    }

    /**
     * Process the files in a Bron and the directories (by recursion) Skip the
     * exceptions. Check for error limit and data limit Log progress Display
     * progress if debug is set
     *
     * @param m the Mapping to process
     * @param actie the action to perform (copy or compare)
     * @param progress the thread status window
     */
    private void runRecursief(Mapping m, int actie, JTextArea progress) {
        String bron = m.bron;
        String bestemming = m.bestemming;
        if (bestemming.compareTo(DataSync.EMPTYFIELD) != 0) {
            File bronDir = new File(bron);
            if (Routines.canAccessDirOrReadFromFile(bronDir, "Bron")) {
                report("Start met Bron " + bron + " en bestemming " + bestemming, INFO, progress);
                File[] fileArray = bronDir.listFiles(new MyFilter(this));
                loopSelectedFiles(fileArray, m, actie, progress);
            } else {
                report("Fout: Geen toegang tot Bron " + bronDir, SEVERE, progress);
                //cannot read Bron
                r.errorCount++;
            }
        }
    }

    /**
     * Synchronize this TaskLine Return immediately if the destination needs to
     * cleaned and this fails
     *
     * @param deleteSet Set of files to be deleted
     * @param progress the thread status window
     * @return the result
     */
    public Result sync(JTextArea progress) {
        r = new Result();
        if (map.maakBestemmingSchoon) {
            report("De directory " + map.bestemming + " wordt eerst schoongemaakt, even geduld a.u.b...", WARNING, progress);
            if (!Routines.cleanDirectory(new File(map.bestemming), r)) {
                report("Skip verdere verwerking van bron " + map.bron + "; kan de bestemming " + map.bestemming + " niet schoonmaken", SEVERE, progress);
                return r;
            }
        }
        r.stopLocation = "- niet van toepassing -";
        runRecursief(this.map, DO_SYNC, progress);
        return r;
    }

    /**
     * Compare this TaskLine
     *
     * @param deleteSet Set of files to be deleted
     * @param progress the thread status window
     * @return the result
     */
    public Result vergelijk(JTextArea progress) {
        r = new Result();
        r.stopLocation = "- niet van toepassing -";
        runRecursief(this.map, DO_COMPARE, progress);
        return r;
    }

    /**
     * Reconstruct the Bron or path + wildcard as entered in the input (mapping)
     * file
     *
     * @return the reconstructed string
     */
    public String getBronNameWithWildCard() {
        if (includeFilter.isSet()) {
            return includeFilter.pathWildCard;
        } else {
            return map.bron;
        }
    }

    /**
     * Implement Comparable
     *
     * @param o the object to compare to
     * @return a negative, 0, or positive value if o is smaller, equal or
     * greater than this object
     */
    @Override
    public int compareTo(Object o) {
        int mapcp = map.compareTo(((TaskLine) o).map);
        if (mapcp != 0) {
            return mapcp;
        } else {
            return includeFilter.compareTo(((TaskLine) o).getIncludeFilter());
        }
    }

    /**
     * Override stringsEqual, two TaskLines are considered equal as the Mappings
     * and the include filters are equal
     *
     * @param o the object to compare to
     * @return true when equal
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof TaskLine) {
            return compareTo(o) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.map != null ? this.map.hashCode() : 0);
        hash = 97 * hash + (this.includeFilter != null ? this.includeFilter.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Opdrachtregel with Bron " + map.bron + " and Bestemming " + map.bestemming
                + ", and include filter: " + includeFilter.toString();
    }
}
