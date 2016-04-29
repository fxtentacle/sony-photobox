package me.hajo.photobox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LiveviewStreamDecoder {
    final InputStream source;

    public LiveviewStreamDecoder(InputStream source) {
        this.source = source;
    }

    public byte[] getNextFrameData() throws IOException {
        while (true) {
            // Common Header
            final int readLength = 1 + 1 + 2 + 4;
            final byte[] commonHeader = readBytes(source, readLength);
            if (commonHeader == null || commonHeader.length != readLength) {
                throw new IOException("Cannot read stream for common header.");
            }

            if (commonHeader[0] != (byte) 0xFF) {
                throw new IOException("Unexpected data format. (Start byte)");
            }

            switch (commonHeader[1]) {
                case (byte) 0x12:
                    // This is information header for streaming.
                    // skip this packet.
                    final int readLength2 = 4 + 3 + 1 + 2 + 118 + 4 + 4 + 24;
                    readBytes(source, readLength2);
                    break;
                case (byte) 0x01:
                case (byte) 0x11:
                    return readFrameData();
                default:
                    break;
            }
        }
    }

    public byte[] readFrameData() throws IOException {
        final int readLength = 4 + 3 + 1 + 4 + 1 + 115;
        final byte[] payloadHeader = readBytes(source, readLength);
        if (payloadHeader == null || payloadHeader.length != readLength) {
            throw new IOException("Cannot read stream for payload header.");
        }
        if (payloadHeader[0] != (byte) 0x24 || payloadHeader[1] != (byte) 0x35
                || payloadHeader[2] != (byte) 0x68
                || payloadHeader[3] != (byte) 0x79) {
            throw new IOException("Unexpected data format. (Start code)");
        }
        int jpegSize = bytesToInt(payloadHeader, 4, 3);
        int paddingSize = bytesToInt(payloadHeader, 7, 1);

        final byte[] jpegData = readBytes(source, jpegSize);
        readBytes(source, paddingSize);

        return jpegData;
    }
    
    private static int bytesToInt(byte[] byteData, int startIndex, int count) {
        int ret = 0;
        for (int i = startIndex; i < startIndex + count; i++) {
            ret = (ret << 8) | (byteData[i] & 0xff);
        }
        return ret;
    }
    
    private static byte[] readBytes(InputStream in, int length) throws IOException {
        ByteArrayOutputStream tmpByteArray = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int trialReadlen = Math.min(buffer.length, length - tmpByteArray.size());
            int readlen = in.read(buffer, 0, trialReadlen);
            if (readlen < 0) {
                break;
            }
            tmpByteArray.write(buffer, 0, readlen);
            if (length <= tmpByteArray.size()) {
                break;
            }
        }
        byte[] ret = tmpByteArray.toByteArray();
        tmpByteArray.close();
        return ret;
    }
}
