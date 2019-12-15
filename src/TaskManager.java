import Tools.Logger;
import jade.core.Agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import jade.core.AID;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.core.behaviours.*;
import jade.domain.FIPAException;
import jade.lang.acl.*;
import messageTemplate.FreeSlotsPositionContent;
import sun.rmi.runtime.Log;

class Parking{
    private AID name;
    private int x;
    private int y;

    public AID getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Parking(AID name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }
}


public class TaskManager extends Agent {

    private Logger logger;
    List<Parking> parkings = new ArrayList<>();
    private double calcDist (int xp, int yp, int xu, int yu){
        return Math.sqrt((xp-xu)*(xp-xu)+(yp-yu)*(yp-yu));
    }
    private double[] getDistAll (int x){
        return new double[] {45.45};
    }


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


    }

    @Override
    protected void takeDown(){
        doDelete();
    }

    class GetParkingsPositionBehaviour extends OneShotBehaviour{
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


    class ReciveParkingPositionBehaviour extends Behaviour{

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

}
