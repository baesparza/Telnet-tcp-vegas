package aplicacion.utils;

import aplicacion.Client.ConsoleLogger;
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

    private final List<TCPPacket> packets;
    private int window_size;
    private boolean canReceive;
    private int min, max;

    /**
     * TODO: manipulate windows size, Control packages, and resend as needed
     */
    public OutputList() {
        this.window_size = 1;
        this.canReceive = true;
        this.min = 0;
        this.max = this.window_size;
        this.packets = new ArrayList<>();
    }

    /**
     * Receive packages and add them to list, no need to sort them since they
     * are already sorted
     *
     * @param packet to be added
     * @return true if it was added, or false, if is locked
     */
    public boolean addPackage(TCPPacket packet) {
        if (this.canReceive) {
            this.packets.add(packet);
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
        for (int i = 0; i < this.packets.size(); i++) {
            TCPPacket pck = this.packets.get(i);
            try {
                this.packageSender(pck, output, hostname, port);
                cLog.info("Send pack sequence: " + pck.sequenceNumber);
            } catch (IOException ex) {
                cLog.error("Package with sequence: " + pck.sequenceNumber + " not sended");
            }
        }
        /*
        while (this.min < this.packets.size()) {
            // lock imput packages 
            this.canReceive = false;
            // send package in range, while starting each timer
            for (int i = this.min; i < this.max; i++) {
                TCPPacket pck = this.packets.get(i);
                // if package has already been send, pass
                if (pck.isWaitingACK() || pck.hasACK()) {
                    continue;
                }
                // package hasnt been send yet
                try {
                    this.packageSender(pck, output, hostname, port);
                    cLog.info("Send pack sequence: " + pck.sequenceNumber);
                } catch (IOException ex) {
                    cLog.error("Package with sequence: " + pck.sequenceNumber + " not sended");
                }
            }
            // verify ack to move window, or verify timeot of packages in range
            int tempMin = this.min, tempMax = this.max;
            for (int i = tempMin; i < tempMax; i++) {
                TCPPacket pck = this.packets.get(i);
                /// move window
                if (this.packets.get(i).hasACK() && i == this.min) {
                    // can move window
                    this.min += this.window_size;
                    if (this.max <= this.packets.size()) {
                        this.max++;
                    }
                    continue;
                }
                // package hasnt been validated yet
                if (pck.timeOut()) {
                    try {
                        this.packageSender(pck, output, hostname, port);
                        cLog.warning("Timeout, resending pack sequence: " + pck.sequenceNumber);
                    } catch (IOException ex) {
                        cLog.error("while resending package with sequence: " + pck.sequenceNumber);
                    }
                }
            }
        }
        // clear list for new packages, and accept more packages
        this.packets.clear();
        this.canReceive = true;
        this.min = 0;
        this.max = this.window_size = 1;
         */
    }

    /**
     * Send package to server
     *
     * @param packet to be sent
     * @param output socket
     * @param hostname of server
     * @param port on server
     * @throws IOException when socket fails
     */
    private void packageSender(TCPPacket packet, DatagramSocket output, InetAddress hostname, int port) throws IOException {
        // TODO: fix delay
        byte[] packetData = packet.getHeader().getBytes();
        DatagramPacket pack = new DatagramPacket(packetData, packetData.length, hostname, port);
        output.send(pack);
        System.out.println(new String(pack.getData()));
        packet.setACKwaiting(true);
        packet.statTimer();
    }

    public void receivedACK(int sequenceNumber) {

        if (this.packets.isEmpty() || this.packets.size() < sequenceNumber) {
            return;
        }
        TCPPacket pck = this.packets.get(sequenceNumber);
        pck.setACKreceived(true);
        pck.setACKwaiting(false);

    }
}
