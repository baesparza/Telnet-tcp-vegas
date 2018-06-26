package aplicacion.utils;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author bruno
 */
public class DATAPackage extends FTPPackage {

    public long checkSum;
    public String data;
    public int fragement;

    private boolean ACKreceived;
    private boolean ACKwaiting;
    private long startTime;

    /**
     * Package with data or message
     *
     * @param id of package to be send
     * @param data of package
     * @param fragement if there are more fragments
     * @throws IOException when error with the checksum
     */
    public DATAPackage(int id, String data, int fragement) throws IOException {
        super(id);
        this.data = data;
        this.checkSum = DATAPackage.getChecksum(this.data.getBytes());
        this.fragement = fragement;

        this.ACKwaiting = false;
        this.ACKreceived = false;
    }

    /**
     * Package with defined checksum
     *
     * @param id
     * @param checkSum when check sum is already defined
     * @param data
     * @param fragement
     */
    public DATAPackage(int id, long checkSum, String data, int fragement) {
        super(id);
        this.checkSum = checkSum;
        this.data = data;
        this.fragement = fragement;

        this.ACKwaiting = false;
        this.ACKreceived = false;
    }

    /**
     * @return structured package
     */
    @Override
    public String toString() {
        return this.id + ";" + this.checkSum + ";" + this.data + ";" + this.fragement;
    }

    /**
     * TODO: add description
     */
    public boolean isWaitingACK() {
        return this.ACKwaiting;
    }

    public boolean hasACK() {
        return this.ACKreceived;
    }

    public void setACKreceived(boolean ACKreceived) {
        this.ACKreceived = ACKreceived;
    }

    public void setACKwaiting(boolean ACKwaiting) {
        this.ACKwaiting = ACKwaiting;
    }

    public void statTimer() {
        this.startTime = System.currentTimeMillis();
    }

    public boolean timeOut() {
        // TODO: validate time
        long time = (new Date()).getTime() - startTime;
        return time > 500;
    }

}
