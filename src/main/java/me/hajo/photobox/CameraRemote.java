package me.hajo.photobox;

import org.json.JSONObject;
import sun.misc.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class CameraRemote {
    final String baseURL;

    public CameraRemote(String baseURL) throws IOException {
        this.baseURL = baseURL;
    }
    
    public void start() throws IOException {
        startRecMode();
        waitUntilCameraIsIDLE();

        setShootModeStill();
        waitUntilCameraIsIDLE();

        setPostviewImageSizeOriginal();
        waitUntilCameraIsIDLE();
    }
    
    // start stop liveview
    
    public String takePictureAndGetURL() throws IOException {
        final JSONObject picture = cameraCallP("{\n" +
                "    \"method\": \"actTakePicture\",\n" +
                "    \"params\": [],\n" +
                "    \"id\": 1,\n" +
                "    \"version\": \"1.0\"\n" +
                "}");
        return picture.getJSONArray("result").getJSONArray(0).getString(0).replace("\\/", "/");
    }
    
    public void stop() throws IOException {
        stopRecMode();
    }
    
    

    public void stopRecMode() throws IOException {
        System.out.println(cameraCall("{\n" +
                "    \"method\": \"stopRecMode\",\n" +
                "    \"params\": [],\n" +
                "    \"id\": 1,\n" +
                "    \"version\": \"1.0\"\n" +
                "}"));
    }

    public void stopLiveView() throws IOException {
        System.out.println(cameraCall("{\n" +
                "    \"method\": \"stopLiveview\",\n" +
                "    \"params\": [],\n" +
                "    \"id\": 1,\n" +
                "    \"version\": \"1.0\"\n" +
                "}"));
    }

    public String startLiveViewAndGetURL() throws IOException {
        final String startLiveView = "{\n" +
                "    \"method\": \"startLiveview\",\n" +
                "    \"params\": [],\n" +
                "    \"id\": 1,\n" +
                "    \"version\": \"1.0\"\n" +
                "}";
        final JSONObject result2 = cameraCallP(startLiveView);
        System.out.println(result2);
        return result2.getJSONArray("result").getString(0);
    }

    public void setPostviewImageSizeOriginal() throws IOException {
        System.out.println(cameraCallRetryNAN("{\n" +
                "    \"method\": \"setPostviewImageSize\",\n" +
                "    \"params\": [\"Original\"],\n" +
                "    \"id\": 1,\n" +
                "    \"version\": \"1.0\"\n" +
                "}"));
    }

    public void setShootModeStill() throws IOException {
        System.out.println(cameraCallRetryNAN("{\n" +
                "    \"method\": \"setShootMode\",\n" +
                "    \"params\": [\"still\"],\n" +
                "    \"id\": 1,\n" +
                "    \"version\": \"1.0\"\n" +
                "}"));
    }

    public void startRecMode() throws IOException {
        final String startRecMode = "{\n" +
                "    \"method\": \"startRecMode\",\n" +
                "    \"params\": [],\n" +
                "    \"id\": 1,\n" +
                "    \"version\": \"1.0\"\n" +
                "}";
        final String result = cameraCall(startRecMode);
        System.out.println(result);
    }


    public void waitUntilCameraIsIDLE() {
        JSONObject event = null;
        boolean wait = false;
        try {
            while(true) {
                event = cameraCallP("{\n" +
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
            throw new HajoRestartException(e);
        }
    }


    private JSONObject cameraCallRetryNAN(String s) throws IOException {
        JSONObject rr;
        do {
            rr = cameraCallP(s);
        } while(rr.has("error") && rr.getJSONArray("error").getString(1).equals("Not Available Now"));
        return rr;
    }
    
    private JSONObject cameraCallP(String payload)throws IOException {
        return new JSONObject(cameraCall(payload));
    }

    private String cameraCall(final String payload) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) new URL(baseURL).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.connect();
        conn.getOutputStream().write(payload.getBytes());
        return new String(IOUtils.readFully(conn.getInputStream(), -1, false));
    }

}
