import Models.Parking;
import Tools.Logger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import messageTemplate.FindFreeSlotsContent;
import messageTemplate.FreeSlotsPositionContent;
import messageTemplate.ParkingsToChooseByUserContent;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TaskManager extends Agent {

    private Logger logger;
    List<Parking> parkings = new ArrayList<>();
    List<Parking> nearlyParkings = new ArrayList<>();
    List<Parking> nearlyParkingsAvailable = new ArrayList<>();

    @Override
    protected void setup(){

        System.out.println("Agent: "+getLocalName());
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
                    FreeSlotsPositionContent freeSlotsPositionContent
                            = (FreeSlotsPositionContent) data;
                    Parking parking = new Parking(msg.getSender(),
                            freeSlotsPositionContent.getXP(),
                            freeSlotsPositionContent.getYP());
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

        @Override
        public void action() {
            MessageTemplate mt =
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if(msg!=null){
                logger.LogReciveMessage(msg, myAgent);
                try {
                    Serializable data = msg.getContentObject();
                    FindFreeSlotsContent findFreeSlotsContent
                            = (FindFreeSlotsContent) data;
                    int carX = findFreeSlotsContent.getPositionX();
                    int carY = findFreeSlotsContent.getPositionY();

                    List<Parking> nearlyParkingsToCheckFreeSpace = getNearlyParkingsToCheckFreeSpace(carX, carY);
                    // Sprawdź zajętość miejsc
                    if(!nearlyParkingsToCheckFreeSpace.isEmpty()){


                        return;
                    }
                    // Wszystkie dane są aktualne więc daj miejsca do wyboru
                    List<Parking> nearlyParkingsToChooseByUser = getNearlyParkingsToChooseByUser(carX, carY);
                    if (!nearlyParkingsToChooseByUser.isEmpty()){
                        ParkingsToChooseByUserContent parkingsToChooseByUser =
                                new ParkingsToChooseByUserContent(nearlyParkingsToChooseByUser);
                        try{
                            ACLMessage answerMsg = msg.createReply();
                            answerMsg.setPerformative(ACLMessage.INFORM);
                            answerMsg.setLanguage("Polish");
                            answerMsg.setContentObject(parkingsToChooseByUser);
                        }catch (IOException ioe){
                            ioe.printStackTrace();
                        }

                    }

                    // Nie znaleziono żadnych pustych miejsc w pobliżu
                    else{

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