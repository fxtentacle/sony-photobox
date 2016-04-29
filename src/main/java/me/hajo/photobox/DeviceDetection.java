package me.hajo.photobox;

import javafx.scene.Camera;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.ServiceTypeHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import sun.misc.IOUtils;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.IOException;
import java.net.URL;

/**
 * Created by fxtentacle on 12.04.16.
 */
public class DeviceDetection implements RegistryListener {

    final ServiceType type = new ServiceType("schemas-sony-com", "ScalarWebAPI", 1);

    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice remoteDevice) {
    }

    public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice remoteDevice, Exception e) {
    }

    public void remoteDeviceAdded(Registry registry, RemoteDevice remoteDevice) {
        final RemoteDeviceIdentity rdi = (RemoteDeviceIdentity) remoteDevice.getIdentity();
        final URL descriptorURL = rdi.getDescriptorURL();
        try {
            JSONObject desc = XML.toJSONObject(new String(IOUtils.readFully(descriptorURL.openStream(),-1,false)));
            System.out.println(desc.toString());
            desc = desc.getJSONObject("root").getJSONObject("device");
            desc = desc.getJSONObject("av:X_ScalarWebAPI_DeviceInfo");
            desc = desc.getJSONObject("av:X_ScalarWebAPI_ServiceList");
            JSONArray services = desc.getJSONArray("av:X_ScalarWebAPI_Service");
            for(int i=0;i<services.length();i++) {
                JSONObject t = services.getJSONObject(i);
                if(t.getString("av:X_ScalarWebAPI_ServiceType").equals("camera")) {
                    cb.notifyCameraURL(t.getString("av:X_ScalarWebAPI_ActionList_URL") + "/camera");
                    registry.removeListener(this);
                    registry.shutdown();
                }
            }
        } catch (IOException e) {
            throw new HajoRestartException(e);
        }
    }

    public void remoteDeviceUpdated(Registry registry, RemoteDevice remoteDevice) {
    }

    public void remoteDeviceRemoved(Registry registry, RemoteDevice remoteDevice) {
    }

    public void localDeviceAdded(Registry registry, LocalDevice localDevice) {
    }

    public void localDeviceRemoved(Registry registry, LocalDevice localDevice) {
    }

    public void beforeShutdown(Registry registry) {
    }

    public void afterShutdown() {
    }

    public static interface CB {
        public void notifyCameraURL(String url);
    }
    private CB cb;
    public void findCamera(CB cbi) {
        cb = cbi;
        
        UpnpService upnpService = new UpnpServiceImpl();
        
        upnpService.getRegistry().addListener(this);
        
        upnpService.getControlPoint().search(
                new ServiceTypeHeader(type)
        );
    }
    
    public static void main(String [] args) {
        new DeviceDetection().findCamera(new CB() {
            public void notifyCameraURL(String url) {
                try {
                    String host = "127.0.0.1:5000";
                    //host = JOptionPane.showInputDialog("Server IP/hostname:port (192.168.42.128:8765)");
                    CameraRemote remote = new CameraRemote(url);
                    FileUploader uploader = new FileUploader(host);
                    PhotoboxGUI dialog = new PhotoboxGUI(remote,uploader);
                } catch (IOException e) {
                    throw new HajoRestartException(e);
                } catch (UnsupportedAudioFileException e) {
                    throw new HajoRestartException(e);
                } catch (LineUnavailableException e) {
                    throw new HajoRestartException(e);
                }
            }
        });
    }
}
