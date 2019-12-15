import Models.Parking;
import Tools.Logger;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import jade.core.Agent;

import java.io.IOException;
import java.io.Serializable;
import java.lang.Math;
import java.util.*;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TaskManager extends Agent {

    private Logger logger;
    private List<Parking> parkings = new ArrayList<>();
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
        addBehaviour(new GetParkingsPositionBehaviour());
        addBehaviour(new AnswerNearlyParkings());

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
            MessageTemplate mt =
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM);
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
                    parkings.add(parking);
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


    private class AnswerNearlyParkings extends Behaviour{

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

                    UserSession userSession = new UserSession(msgFromUser.getSender(), msgFromUser.getConversationId());
                    userSessions.put(msgFromUser.getConversationId(), userSession);


                    // Sprawdź zajętość miejsc
                    List<Parking> nearlyParkingsToCheckFreeSpace = getNearlyParkingsToCheckFreeSpace(carX, carY);
                    if(!nearlyParkingsToCheckFreeSpace.isEmpty()){
                        userSessions.get(msgFromUser.getConversationId())
                                .setRequestAboutFreeSpaces(nearlyParkingsToCheckFreeSpace.size());
                        ACLMessage requestAboutFreeSpace = new ACLMessage(ACLMessage.REQUEST);
                        requestAboutFreeSpace.setLanguage("Polish");
                        requestAboutFreeSpace.setContent("Podaj liczbe wolnych miejsc");
                        for (Parking parking: nearlyParkingsToCheckFreeSpace){
                            requestAboutFreeSpace.addReceiver(parking.getName());
                        }
                        send(requestAboutFreeSpace);
                        logger.LogSendMessage(requestAboutFreeSpace, myAgent);
                        userSessions.get(msgFromUser.getConversationId())
                                .IsDoneAnswerNearlyParkings = true;
                        return;
                    }


                    // Wszystkie dane są aktualne więc daj miejsca do wyboru dla usera
                    List<Parking> nearlyParkingsToChooseByUser = getNearlyParkingsToChooseByUser(carX, carY);
                    if (!nearlyParkingsToChooseByUser.isEmpty()){
                        ParkingsToChooseByUserContent parkingsToChooseByUser =
                                new ParkingsToChooseByUserContent(nearlyParkingsToChooseByUser);
                        try{
                            ACLMessage answerMsg = msgFromUser.createReply();
                            answerMsg.setPerformative(ACLMessage.INFORM);
                            answerMsg.setLanguage("Polish");
                            answerMsg.setContentObject(parkingsToChooseByUser);
                            send(answerMsg);
                            logger.LogSendMessage(answerMsg, myAgent);
                            userSessions.get(msgFromUser.getConversationId())
                                    .IsDoneAnswerNearlyParkings = true;
                            userSessions.get(msgFromUser.getConversationId())
                                    .IsDoneCheckFreeSpaces = true;
                        }catch (IOException ioe){
                            ioe.printStackTrace();
                        }

                    }

                    // Nie znaleziono żadnych pustych miejsc w pobliżu
                    else{
                        ACLMessage answerMsg = msgFromUser.createReply();
                        answerMsg.setPerformative(ACLMessage.INFORM);
                        answerMsg.setLanguage("Polish");
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
            if (msgFromUser == null)
                return false;
            return userSessions.get(msgFromUser.getConversationId()).IsDoneAnswerNearlyParkings;
        }
    }


    private double calcDist (int xp, int yp, int xu, int yu){
        return Math.sqrt((xp-xu)*(xp-xu)+(yp-yu)*(yp-yu));
    }
    private double[] getDistAll (int x){
        return new double[] {45.45};
    }



    private List<Parking> getNearlyParkingsToCheckFreeSpace(int carX, int carY){
        int radius = 20;
        if (parkings == null) {
            return nearlyParkings;
        }
            for (Parking parking : parkings) {
                int xp = parking.getX();
                int yp = parking.getY();
                if (calcDist(xp, yp, carX, carY) <= radius) {
                    nearlyParkings.add(parking);
                }
            }
        return nearlyParkings;
    }

    private List<Parking> getNearlyParkingsToChooseByUser(int carX, int carY){
        nearlyParkingsAvailable = getNearlyParkingsToCheckFreeSpace(carX, carY);
        for (int i = 0; i < nearlyParkingsAvailable.size(); i++) {
            if (nearlyParkingsAvailable.get(i).getFreeSpace() == 0) {
                nearlyParkingsAvailable.remove(i);
            }
        }
        return nearlyParkingsAvailable;
    }


}
