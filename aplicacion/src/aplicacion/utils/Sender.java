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
    private final ConsoleLogger cLog;
    private boolean locked;

    /**
     * Manager for sending packets
     *
     * @param cLog to log messages to console
     */
    public Sender(ConsoleLogger cLog) {
        this.packets = new ArrayList<>();
        this.cLog = cLog;
    }

    /**
     * Receive packages and add them to list, no need to sort them since they
     * are already sorted
     *
     * @param packet to be added
     * @return true if it was added, or false, if not or locked
     */
    public boolean addPackage(TCPPacket packet) {
        return (this.locked) ? this.locked : this.packets.add(packet);
    }

    /**
     * Send messages stored in list, and simulation TCP/Vegas
     *
     * @param output socket
     * @param hostname ip address
     * @param port socket port
     * @param cLog log error messages
     */
    private int currentWindow;
    private int start, end;
    private int lastSeq, count;
    private boolean additiveIncrease;

    public void sendPackages(DatagramSocket output, InetAddress hostname, int port) {
        // set all variables with default values each time
        this.currentWindow = 1;
        this.start = 0;
        this.end = this.start + this.currentWindow;
        this.lastSeq = this.count = 0;
        this.additiveIncrease = false;
        this.locked = true;
        // start sending packets
        // variables to control end to end sendeing of packets
        while (this.start < this.packets.size()) {
            // temporal range
            int tempStart = this.start, tempEnd = this.end;
            for (int i = tempStart; i < tempEnd && i < this.packets.size(); i++) {
                TCPPacket packet = this.packets.get(i);
                if (packet.timeOut(2)) {
                    this.packageSender(packet, output, hostname, port);
                    cLog.info("Packet sent, seq: " + packet.sequence);
                }
            }
        }
        // all packets have been sent, clear list
        this.packets.clear();
        this.locked = false;
    }

    public void receivedACK(int sequence) {
        if (this.packets.isEmpty()) {
            return;
        }
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
     * Send package to server, and simulate delay by using multiThread
     *
     * @param packet to be sent
     * @param socket socket
     * @param hostname of server
     * @param port on server
     * @throws IOException when socket fails
     */
    private void packageSender(TCPPacket packet, DatagramSocket socket, InetAddress hostname, int port) {
        new Thread() {
            @Override
            public void run() {
                try {
                    // sleep to simulate delay
                    // Thread.sleep((long) (Math.random() * 500 + 500)); // range 500 - 1000 milliseconds
                    byte[] packetData = packet.getHeader().getBytes();
                    DatagramPacket pack = new DatagramPacket(packetData, packetData.length, hostname, port);
                    socket.send(pack);
                    packet.setACKwaiting(true);
                    packet.statTimer();
                } catch (IOException ex) {
                    cLog.warning("Socket failed to send packet, seq: " + packet.sequence);
                    //} catch (InterruptedException ex) {
                    //    cLog.warning("Thread interrupted");
                }
            }

        }.start();
    }

    /**
     * @return size of current window
     */
    public int getCurrentWindow() {
        return currentWindow;
    }
}
