import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class FXMLController implements Initializable {

    @FXML
    private ImageView piFrame;

    DatagramSocket socket;
    DatagramPacket receivedPacket;
    byte[] receivedData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            socket = new DatagramSocket(2711);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        receivedData = new byte[1024];
        receivedPacket = new DatagramPacket(receivedData, receivedData.length);
    }

    public void startStream(){
        Runnable frameExtracter = () -> {
            String temp = "";
            ByteArrayOutputStream imageByteStream = new ByteArrayOutputStream();
            while(true) {
                try {
                    socket.receive(receivedPacket);
                    if (new String(receivedPacket.getData()).contains("start")) break;
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
            while(true){
                try {
                    socket.receive(receivedPacket);
                    temp = new String(receivedPacket.getData());
                    if (temp.contains("finished")) break;
                    imageByteStream.write(Base64.getDecoder().decode(temp));
                } catch (IOException | RuntimeException e) {
//                    e.printStackTrace();
                    break;
                }
            }
            Image image = new Image(new ByteArrayInputStream(imageByteStream.toByteArray()));
            piFrame.setImage(image);
        };
        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(frameExtracter, 0, 33, TimeUnit.MILLISECONDS);
    }
}