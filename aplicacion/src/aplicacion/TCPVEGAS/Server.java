package aplicacion.TCPVEGAS;

import aplicacion.utils.InputList;
import aplicacion.utils.TCPPacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bruno
 */
public class Server implements Runnable {

    /**
     * Default port for sever
     */
    public static final int PORT = 5000;

    private DatagramSocket socket;
    private Thread thread;
    //    private InputList listPackages;
    private byte[] receiveData;

    /**
     * Initialize server and start listening for a handShake
     */
    public Server() {
        try {
            //  listPackages = new InputList();
            this.socket = new DatagramSocket(Server.PORT);
            receiveData = new byte[1024];
            this.handShake();
        } catch (SocketException ex) {
            System.out.println("Can't initialize socket");
            System.exit(1);
        } catch (Exception ex) {
            System.out.println("Can't initailize handshake");
            System.exit(1);
        }
    }

    /**
     * Start this thread
     */
    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Main loop for application, always listening for packages
     */
    @Override
    public void run() {

        while (true) {
            try {
                DatagramPacket packetIN = new DatagramPacket(this.receiveData, this.receiveData.length);
                this.socket.receive(packetIN);
                TCPPacket packet = new TCPPacket(new String(packetIN.getData()));
                // get type of packet
                if (packet.finishBit == 1) {
                    // client wants to disconnect
                    this.disconnect(packetIN.getAddress(), packetIN.getPort());
                    System.exit(0);
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /*
        while (true) {
            try {
                // set a incoming package size, and wait for incoming package
                DatagramPacket packageIN = new DatagramPacket(new byte[100], 100);
                socket.receive(packageIN);
                // new package in, get type
                if (FTPPackage.isACK(new String(packageIN.getData()))) {
                    // package is an ACK from client
                    continue;
                }
                // This package is not an ACK, it has data
                // if valid, store this data, and wait for full message
                DATAPackage dataPackage = (DATAPackage) FTPPackage.getPackage(new String(packageIN.getData()));
                // if after validations is valid, add package 
                if (listPackages.add(dataPackage)) {
                    this.sendACK(dataPackage.id, packageIN.getAddress(), packageIN.getPort());
                    // veryfy if all packages have been receibed
                    if (listPackages.hasEnded()) {
                        // pass data to application
                        System.out.println(listPackages.getMessage());
                        listPackages.clear();
                    }
                } else {
                    System.out.println("Invalid package have been deleted");
                }

            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
    }
    }
         */
    }

    private void sendACK(int id, InetAddress hostname, int destPort) {
        /*
        try {
            // generate package with usefull data for tcp-vegas
            ACKPackage dataPack = new ACKPackage(id);

            DatagramPacket pack = new DatagramPacket(
                    dataPack.getBytes(),
                    dataPack.getBytes().length,
                    hostname,
                    destPort
            );
            // send package with data, checksum, seq number, and datagramPacketdata
            socket.send(pack);
            System.out.println("Sending ACK, id: " + id);
        } catch (IOException ex) {
            System.out.println("ERROR while sending ACK, id: " + id);
            Logger
                    .getLogger(Server.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
         */
    }

    /**
     * Server handshake with a client
     *
     * @throws IOException when socket fails
     */
    public void handShake() throws IOException {
        DatagramPacket packetIN = new DatagramPacket(this.receiveData, this.receiveData.length);
        // Whait for client to connect
        this.socket.receive(packetIN);
        InetAddress clientAddress = packetIN.getAddress();
        int clientPort = packetIN.getPort();
        TCPPacket receivePacket = new TCPPacket(new String(packetIN.getData()));
        // check if is a synchronization package
        if (receivePacket.synchronizationBit == 1) {
            // Send synchronization ACK
            TCPPacket packet = new TCPPacket();
            packet.acknowledgementNumber = 1;
            packet.synchronizationBit = 1;
            packet.acknowledgementBit = 1;
            // get bytes of package with empty body
            byte[] data = packet.getHeader().getBytes();
            DatagramPacket packetOUT = new DatagramPacket(data, data.length, clientAddress, clientPort);
            this.socket.send(packetOUT);
            // wait for a confirmation from the client
            packetIN = new DatagramPacket(receiveData, receiveData.length);
            this.socket.receive(packetIN);
            packet = new TCPPacket(new String(packetIN.getData()));
            // validate if conection was made
            if (packet.acknowledgementBit == 1 && packet.synchronizationBit == 1) {
                System.out.println("Client conected, Client IP: " + clientAddress.getHostAddress() + ", Client PORT: " + clientPort);
            }
        }
        // TODO: add when package fails
    }

    /**
     * TODO: add timer when trigger started-----------------------------------
     * Disconnect and free socket
     *
     * @param clientAddress of client
     * @param clientPort of client
     */
    public void disconnect(InetAddress clientAddress, int clientPort) {
        try {
            // disconnection triggered, send finish ACK
            TCPPacket sendData = new TCPPacket();
            sendData.finishBit = 1;
            sendData.acknowledgementBit = 1;
            byte[] data = sendData.getHeader().getBytes();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, clientAddress, clientPort);
            this.socket.send(sendPacket);
            this.socket.close();
            System.out.println("Client disconnected");
        } catch (IOException ex) {
            System.out.println("Can't send finish ACK");
        }
    }

}
