package aplicacion.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bruno
 */
public class Receiver {

    private final List<TCPPacket> packages;

    /**
     * Class to manage packages, by sorting them while being received
     */
    public Receiver() {
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
        int sequenceNumber = packet.sequence;
        // search position backwards, to add higher id's to the end
        for (int i = packages.size() - 1; i >= 0; i--) {
            if (packages.get(i).sequence == sequenceNumber) {
                // package is already in the list
                return true;
            }
            if (packages.get(i).sequence > sequenceNumber) {
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
            resp.append(p.body.equals("_") ? " " : p.body);
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
        // validate if all packages have been received, by chacking sequense
        for (int i = 1; i < this.packages.size(); i++) {
            if (this.packages.get(i - 1).sequence + 1 != this.packages.get(i).sequence) {
                return false;
            }
        }
        // validate fragment flag
        return 0 == packages.get(packages.size() - 1).fragementFlag;
    }

}
