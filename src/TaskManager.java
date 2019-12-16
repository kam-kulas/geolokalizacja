import Models.Parking;
import Tools.Logger;
import jade.core.Agent;

import java.io.IOException;
import java.io.Serializable;
import java.lang.Math;
import java.util.*;
import java.util.List;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import MessageTemplate.FindFreeSlotsContent;
import MessageTemplate.ParkingPositionContent;
import MessageTemplate.ParkingsToChooseByUserContent;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;

public class TaskManager extends Agent {

    private Logger logger;
    private Dictionary<AID, Parking> parkings = new Hashtable<>();
    private Dictionary<String, UserSession> userSessions = new Hashtable<>();


    @Override
    protected void setup(){

        System.out.println("Agent: " + getLocalName());
        logger = new Logger();

        // Rejestracja agenta na DFD
        DFAgentDescription dfad = new DFAgentDescription();
        dfad.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("TaskManager");
        sd.setName(getLocalName());
        dfad.addServices(sd);
        try {
            DFService.register(this, dfad);
        }
        catch (FIPAException ex) {
            ex.printStackTrace();
        }

        //Zachownania
        addBehaviour(new ReciveParkingPositionBehaviour());
        addBehaviour(new ReciveParkingFreeSpaceBehaviour());
        addBehaviour(new GetParkingsPositionBehaviour());
        addBehaviour(new StartNearlyParkings());

    }

    @Override
    protected void takeDown(){
        doDelete();
    }


    private class GetParkingsPositionBehaviour extends OneShotBehaviour{
        @Override
        public void action(){
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Parking");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                for (int i = 0; i < result.length; i++){
                    AID nameReciver = result[i].getName();
                    msg.addReceiver(nameReciver);
                }
                msg.setLanguage("Polish");
                msg.setContent("Podaj pozycje");
                send(msg);
                logger.LogSendMessage(msg, myAgent);
            }
            catch (FIPAException ex) {
                ex.printStackTrace();
            }
        }
    }


    private class ReciveParkingPositionBehaviour extends Behaviour{

        public void action(){
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchEncoding("ParkingPositionContent")
            );

            ACLMessage msg = myAgent.receive(mt);
            if(msg!=null){
                    Logger.LogReciveMessage(msg, myAgent);
                    try{
                        Serializable data = msg.getContentObject();
                        ParkingPositionContent parkingPositionContent
                                = (ParkingPositionContent) data;
                        Parking parking = new Parking(msg.getSender(),
                                parkingPositionContent.getXP(),
                                parkingPositionContent.getYP());
                        parkings.put(parking.getName(), parking);
                    }
                    catch (UnreadableException e){
                        e.printStackTrace();
                    }
            }
            else{
                block();
            }
        }

        public boolean done(){
            return false;
        }
    }


    private class ReciveParkingFreeSpaceBehaviour extends Behaviour{

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchEncoding("FreeSpaces")
            );

            ACLMessage msg = myAgent.receive(mt);
            if(msg!=null){
                Logger.LogReciveMessage(msg, myAgent);
                int freeSpace = Integer.parseInt(msg.getContent());
                parkings.get(msg.getSender()).setFreeSpace(freeSpace);

                if (userSessions.get(msg.getConversationId())!=null){
                    userSessions.get(msg.getConversationId()).decrementRequestAboutFreeSpaces();
                    if(userSessions.get(msg.getConversationId()).IsDoneCheckFreeSpaces){
                        myAgent.addBehaviour(new AnswerNearlyParkings(userSessions.get(msg.getConversationId())));
                    }
                }
            }
            else{
                block();
            }
        }

        @Override
        public boolean done() {
            return false;
        }
    }


    private class StartNearlyParkings extends Behaviour{

        private ACLMessage msgFromUser = null;

        @Override
        public void action() {
            MessageTemplate mt =
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if(msg!=null){
                msgFromUser = msg;
                logger.LogReciveMessage(msgFromUser, myAgent);
                try {
                    Serializable data = msgFromUser.getContentObject();
                    FindFreeSlotsContent findFreeSlotsContent
                            = (FindFreeSlotsContent) data;
                    int carX = findFreeSlotsContent.getPositionX();
                    int carY = findFreeSlotsContent.getPositionY();

                    UserSession userSession = new UserSession(
                            msgFromUser.getSender(),
                            msgFromUser.getConversationId(),
                            carX,
                            carY);
                    userSessions.put(msgFromUser.getConversationId(), userSession);


                    // Sprawdź zajętość miejsc
                    List<Parking> nearlyParkingsToCheckFreeSpace = getNearlyParkingsToCheckFreeSpace(carX, carY);
                    if(!nearlyParkingsToCheckFreeSpace.isEmpty()){
                        userSessions.get(msgFromUser.getConversationId())
                                .setRequestAboutFreeSpaces(nearlyParkingsToCheckFreeSpace.size());
                        ACLMessage requestAboutFreeSpace = new ACLMessage(ACLMessage.REQUEST);
                        requestAboutFreeSpace.setLanguage("Polish");
                        requestAboutFreeSpace.setContent("Podaj liczbe wolnych miejsc");
                        requestAboutFreeSpace.setConversationId(msgFromUser.getConversationId());
                        for (Parking parking: nearlyParkingsToCheckFreeSpace){
                            requestAboutFreeSpace.addReceiver(parking.getName());
                        }
                        send(requestAboutFreeSpace);
                        logger.LogSendMessage(requestAboutFreeSpace, myAgent);
                        return;
                    }


                    // Wszystkie dane są aktualne więc daj miejsca do wyboru dla usera
                    List<Parking> nearlyParkingsToChooseByUser = getNearlyParkingsToChooseByUser(carX, carY);
                    ACLMessage answerMsg = msgFromUser.createReply();
                    answerMsg.setPerformative(ACLMessage.INFORM);
                    answerMsg.setLanguage("Polish");

                    if (!nearlyParkingsToChooseByUser.isEmpty()){
                        ParkingsToChooseByUserContent parkingsToChooseByUser =
                                new ParkingsToChooseByUserContent(nearlyParkingsToChooseByUser);
                        try{
                            answerMsg.setContentObject(parkingsToChooseByUser);
                            send(answerMsg);
                            logger.LogSendMessage(answerMsg, myAgent);
                        }catch (IOException ioe){
                            ioe.printStackTrace();
                        }
                    }
                    // Nie znaleziono żadnych pustych miejsc w pobliżu
                    else{
                        answerMsg.setContent("Brak wolnych miejsc parkingowych");
                        send(answerMsg);
                        logger.LogSendMessage(answerMsg, myAgent);
                    }
                }
                catch (UnreadableException ue){
                    ue.printStackTrace();
                }

            }else{
                block();
            }
        }

        @Override
        public boolean done() {
                return false;
        }
    }

    private class AnswerNearlyParkings extends OneShotBehaviour{

        private UserSession session;
        public AnswerNearlyParkings(UserSession session){
            this.session = session;
        }

        @Override
        public void action() {
            // Wszystkie dane są aktualne więc daj miejsca do wyboru dla usera
            List<Parking> nearlyParkingsToChooseByUser =
                    getNearlyParkingsToChooseByUser(session.getPosX(), session.getPosY());

            ACLMessage answerMsg = new ACLMessage(ACLMessage.INFORM);
            answerMsg.addReceiver(session.getCustomerId());
            answerMsg.setConversationId(session.getConversationId());
            answerMsg.setLanguage("Polish");

            if (!nearlyParkingsToChooseByUser.isEmpty()){
                ParkingsToChooseByUserContent parkingsToChooseByUser =
                        new ParkingsToChooseByUserContent(nearlyParkingsToChooseByUser);
                try{

                    answerMsg.setContentObject(parkingsToChooseByUser);
                    send(answerMsg);
                    logger.LogSendMessage(answerMsg, myAgent);
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }
            }
            // Nie znaleziono żadnych pustych miejsc w pobliżu
            else{
                answerMsg.setContent("Brak wolnych miejsc parkingowych");
                send(answerMsg);
                logger.LogSendMessage(answerMsg, myAgent);
            }
            userSessions.remove(session.getConversationId());
        }
    }


    private double calcDist (int xp, int yp, int xu, int yu){
        return Math.sqrt((xp-xu)*(xp-xu)+(yp-yu)*(yp-yu));
    }

    private List<Parking> getNearlyParkingsToCheckFreeSpace(int carX, int carY){
        int radius = 40;
        List<Parking> nearlyParkings = new ArrayList<>();
        if (parkings == null) {
            return nearlyParkings;
        }
        Enumeration enumerationParking = parkings.elements();
            while (enumerationParking.hasMoreElements()) {
                Parking parking = (Parking) enumerationParking.nextElement();
                int xp = parking.getX();
                int yp = parking.getY();
                if (calcDist(xp, yp, carX, carY) <= radius) {
                    nearlyParkings.add(parking);
                }
            }
        return nearlyParkings;
    }

    private List<Parking> getNearlyParkingsToChooseByUser(int carX, int carY){
        List<Parking> nearlyParkingsAvailable = new ArrayList<>();
        nearlyParkingsAvailable = getNearlyParkingsToCheckFreeSpace(carX, carY);
        for (int i = 0; i < nearlyParkingsAvailable.size(); i++) {
            if (nearlyParkingsAvailable.get(i).getFreeSpace() == 0) {
                nearlyParkingsAvailable.remove(i);
            }
        }
        return nearlyParkingsAvailable;
    }


}
