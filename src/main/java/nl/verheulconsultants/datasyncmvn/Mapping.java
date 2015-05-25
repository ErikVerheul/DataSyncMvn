package nl.verheulconsultants.datasyncmvn;

/**
 *In de mapping tabel geeft de gebruiker aan welke bronnen met welke bestemmingen moeten worden gesynchroniseerd.
 *Per regel (tuple van bron en bestemming) worden bovendien nog een aantal parameters ingevuld.
 */
class Mapping implements Comparable {

    String bron;
    String bestemming;
    boolean maakBestemmingSchoon;
    boolean verwijderBron;
    // in Bytes/sec.
    long maxBandWidth; 
    // in Bytes
    long dataLimit; 
    int maxErrors;
    boolean dumpFiles;
    boolean ignoreCase;

    Mapping(String bron, String bestemming, boolean maakBestemmingSchoon, boolean verwijderBron, long maxBandWidth, long dataLimit,
            int maxErrors, boolean dumpFiles, boolean ignoreCase) {
        this.bron = bron;
        this.bestemming = bestemming;
        this.maakBestemmingSchoon = maakBestemmingSchoon;
        this.verwijderBron = verwijderBron;
        this.maxBandWidth = maxBandWidth;
        this.dataLimit = dataLimit;
        this.maxErrors = maxErrors;
        this.dumpFiles = dumpFiles;
        this.ignoreCase = ignoreCase;
    }

    /**
     * Use this constructor to create a Mapping from an existing one with a new Bron and Bestemming
     * @param bron the source file or directory name
     * @param bestemming the destination file or directory name
     * @param oldMapping an existing map
     */
    Mapping(String bron, String bestemming, Mapping oldMapping) {
        this.bron = bron;
        this.bestemming = bestemming;
        this.maakBestemmingSchoon = oldMapping.maakBestemmingSchoon;
        this.verwijderBron = oldMapping.verwijderBron;
        this.maxBandWidth = oldMapping.maxBandWidth;
        this.dataLimit = oldMapping.dataLimit;
        this.maxErrors = oldMapping.maxErrors;
        this.dumpFiles = oldMapping.dumpFiles;
        this.ignoreCase = oldMapping.ignoreCase;
    }

    /**
     * implement Comparable
     * @param o the object to compare to
     * @return a negative, 0, or positive value if o is smaller, equal or greater than this object
     */
    @Override
    public int compareTo(Object o) {
        Mapping x = (Mapping) o;
        int bronComparison;
        if (ignoreCase) {
            bronComparison = this.bron.compareToIgnoreCase(x.bron);
        } else {
            bronComparison = this.bron.compareTo(x.bron);
        }
        if (bronComparison != 0) {
            return bronComparison;
        }
        if (ignoreCase) {
            return x.bestemming.compareToIgnoreCase(this.bestemming);
        } else {
            return x.bestemming.compareTo(this.bestemming);
        }
    }

    /**
     * override equals, two Mappings are considered equal as both the Bron and Bestemming are equal
     * @param o the object to compare to
     * @return true when equal
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Mapping) {
            return compareTo(o) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.bron != null ? this.bron.hashCode() : 0);
        hash = 89 * hash + (this.bestemming != null ? this.bestemming.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Mapping from Bron " + bron + " to Bestemming " + bestemming;
    }
}