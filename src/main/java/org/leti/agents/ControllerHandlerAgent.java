package org.leti.agents;

import jade.core.Agent;
import org.leti.gui.MainController;

public class ControllerHandlerAgent extends Agent {
    private static ControllerHandlerAgent instance = new ControllerHandlerAgent();
    private MainController controller;

    public static ControllerHandlerAgent getInstance() {
        return instance;
    }

    public void setController(MainController controller) {
        this.controller = controller;
    }

    public MainController getController() {
        return controller;
    }
}
