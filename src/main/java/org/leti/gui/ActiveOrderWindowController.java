package org.leti.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class ActiveOrderWindowController {
    @FXML
    private ListView<Integer> listView;
    private ObservableList<Integer> orders;

    @FXML
    public void initialize() {
        // Создание наблюдаемого списка
        orders = FXCollections.observableArrayList();

        // Связывание ListView с наблюдаемым списком
        listView.setItems(orders);
    }
}