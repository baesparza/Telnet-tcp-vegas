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
    public int synchronizationBit = 0;
    public int acknowledgementBit = 0;
    public int finishBit = 0;
    public int windowSize = 0;
    public long checksum = 0;
    public int fragementNumber = 0;
    public int sequenceNumber = 0;
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

        this.synchronizationBit = Integer.parseInt(data[0]);
        this.acknowledgementBit = Integer.parseInt(data[1]);
        this.finishBit = Integer.parseInt(data[2]);
        this.windowSize = Integer.parseInt(data[3]);
        this.checksum = Long.parseLong(data[4]);
        this.fragementNumber = Integer.parseInt(data[5]);
        this.sequenceNumber = Integer.parseInt(data[6]);

        if (data.length == 8) {
            this.body = data[7].equals("_") ? " " : data[7];
        }
    }
// sequense

    public TCPPacket(int sequenceNumber, int fragementNumber, String body) {
        this.body = body;
        this.fragementNumber = fragementNumber;
        this.sequenceNumber = sequenceNumber;
        this.checksum = TCPPacket.getChecksum(body);
    }

    @Override
    public String toString() {
        return "Synchronization Bit: " + this.synchronizationBit + '\n'
                + "Acknowledgement Bit: " + this.acknowledgementBit + '\n'
                + "Finish Bit: " + this.finishBit + '\n'
                + "Window Size: " + this.windowSize + '\n'
                + "Checksum: " + this.checksum + '\n'
                + "Fragment Number: " + this.fragementNumber + '\n'
                + "Sequense Number: " + this.sequenceNumber + '\n'
                + "Body: " + this.body;
    }

    public String getHeader() {
        return this.synchronizationBit + TCPPacket.SEPARATOR
                + this.acknowledgementBit + TCPPacket.SEPARATOR
                + this.finishBit + TCPPacket.SEPARATOR
                + this.windowSize + TCPPacket.SEPARATOR
                + this.checksum + TCPPacket.SEPARATOR
                + this.fragementNumber + TCPPacket.SEPARATOR
                + this.sequenceNumber + TCPPacket.SEPARATOR
                + body;
    }

    /**
     * Algorithm to gen checksum with given data
     *
     * @param data input
     * @return checksum algorithm output
     */
    public static long getChecksum(String data) {
        byte[] bytes = data.trim().getBytes();
        CRC32 checksum = new CRC32();
        checksum.update(bytes, 0, bytes.length);
        return checksum.getValue();
    }

    /**
     * Validate a checksums and checksum of data
     *
     * @param checkSum value
     * @param data to be proceed and compared
     * @return
     */
    public static boolean validChecksum(long checkSum, String data) {
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

    private static Thread timerThread(final int seconds) {
        return new Thread(() -> {
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException ie) {
            }
        });
    }
}
