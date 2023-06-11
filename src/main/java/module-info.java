module org.leti {
    requires javafx.controls;
    requires javafx.fxml;
    requires jade;
    requires java.desktop;


    opens org.leti.gui to javafx.fxml;
    exports org.leti.gui;
    exports org.leti.agents to jade;
    exports org.leti.utils;
    opens org.leti.utils to javafx.fxml;
}