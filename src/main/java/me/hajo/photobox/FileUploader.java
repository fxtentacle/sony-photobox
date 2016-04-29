package me.hajo.photobox;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import sun.net.www.http.HttpClient;

import java.net.URL;


public class FileUploader {
    final String host; // 192.168.42.128:8765
    
    public FileUploader(String host) {
        this.host = host;
    }
    
    public void upload(final byte[] data) {
        new Thread() {
            @Override
            public void run() {
                try {
                    org.apache.http.client.HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost("http://" + host + "/post_pic");

                    MultipartEntity reqEntity = new MultipartEntity();
                    reqEntity.addPart("file", new ByteArrayBody(data, "image/jpeg", "photobox.jpg"));
                    httpPost.setEntity(reqEntity);

                    httpclient.execute(httpPost);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
