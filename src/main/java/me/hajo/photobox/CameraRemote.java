package me.hajo.photobox;

import org.json.JSONObject;
import sun.misc.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class CameraRemote extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel image;

    final String url;
    public CameraRemote(String urli) throws IOException {
        url = urli;


        final String startRecMode = "{\n" +
                "    \"method\": \"startRecMode\",\n" +
                "    \"params\": [],\n" +
                "    \"id\": 1,\n" +
                "    \"version\": \"1.0\"\n" +
                "}";
        final String result = cameraCall(url, startRecMode);
        System.out.println(result);

       waitCameraIDLE();

        System.out.println(cameraCallRetryNAN(url, "{\n" +
                "    \"method\": \"setShootMode\",\n" +
                "    \"params\": [\"still\"],\n" +
                "    \"id\": 1,\n" +
                "    \"version\": \"1.0\"\n" +
                "}"));

        waitCameraIDLE();

        System.out.println(cameraCallRetryNAN(url, "{\n" +
                "    \"method\": \"setPostviewImageSize\",\n" +
                "    \"params\": [\"Original\"],\n" +
                "    \"id\": 1,\n" +
                "    \"version\": \"1.0\"\n" +
                "}"));

        waitCameraIDLE();

        final String startLiveView = "{\n" +
                "    \"method\": \"startLiveview\",\n" +
                "    \"params\": [],\n" +
                "    \"id\": 1,\n" +
                "    \"version\": \"1.0\"\n" +
                "}";
        final JSONObject result2 = cameraCallP(url, startLiveView);
        System.out.println(result2);

        final String liveViewURL = result2.getJSONArray("result").getString(0);
        
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private String cameraCallRetryNAN(String url, String s) throws IOException {
        String r;
        JSONObject rr;
        do {
            r = cameraCall(url, s);
            rr = new JSONObject(r);
        } while(rr.has("error") && rr.getJSONArray("error").getString(1).equals("Not Available Now"));
        return r;
    }

    private void waitCameraIDLE() {
        JSONObject event = null;
        boolean wait = false;
        try {
            while(true) {
                event = cameraCallP(url, "{\n" +
                        "    \"method\": \"getEvent\",\n" +
                        "    \"params\": ["+(wait?"true":"false")+"],\n" +
                        "    \"id\": 1,\n" +
                        "    \"version\": \"1.0\"\n" +
                        "}");
                final String status = event.getJSONArray("result").getJSONObject(1).getString("cameraStatus");
                System.out.println("cameraStatus: " + status);
                if(status.equals("IDLE")) return;
                wait = true;
            }
            
        }catch (Exception e) {
            e.printStackTrace();
            if(event != null) System.out.println(event);
        }
    }

    private String cameraCall(String url, String payload) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.connect();
        conn.getOutputStream().write(payload.getBytes());
        return new String(IOUtils.readFully(conn.getInputStream(), -1, false));
    }
    
    private JSONObject cameraCallP(String url, String payload)throws IOException {
        return new JSONObject(cameraCall(url,payload));
    }


    private void onOK() {
        try {
        final JSONObject picture = cameraCallP(url, "{\n" +
                "    \"method\": \"actTakePicture\",\n" +
                "    \"params\": [],\n" +
                "    \"id\": 1,\n" +
                "    \"version\": \"1.0\"\n" +
                "}");
        final String url = picture.getJSONArray("result").getJSONArray(0).getString(0).replace("\\/", "/");
        System.out.println(url);

            ImageIcon img = new ImageIcon(new URL(url));
            image.setIcon(new ImageIcon(img.getImage().getScaledInstance(600,400, Image.SCALE_AREA_AVERAGING)));
            image.setText("");
            pack();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onCancel() {

        try {
            System.out.println(cameraCall(url, "{\n" +
                    "    \"method\": \"stopLiveview\",\n" +
                    "    \"params\": [],\n" +
                    "    \"id\": 1,\n" +
                    "    \"version\": \"1.0\"\n" +
                    "}"));
            System.out.println(cameraCall(url, "{\n" +
                    "    \"method\": \"stopRecMode\",\n" +
                    "    \"params\": [],\n" +
                    "    \"id\": 1,\n" +
                    "    \"version\": \"1.0\"\n" +
                    "}"));
        }catch (Exception e) {
            e.printStackTrace();
        }
        
        dispose();
        
        System.exit(0);
    }

}
