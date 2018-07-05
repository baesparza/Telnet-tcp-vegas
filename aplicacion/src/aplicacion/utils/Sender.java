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
public class Sender {

    private final List<TCPPacket> packets;

    /**
     * Manager for sending packets
     */
    public Sender() {
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
        return this.packets.add(packet);
    }

    /**
     * Send messages stored in lists
     *
     * @param output socket
     * @param hostname ip address
     * @param port socket port
     * @param cLog log error messages
     */
    private int currentWindow;
    private int start, end;

    public void sendPackages(DatagramSocket output, InetAddress hostname, int port, ConsoleLogger cLog) {
        this.currentWindow = 1;
        this.start = 0;
        this.end = this.currentWindow;
        // start sending packets
        // variables to control end to end sendeing of packets
        while (this.start < this.packets.size()) {
            int tempStart = this.start;
            int tempEnd = this.end;
            for (int i = tempStart; i < tempEnd && i < this.packets.size(); i++) {
                TCPPacket packet = this.packets.get(i);
                if (packet.timeOut(2)) {
                    try {
                        this.packageSender(packet, output, hostname, port);
                        cLog.info("Packet sent, seq: " + packet.sequence);
                    } catch (IOException ex) {
                        cLog.warning("Packet not sent, seq: " + packet.sequence);
                    }
                }
            }
        }
        // all packets have been sent, clear all variables
        this.packets.clear();
        this.lastSeq = 0;
        this.count = 0;
        this.additiveIncrease = false;
    }

    private int lastSeq = 0;
    private int count = 0;
    private boolean additiveIncrease = false;

    public void receivedACK(int sequence) {
        this.packets.get(sequence).ACKreceived();
        boolean canMoveWind = true;

        if (sequence < this.start || sequence > this.end) {
            // this packet is out of range of actual packets
            return;
        }
        // validate all packets have been ACKed
        for (int i = this.start; i < this.end && i < this.packets.size(); i++) {
            if (this.packets.get(i).isWaitingACK()) {
                canMoveWind = false;
            }
        }

        if (canMoveWind) {
            // move window controls
            this.start = this.end;
            this.currentWindow += (additiveIncrease) ? 1 : this.currentWindow;
            this.end = this.start + this.currentWindow;
        } else {
            if (this.lastSeq == sequence) {
                this.count++;
            } else if (sequence > this.lastSeq) {
                // new packet with higher seq
                this.lastSeq = sequence;
                this.count = 0;
            }
            // check count
            if (this.count >= 3) {
                // congestion control
                this.currentWindow = (int) this.currentWindow / 2;
                this.end = this.start + this.currentWindow;
                this.additiveIncrease = true;
                this.count = 0;
            }
        }

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
        packet.setACKwaiting(true);
        packet.statTimer();
    }
}
