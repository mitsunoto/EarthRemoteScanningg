package org.leti.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import javafx.application.Platform;
import org.leti.gui.MainController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.lang.Math;

public class SatelliteAgent extends Agent {
    private int xPos = 0, yPos = 0, orderId;
    double speed = 20; // Скорость передвижения (20 пикселей в секунду)

    protected void setup() {
        // Создание описания услуги
        ServiceDescription sd = new ServiceDescription();
        sd.setType("satellite-agent");
        sd.setName(getLocalName());

        // Создание описания агента и добавление к нему описания услуги
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);

        try {
            // Регистрация агента и его услуги в DF (сервис желтых страниц)
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        Random rand = new Random();
        xPos = rand.nextInt(2048);
        yPos = rand.nextInt(1024);

        updateInterface();
        System.out.println("Responder agent " + getLocalName() + " is ready. x = " + xPos + " y = " + yPos);

        // Создание шаблона для сообщения-инициатора
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));

        // Добавляем поведение, которое будет обрабатывать сообщения менеджера
        addBehaviour(new MyContractNetResponder(this, template));
    }

    private class MyContractNetResponder extends ContractNetResponder {
        private String result;
        public MyContractNetResponder(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        protected ACLMessage handleCfp(ACLMessage cfp) {
            System.out.println(getLocalName() + ": Received CFP from " +
                    cfp.getSender().getName() + ". Action is " + cfp.getContent());

            // Получаем координаты из сообщения-инициатора
            int[] coordinates = parseCoordinates(cfp.getContent());

            // Проверка, возможно ли выполнить предлагаемое действие
            if (checkAction(coordinates)) {
                // Если возможно, то рассчитываем примерное время выполнения
                double actionTime = calculateActionTime(coordinates);

                // Создаем сообщение с предложением выполнить задачу и отправляем его инициатору
                System.out.println(getLocalName() + ": Agree");
                ACLMessage proposal = cfp.createReply();
                proposal.setPerformative(ACLMessage.PROPOSE);
                proposal.setContent(String.valueOf((int) actionTime));
                proposal.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
                return proposal;
            } else {
                // Если нет, то отказываемся
                System.out.println(getLocalName() + ": Refuse");
                ACLMessage refuse = cfp.createReply();
                refuse.setPerformative(ACLMessage.REFUSE);
                return refuse;
            }
        }

        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            System.out.println(getLocalName() + ": " +
                    "Received ACCEPT_PROPOSAL from " + accept.getSender().getName());

            orderId = Integer.parseInt(accept.getContent());

            // Получаем координаты из сообщения-инициатора
            int[] coordinates = parseCoordinates(cfp.getContent());

            // Пытаемся выполнить задачу
            try {
                System.out.println(getLocalName() + ": Started to perform the action");

                long startTime = System.currentTimeMillis();

                // Если задача выполнена успешно, то отправляем ответ
                performAction(coordinates);

                long endTime = System.currentTimeMillis();

                System.out.println(getLocalName() + ": Action successfully performed. " +
                        "Elapsed time " + (endTime - startTime) + " milliseconds. " +
                        "The answer is " + result);
                ACLMessage inform = cfp.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                inform.setContent(result);

                // Создаем сообщение для DispatcherAgent
                ACLMessage msgToDispatcher = new ACLMessage(ACLMessage.INFORM);
                msgToDispatcher.addReceiver(new AID("dispatcherAgent", AID.ISLOCALNAME));
                msgToDispatcher.setContent(getLocalName());
                msgToDispatcher.setOntology("OrderCompleted");
                send(msgToDispatcher);

                updateInterface(orderId);

                return inform;
            } catch (InterruptedException e) {
                // иначе отправляем сообщение об ошибке
                System.out.println(getLocalName() + ": Action failed");
                ACLMessage failure = cfp.createReply();
                failure.setPerformative(ACLMessage.FAILURE);
                failure.setContent(e.getMessage());
                return failure;
            }
        }

        private int[] parseCoordinates(String input) {
            // Удаление скобок по краям
            if (input.startsWith("(")) {
                input = input.substring(1, input.length() - 1).trim();
            }

            // Разделение координат
            String[] stringCoordinates = input.split(",");
            int[] coordinates = new int[4];
            for (int i = 0; i < 4; ++i) {
                coordinates[i] = Integer.parseInt(stringCoordinates[i].trim());
            }

            return coordinates;
        }

        private double calculateActionTime(int[] coordinates) {
            int x1 = coordinates[0];
            int y1 = coordinates[1];
            int w = coordinates[2];
            int h = coordinates[3];
            double distance;

            // Находим кратчайшее расстояние до зондируемой области
            distance = Math.sqrt(Math.pow((x1 - xPos),2) + Math.pow((y1 - yPos),2));
            distance = Math.min(distance, Math.sqrt(Math.pow((x1+w - xPos),2) + Math.pow((y1+h - yPos),2)));
            distance = Math.min(distance, Math.sqrt(Math.pow((x1 - xPos),2) + Math.pow((y1+h - yPos),2)));
            distance = Math.min(distance, Math.sqrt(Math.pow((x1+w - xPos),2) + Math.pow((y1 - yPos),2)));

            return (distance/speed)*1000 + 2000; // (moving + action) time in milliseconds
        }

        private boolean checkAction(int[] coordinates) {
            int x1 = coordinates[0];
            int y1 = coordinates[1];
            int x2 = coordinates[2];
            int y2 = coordinates[3];
            return (x1 >= 0 && y1 >= 0 && x2 <= 2048 && y2 <= 1024);
        }

        private void performAction(int[] coordinates) throws InterruptedException {
            int x1 = coordinates[0];
            int y1 = coordinates[1];
            int w = coordinates[2];
            int h = coordinates[3];

            // Плавное перемещение агента к целевым координатам
            moveto(x1+(w/2), y1+(h/2));

            // Путь к файлу изображения Земли
            String sourcePath = "src/main/resources/pictures/8k_earth_daymap.jpg";

            // Путь к будущему изображению результата зондирования
            String destinationPath = "src/main/resources/pictures/Completed Orders/" + orderId + ".jpg";

            try {
                // Открытие файла изображения
                File sourceFile = new File(sourcePath);
                BufferedImage image = ImageIO.read(sourceFile);

                // Кадрирование изображения
                BufferedImage croppedImage = image.getSubimage(x1*4, y1*4, w*4, h*4);

                Thread.sleep(2000);

                // Сохранение нового файла изображения
                File destinationFile = new File(destinationPath);
                File destinationDir = destinationFile.getParentFile();
                if (!destinationDir.exists()) {
                    destinationDir.mkdirs(); // создание каталога, если он не существует
                }
                ImageIO.write(croppedImage, "jpg", destinationFile);

                result = destinationPath;
            } catch (IOException e) {
                // Если произошла ошибка при чтении или записи файла, выводим сообщение об ошибке
                System.err.println("Error performing action: " + e.getMessage());
            }
        }
    }

    private void moveto(int x, int y) throws InterruptedException {
        while (xPos != x || yPos != y) {
            Thread.sleep((long) (1000/speed));
            // Проверяем текущие координаты агента и целевые координаты
            if (xPos < x) {
                xPos++;
            } else if (xPos > x) {
                xPos--;
            }
            if (yPos < y) {
                yPos++;
            } else if (yPos > y) {
                yPos--;
            }
            // Обновление местоположения спутника в интерфейсе приложения
            updateInterface();
        }
    }

    private void updateInterface() {
        MainController controller = ControllerHandlerAgent.getInstance().getController();
        Platform.runLater(() -> controller.updateSatelliteLocation(this.getLocalName(), xPos+28, yPos+48));
    }

    private void updateInterface(int orderId) {
        MainController controller = ControllerHandlerAgent.getInstance().getController();
        Platform.runLater(() -> controller.removeOrder(orderId));
    }
}