/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aplicacion.TCPVEGAS;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 *
 * @author bruno
 */
public class Server {

    private DatagramSocket socket; // socket para conectarse al cliente

    public Server() {
        try // crea objeto DatagramSocket para enviar y recibir paquetes
        {
            socket = new DatagramSocket(5000);
        } // fin de try
        catch (SocketException excepcionSocket) {
            excepcionSocket.printStackTrace();
            System.exit(1);
        } // fin de catch
    }

    // espera a que lleguen los paquetes, muestra los datos y repite el paquete al cliente
    public void esperarPaquetes() {
        while (true) {
            try // recibe el paquete, muestra su contenido, devuelve una copia al cliente
            {
                byte datos[] = new byte[100]; // establece un paquete
                DatagramPacket paqueteRecibir
                        = new DatagramPacket(datos, datos.length);

                socket.receive(paqueteRecibir); // espera a recibir el paquete

                // muestra la información del paquete recibido
                System.out.println("\nPaquete recibido:"
                        + "\nDe host: " + paqueteRecibir.getAddress()
                        + "\nPuerto host: " + paqueteRecibir.getPort()
                        + "\nLongitud: " + paqueteRecibir.getLength()
                        + "\nContiene:\n\t" + new String(paqueteRecibir.getData(),
                                0, paqueteRecibir.getLength()));

                enviarPaqueteAlCliente(paqueteRecibir); // envía el paquete al cliente
            } // fin de try
            catch (IOException excepcionES) {
                System.err.println(excepcionES.toString() + "\n");
                excepcionES.printStackTrace();
            } // fin de catch
        } // fin de while
    } // fin del método esperarPaquetes

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
}
