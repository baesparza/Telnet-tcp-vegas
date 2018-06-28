package aplicacion.utils;

import java.io.IOException;
import java.util.Date;
import java.util.zip.CRC32;

/**
 *
 * @author bruno
 */
public class TCPPacket {

    public static final String SEPARATOR = ";";

    // TODO: add segment
    public int acknowledgementNumber = 0;
    public int synchronizationBit = 0;
    public int acknowledgementBit = 0;
    public int finishBit = 0;
    public int windowSize = 0;
    public long checksum = 0;
    public int fragementNumber = 0;
    public String body = "";

    private boolean ACKreceived = false;
    private boolean ACKwaiting = false;
    private long startTime = 0;

    /**
     * Get an empty packet
     */
    public TCPPacket() {
    }

    /**
     * Get packet based on data
     *
     * @param rawData to be parsed into an object
     */
    public TCPPacket(String rawData) {
        String data[] = rawData.split(TCPPacket.SEPARATOR);

        this.acknowledgementNumber = Integer.parseInt(data[0]);
        this.synchronizationBit = Integer.parseInt(data[1]);
        this.acknowledgementBit = Integer.parseInt(data[2]);
        this.finishBit = Integer.parseInt(data[3]);
        this.windowSize = Integer.parseInt(data[4]);
        this.checksum = Long.parseLong(data[5]);
        this.fragementNumber = Integer.parseInt(data[6]);

        if (data.length == 8) {
            this.body = data[7];
        }
    }

    @Override
    public String toString() {
        return "Acknowledgement Number: " + this.acknowledgementNumber + '\n'
                + "Synchronization Bit: " + this.synchronizationBit + '\n'
                + "Acknowledgement Bit: " + this.acknowledgementBit + '\n'
                + "Finish Bit: " + this.finishBit + '\n'
                + "Window Size: " + this.windowSize + '\n'
                + "Checksum: " + this.checksum + '\n'
                + "Fragment Number: " + this.fragementNumber + '\n'
                + "Body: " + this.body;
    }

    public String getHeader(String data, int fragementNumber) {
        return this.acknowledgementNumber + TCPPacket.SEPARATOR
                + this.synchronizationBit + TCPPacket.SEPARATOR
                + this.acknowledgementBit + TCPPacket.SEPARATOR
                + this.finishBit + TCPPacket.SEPARATOR
                + this.windowSize + TCPPacket.SEPARATOR
                + TCPPacket.getChecksum(data) + TCPPacket.SEPARATOR
                + fragementNumber + TCPPacket.SEPARATOR
                + data;
    }

    public String getHeader() {
        return this.acknowledgementNumber + TCPPacket.SEPARATOR
                + this.synchronizationBit + TCPPacket.SEPARATOR
                + this.acknowledgementBit + TCPPacket.SEPARATOR
                + this.finishBit + TCPPacket.SEPARATOR
                + this.windowSize + TCPPacket.SEPARATOR
                + this.checksum + TCPPacket.SEPARATOR
                + this.fragementNumber + TCPPacket.SEPARATOR
                + body;
    }

    /**
     * Algorithm to gen checksum with given data
     *
     * @param data input
     * @return checksum algorithm output
     */
    public static long getChecksum(String body) {
        byte[] data = body.getBytes();
        CRC32 checksum = new CRC32();
        checksum.update(data, 0, data.length);
        return checksum.getValue();
    }

    /**
     * Validate a checksums and checksum of data
     *
     * @param checkSum value
     * @param data to be proceed and compared
     * @return
     * @throws IOException
     */
    public static boolean validChecksum(long checkSum, String data) throws IOException {
        return checkSum == TCPPacket.getChecksum(data);
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
        // TODO: fix timer
        long time = (new Date()).getTime() - startTime;
        return time > 2000;
    }
}
