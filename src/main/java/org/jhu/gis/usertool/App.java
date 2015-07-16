package org.jhu.gis.usertool;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Controller controller = new Controller();
        controller.startApp();

        Scene scene = new Scene(controller.asParent(), 700, 600);
        scene.getStylesheets().add("/css/app.css");

        ClassLoader cl = ClassLoader.getSystemClassLoader();
        ResourceBundle bundle = ResourceBundle.getBundle("bundles/labels", Locale.getDefault());

        primaryStage.setTitle(bundle.getString("usertool.label"));
        primaryStage.getIcons().add(new Image(App.class.getResourceAsStream("/logo.png")));
        primaryStage.setScene(scene);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
