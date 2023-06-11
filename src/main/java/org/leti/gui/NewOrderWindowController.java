package org.leti.gui;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NewOrderWindowController implements Initializable {
    private static int gatewayId = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    private TextField xTF;

    @FXML
    private TextField yTF;

    @FXML
    private TextField wTF;

    @FXML
    private TextField hTF;

    @FXML
    private Label orderCreatedLabel;

    @FXML
    private Label emptyCoordinatesErrorLabel;

    @FXML
    private Label xErrorLabel;

    @FXML
    private Label yErrorLabel;

    @FXML
    private Label wErrorLabel;

    @FXML
    private Label hErrorLabel;

    @FXML
    private Label wMinErrorLabel;

    @FXML
    private Label hMinErrorLabel;

    @FXML
    void createOrder(ActionEvent event) {
        // Обработка ошибки пустого ввода
        if (xTF.getText().isEmpty() || yTF.getText().isEmpty() ||
                wTF.getText().isEmpty() || hTF.getText().isEmpty()) {
            emptyCoordinatesErrorLabel.setVisible(true);
        } else { // Обработка ошибки некорректных координат
            int x = Integer.parseInt(xTF.getText());
            int y = Integer.parseInt(yTF.getText());
            int w = Integer.parseInt(wTF.getText());
            int h = Integer.parseInt(hTF.getText());

            if (x < 0 || x > 2048) {
                setOtherLabelsInvisible();
                xErrorLabel.setVisible(true);
            } else if (y < 0 || y > 1024) {
                setOtherLabelsInvisible();
                yErrorLabel.setVisible(true);
            } else if (x + w > 2048) {
                setOtherLabelsInvisible();
                wErrorLabel.setVisible(true);
            } else if (y + h > 1024) {
                setOtherLabelsInvisible();
                hErrorLabel.setVisible(true);
            } else if (h < 10) {
                setOtherLabelsInvisible();
                hMinErrorLabel.setVisible(true);
            } else if (w < 10) {
                setOtherLabelsInvisible();
                wMinErrorLabel.setVisible(true);
            } else { // Создание заказа
                setOtherLabelsInvisible();

                // Получение интерфейса запущенной среды JADE
                jade.core.Runtime runtime = jade.core.Runtime.instance();

                // Создание профиля с настройками контейнера
                Profile profile = new ProfileImpl();
                profile.setParameter(Profile.MAIN_HOST, "localhost");
                profile.setParameter(Profile.MAIN_PORT, "1099");
                profile.setParameter(Profile.CONTAINER_NAME, "Main-Container");

                // Определение номера заказа
                int orderId = findMaxOrderId() + 1;

                try {
                    // Инициализация контейнера
                    ContainerController container = runtime.createAgentContainer(profile);

                    // Создание агента с указанными параметрами
                    Object[] args = {orderId , x, y, w, h};
                    AgentController agent = container.createNewAgent("gatewayAgent" + gatewayId++,
                            "org.leti.agents.GatewayAgent", args);

                    // Создание заглушки для результирующего изображения
                    createPlug(orderId);

                    // Запуск агента
                    agent.start();
                    orderCreatedLabel.setText("Заказ №" + orderId + " создан");
                    orderCreatedLabel.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    int findMaxOrderId() {
        String folderPath = "src/main/resources/pictures/Completed Orders/";

        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> {
            return name.matches("\\d+\\.jpg"); // Фильтр для файлов с расширением .jpg и именами вида число.jpg
        });

        int maxNumber = 0;
        if (files != null && files.length > 0) {
            for (File file : files) {
                String fileName = file.getName();
                int dotIndex = fileName.lastIndexOf(".");
                String numberString = fileName.substring(0, dotIndex);
                try {
                    int number = Integer.parseInt(numberString);
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid file name format: " + fileName);
                }
            }
        }

        return maxNumber;
    }

    void setOtherLabelsInvisible() {
        emptyCoordinatesErrorLabel.setVisible(false);
        xErrorLabel.setVisible(false);
        yErrorLabel.setVisible(false);
        wErrorLabel.setVisible(false);
        hErrorLabel.setVisible(false);
        wMinErrorLabel.setVisible(false);
        hMinErrorLabel.setVisible(false);
        orderCreatedLabel.setVisible(false);
    }

    void createPlug(int orderId) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        // Установка цвета пикселя
        int pixelColor = Color.BLACK.getRGB();
        image.setRGB(0, 0, pixelColor);

        File outputFile = new File("src/main/resources/pictures/Completed Orders/" + orderId + ".jpg");

        ImageIO.write(image, "jpg", outputFile);
    }
}
