package aplicacion.Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author bruno
 */
public class Telnet {

    /**
     * Return a response for a command
     *
     * @param c command
     * @return response
     */
    public static String getCommand(String c) {
        System.out.println(c.trim().contains("date"));
        if (c.contains("date")) {
            return Telnet.date();
        }
        if (c.contains("open")) {
            return Telnet.open(c);
        }
        return "The command doesn't exist";
    }

    /**
     * @return actual date
     */
    private static String date() {
        Date date = new Date();
        return date.toString();
    }

    /**
     * Open file and return its content
     *
     * @param c command
     * @return file content
     */
    private static String open(final String c) {
        String[] args = c.split(" ");

        StringBuilder resp = new StringBuilder();
        List<String> records = new ArrayList<>();
        // read file
        try {
            BufferedReader reader = new BufferedReader(new FileReader("./assets/" + args[1]));
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line);
            }
            reader.close();
        } catch (IOException e) {
            return ("Can't read file or file doesn't exist");
        }
        // return file content
        for (String r : records) {
            resp.append(r).append("\n");
        }
        return resp.toString();
    }
}
