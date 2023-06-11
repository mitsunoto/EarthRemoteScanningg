package org.leti.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MainController {

    @FXML
    private Button newOrderButton;

    @FXML
    private ImageView satelliteIcon1;

    @FXML
    private ImageView satelliteIcon2;
    @FXML
    private ImageView satelliteIcon3;
    @FXML
    private ImageView satelliteIcon4;

    @FXML
    private ListView<String> listView;

    private ObservableList<String> orders;

    @FXML
    public void initialize() {
        // Создание наблюдаемого списка
        orders = FXCollections.observableArrayList();

        // Связывание ListView с наблюдаемым списком
        listView.setItems(orders);
    }

    @FXML
    void openNewOrderWindow(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ERSApplication.class.getResource("NewOrderWindow.fxml"));

        // Настройка сцены для создания заказов
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pictures/newOrder.png")));
        Stage stage = new Stage();
        stage.getIcons().add(icon);
        Scene scene = new Scene(fxmlLoader.load(), 293, 155);
        stage.setTitle("Создание нового заказа");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(newOrderButton.getScene().getWindow());
        stage.show();
    }

    @FXML
    void showCompletedOrders(ActionEvent event) {
        String folderPath = "src/main/resources/pictures/Completed Orders";

        File folder = new File(folderPath);
        if (folder.exists()) {
            try {
                Desktop.getDesktop().open(folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Folder doesn't exists");
        }
    }

    public void updateSatelliteLocation(String agentName, int x, int y) {
        if (Objects.equals(agentName, "satellite1")) {
            satelliteIcon1.setLayoutX(x / 2);
            satelliteIcon1.setLayoutY(y / 2);
        } else if (Objects.equals(agentName, "satellite2")) {
            satelliteIcon2.setLayoutX(x / 2);
            satelliteIcon2.setLayoutY(y / 2);
        } else if (Objects.equals(agentName, "satellite3")) {
            satelliteIcon3.setLayoutX(x / 2);
            satelliteIcon3.setLayoutY(y / 2);
        } else if (Objects.equals(agentName, "satellite4")) {
            satelliteIcon4.setLayoutX(x / 2);
            satelliteIcon4.setLayoutY(y / 2);
        }
    }

    public void addOrder(int orderId, String status) {
        if (Platform.isFxApplicationThread()) {
            String statusText = null;
            if (status.equals("queue")) {
                //statusCircle.setFill(Color.GRAY);
                statusText = "В очереди";
            } else if (status.equals("in progress")) {
                orders.remove(orderId + " - " + "В очереди");
                //statusCircle.setFill(Color.GREEN);
                statusText = "Выполняется";
            }

            orders.add(orderId + " - " + statusText);
        }
    }

    public void removeOrder(int orderId) {
        if (Platform.isFxApplicationThread()) {
            orders.remove(orderId + " - " + "В очереди");
            orders.remove(orderId + " - " + "Выполняется");
        }
    }
}
