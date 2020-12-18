import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;


public class MainApp extends Application {
//    private Boolean aPressed = false;

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("scene.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

//        scene.setOnKeyPressed(event -> {
//            switch (event.getCode().getName()) {
//                case "A":
//                    System.out.println("A");
//                    aPressed = true;
//                    System.out.println(aPressed);
//                    break;
//                default:
//                    //Do nothing
//            }
//        });
//
//        scene.setOnKeyReleased(event -> {
//            switch (event.getCode().getName()) {
//                case "A":
//                    aPressed = false;
//                    System.out.println(aPressed);
//                    break;
//                default:
//                    //Do nothing
//            }
//        });

        stage.setTitle("Control Station");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}