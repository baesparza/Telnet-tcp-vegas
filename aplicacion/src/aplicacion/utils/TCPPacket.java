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
    public int synchronizationFlag = 0;
    public int acknowledgementFlag = 0;
    public int finishFlag = 0;
    public int windowSize = 0;
    public long checksum = 0;
    public int fragementFlag = 0;
    public int sequence = 0;
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

        this.synchronizationFlag = Integer.parseInt(data[0]);
        this.acknowledgementFlag = Integer.parseInt(data[1]);
        this.finishFlag = Integer.parseInt(data[2]);
        this.windowSize = Integer.parseInt(data[3]);
        this.checksum = Long.parseLong(data[4]);
        this.fragementFlag = Integer.parseInt(data[5]);
        this.sequence = Integer.parseInt(data[6]);

        if (data.length == 8) {
            this.body = data[7].equals("_") ? " " : data[7];
        }
    }

    public TCPPacket(int sequenceNumber, int fragementNumber, String body) {
        this.body = body;
        this.fragementFlag = fragementNumber;
        this.sequence = sequenceNumber;
        this.checksum = TCPPacket.getChecksum(body);
    }

    public static TCPPacket ACKPacket(int sequenceNumber) {
        TCPPacket packet = new TCPPacket();
        packet.acknowledgementFlag = 1;
        packet.sequence = sequenceNumber;
        return packet;
    }

    public static TCPPacket SYNCACKPacket() {
        TCPPacket packet = new TCPPacket();
        packet.synchronizationFlag = 1;
        packet.acknowledgementFlag = 1;
        return packet;
    }

    public static TCPPacket SYNCPacket() {
        TCPPacket packet = new TCPPacket();
        packet.synchronizationFlag = 1;
        return packet;
    }
    public static TCPPacket FINPacket() {
        TCPPacket packet = new TCPPacket();
        packet.finishFlag = 1;
        return packet;
    }

    @Override
    public String toString() {
        return "Synchronization Bit: " + this.synchronizationFlag + '\n'
                + "Acknowledgement Bit: " + this.acknowledgementFlag + '\n'
                + "Finish Bit: " + this.finishFlag + '\n'
                + "Window Size: " + this.windowSize + '\n'
                + "Checksum: " + this.checksum + '\n'
                + "Fragment Number: " + this.fragementFlag + '\n'
                + "Sequense Number: " + this.sequence + '\n'
                + "Body: " + this.body;
    }

    public String getHeader() {
        return this.synchronizationFlag + TCPPacket.SEPARATOR
                + this.acknowledgementFlag + TCPPacket.SEPARATOR
                + this.finishFlag + TCPPacket.SEPARATOR
                + this.windowSize + TCPPacket.SEPARATOR
                + this.checksum + TCPPacket.SEPARATOR
                + this.fragementFlag + TCPPacket.SEPARATOR
                + this.sequence + TCPPacket.SEPARATOR
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
