import Tools.Logger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import MessageTemplate.FindFreeSlotsContent;

import java.io.IOException;
import java.util.Date;

public class UserTracker extends Agent {

    String conversationId = null;
    private Logger logger;
    private int PositionX;
    private int PositionY;

    @Override
    protected void setup(){
        System.out.println("Agent: "+getLocalName());
        logger = new Logger();

        PositionX = 2;
        PositionY = 4;


        // Zachowania

        addBehaviour(new GetParkings());
        addBehaviour(new ReciveParkingsBehaviour());


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
                logger.LogReciveMessage(msg, myAgent);
                DFAgentDescription template  = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setName("TaskManager");
                template.addServices(sd);
                try{
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
                    logger.LogSendMessage(requestMsg, myAgent);
                }
                catch (FIPAException ex){
                    ex.printStackTrace();
                }
                catch (IOException ex){
                    ex.printStackTrace();
                }
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

            ACLMessage msg = myAgent.receive(mt);
            if(msg!=null){
                logger.LogReciveMessage(msg, myAgent);
            }else{
                block();
            }

        }
        public boolean done() {
            return false;
        }
    }
}
