package aplicacion.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bruno
 */
public class InputList {

    private final List<TCPPacket> packages;

    /**
     * Class to manage packages, by sorting them while being received
     */
    public InputList() {
        this.packages = new ArrayList<>();
    }

    /**
     * Add Packages in sorted way for later usage, delete repeated and invalid
     *
     * @param packet of data
     * @return true if package has been added or repeated, false if deleted
     */
    public boolean add(TCPPacket packet) {
        // validate if is a valid package to be added
        if (!TCPPacket.validChecksum(packet.checksum, packet.body)) {
            return false;
        }
        // package can be added
        int sequenceNumber = packet.sequenceNumber;
        // search position backwards, to add higher id's to the end
        for (int i = packages.size() - 1; i >= 0; i--) {
            if (packages.get(i).sequenceNumber == sequenceNumber) {
                // package is already in the list
                return true;
            }
            if (packages.get(i).sequenceNumber > sequenceNumber) {
                continue;
            }
            // found position
            packages.add(i + 1, packet);
            return true;
        }
        // position not found, add to end
        packages.add(packet);
        return true;
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
        for (TCPPacket p : packages) {
            resp.append(p.body);
        }
        return resp.toString();
    }

    /**
     * TODO: validate with id if all packages have been received ...............
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
        return 0 == packages.get(packages.size() - 1).fragementNumber;
    }

    /**
     * TODO: return last segment package ......................................
     * Get last from a segment package from the array
     *
     * @return
     */
    public TCPPacket getLastPackage() {
        if (packages.isEmpty()) {
            // no packages
            return null;
        }
        return packages.get(packages.size() - 1);
    }

}
