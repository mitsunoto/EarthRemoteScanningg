package org.leti.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class GatewayAgent extends Agent {

    protected void setup() {
        // Получение аргументов
        Object[] arguments = getArguments();
        String[] input = new String[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            input[i] = arguments[i].toString();
        }

        // Создание и отправка сообщения типа INFORM
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("dispatcherAgent", AID.ISLOCALNAME));
        msg.setContent(String.join(",", input));
        msg.setOntology("NewOrder");
        send(msg);

        // Удаление агента-шлюза
        doDelete();
    }
}
