package me.hajo.photobox;

import sun.awt.image.codec.JPEGImageDecoderImpl;
import sun.misc.IOUtils;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TimerTask;

/**
 * Created by fxtentacle on 29.04.16.
 */
public class PhotoboxGUI extends JFrame {
    final CameraRemote remote;
    final Liveview liveview = new Liveview();
    final ResizedImagePanel postview = new ResizedImagePanel();
    final java.util.Timer timer = new java.util.Timer();
    final BufferedImage waitImage;
    final Clip countdownBeep;
    final FileUploader uploader;

    public PhotoboxGUI(CameraRemote remote, FileUploader uploader) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        this.remote = remote;
        this.uploader = uploader;
        
        this.waitImage = new JPEGImageDecoderImpl(PhotoboxGUI.class.getResourceAsStream("/wait.jpg")).decodeAsBufferedImage();
        this.countdownBeep = AudioSystem.getClip();
        countdownBeep.open(AudioSystem.getAudioInputStream(PhotoboxGUI.class.getResourceAsStream("/beep.wav")));
        
        remote.start();
        liveview.init(remote.startLiveViewAndGetURL());

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);

        final GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
        device.setFullScreenWindow(this);

        liveview.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1)
                    countDownAndTakePicture();
            }
        });
        
        add(liveview);

        setBackground(Color.black);
        setVisible(true);

        postview.addMouseListener(new MouseAdapter() {
            boolean isDownL = false, isDownR = false;
            int countdown = 3;
            
            class PostviewCountdown extends TimerTask {
                @Override
                public void run() {
                    if(!isDownL && !isDownR) {
                        return;
                    }
                    
                    countdown--;
                    if(countdown > 0) {
                        postview.setOverlay((isDownL ? "Ja / Yes " : "Nein / No ") + countdown);
                        postview.repaint();
                        timer.schedule(new PostviewCountdown(), 1*1000);
                    } else {
                        if(isDownL) sendPicture();
                        else if(isDownR) returnToTakePicture();
                    }
                }
            };
            
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1) isDownL = true;
                else if(e.getButton() == MouseEvent.BUTTON3) isDownR = true;
                if(isDownL && isDownR) {
                    isDownL = isDownR = false;
                }
                if(isDownL || isDownR) {
                    countdown = 4;
                    timer.schedule(new PostviewCountdown(), 1);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1) isDownL = false;
                else if(e.getButton() == MouseEvent.BUTTON3) isDownR = false;
                postview.setOverlay(null);
                postview.repaint();
            }
        });

    }
    
    public void sendPicture() {
        if(currentPostviewImageData != null) {
            uploader.upload(currentPostviewImageData);
        }
        returnToTakePicture();
    }
    
    public void returnToTakePicture() {
        remove(postview);
        add(liveview);
    }

    boolean countdownIsRunning = false;
    public void countDownAndTakePicture() {
        if(countdownIsRunning) return;
        countdownIsRunning = true;

        liveview.setOverlay("3");
        liveview.repaint();
        countdownBeep.setFramePosition(0);
        countdownBeep.start();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                liveview.setOverlay("2");
                liveview.repaint();
                countdownBeep.setFramePosition(0);
                countdownBeep.start();
            }
        }, 1*1000);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                liveview.setOverlay("1");
                liveview.repaint();
                countdownBeep.setFramePosition(0);
                countdownBeep.start();
            }
        }, 2*1000);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                liveview.setOverlay(null);
                takePicture();
                countdownIsRunning = false;
            }
        }, 3*1000);
    }

    byte[] currentPostviewImageData;
    public void takePicture() {
        try {
            final String pictureURL = remote.takePictureAndGetURL();

            remove(liveview);
            postview.setSize(liveview.getSize());
            add(postview);
            postview.setOverlay(null);
            postview.setImage(waitImage);
            
            final HttpURLConnection connection = (HttpURLConnection) new URL(pictureURL).openConnection();
            connection.connect();
            currentPostviewImageData = IOUtils.readFully(connection.getInputStream(), -1, false);
            final BufferedImage image = new JPEGImageDecoderImpl(new ByteArrayInputStream(currentPostviewImageData)).decodeAsBufferedImage();
            
            postview.setImage(image);
        } catch (IOException e) {
            throw new HajoRestartException(e);
        }
    }
}
