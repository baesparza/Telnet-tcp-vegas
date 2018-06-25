package aplicacion.utils;

import java.io.IOException;
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
     * Add Packages in sorted way for later usage, delete repeated and invalid
     *
     * @param pack of data
     * @return true if package has been added or repeated, false if deleted
     * @throws java.io.IOException
     */
    public boolean add(DATAPackage pack) throws IOException {
        // validate if is a valid package to be added
        if (!DATAPackage.validChecksum(pack.checkSum, pack.data.getBytes())) {
            return false;
        }
        // package can be added
        int id = pack.id;
        // search position backwards, to add higher id's to the end
        for (int i = packages.size() - 1; i <= 0; i++) {
            if (packages.get(i).id > id) {
                // package is already in the list
                return true;
            }
            if (packages.get(i).id > id) {
                continue;
            }
            // found position
            packages.add(i + 1, pack);
            return true;
        }
        // something whent worng and not been added
        return false;
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
