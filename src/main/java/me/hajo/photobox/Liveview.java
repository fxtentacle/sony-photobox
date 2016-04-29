package me.hajo.photobox;

import sun.awt.image.codec.JPEGImageDecoderImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Liveview extends ResizedImagePanel {
    
    final BlockingQueue<byte[]> stream2decoder = new LinkedBlockingQueue<byte[]>(5);
    final BlockingQueue<Image> decoder2view = new LinkedBlockingQueue<Image>(5);
    
    InputStream source;
    
    public class Streamer extends Thread {
        public void run() {
            try {
            LiveviewStreamDecoder decoder = new LiveviewStreamDecoder(source);
                while(true) {
                    stream2decoder.put(decoder.getNextFrameData());
                }
            } catch (Exception e) {
                throw new HajoRestartException(e);
            }
        }
    };

    public class Decoder extends Thread {
        public void run() {
            try {
                while(true) {
                    final byte[] data = stream2decoder.take();
                    final BufferedImage image = new JPEGImageDecoderImpl(new ByteArrayInputStream(data)).decodeAsBufferedImage();
                    decoder2view.put(image);
                }
            } catch (Exception e) {
                throw new HajoRestartException(e);
            }
        }
    };

    public class Updater extends Thread {
        public void run() {
            try {
                while(true) {
                    final Image img = decoder2view.take();
                    setImage(img);
                }
            } catch (Exception e) {
                throw new HajoRestartException(e);
            }
        }
    }

    
    void init(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5*1000);
        connection.connect();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) 
            throw new HajoRestartException(new Exception("Could not init live view."));
        
        source = connection.getInputStream();
        new Streamer().start();
        for(int i=0;i<4;i++) new Decoder().start();
        new Updater().start();
    }
}
