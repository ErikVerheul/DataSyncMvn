package nl.verheulconsultants.datasyncmvn;

import org.apache.log4j.Logger;

/**
 * File or directory filter specification
 */
public class FilterSpec implements Comparable {

    private static final Logger LOGGER = Logger.getLogger(FilterSpec.class.getName());
    //path+wildcard
    String pathWildCard;
    //path
    private final String wildCardPath;
    //wildcard
    private final String wildcard;
    boolean filterDirectories;
    boolean filterFiles;
    boolean continueWithSubdirectories;
    boolean ignoreCase;

    public FilterSpec(String pathWildCard, boolean filterDirectories, boolean filterFiles, boolean continueWithSubdirectories, boolean ignoreCase) {
        this.pathWildCard = pathWildCard;
        int pos = pathWildCard.lastIndexOf('\\');
        //split the path from the wildcard
        if (pos > -1) {
            wildCardPath = pathWildCard.substring(0, pos);
            wildcard = pathWildCard.substring(pos + 1);
        } else {
            wildCardPath = "";
            wildcard = pathWildCard;
        }
        this.filterDirectories = filterDirectories;
        this.filterFiles = filterFiles;
        this.continueWithSubdirectories = continueWithSubdirectories;
        this.ignoreCase = ignoreCase;
    }

    /**
     * @return true if a wildcard is set
     */
    public boolean isSet() {
        //XOR
        return filterDirectories ^ filterFiles;
    }

    /**
     * Check for wildcard overlap
     *
     * @param aFileWildcard a FileWildcard instance
     * @return true if paths are equal and overlap in the wildcard
     */
    boolean overlap(FilterSpec aFileWildcard) {
        if (Routines.stringsEqual(wildCardPath, aFileWildcard.wildCardPath, ignoreCase)) {
            return overlapDoubleComponent(aFileWildcard.wildcard);
        } else {
            return false;
        }
    }

    /**
     * Split the two wildcard names in two components if needed, then check for
     * overlap
     *
     * @param aWildcard a wildcard name eg ab??c*.ext
     * @return true if overlap
     */
    private boolean overlapDoubleComponent(String aWildcard) {
        int aWildCardDotPos = aWildcard.lastIndexOf('.');
        int wildCardDotPos = wildcard.lastIndexOf('.');
        if (aWildCardDotPos > -1 && wildCardDotPos > -1) {
            //compare before the dot
            return overlapSingleComponent(aWildcard.substring(0, aWildCardDotPos), wildcard.substring(0, wildCardDotPos))
                    // and compare after the dot
                    && overlapSingleComponent(aWildcard.substring(aWildCardDotPos + 1), wildcard.substring(wildCardDotPos + 1));
        } else {
            if (aWildCardDotPos > -1 && wildCardDotPos == -1) {
                return overlapSingleComponent(aWildcard.substring(0, aWildCardDotPos), wildcard);
            }
            if (aWildCardDotPos == -1 && wildCardDotPos > -1) {
                return overlapSingleComponent(aWildcard, wildcard.substring(0, wildCardDotPos));
            }
            //(aWildCardDotPos == -1 && wildCardDotPos == -1) no extentions to match
            return overlapSingleComponent(aWildcard, wildcard);
        }
    }

    private boolean scanWildcard(String s1Copy, String s2Copy) {
        int i = 0;
        while (i < wildcard.length() && i < s2Copy.length()) {
            char wc2 = s2Copy.charAt(i);
            char wc1 = s1Copy.charAt(i);
            if (wc2 != wc1 && wc2 != '*' && wc1 != '*' && wc2 != '?' && wc1 != '?') {
                return false;
            }
            if (wc2 == '*' || wc1 == '*') {
                return true;
            }
            i++;
        }
        //strings containing ? only match when they have same length (wildcards with * were handled above)
        if (s1Copy.length() == s2Copy.length()) {
            return true;
        } else {
            //check for trailing *
            if (s1Copy.length() > s2Copy.length()) {
                return s1Copy.charAt(i) == '*';
            }
            if (s2Copy.length() > s1Copy.length()) {
                return s2Copy.charAt(i) == '*';
            }
        }
        return false;
    }

    /**
     * Check for wildcard overlap for one component (name without extention, or
     * extention only)
     *
     * @param s1 wildcard 1
     * @param s2 wildcard 2
     * @return true if overlap
     */
    private boolean overlapSingleComponent(String s1, String s2) {
        String s1Copy = s1;
        String s2Copy = s2;
        if (ignoreCase) {
            s1Copy = s1Copy.toLowerCase();
            s2Copy = s2Copy.toLowerCase();
        }
        return scanWildcard(s1Copy, s2Copy);
    }

    /**
     * Match a file name with preceeding path with this Filter
     *
     * The pathWildCard can have 2 parts:
     *
     * before the dot (.) star(*) means: any string including dots allowed in a
     * UNC path question mark (?) means: any character allowed in a file name,
     * one * and many ? are allowed; one or more ? after a * are ignored
     *
     * after the dot (.) star(*) means: any single component of an UNC path (no
     * \ or . allowed) question mark (?) means: any character allowed in a file
     * name, one * and many ? are allowed; one or more ? after a * are ignored
     *
     * examples: abc.* abc*.* *.abc *.?bc ??abc*.* ??a*.* abc.? ??*.* abc. does
     * nothing on Windows abc is the same as abc.*
     *
     * @param pathFileName the file name with preceeding path
     * @return true if a match
     */
    public boolean match(String pathFileName) {
        if (MainFrame.debugMode) {
            LOGGER.info("match: this.isSet() , this.pathWildCard , pathFileName = "
                    + this.isSet() + " , " + this.pathWildCard + " , " + pathFileName);
        }
        if (this.isSet()) {
            String filePath;
            String fileName;
            // filterdirecories, just compare the start of the path with the wildcard
            if (filterDirectories) {
                return matchSingleComponent(pathFileName, this.pathWildCard);
            }

            //split the path from the file name
            int pos = pathFileName.lastIndexOf('\\');
            if (pos > -1) {
                filePath = pathFileName.substring(0, pos);
                fileName = pathFileName.substring(pos + 1);
            } else {
                filePath = "";
                fileName = pathFileName;
            }

            //calculate the strings to compare
            if (continueWithSubdirectories) {
                //if file path starts with wildcard path, subdirs of file path are allowed
                if (Routines.startsWith(filePath, wildCardPath, ignoreCase)) {
                    return matchDoubleComponent(fileName);
                } else {
                    return false;
                }
            } else {
                //file and wildcard path must be equal
                if (Routines.stringsEqual(filePath, wildCardPath, ignoreCase)) {
                    return matchDoubleComponent(fileName);
                } else {
                    return false;
                }
            }
        } else {
            //no filter was set
            return true;
        }
    }

    /**
     * Split the file name and the wildcard in two if needed, then match both
     *
     * @param fileName a file name eg abc.ext
     * @return true if a match
     */
    private boolean matchDoubleComponent(String fileName) {
        int sDotPos = fileName.lastIndexOf('.');
        int wildCardDotPos = wildcard.lastIndexOf('.');
        if (sDotPos > -1 && wildCardDotPos > -1) {
            //compare before the dot
            return matchSingleComponent(fileName.substring(0, sDotPos), wildcard.substring(0, wildCardDotPos))
                    // and compare after the dot
                    && matchSingleComponent(fileName.substring(sDotPos + 1), wildcard.substring(wildCardDotPos + 1));
        } else {
            if (sDotPos > -1 && wildCardDotPos == -1) {
                //all extentions are good
                return matchSingleComponent(fileName.substring(0, sDotPos), wildcard);
            }
            if (sDotPos == -1 && wildCardDotPos > -1) {
                //all extentions are good
                return matchSingleComponent(fileName, wildcard.substring(0, wildCardDotPos));
            }
            //(sDotPos == -1 && wildCardDotPos == -1) no extentions to match
            return matchSingleComponent(fileName, wildcard);
        }
    }

    /**
     * Match a component (name without extention or the extention only)
     *
     * @param s the string
     * @param wildcard the wildcard
     * @return true if a match
     */
    private boolean matchSingleComponent(String s, String wildcard) {
        if (MainFrame.debugMode) {
            LOGGER.info("s, wildcard = " + s + " , " + wildcard);
        }
        String wc = wildcard;
        String sCopy = s;
        if (ignoreCase) {
            sCopy = sCopy.toLowerCase();
            wc = wc.toLowerCase();
        }
        if (wc.startsWith("*")) {
            return true;
        }
        int i = 0;
        while (i < wc.length() && i < sCopy.length()) {
            char cs = sCopy.charAt(i);
            char cwc = wc.charAt(i);
            //characters are different
            if (cs != cwc) {
                //ignore remainder
                if (cwc == '*') {
                    return true;
                }
                //not corresponding to a ? in wildcard
                if (cwc != '?') {
                    return false;
                }
            }
            i++;
        }
        //strings containing ? only match when they have same length (wildcards with * were handled above)
        return wc.length() == sCopy.length();
    }

    /**
     * implement Comparable
     *
     * @param o the object to compare to
     * @return a negative, 0, or positive value if o is smaller, equal or
     * greater than this object
     */
    @Override
    public int compareTo(Object o) {
        FilterSpec x = (FilterSpec) o;
        if (ignoreCase) {
            return this.wildCardPath.compareToIgnoreCase(x.pathWildCard);
        } else {
            return this.wildCardPath.compareTo(x.pathWildCard);
        }
    }

    /**
     * Override stringsEqual, two FileWildcards are considered equal as the
     * wildCardPath\wildcard are equal
     *
     * @param o the object to compare to
     * @return true when equal
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof FilterSpec) {
            return Routines.stringsEqual(this.pathWildCard, ((FilterSpec) o).pathWildCard, ignoreCase);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.pathWildCard != null ? this.pathWildCard.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "File filter specification with path " + wildCardPath + " and wildcard " + wildcard;
    }
}
