import Tools.MySerializable;
import jade.core.AID;
import jade.core.Agent;
import jade.core.MessageQueue;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import messageTemplate.FindFreeSlotsContent;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;

public class UserTracker extends Agent {

    private boolean IsProcessing_GetParking = false;
    private int PositionX;
    private int PositionY;

    @Override
    protected void setup(){
        System.out.println("Agent: "+getLocalName());
        addBehaviour(new ReciveParkingsBehaviour());
        PositionX = 2;
        PositionY = 4;


        startGetParkings();
    }

    @Override
    protected void takeDown(){
        doDelete();
    }

    private void startGetParkings(){
        DFAgentDescription template  = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName("FindFreeSlots");
        template.addServices(sd);
        try{
            DFAgentDescription[] result = DFService.search(this, template);
            AID nameReciver = result[0].getName();
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(nameReciver);
            msg.setLanguage("Polish");
            FindFreeSlotsContent content = new FindFreeSlotsContent(PositionX, PositionY);
            msg.setContentObject(content);
            send(msg);
        }
        catch (FIPAException ex){
            ex.printStackTrace();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
        IsProcessing_GetParking = true;


    }


    public class ReciveParkingsBehaviour extends Behaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("fadsf")
            );

            ACLMessage msg = myAgent.receive();
            if(msg!=null){
                IsProcessing_GetParking = true;

            }else{
                block();
            }





        }
        public boolean done() {
            if (!IsProcessing_GetParking) return true; else return false;
        }
    }
}
