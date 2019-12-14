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

import java.awt.*;

public class UserTracker extends Agent {

    private boolean IsProcessing_GetParking = false;

    @Override
    protected void setup(){
        System.out.println("Agent: "+getLocalName());
        addBehaviour(new ReciveParkingsBehaviour());
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
            AID a = result[0].getName();
            System.out.println(a);
        }
        catch (FIPAException ex){

        }


        IsProcessing_GetParking = true;
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
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
