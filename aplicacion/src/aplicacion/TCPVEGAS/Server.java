package aplicacion.TCPVEGAS;

import aplicacion.utils.ACKPackage;
import aplicacion.utils.DATAPackage;
import aplicacion.utils.FTPPackage;
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

    public Server() {
        try {
            socket = new DatagramSocket(5000);
        } catch (SocketException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Start this thread
     */
    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
    }

    // repite el paquete al cliente
    private void enviarPaqueteAlCliente(DatagramPacket paqueteRecibir) throws IOException {
        System.out.println("\n\nRepitiendo datos al cliente...");

        // crea paquete para enviar
        DatagramPacket paqueteEnviar = new DatagramPacket(
                paqueteRecibir.getData(), paqueteRecibir.getLength(),
                paqueteRecibir.getAddress(), paqueteRecibir.getPort());

        socket.send(paqueteEnviar); // envía paquete al cliente
        System.out.println("Paquete enviado\n");
    } // fin del método enviarPaqueteAlCliente

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
                DATAPackage dataPackage = (DATAPackage) FTPPackage.getPackage(new String(packageIN.getData()));
                // validate if chechsums are equal
                if (DATAPackage.validChecksum(new String(dataPackage.checkSum), dataPackage.data.getBytes())) {
                    this.sendACK(String.valueOf(dataPackage.id), packageIN.getAddress(), packageIN.getPort());
                    System.out.println("Valid package");
                    System.out.println("Value: " + dataPackage.data);
                } else {
                    System.out.println("Invalid package");
                }

            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void sendACK(String id, InetAddress hostname, int destPort) {
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
}
