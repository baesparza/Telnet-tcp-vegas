package aplicacion.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bruno
 */
public class ListPackages {

    private List<DATAPackage> packages;

    /**
     * Class to manage packages, by sorting them while being received
     */
    public ListPackages() {
        this.packages = new ArrayList<>();
    }

    /**
     * TODO: add verification of checksum and deletion of repeated ............
     * TODO: return boolean for controlling packages included or deleted
     * Add packages in sorted way for later usage, delete repeated and invalid 
     *
     * @param pack of data
     */
    public void add(DATAPackage pack) {
        int id = pack.id;
        if (id == packages.size()) {
            // the package is in order, can be added to list
            packages.add(pack);
            return;
        }
        // search position backwards
        for (int i = packages.size() - 1; i <= 0; i++) {
            if (packages.get(i).id > id) {
                continue;
            }
            // found position
            packages.add(i + 1, pack);
            return;
        }
    }

    /**
     * Clear for new input
     */
    public void clear() {
        this.packages.clear();
    }

    /**
     * Getter for full message stored in array
     *
     * @return string with sentence
     */
    public String getMessage() {
        StringBuilder resp = new StringBuilder();
        for (DATAPackage p : packages) {
            resp.append(p.data).append(" ");
        }
        return resp.toString();
    }

    /**
     * Verify if all packages have been received by checking last package
     * received
     *
     * @return boolean depending on fragment flag
     */
    public boolean hasEnded() {
        if (packages.isEmpty()) {
            // no packages
            return false;
        }
        return 0 == packages.get(packages.size() - 1).fragement;
    }

    /**
     * TODO: add segment package.............................................
     * Get last from a segment package from the array
     *
     * @return
     */
    public DATAPackage getLastPackage() {
        if (packages.isEmpty()) {
            // no packages
            return null;
        }
        return packages.get(packages.size() - 1);
    }

}
