import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.ResourceBundle;
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
        String temp = "";
        int i = 0;
        while (i < 10) {
            FileOutputStream imageOutFile = null;
            try {
                imageOutFile = new FileOutputStream("image" + i + ".jpeg", true);
            } catch (FileNotFoundException e) {
                    e.printStackTrace();
                continue;
            }
            while(true) {
                try {
                    socket.receive(receivedPacket);
                    if (new String(receivedPacket.getData()).contains("start")) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            while(true){
                try {
                    socket.receive(receivedPacket);
                    temp = new String(receivedPacket.getData());
                    if (temp.contains("finished")) break;
                    imageOutFile.write(Base64.getDecoder().decode(temp));
                } catch (IOException | RuntimeException e) {
                    System.out.println(temp);
                    e.printStackTrace();
                    break;
                }
            }
            try {
                imageOutFile.close();
//                piFrame.setImage(new Image("file:image.jpg"));
//                new File("image.jpg").delete();
            } catch (IOException | RuntimeException e) {
                    e.printStackTrace();
            }
            i++;
        }
    }
}