package org.leti.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.util.*;

public class ManagerAgent extends Agent {
    private int nResponders;
    private int orderId;

    protected void setup() {
        System.out.println("Manager agent " + getLocalName() + " is ready.");

        // Получение аргументов
        Object[] arguments = getArguments();
        String[] input = new String[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            input[i] = arguments[i].toString();
        }

        // Проверка количества аргументов на минимально корректное
        // (номер заказа, список исполнителей, координаты)
        if (input.length == 3) {
            // Получение номера заказа
            orderId = Integer.parseInt(input[0]);
            // Получение списка исполнителей
            String[] responders = input[1].replaceAll("\\[|\\]|\\s", "").split(",");
            nResponders = responders.length;
            // Получение координат
            String[] coordinates = input[2].replaceAll("\\[|\\]|\\s", "").split(",");

            // Формирование ACL-запроса
            ACLMessage msg = new ACLMessage(ACLMessage.CFP);
            // Формирование списка получателей
            for (String arg : responders) {
                msg.addReceiver(new AID(arg, AID.ISLOCALNAME));
            }

            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            // Ограничение на ответное сообщение в течение 10 секунд
            msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
            msg.setContent(String.join(",", coordinates));
            msg.setOntology("EarthRemoteScanning");

            System.out.println(getLocalName() + ": " + "Requesting action "
                    + msg.getContent() + " to " + nResponders + " responders.");

            // Добавляем поведение к агенту
            addBehaviour(new MyContractNetInitiator(this, msg));
        }
        else {
            System.out.println("Incorrect input. Should be (orderId):(receivers via .):(coordinates via .)");
        }
    }

    private class MyContractNetInitiator extends ContractNetInitiator {
        public MyContractNetInitiator(Agent a, ACLMessage cfp) {
            super(a, cfp);
        }

        // Обработчик предложений
        protected void handlePropose(ACLMessage propose, Vector v) {
            System.out.println(getLocalName() + ": " +
                    "Agent " + propose.getSender().getName() + " " +
                    "proposed " + propose.getContent() + " milliseconds");
        }

        // Обработчик отказов
        protected void handleRefuse(ACLMessage refuse) {
            System.out.println(getLocalName() + ": " +
                    "Agent " + refuse.getSender().getName() +
                    " refused to perform the requested action");
            nResponders--;
        }

        // Обработчик неудач
        protected void handleFailure(ACLMessage failure) {
            if (failure.getSender().equals(myAgent.getAMS())) {
                System.out.println(getLocalName() + ": " +
                        "No responder agents available");
            } else {
                System.out.println(getLocalName() + ": " +
                        "Agent " + failure.getSender().getName() +
                        " failed to perform the requested action");
            }
        }

        // Обработчик выполненных предложений
        protected void handleInform(ACLMessage inform) {
            System.out.println(getLocalName() + ": " +
                    "Agent " + inform.getSender().getName() +
                    " successfully performed the requested action");
            doDelete();
        }

        // Обработчик ответных предложений
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            if (responses.size() > 0) {
                ACLMessage bestProposal = null;
                double bestProposalValue = Double.MAX_VALUE;

                for (Object resp : responses) {
                    ACLMessage proposal = (ACLMessage) resp;
                    String content = proposal.getContent();

                    if (content != null && !content.isEmpty()) {
                        try {
                            double proposalValue = Double.parseDouble(content);

                            if (proposalValue < bestProposalValue) {
                                bestProposal = proposal;
                                bestProposalValue = proposalValue;
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid proposal value: " + content);
                        }
                    }
                }

                if (bestProposal != null) {
                    ACLMessage acceptance = bestProposal.createReply();
                    acceptance.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    acceptance.setContent(String.valueOf(orderId));
                    acceptances.add(acceptance);
                    System.out.println(getLocalName() + ": " + bestProposal.getSender().getName() + " has the best proposal");

                    // Отправка сообщения DispatcherAgent об исполнителе заказа
                    ACLMessage msgToDispatcher = new ACLMessage(ACLMessage.INFORM);
                    msgToDispatcher.addReceiver(new AID("dispatcherAgent", AID.ISLOCALNAME));
                    msgToDispatcher.setContent(bestProposal.getSender().getLocalName());
                    msgToDispatcher.setOntology("OrderAssigned");
                    send(msgToDispatcher);
                } else {
                    System.out.println(getLocalName() + ": No valid proposals received");
                }
            } else {
                System.out.println(getLocalName() + ": No proposals received");
            }
        }
    }
}