package me.hajo.photobox;

import sun.misc.IOUtils;

import javax.swing.*;
import java.io.IOException;

/**
 * Created by fxtentacle on 29.04.16.
 */
public class UploadTest {
    public static void main(String[] args) throws IOException {
        String host = JOptionPane.showInputDialog("Server IP/hostname:port (192.168.42.128:8765)");
        FileUploader uploader = new FileUploader(host);
        final byte[] data = IOUtils.readFully(PhotoboxGUI.class.getResourceAsStream("/wait.jpg"), -1, false);
        uploader.upload(data);
    }
}
