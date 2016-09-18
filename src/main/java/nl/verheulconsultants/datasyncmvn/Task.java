package nl.verheulconsultants.datasyncmvn;

import org.apache.log4j.Logger;
import static nl.verheulconsultants.datasyncmvn.Routines.stringsEqual;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.*;
import java.util.*;
import javax.swing.*;


/**
 * A List of TaskLines and some actions to perform.
 */
class Task {

    @SuppressWarnings("URF_UNREAD_FIELD")
    private static final Logger LOGGER = Logger.getLogger(Task.class.getName());
    private static final int AANTAL_VELDEN = 7;
    private static final int DO_SYNC = 1;
    private static final int DO_COMPARE = 2;
    private static final boolean IGNORE_CASE = DataSync.runningWindows;
    private final File mappingTabel;
    private final JTextArea statusBar;
    private final List<TaskLine> regels;
    //processLine
    private String bron;
    private String pathWildcard;
    private boolean filterDirectories;
    private boolean filterFiles;
    private boolean filterSubdirectories;
    private boolean dumpFiles;
    private String bestemming;
    private boolean maakBestemmingSchoon;
    private boolean verwijderBron;
    private long maxBandWidth;
    private long dataLimit;
    private int maxErrors;

    /**
     * Executes leesTabel to read and validate the mapping table. Reports any
     * errors in the main Windows statusbar. Stores the lines read in the
     * ArrayList named regels.
     *
     * @param mappingTabel the File containing the mapping table
     * @param statusBar the main Windows statusbar
     */
    Task(File mappingTabel, JTextArea statusBar) {
        this.mappingTabel = mappingTabel;
        this.statusBar = statusBar;
        regels = new ArrayList<>();
    }

    /**
     * Read and validate the mapping table Report any errors with line number in
     * the main window Do not check the accessibility of the Bron and Bestemming
     * when the the user selected not to (of-line validation)
     *
     * @return true if successful (no errors)
     */
    boolean leesTabel() {
        boolean allOK = true;
        statusBar.append("Start met inlezen en valideren mapping tabel\n");
        statusBar.append("WACHT op time-out onbekende \\\\server of \\\\domain\\DFS-share...\n");
        statusBar.update(statusBar.getGraphics());
        try (BufferedReader br = new BufferedReader(new FileReader(mappingTabel))) {
            boolean eof = false;
            int lineNr = 0;
            while (!eof) {
                String line = br.readLine();
                if (line == null) {
                    eof = true;
                } else {
                    lineNr++;
                    //skip empty and COMMENT lines
                    if (line.length() != 0 && !line.startsWith("!") && !processLine(line, lineNr)) {
                        allOK = false;
                    }
                }
            }
            statusBar.append("Gereed met inlezen en valideren mapping tabel\n");
        } catch (IOException e) {
            statusBar.append("Kan mapping tabel niet openen, fout = " + e.toString() + "\n");
            return false;
        }
        return allOK;
    }

    private boolean processOptions(int pos, String field, int lineNr) {
        boolean localLineOK = true;
        //check for any options set
        int pos2 = field.indexOf('/', pos);
        String option = "";
        if (pos2 > -1) {
            pathWildcard = field.substring(0, pos2).trim();
            option = field.substring(pos2 + 1).trim().toLowerCase();
            for (int i = 0; i < option.length(); i++) {
                char opt = option.charAt(i);
                switch (opt) {
                    case 'f':
                        filterFiles = true;
                        break;
                    case 'd':
                        filterDirectories = true;
                        break;
                    case 's':
                        filterSubdirectories = true;
                        break;
                //ignore '/' and spaces
                    case '/':
                    case ' ':
                        break;
                    default:
                        statusBar.append("Fout in regel " + lineNr
                                + ", onbekende optie: " + opt + " in " + option + "\n");
                        localLineOK = false;
                        break;
                }
            }
        }

        if (!filterFiles && !filterDirectories) {
            statusBar.append("Fout in regel " + lineNr
                    + ", optie voor 'filter files' (f) of 'filter directories' (d) ontbreekt in optie: "
                    + option + "\n");
            localLineOK = false;
        }
        if (filterFiles && filterDirectories) {
            statusBar.append("Fout in regel " + lineNr
                    + ", kies 'filter files' (f) of 'filter directories' (d), niet beide in optie: "
                    + option + "\n");
            localLineOK = false;
        }

        bron = field.substring(0, pos).trim();
        if (MainFrame.debugMode) {
            statusBar.append("File filter : " + field + " gevonden in regel " + lineNr + "\n");
        }
        return localLineOK;
    }

    private boolean processField1(String field, int lineNr) {
        boolean localLineOK = true;
        filterSubdirectories = false;
        bron = field.trim();
        if (!bron.startsWith(DataSync.EMPTYFIELD)) {
            //check for a wildcard at end of path
            int pos = field.lastIndexOf('\\');
            if ((field.indexOf('*') > pos || field.indexOf('?') > pos) && !processOptions(pos, field, lineNr)) {
                localLineOK = false;
            }

            if (Routines.simpleUNCcheck(bron)) {
                if (MainFrame.checkAccessToBronAndBestemming && !checkBronToegang(bron, lineNr)) {
                    localLineOK = false;
                }
            } else {
                statusBar.append("Fout in regel " + lineNr + ", Bron heeft geen UNC naam\n");
                localLineOK = false;
            }
        } else {
            statusBar.append("Fout in regel " + lineNr + ", Bron niet gespecificeerd\n");
            localLineOK = false;
        }
        return localLineOK;
    }

    private boolean processField2(String field, int lineNr) {
        boolean locaLineOK = true;
        bestemming = field.trim();
        if (!bestemming.startsWith(DataSync.EMPTYFIELD)) {
            //geen bestemming mag
            //check for an option at end of path
            int pos = field.indexOf('/');
            if (pos > -1) {
                bestemming = field.substring(0, pos).trim();
                //check for any options set
                String option = field.substring(pos + 1).trim().toLowerCase();
                for (int i = 0; i < option.length(); i++) {
                    char opt = option.charAt(i);
                    switch (opt) {
                        case 'd':
                            dumpFiles = true;
                            break;
                    //ignore '/' and spaces
                        case '/':
                        case ' ':
                            break;
                        default:
                            statusBar.append("Fout in regel " + lineNr
                                    + ", onbekende optie: " + opt + " in " + option + "\n");
                            locaLineOK = false;
                            break;
                    }
                }
                if (MainFrame.debugMode) {
                    statusBar.append("Optie : " + option + " gevonden in regel " + lineNr + "\n");
                }
            }

            if (Routines.simpleUNCcheck(bestemming)) {
                if (MainFrame.checkAccessToBronAndBestemming && !checkBestemmingToegang(bestemming, lineNr)) {
                    locaLineOK = false;
                }
            } else {
                statusBar.append("Fout in regel " + lineNr
                        + ", Bestemming heeft geen UNC naam\n");
                locaLineOK = false;
            }
        }

        if (Routines.stringsEqual(bron, bestemming, IGNORE_CASE) || Routines.startsWith(bestemming, bron, IGNORE_CASE)) {
            statusBar.append("Fout in regel " + lineNr
                    + ", Bestemming mag geen onderdeel zijn van de Bron\n");
            //mapping of a Bron to it self
            locaLineOK = false;
        }
        return locaLineOK;
    }

    private boolean processField3(String field, int lineNr) {
        boolean locaLineOK = true;
        try {
            int maakBestemmingSchoonInt = Integer.parseInt(field);
            switch (maakBestemmingSchoonInt) {
                case 1:
                    maakBestemmingSchoon = true;
                    break;
                case 0:
                    maakBestemmingSchoon = false;
                    break;
                default:
                    statusBar.append("Fout in regel " + lineNr
                            + ", toegestane waarden voor 'maak Bestemming Schoon' zijn true, waar, 1, false, onwaar, 0\n");
                    locaLineOK = false;
                    break;
            }
        } catch (NumberFormatException e) {
            maakBestemmingSchoon = field.compareToIgnoreCase("true") == 0 || field.compareToIgnoreCase("waar") == 0;
            if (!maakBestemmingSchoon
                    && !(field.compareToIgnoreCase("false") == 0 || field.compareToIgnoreCase("onwaar") == 0)) {
                statusBar.append("Fout in regel " + lineNr
                        + ", toegestane waarden voor 'maak Bestemming Schoon' zijn true, waar, 1, false, onwaar, 0\n");
                locaLineOK = false;
            }
        }
        return locaLineOK;
    }

    private boolean processField4(String field, int lineNr) {
        boolean locaLineOK = true;
        try {
            int verwijderBronInt = Integer.parseInt(field);
            switch (verwijderBronInt) {
                case 1:
                    verwijderBron = true;
                    break;
                case 0:
                    verwijderBron = false;
                    break;
                default:
                    statusBar.append("Fout in regel " + lineNr
                            + ", toegestane waarden voor 'verwijder Bron' zijn true, waar, 1, false, onwaar, 0\n");
                    locaLineOK = false;
                    break;
            }
        } catch (NumberFormatException e) {
            verwijderBron = field.compareToIgnoreCase("true") == 0 || field.compareToIgnoreCase("waar") == 0;
            if (!verwijderBron
                    && !(field.compareToIgnoreCase("false") == 0 || field.compareToIgnoreCase("onwaar") == 0)) {
                statusBar.append("Fout in regel " + lineNr
                        + ", toegestane waarden voor 'verwijder Bron' zijn true, waar, 1, false, onwaar, 0\n");
                locaLineOK = false;
            }
        }
        return locaLineOK;
    }

    private boolean processField5(String field, int lineNr) {
        boolean locaLineOK = true;
        try {
            int maxBandWidthMB = Integer.parseInt(field);
            if (maxBandWidthMB < 0) {
                statusBar.append("Fout in regel " + lineNr
                        + ", 'maxBandWidth' moet groter gelijk 0 zijn\n");
                locaLineOK = false;
            } else {
                // convert to Bytes/s.
                maxBandWidth = maxBandWidthMB * 1_000_000L;
            }
        } catch (NumberFormatException e) {
            statusBar.append("Fout in regel " + lineNr
                    + ", veld 'maxBandWidth' bevat geen geldig getal\n");
            locaLineOK = false;
        }
        return locaLineOK;
    }

    private boolean processField6(String field, int lineNr) {
        boolean locaLineOK = true;
        if (!field.startsWith(DataSync.EMPTYFIELD)) {
            try {
                dataLimit = Long.parseLong(field) * 1_000_000;
                if (dataLimit < 0) {
                    statusBar.append("Fout in regel " + lineNr
                            + ", 'data limiet' moet groter gelijk 0 zijn\n");
                    locaLineOK = false;
                }
            } catch (NumberFormatException e) {
                statusBar.append("Fout in regel " + lineNr
                        + ", veld 'data limiet' bevat geen geldig getal\n");
                locaLineOK = false;
            }
        } else {
            dataLimit = Long.MAX_VALUE;
        }
        return locaLineOK;
    }

    private boolean processField7(String field, int lineNr) {
        boolean locaLineOK = true;
        if (!field.startsWith(DataSync.EMPTYFIELD)) {
            try {
                maxErrors = Integer.parseInt(field);
                if (maxErrors < 1) {
                    statusBar.append("Fout in regel " + lineNr
                            + ", 'max errors' moet groter gelijk 1 zijn\n");
                    locaLineOK = false;
                }
            } catch (NumberFormatException e) {
                statusBar.append("Fout in regel " + lineNr
                        + ", veld 'max errors' bevat geen geldig getal\n");
                locaLineOK = false;
            }
        } else {
            maxErrors = Integer.MAX_VALUE;
        }
        return locaLineOK;
    }

    private boolean createAndCheckTaskLine(int lineNr) {
        boolean locaLineOK = true;
        Mapping map = new Mapping(bron, bestemming, maakBestemmingSchoon, verwijderBron, maxBandWidth, dataLimit,
                maxErrors, dumpFiles, IGNORE_CASE);
        FilterSpec fspec = new FilterSpec(pathWildcard, filterDirectories, filterFiles, filterSubdirectories,
                IGNORE_CASE);
        TaskLine taskLine = new TaskLine(map, fspec);
        int f = checkOnErrorsAddExceptions(taskLine);
        if (f == 0) {
            regels.add(taskLine);
        } else {
            if (f == 1) {
                statusBar.append("Fout in regel " + lineNr
                        + ", Bron komt overeen met een voorgaande Bron. Twee gelijke bronnen zijn niet toegestaan\n");
            }
            if (f == 2) {
                statusBar.append("Fout in regel " + lineNr
                        + ", file filter heeft overlap met ander file filter\n");
            }
            locaLineOK = false;
        }
        return locaLineOK;
    }

    private boolean processField(String field, int fieldNr, int lineNr) {
        boolean locaLineOK = true;
        switch (fieldNr) {
            case 1:
                if (!processField1(field, lineNr)) {
                    locaLineOK = false;
                }
                break;
            case 2:
                if (!processField2(field, lineNr)) {
                    locaLineOK = false;
                }
                break;
            case 3:
                if (!processField3(field, lineNr)) {
                    locaLineOK = false;
                }
                break;
            case 4:
                if (!processField4(field, lineNr)) {
                    locaLineOK = false;
                }
                break;
            case 5:
                if (!processField5(field, lineNr)) {
                    locaLineOK = false;
                }
                break;
            case 6:
                if (!processField6(field, lineNr)) {
                    locaLineOK = false;
                }
                break;
            case 7:
                if (!processField7(field, lineNr)) {
                    locaLineOK = false;
                }
                break;
            default:
                //do nothing
                break;
        }
        return locaLineOK;
    }

    /**
     * Process (validate) one line from the mapping table Report any errors in
     * the main window
     *
     * @param line the line
     * @param lineNr line number
     * @return true if no errors
     */
    private boolean processLine(String line, int lineNr) {
        bron = null;
        pathWildcard = "";
        filterDirectories = false;
        filterFiles = false;
        filterSubdirectories = false;
        dumpFiles = false;
        bestemming = null;
        maakBestemmingSchoon = false;
        verwijderBron = false;
        maxBandWidth = 0;
        dataLimit = 0;
        maxErrors = 0;
        //line must start with a \\ delimeter
        if (!line.startsWith("\\\\")) {
            return false;
        }

        StringTokenizer st = new StringTokenizer(line, DataSync.DELIMITER, false);
        // line must have at least 7 fields
        if (st.countTokens() < AANTAL_VELDEN) {
            return false;
        }

        String field;
        int fieldNr = 0;
        boolean lineOK = true;
        while (st.hasMoreTokens() && fieldNr <= AANTAL_VELDEN) {
            field = st.nextToken().trim();
            if (MainFrame.debugMode) {
                statusBar.append("Ingelezen veld: " + field + " van regel " + lineNr + "\n");
            }
            if (!field.startsWith(DataSync.COMMENT)) {
                fieldNr++;
                if (!processField(field, fieldNr, lineNr)) {
                    lineOK = false;
                }
            }
        }//while
        if (fieldNr != AANTAL_VELDEN) {
            statusBar.append("Fout in regel " + lineNr
                    + ", het aantal velden is ongelijk " + AANTAL_VELDEN + "\n");
            lineOK = false;
        } else {
            if (!createAndCheckTaskLine(lineNr)) {
                lineOK = false;
            }
        }
        return lineOK;
    }

    /**
     * Seek and store exceptions and filters found in this line in the
     * appropriate TaskLine(s) A Bron A which is a subdirectory of Bron B
     * overrules B for the files in A A wildcard to Bron A which is a
     * subdirectory of Bron B overrules B for the files specified with the
     * wildcard A overrule means that the files are copied to the overruled
     * destination, NOT to the original destination.
     *
     * examples: 1: nieuw is \\share\dir1\dir2, any previous TaskLine x is
     * \\share\dir1 then nieuw is a subdirectory from x add nieuw.bron to the
     * Uitzondering set from x to exclude these files from copying with x 2:
     * nieuw is \\share\dir1, any previous TaskLine x is \\share\dir1\dir2 then
     * x is a subdirectory from nieuw add x.bron to the Uitzondering set of
     * nieuw to exclude these files from copying with nieuw 3: nieuw is
     * \\share\dir1\dir2\abc?.*, any previous TaskLine x is \\share\dir1 then
     * the file filter nieuw overlaps x add dir2\abc?.* as wildcard to the
     * excludeFiles set of x to prevent these files from copying with x 4: nieuw
     * is \\share\dir1, any previous TaskLine x is \\share\dir1\dir2\abc?.* then
     * the file filter x overlaps nieuw add dir2\abc?.* as wildcard to the
     * excludeFiles set of nieuw to prevent these files from copying with nieuw
     * 5: nieuw is \\share\dir1\dir2\abc?.*, any previous TaskLine x is
     * \\share\dir1\abcd.* / FS then the file filters overlap add dir2\abc?.* as
     * wildcard to the excludeFiles set of x to prevent these files from copying
     * with x 6: nieuw is \\share\dir1\abcd.* / FS, any previous TaskLine x is
     * \\share\dir1\dir2\abc?.* then the file filters overlap add dir2\abc?.* as
     * wildcard to the excludeFiles set of nieuw to prevent these files from
     * copying with nieuw
     *
     * Note: Two wildcards to the same Bron must not overlap.
     *
     * Check for multiple entries of the same Bron (not allowed) Check for
     * mappings of a Bron to it self (not allowed)
     *
     * @param nieuw a TaskLine
     * @return 2 if filefilter has overlap with other filter, 1 if a Bron double
     * is found; 0 for no errors
     */
    private int checkOnErrorsAddExceptions(TaskLine nieuw) {
        String nieuwBron = nieuw.getBron();
        TaskLine x;
        Iterator it = regels.iterator();
        while (it.hasNext()) {
            x = (TaskLine) it.next();
            String xBron = x.getBron();
            if (stringsEqual(xBron, nieuwBron, IGNORE_CASE) && x.getIncludeFilter().equals(nieuw.getIncludeFilter())) {
                //double found
                return 1;
            }
            //check for overlap in wildcards
            if (nieuw.isFilter() && x.isFilter() && nieuw.getIncludeFilter().overlap(x.getIncludeFilter())) {
                //overlap in file filters
                return 2;
            }
            addFilterSpecs(xBron, nieuwBron, nieuw, x);
        }
        //no errors        
        return 0;
    }

    private void addFilterSpecs(String xBron, String nieuwBron, TaskLine nieuw, TaskLine x) {
        //prevent files to be copied more than once; xBron is subdir van nieuwBron
        if (Routines.startsWith(xBron, nieuwBron, IGNORE_CASE)) {
            if (!x.isFilter()) {
                //add x.bron to the excludeFiles set of nieuw, see example 2
                nieuw.addFilterSpec(new FilterSpec(xBron, true, false, true, IGNORE_CASE));
            } else if (!nieuw.isFilter() || nieuw.continueWithSubdirectories()) {
                //add this wildcard to TaskLine nieuw excludeFiles set, see example 4,6                    
                nieuw.addFilterSpec(x.getIncludeFilter());
            }
            //nieuwBron is subdir van xBron                
        } else if (Routines.startsWith(nieuwBron, xBron, IGNORE_CASE)) {
            if (!nieuw.isFilter()) {
                //add this Bron to TaskLine x excludeFiles set, see example 1                    
                x.addFilterSpec(new FilterSpec(nieuwBron, true, false, true, IGNORE_CASE));
            } else if (!x.isFilter() || x.continueWithSubdirectories()) {
                //add this wildcard to TaskLine x excludeFiles set, see example 3,5                    
                x.addFilterSpec(nieuw.getIncludeFilter());
            }
        }
    }

    /**
     * Check if a Bron is accessable
     *
     * @param bron the Bron
     * @param lineNr the line number of the TaskLine
     * @return true if successful
     */
    private boolean checkBronToegang(String bron, int lineNr) {
        statusBar.append("Test bereikbaarheid bron " + bron + "\n");
        File dir = new File(bron);
        if (dir.exists() && dir.isDirectory()) {
            if (!Routines.canAccessDir(dir, "Bron")) {
                statusBar.append("Fout in regel " + lineNr + ", geen toegang tot Bron directory " + dir.getPath() + "\n");
                return false;
            }
        } else {
            statusBar.append("Fout in regel " + lineNr + ", deze Bron is geen directory of bestaat niet " + dir.getPath() + "\n");
            return false;
        }
        return true;
    }

    /**
     * Check if a Bron is accessable
     *
     * @param bestemming the Bestemming
     * @param lineNr the line number of the TaskLine
     * @return true if successful
     */
    private boolean checkBestemmingToegang(String bestemming, int lineNr) {
        if (bestemming.compareTo(DataSync.EMPTYFIELD) != 0) {
            statusBar.append("Test bereikbaarheid bestemming " + bestemming + "\n");
            File dir = new File(bestemming);
            if (dir.exists() && dir.isDirectory()) {
                if (!Routines.canWriteInDir(dir, "Bestemming")) {
                    statusBar.append("Fout in regel " + lineNr + ", kan niet schrijven in Bestemming directory " + dir.getPath() + "\n");
                    return false;
                }
            } else {
                statusBar.append("Fout in regel " + lineNr + ", deze Bestemming is geen directory of bestaat niet " + dir.getPath() + "\n");
                return false;
            }
        }
        return true;
    }

    private Result doCompare(JTextArea progressWindow, TaskLine x) {
        LOGGER.info("Start vergelijking tussen Bron " + x.getBronNameWithWildCard() + " en Bestemming " + x.getBestemming());
        progressWindow.append("Start vergelijking tussen Bron " + x.getBronNameWithWildCard() + " en Bestemming " + x.getBestemming());
        Result r = x.vergelijk(progressWindow);
        String msg = "Gereed met Bron " + x.getBronNameWithWildCard() + ". Aantal files vergeleken " + r.fileCount
                + ". Te kopieren volume " + Routines.makeVolumeStr(r.size) + ".  ";
        msg = msg + "Er zijn " + r.errorCount + " fouten gevonden";
        String s1 = "Nieuwe bron files: " + r.fileCountNewFile
                + ", waarvan mogelijk te synchroniseren: " + r.fileCountNewFileSynct;
        String s2 = "Bron files nieuwer dan bestemming: " + r.fileCountSourceNewer
                + ", waarvan mogelijk te synchroniseren: " + r.fileCountSourceNewerSynct;
        String s3 = "Bestemming files nieuwer dan bron: " + r.fileCountDestNewer;
        String s4 = "Aantal te verwijderen bron files: " + r.sourceFilesToBeDeleted;
        LOGGER.info(msg);
        LOGGER.info(s1 + ", " + s2 + ", " + s3 + ", " + s4);
        progressWindow.append(msg + "\n     " + s1 + "\n     " + s2 + "\n     " + s3 + "\n     " + s4 + "\n");
        return r;
    }

    private Result doSync(JTextArea progressWindow, TaskLine x) {
        LOGGER.info("Start synchronisatie tussen Bron " + x.getBronNameWithWildCard() + " en Bestemming " + x.getBestemming());
        progressWindow.append("Start synchronisatie tussen Bron " + x.getBronNameWithWildCard() + " en Bestemming " + x.getBestemming());
        Result r = x.sync(progressWindow);
        String msg = "Gereed met Bron " + x.getBronNameWithWildCard() + ". Aantal files vergeleken " + r.fileCount
                + ". Gekopieerd volume " + Routines.makeVolumeStr(r.size) + ".  ";
        msg = msg + "Er zijn " + r.errorCount + " fouten gevonden";
        String s1 = "Nieuwe bron files: " + r.fileCountNewFile
                + ", waarvan gesynchroniseerd: " + r.fileCountNewFileSynct;
        String s2 = "Bron files nieuwer dan bestemming: " + r.fileCountSourceNewer
                + ", waarvan gesynchroniseerd: " + r.fileCountSourceNewerSynct;
        String s3 = "Bestemming files nieuwer dan bron: " + r.fileCountDestNewer;
        String s4 = "Aantal verwijderde bron files: " + r.sourceFilesDeleted;
        String s5 = "Vooraf verwijderde bestemming files en subdirectories: " + r.targetFilesDeleted;
        LOGGER.info(msg);
        LOGGER.info(s1 + ", " + s2 + ", " + s3 + ", " + s4 + ", " + s5);
        progressWindow.append(msg + "\n     " + s1 + "\n     " + s2 + "\n     " + s3 + "\n     " + s4 + "\n     " + s5 + "\n");
        return r;
    }

    /**
     * Process the entire mapping table
     *
     * @param actie do_sync or do_vergelijk
     * @param deleteSet Set of Bronnen to be deleted
     * @param progressWindow the thread status window
     * @return the Result record
     */
    private Result process(int actie, JTextArea progressWindow) {
        TaskLine x;
        Result rCumm = new Result();
        Iterator it = regels.iterator();
        //stopByUserNaBron catched here
        while (rCumm.stopByUser == Result.NOT_STOPPED && it.hasNext()) {
            //an empty but valid result
            Result r = new Result();
            x = (TaskLine) it.next();
            if (x.getBestemming().compareTo(DataSync.EMPTYFIELD) != 0) {
                if (actie == DO_COMPARE) {
                    r = doCompare(progressWindow, x);
                }
                if (actie == DO_SYNC) {
                    r = doSync(progressWindow, x);
                }

                rCumm.fileCount += r.fileCount;
                rCumm.size += r.size;
                rCumm.fileCountDestNewer += r.fileCountDestNewer;
                rCumm.fileCountNewFile += r.fileCountNewFile;
                rCumm.fileCountNewFileSynct += r.fileCountNewFileSynct;
                rCumm.fileCountSameDateStamp += r.fileCountSameDateStamp;
                rCumm.fileCountSourceNewer += r.fileCountSourceNewer;
                rCumm.fileCountSourceNewerSynct += r.fileCountSourceNewerSynct;
                rCumm.sourceFilesToBeDeleted += r.sourceFilesToBeDeleted;
                rCumm.sourceFilesDeleted += r.sourceFilesDeleted;
                rCumm.targetFilesToBeDeleted += r.targetFilesToBeDeleted;
                rCumm.targetFilesDeleted += r.targetFilesDeleted;
                rCumm.stopByProgram = r.stopByProgram;
                rCumm.stopByUser = r.stopByUser;
                rCumm.stopLocation = r.stopLocation;
                rCumm.errorCount += r.errorCount;
                rCumm.reportLinesCount += r.reportLinesCount;
            }
        }
        return rCumm;
    }

    /**
     * Do the 'vergelijk' action
     *
     * @param progressWindow the thread status window
     * @return the Result record
     */
    Result vergelijk(JTextArea progressWindow) {
        progressWindow.append("Vergelijking is gestart\n"
                + "Let op: de vergelijking gaat er vanuit dat bij de synchronisatie: \n"
                + "- de creatie van nieuwe directories mogelijk is of\n"
                + "- bestaande bestemming directories schrijfrechten hebben.\n"
                + "Bij fouten tijdens de synchronisatie zal het aantal gekopieerde files/bytes afwijken van de prognose uit deze vergelijking\n");
        return process(DO_COMPARE, progressWindow);
    }

    /**
     * Do the 'copy' action
     *
     * @param progressWindow the thread status window
     * @return the Result record
     */
    Result sync(JTextArea progressWindow) {
        progressWindow.append("Synchronisatie is gestart");
        return process(DO_SYNC, progressWindow);
    }
}
