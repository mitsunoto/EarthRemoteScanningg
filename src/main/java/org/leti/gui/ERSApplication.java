package org.leti.gui;

import jade.Boot;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.leti.agents.ControllerHandlerAgent;

import java.io.IOException;
import java.util.Objects;

public class ERSApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        // Запуск настройки графического интерфейса
        startGUI(stage);

        // Запуск среды JADE и стартовых агентов
        startJade();

        stage.show();
    }

    public void startGUI(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ERSApplication.class.getResource("MainController.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1184, 572);

        // Передача контроллера агенту-локатору
        MainController controller = fxmlLoader.getController();
        ControllerHandlerAgent.getInstance().setController(controller);

        // Настройка главной сцены приложения
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pictures/satelliteRedIcon.png")));
        stage.getIcons().add(icon);
        stage.setTitle("Earth Remote Scanning");
        stage.setScene(scene);
        stage.setResizable(false);
    }

    public void startJade() {
        // Установка CLASSPATH для JADE
//        String classpath = System.getProperty("java.class.path");
//        classpath += ";../../../../resources/lib/jade.jar;";
//        System.setProperty("java.class.path", classpath);

        // Команда для запуска агентов
        String[] jadeArgs = {
                "-gui",
                "-agents",
                "satellite1:org.leti.agents.SatelliteAgent;" +
                        "satellite2:org.leti.agents.SatelliteAgent;" +
                        "satellite3:org.leti.agents.SatelliteAgent;" +
                        "satellite4:org.leti.agents.SatelliteAgent;" +
                        "dispatcherAgent:org.leti.agents.DispatcherAgent;"
        };

        // Запуск платформы JADE
        Boot.main(jadeArgs);
    }

    public static void main(String[] args) {
        launch();
    }
}