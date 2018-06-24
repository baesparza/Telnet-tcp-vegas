package aplicacion.utils;

import java.io.IOException;

/**
 *
 * @author bruno
 */
public class DATAPackage extends FTPPackage {

    public String checkSum;
    public String data;
    public String fragement;

    /**
     * Package with data or message
     *
     * @param id of package to be send
     * @param data of package
     * @param fragement if there are more fragments
     * @throws IOException when error with the checksum
     */
    public DATAPackage(String id, String data, String fragement) throws IOException {
        super(id);
        this.data = data;
        this.checkSum = String.valueOf(DATAPackage.getChecksum(this.data.getBytes()));
        this.fragement = fragement;
    }

    /**
     * Package with defined checksum
     *
     * @param id
     * @param checkSum when check sum is already defined
     * @param data
     * @param fragement
     */
    public DATAPackage(String id, String checkSum, String data, String fragement) {
        super(id);
        this.checkSum = checkSum;
        this.data = data;
        this.fragement = fragement;
    }

    /**
     * @return structured package
     */
    @Override
    public String toString() {
        return this.id + ";" + this.checkSum + ";" + this.data + ";" + this.fragement;
    }
}
