package aplicacion.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bruno
 */
public class Receiver {

    private final List<TCPPacket> packets;

    /**
     * Class to manage packages, by sorting them while being received
     */
    public Receiver() {
        this.packets = new ArrayList<>();
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
        for (int i = this.packets.size() - 1; i >= 0; i--) {
            if (this.packets.get(i).sequence == sequenceNumber) {
                // package is already in the list
                return true;
            }
            if (this.packets.get(i).sequence > sequenceNumber) {
                continue;
            }
            // found position
            this.packets.add(i + 1, packet);
            return true;
        }
        // position not found, add to end
        this.packets.add(packet);
        return true;
    }

    /**
     * Clear for new input
     */
    public void clear() {
        this.packets.clear();
    }

    /**
     * Getter for full message stored in array
     *
     * @return string with sentence
     */
    public String getMessage() {
        StringBuilder resp = new StringBuilder();
        for (TCPPacket p : packets) {
            String s = p.body.trim();
            resp.append(s.equals("_") ? " " : s);
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
        if (packets.isEmpty()) {
            // no packages
            return false;
        }
        // validate if all packages have been received, by chacking sequense
        for (int i = 1; i < this.packets.size(); i++) {
            if (this.packets.get(i - 1).sequence + 1 != this.packets.get(i).sequence) {
                return false;
            }
        }
        // validate fragment flag
        return 0 == packets.get(packets.size() - 1).fragementFlag;
    }

    

}
