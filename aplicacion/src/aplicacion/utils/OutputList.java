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

    /**
     * TODO: manipulate windows size, Control packages, and resend as needed
     */
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
        while (this.min < this.packages.size()) {
            // lock imput packages 
            this.canReceive = false;
            // send package in range, while starting each timer
            for (int i = this.min; i < this.max; i++) {
                DATAPackage pck = this.packages.get(i);
                // if package has already been send, pass
                if (pck.isWaitingACK() || pck.hasACK()) {
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
            int tempMin = this.min, tempMax = this.max;
            for (int i = tempMin; i < tempMax; i++) {
                DATAPackage pck = this.packages.get(i);
                /// move window
                if (this.packages.get(i).hasACK() && i == this.min) {
                    // can move window
                    this.min += this.window_size;
                    if (this.max <= this.packages.size()) {
                        this.max++;
                    }
                    continue;
                }
                // package hasnt been validated yet
                if (pck.timeOut()) {
                    try {
                        this.packageSender(pck, output, hostname, port);
                        cLog.warning("Timeout, resending pack ID: " + pck.id);
                    } catch (IOException ex) {
                        cLog.error("while resending package with ID: " + pck.id);
                    }
                }
            }
        }
        // clear list for new packages, and accept more packages
        this.packages.clear();
        this.canReceive = true;
        this.min = 0;
        this.max = this.window_size = 1;
    }

    private void packageSender(DATAPackage pck, DatagramSocket output, InetAddress hostname, int port) throws IOException {
        // TODO: fix delay
        pck.setACKwaiting(true);
        pck.statTimer();
        byte[] pckByte = pck.getBytes();
        DatagramPacket pack = new DatagramPacket(pckByte, pckByte.length, hostname, port);
        // send package with data, checksum, seq number, and datagramPacketdata
        output.send(pack);
    }

    public void receivedACK(int id) {
        if (this.packages.isEmpty() || this.packages.size() < id) {
            return;
        }
        DATAPackage pck = this.packages.get(id);
        pck.setACKreceived(true);
        pck.setACKwaiting(false);
    }
}
