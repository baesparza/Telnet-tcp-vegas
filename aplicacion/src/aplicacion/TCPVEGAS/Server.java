package aplicacion.TCPVEGAS;

import aplicacion.utils.ACKPackage;
import aplicacion.utils.DATAPackage;
import aplicacion.utils.FTPPackage;
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

    private DatagramSocket socket;
    private Thread thread;
    private InputList listPackages;

    private InetAddress IPAddress;
    private int port;
    private byte[] receiveData;

    // TODO: initialize variables
    public Server() {
        try {
            socket = new DatagramSocket(5000);
            listPackages = new InputList();
        } catch (SocketException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
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

    }

    private void sendACK(int id, InetAddress hostname, int destPort) {
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
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void handshake() throws Exception {
        TCPPacket sendData = new TCPPacket();
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        IPAddress = receivePacket.getAddress();
        port = receivePacket.getPort();
        TCPPacket receivedData = new TCPPacket(new String(receivePacket.getData()));

        if (receivedData.synchronization_bit == 1) {
            sendData = new TCPPacket();
            sendData.acknowledgement_number = 1;
            sendData.synchronization_bit = 1;
            sendData.acknowledgement_bit = 1;

            byte[] data = sendData.getHeader().getBytes();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, port);
            serverSocket.send(sendPacket);

            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            receivedData = new TCPPacket(new String(receivePacket.getData()));

            if (receivedData.synchronization_number == 1) {
                System.out.println("Three-way Handshake Complete!");
            }
        }
    }

    public static void disconnect() throws Exception {
        TCPPacket sendData = new TCPPacket();
        sendData.finish_bit = 1;

        byte[] data = sendData.getHeader().getBytes();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, port);
        serverSocket.send(sendPacket);

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        TCPPacket receivedData = new TCPPacket(new String(receivePacket.getData()));

        if (receivedData.acknowledgement_bit == 1) {
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            receivedData = new TCPPacket(new String(receivePacket.getData()));

            if (receivedData.finish_bit == 1) {
                sendData = new TCPPacket();
                sendData.acknowledgement_bit = 1;

                data = sendData.getHeader().getBytes();
                sendPacket = new DatagramPacket(data, data.length, IPAddress, port);
                serverSocket.send(sendPacket);
            }
        }
    }
}
