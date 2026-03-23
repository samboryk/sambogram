package pr.sambogram;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 850, 650);

        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("LOGO.png")));
        } catch (Exception e) { }

        stage.setTitle("Sambogram Messenger");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}