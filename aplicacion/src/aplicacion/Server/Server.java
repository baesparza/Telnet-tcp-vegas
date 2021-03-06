package aplicacion.Server;

import aplicacion.utils.ConsoleLogger;
import aplicacion.utils.Receiver;
import aplicacion.utils.Sender;
import aplicacion.utils.TCPPacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author bruno
 */
public final class Server implements Runnable {

    /**
     * Default port for sever
     */
    public static final int PORT = 5000;

    private final DatagramSocket socket;
    private Thread thread;
    private final Receiver receiver;
    private final Sender sender;
    private final ConsoleLogger cLog;

    /**
     * Initialize server and start listening for a handShake
     *
     * @param cLog to print messages
     * @throws java.io.IOException when socket doesn't start
     */
    public Server(ConsoleLogger cLog) throws IOException {
        this.cLog = cLog;
        this.receiver = new Receiver(cLog);
        this.sender = new Sender(cLog);
        this.socket = new DatagramSocket(Server.PORT);
        this.handShake();
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
            byte[] buffer = new byte[1024];
            try {
                DatagramPacket packetIN = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(packetIN);
                TCPPacket tcpPacket = new TCPPacket(new String(packetIN.getData()));
                // act depending on packet type
                if (tcpPacket.checksum != 0) {
                    // packet has data, verify content, send ack if valid
                    if (this.receiver.add(tcpPacket)) {
                        this.receiver.sendACK(socket, tcpPacket.sequence, packetIN.getAddress(), packetIN.getPort());
                        // veryfy if all packages have been receibed
                        if (this.receiver.hasEnded()) {
                            // send a telnet response
                            sendResponse(Telnet.getCommand(this.receiver.getMessage()), packetIN.getAddress(), packetIN.getPort());
                            this.receiver.clear();
                        }
                    } else {
                        cLog.warning("Invalid package have been deleted, seq: " + tcpPacket.sequence);
                    }
                } else if (tcpPacket.acknowledgementFlag == 1) {
                    // packet is an ACK, tell out manager and ACK arrived
                    cLog.info("Recieved ACK, Sequence: " + tcpPacket.sequence);
                    this.sender.receivedACK(tcpPacket.sequence);
                } else if (tcpPacket.finishFlag == 1) {
                    // client wants to disconnect
                    this.disconnect(packetIN.getAddress(), packetIN.getPort());
                    System.exit(0);
                }
            } catch (IOException ex) {
                cLog.error("Socket fail at receive");
            }
        }
    }

    /**
     * Trigger from APP to send command to server
     *
     * @param command to be sent
     */
    /**
     * Takes a command and return a telnet response
     *
     * @param resp to be sent
     * @param address of client
     * @param port of client
     */
    public void sendResponse(String resp, InetAddress address, int port) {
        // get telnet response
        new Thread() {
            @Override
            public void run() {
                cLog.info("Telnet response: " + resp);
                sender.sendMessage(resp, socket, address, port);
            }
        }.start();
    }

    /**
     * Server waits for a client to handshake
     *
     * @throws IOException when socket fails
     */
    public void handShake() throws IOException {
        // TODO: add timeout
        byte[] buffer = new byte[1024];
        // Whait for client to connect
        DatagramPacket packetIN = new DatagramPacket(buffer, buffer.length);
        this.socket.receive(packetIN);
        TCPPacket tcpPacket = new TCPPacket(new String(packetIN.getData()));
        // check if is a synchronization package
        if (tcpPacket.synchronizationFlag == 1) {
            // Send synchronization ACK
            byte[] data = TCPPacket.SYNCACKPacket().getHeader().getBytes();
            this.socket.send(new DatagramPacket(data, data.length, packetIN.getAddress(), packetIN.getPort()));
            // wait for a confirmation from the client
            packetIN = new DatagramPacket(buffer, buffer.length);
            this.socket.receive(packetIN);
            tcpPacket = new TCPPacket(new String(packetIN.getData()));
            // validate if conection was made
            if (tcpPacket.acknowledgementFlag == 1 && tcpPacket.synchronizationFlag == 1) {
                cLog.info("Conected, IP: " + packetIN.getAddress().getHostAddress() + ", PORT: " + packetIN.getPort());
            }
        }
        // TODO: add when package fails
    }

    /**
     * Disconnect and free socket
     *
     * @param address of client
     * @param port of client
     */
    public void disconnect(InetAddress address, int port) {
        // TODO: add timeout
        try {
            // send confirmation to client get disconnected
            byte[] data = TCPPacket.FINACKPacket().getHeader().getBytes();
            this.socket.send(new DatagramPacket(data, data.length, address, port));
            this.socket.close();
            cLog.info("Client disconnected");
        } catch (IOException ex) {
            cLog.error("Can't send finish ACK");
        }
    }

    public int getWindowSize() {
        return this.sender.getCurrentWindow();
    }
}
