package aplicacion.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bruno
 */
public class OutputList {

    public List<DATAPackage> packages;
    public int window_size;
    private boolean canReceive;
    private int min, max;

    public OutputList() {
        this.packages = new ArrayList<>();
        this.window_size = 1;
        this.canReceive = true;
        this.min = 0;
        this.max = this.window_size;
    }

    /**
     * Receive packages and add them to list, no need to sort them since they
     * are already sorted
     *
     * @param pack to be added
     * @return true if it was added, or false, if is locked
     */
    public boolean addPackage(DATAPackage pack) {
        if (this.canReceive) {
            this.packages.add(pack);
        }
        return this.canReceive;
    }

    /**
     * Send messages stored in lists
     *
     * @param output socket
     * @param hostname ip address
     * @param port socket port
     * @param cLog log error messages
     */
    public void sendPackages(DatagramSocket output, InetAddress hostname, int port, ConsoleLogger cLog) {
        // TODO: raise flag and dont admit more packages
        while (this.min < this.packages.size()) {
            // lock imput packages 
            this.canReceive = false;
            // send package in range, while starting each timer
            for (int i = this.min; i < this.max; i++) {
                DATAPackage pck = this.packages.get(i);
                // if package has already been send, pass
                if (pck.isWaitingACK()) {
                    continue;
                }
                // package hasnt been send yet
                try {
                    this.packageSender(pck, output, hostname, port);
                    cLog.info("Send pack ID: " + pck.id);
                } catch (IOException ex) {
                    cLog.error("Package with ID: " + pck.id + " not sended");
                }
            }
            // verify ack to move window, or verify timeot of packages in range
            for (int i = this.min; i < this.max; i++) {
                DATAPackage pck = this.packages.get(i);
                // verify id any package has received an ack
                if (pck.hasACK()) {
                    // move window if packages can 
                    // TODO: move window, after validating packages doesn't lock moving
                    this.min += this.window_size;
                    this.max = (this.max <= this.packages.size()) ? (this.max + 1) : this.packages.size();
                    continue;
                }
                if (pck.timeOut()) {
                    try {
                        this.packageSender(pck, output, hostname, port);
                        cLog.info("Resending pack ID: " + pck.id);
                    } catch (IOException ex) {
                        cLog.error("while resending package with ID: " + pck.id);
                    }
                }
            }
        }
        // clear list for new packages, and accept more packages
        this.packages.clear();
        this.canReceive = true;
    }

    private void packageSender(DATAPackage pck, DatagramSocket output, InetAddress hostname, int port) throws IOException {
        // TODO: add delay
        pck.setACKwaiting(true);
        pck.statTimer();
        byte[] pckByte = pck.getBytes();
        DatagramPacket pack = new DatagramPacket(pckByte, pckByte.length, hostname, port);
        // send package with data, checksum, seq number, and datagramPacketdata
        output.send(pack);
    }

    public void receivedACK(int id) {
        for (int i = this.min; i < this.max; i++) {
            if (this.packages.get(i).id == id) {
                DATAPackage pck = this.packages.get(i);
                pck.setACKreceived(true);
                pck.setACKwaiting(false);
            }
        }
    }
}
