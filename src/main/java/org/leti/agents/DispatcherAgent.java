package org.leti.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import javafx.application.Platform;
import org.leti.gui.MainController;

import java.util.*;

public class DispatcherAgent extends Agent {
    private static int managerId = 1;
    private Queue<Order> ordersQueue = new LinkedList<>();
    private List<String> freeExecutors = new ArrayList<>();

    protected void setup() {
        // Задержка в 10 миллисекунд, чтобы остальные агенты успели инициализироваться
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Заполнение списка свободных агентов-исполнителей
        findAgents("satellite");
        System.out.println(freeExecutors);

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    // Если сообщение от GatewayAgent о новом заказе
                    if (msg.getOntology().equals("NewOrder")) {
                        String[] content = msg.getContent().split(",");
                        int orderId = Integer.parseInt(content[0]);
                        int[] coordinates = new int[4];
                        for (int i = 0; i < 4; ++i) {
                            coordinates[i] = Integer.parseInt(content[i+1]);
                        }
                        // Добавление заказа в очередь
                        Order order = new Order(orderId, coordinates);
                        ordersQueue.add(order);
                        updateInterface(orderId, "queue");

                        // Если есть свободные исполнители, поручить заказ
                        if (!freeExecutors.isEmpty()) {
                            assignOrder();
                        }
                    } // Если сообщение от satelliteAgent об освобождении исполнителя
                    else if (msg.getOntology().equals("OrderCompleted")) {
                        freeExecutors.add(msg.getContent());
                        // Если есть заказы в очереди, поручить заказ
                        if (!ordersQueue.isEmpty()) {
                            assignOrder();
                        }
                    } // Если сообщение от ManagerAgent о назначении заказа исполнителю
                    else if (msg.getOntology().equals("OrderAssigned")) {
                        String agentName = msg.getContent();
                        freeExecutors.remove(agentName);
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void findAgents(String searchName) {
        // Создаем описание агента и добавляем в него искомое имя
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("satellite-agent");
        dfd.addServices(sd);
        try {
            // Выполняем поиск агентов с указанным именем в DF (сервис желтых страниц)
            DFAgentDescription[] result = DFService.search(this, dfd);
            if (result.length > 0) {
                for (DFAgentDescription agentDesc : result) {
                    String agentName = agentDesc.getName().getLocalName();
                    if (agentName.contains(searchName)) {
                        freeExecutors.add(agentName);
                    }
                }
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private void assignOrder() {
        AgentContainer container = getContainerController();
        // Поручение заказа агенту-менеджеру
        try {
            int orderId = ordersQueue.peek().getOrderId();
            updateInterface(orderId, "in progress");
            Object[] args = {ordersQueue.peek().getOrderId(),
                    freeExecutors,
                    Arrays.toString(ordersQueue.poll().getCoordinates())
            };
            AgentController agent = container.createNewAgent("managerAgent" + managerId++,
                    "org.leti.agents.ManagerAgent", args);
            agent.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private void updateInterface(int orderId, String status) {
        MainController controller = ControllerHandlerAgent.getInstance().getController();
        Platform.runLater(() -> controller.addOrder(orderId, status));
    }

    public class Order {
        private int[] coordinates;
        private int orderId;

        public Order(int orderId, int[] coordinates) {
            this.orderId = orderId;
            this.coordinates = coordinates;
        }

        public int[] getCoordinates() {
            return coordinates;
        }

        public int getOrderId() {
            return orderId;
        }
    }
}