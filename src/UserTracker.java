import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import messageTemplate.FindFreeSlotsContent;

import java.io.IOException;
import java.util.Date;

public class UserTracker extends Agent {

    private boolean IsProcessing_GetParking = false;
    String conversationId = null;
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
            Thread.sleep(3000);
            DFAgentDescription[] result = DFService.search(this, template);
            AID nameReciver = result[0].getName();
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(nameReciver);
            msg.setLanguage("Polish");
            FindFreeSlotsContent content = new FindFreeSlotsContent(PositionX, PositionY);
            msg.setContentObject(content);
            long currentTime = new Date().getTime();
            conversationId = getLocalName() + currentTime;
            msg.setConversationId(conversationId);
            send(msg);
        }
        catch (FIPAException ex){
            ex.printStackTrace();
        }
        catch (InterruptedException ex){
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
                    MessageTemplate.MatchConversationId(conversationId)
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
