import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableRow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class FXMLController implements Initializable {

    @FXML
    private ImageView piFrame;
    @FXML
    private Button forwardButton;
    @FXML
    private Button backButton;
    @FXML
    private Button leftButton;
    @FXML
    private Button rightButton;
    @FXML
    private Button liftButton;
    @FXML
    private Button dropButton;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button speedUpButton;
    @FXML
    private Button speedDownButton;
    @FXML
    private Rectangle lowBar;
    @FXML
    private Rectangle mediumBar;
    @FXML
    private Rectangle highBar;

    //For detecting key combinations
//    final BooleanProperty aPressed = new SimpleBooleanProperty(false);
//    final BooleanProperty wPressed = new SimpleBooleanProperty(false);
//    final BooleanBinding awPressed = aPressed.and(wPressed);
    private Boolean aPressed = false;
    private Boolean wPressed = false;
    private Boolean sPressed = false;
    private Boolean dPressed = false;

    DatagramSocket socket;
    DatagramPacket receivedPacket;
    byte[] receivedData;

    //Related to sending key signals to Pi
    DatagramSocket socket2;
    InetAddress ip;
    byte[] buf;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        startButton.setStyle("-fx-background-color: green; -fx-text-fill: white");
        stopButton.setStyle("-fx-background-color: red; -fx-text-fill: white");
        showBattery("High");

        try {
            socket = new DatagramSocket(2711);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        receivedData = new byte[1024];
        receivedPacket = new DatagramPacket(receivedData, receivedData.length);

        //Set up socket to send key press signal to Pi
        try {
            socket2 = new DatagramSocket(1234);
            ip = InetAddress.getByName("localhost");
            buf = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    @FXML
    private void onKeyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getCode().getName()) {
            case "A":
                leftButton.setStyle("-fx-background-color: black; -fx-text-fill: white");
                aPressed = true;
                break;
            case "W":
                forwardButton.setStyle("-fx-background-color: black; -fx-text-fill: white");
                wPressed = true;
                break;
            case "S":
                backButton.setStyle("-fx-background-color: black; -fx-text-fill: white");
                sPressed = true;
                break;
            case "D":
                rightButton.setStyle("-fx-background-color: black; -fx-text-fill: white");
                dPressed = true;
                break;
            case "J":
                liftButton.setStyle("-fx-background-color: black; -fx-text-fill: white");
                sendKey("J");
                break;
            case "K":
                dropButton.setStyle("-fx-background-color: black; -fx-text-fill: white");
                sendKey("K");
                break;
            case "Equals":
                speedUpButton.setStyle("-fx-background-color: black; -fx-text-fill: white");
                sendKey("Equals");
                break;
            case "Minus":
                speedDownButton.setStyle("-fx-background-color: black; -fx-text-fill: white");
                sendKey("Minus");
                break;
            default:
                //Do nothing
        }

        //Check for key combinations
        if (aPressed && wPressed) {
            sendKey("aw");
        } else if (dPressed && wPressed) {
            sendKey("dw");
        } else if (aPressed && sPressed) {
            sendKey("as");
        } else if (dPressed && sPressed) {
            sendKey("ds");
        } else if (aPressed) {
            sendKey("a");
        } else if (wPressed) {
            sendKey("w");
        } else if (dPressed) {
            sendKey("d");
        } else if (sPressed) {
            sendKey("s");
        }
    }

    @FXML
    private void onKeyReleased(KeyEvent keyEvent) {
        switch (keyEvent.getCode().getName()) {
            case "A":
                leftButton.setStyle(null);
                aPressed = false;
                break;
            case "W":
                forwardButton.setStyle(null);
                wPressed = false;
                break;
            case "S":
                backButton.setStyle(null);
                sPressed = false;
                break;
            case "D":
                rightButton.setStyle(null);
                dPressed = false;
                break;
            case "J":
                liftButton.setStyle(null);
                break;
            case "K":
                dropButton.setStyle(null);
                break;
            case "Enter":
                startButton.setDisable(true);
                sendKey("Enter");
                startStream();
                break;
            case "Backspace":
                startButton.setDisable(false);
                sendKey("Backspace");
            case "Equals":
                speedUpButton.setStyle(null);
                break;
            case "Minus":
                speedDownButton.setStyle(null);
                break;
            default:
                //Do nothing
        }

        //Check for key combinations
        if (aPressed && wPressed) {
            sendKey("aw");
        } else if (dPressed && wPressed) {
            sendKey("dw");
        } else if (aPressed && sPressed) {
            sendKey("as");
        } else if (dPressed && sPressed) {
            sendKey("ds");
        } else if (aPressed) {
            sendKey("a");
        } else if (wPressed) {
            sendKey("w");
        } else if (dPressed) {
            sendKey("d");
        } else if (sPressed) {
            sendKey("s");
        }
    }

    //Send the key character from JavaFX to Pi
    public void sendKey(String key) {
        try {
            buf = key.getBytes();
            DatagramPacket dpSend = new DatagramPacket(buf, buf.length, ip, 2345);
            socket2.send(dpSend);
            System.out.println("key signal sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Show battery percentage in 3 levels
    public void showBattery(String level) {
        switch (level) {
            case "Low":
                lowBar.setFill(Color.RED);
                mediumBar.setFill(Color.WHITE);
                highBar.setFill(Color.WHITE);
                break;
            case "Medium":
                lowBar.setFill(Color.YELLOW);
                mediumBar.setFill(Color.YELLOW);
                highBar.setFill(Color.WHITE);
                break;
            case "High":
                lowBar.setFill(Color.GREEN);
                mediumBar.setFill(Color.GREEN);
                highBar.setFill(Color.GREEN);
                break;
            default:
                //Do nothing
        }
    }
}