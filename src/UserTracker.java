import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
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


        // Zachowania

        addBehaviour(new GetParkings());


    }

    @Override
    protected void takeDown(){
        doDelete();
    }

    public class GetParkings extends Behaviour{

        public void action(){
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchContent("Start")
            );
            ACLMessage msg = myAgent.receive(mt);
            if(msg!=null){
                System.out.println("Odebrał");
                DFAgentDescription template  = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setName("TaskManager");
                template.addServices(sd);
                try{
                    Thread.sleep(3000);
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    AID nameReciver = result[0].getName();
                    ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
                    requestMsg.addReceiver(nameReciver);
                    requestMsg.setLanguage("Polish");
                    FindFreeSlotsContent content = new FindFreeSlotsContent(PositionX, PositionY);
                    requestMsg.setContentObject(content);
                    long currentTime = new Date().getTime();
                    conversationId = getLocalName() + currentTime;
                    requestMsg.setConversationId(conversationId);
                    send(requestMsg);
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
            else{
                block();
            }


        }

        public boolean done() {
           return false;
        }
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
