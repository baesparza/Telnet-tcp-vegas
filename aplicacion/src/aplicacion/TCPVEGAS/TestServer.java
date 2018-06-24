package aplicacion.TCPVEGAS;

/**
 *
 * @author bruno
 */
public class TestServer {

    public static void main(String[] args) {

        Server s = new Server();

        s.start();
        System.out.println("server running");
    }
}
