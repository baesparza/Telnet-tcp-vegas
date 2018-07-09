package aplicacion.Client;

/**
 *
 * @author bruno
 */
public class TestCliente {

    public static void main(String[] args) {
        // initialize client app
        ClientGui gui = new ClientGui();
        gui.setLocationRelativeTo(null);
        gui.setVisible(true);
        gui.setAlwaysOnTop(true);
    }

}
