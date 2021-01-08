import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;


public class FXMLController implements Initializable {

    @FXML
    private StackPane streamView;
    @FXML
    private Pane streamDataView;
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
    private Text velocityText;
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
    DatagramSocket socket_metadata;
    DatagramPacket receivedMetadataPacket;
    DatagramPacket receivedPacket;
    byte[] receivedData;
    byte[] receivedMetadata;

    //Related to sending key signals to Pi
    DatagramSocket socket2;
    InetAddress ip;
    byte[] buf;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        startButton.setStyle("-fx-background-color: green; -fx-text-fill: white");
        stopButton.setStyle("-fx-background-color: red; -fx-text-fill: white");
        Line line1 = new Line(340, 631, 380, 531);
        Line line2 = new Line( 720, 631, 620, 531);
        line1.getStyleClass().add("line");
        line2.getStyleClass().add("line");
        streamDataView.getChildren().addAll(line1, line2);
        showBattery("High");

        try {
            socket = new DatagramSocket(2711);
            socket_metadata = new DatagramSocket(3333);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        receivedData = new byte[1024];
        receivedPacket = new DatagramPacket(receivedData, receivedData.length);
        receivedMetadata = new byte[20];
        receivedMetadataPacket = new DatagramPacket(receivedMetadata, receivedMetadata.length);

        //Set up socket to send key press signal to Pi
        try {
            socket2 = new DatagramSocket(1234);
//            ip = InetAddress.getByName("localhost");
            ip = InetAddress.getByName("192.168.137.254");
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
                    if (new String(receivedPacket.getData(), 0, receivedPacket.getLength()).contains("start")) break;
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
            while(true){
                try {
                    socket.receive(receivedPacket);
                    temp = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                    if (temp.contains("finished")) break;
                    imageByteStream.write(Base64.getDecoder().decode(temp));
                } catch (IOException | RuntimeException e) {
//                    e.printStackTrace();
                    break;
                }
            }
            Image image = new Image(new ByteArrayInputStream(imageByteStream.toByteArray()));
            if (image.getProgress() == 1 && !image.isError()) {
                piFrame.setImage(image);
            }
        };

        Runnable getMetadata = () -> {
            String temp;
            try {
                socket_metadata.receive(receivedMetadataPacket);
                temp = new String(receivedMetadataPacket.getData()) + "";
                double velocity = Double.parseDouble(temp);
                if (velocity < 1) velocity = 0;
                velocityText.setText(new DecimalFormat("#00.00").format(velocity) + " cm/s");
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(frameExtracter, 0, 1, TimeUnit.MILLISECONDS);
        ScheduledExecutorService timer1 = Executors.newSingleThreadScheduledExecutor();
        timer1.scheduleAtFixedRate(getMetadata, 0, 1, TimeUnit.MICROSECONDS);
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
                sendKey("+");
                break;
            case "Minus":
                speedDownButton.setStyle("-fx-background-color: black; -fx-text-fill: white");
                sendKey("-");
                break;
            default:
                //Do nothing
        }

        //Check for key combinations
        if (aPressed && wPressed) {
            sendKey("q");
        } else if (dPressed && wPressed) {
            sendKey("e");
        } else if (aPressed && sPressed) {
            sendKey("z");
        } else if (dPressed && sPressed) {
            sendKey("c");
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
            sendKey("q");
        } else if (dPressed && wPressed) {
            sendKey("e");
        } else if (aPressed && sPressed) {
            sendKey("z");
        } else if (dPressed && sPressed) {
            sendKey("c");
        } else if (aPressed) {
            sendKey("a");
        } else if (wPressed) {
            sendKey("w");
        } else if (dPressed) {
            sendKey("d");
        } else if (sPressed) {
            sendKey("s");
        } else {
            sendKey("x");
            sendKey("x");
            sendKey("x");
            aPressed = false;
            wPressed = false;
            dPressed = false;
            sPressed = false;
        }
    }

    //Send the key character from JavaFX to Pi
    public void sendKey(String key) {
        try {
            buf = key.getBytes();
            DatagramPacket dpSend = new DatagramPacket(buf, buf.length, ip, 2345);
            socket2.send(dpSend);
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