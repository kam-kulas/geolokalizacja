import Tools.Logger;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import MessageTemplate.ParkingPositionContent;

import java.io.IOException;
import java.io.Serializable;

public class ParkingTracker extends Agent {

    private Logger logger;
    private int XP;
    private int YP;
    private int CAPACITY;
    private int freeSpaces;


    @Override
    protected void setup(){

        logger = new Logger();
        // Inicjalizacja parametrów parkingu
        Object[] objects = getArguments();
        XP = (int)objects[0];
        YP = (int)objects[1];
        CAPACITY = (int)objects[2];
        freeSpaces = CAPACITY * (int)objects[3];
        System.out.println("Agent: "+getLocalName()+ " is ready for work!");


        //rejestracja parkingu w DFD
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Parking");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try{
            DFService.register(this, dfd);
        }catch (FIPAException fe){
            fe.printStackTrace();
        }

        // Zachowania
        addBehaviour(new AnswerPositionBehavior());
        addBehaviour(new AnswerFreeSpaceBehavior());


    }

    private boolean isFreeSpaceAvailable() {
        if (freeSpaces == 0) {
            return false;
        }
        return true;
    }

    protected void takeDown(){
        System.out.println("Agent: "+getLocalName()+ " is done.");
    }



    private class AnswerPositionBehavior extends Behaviour{

        public void action(){
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchContent("Podaj pozycje")
            );
            ACLMessage msg = myAgent.receive(mt);
            if(msg!=null){
                logger.LogReciveMessage(msg, myAgent);
                try{
                    ACLMessage answerMsg = msg.createReply();
                    answerMsg.setPerformative(ACLMessage.INFORM);
                    answerMsg.setLanguage("Polish");
                    ParkingPositionContent parkingPositionContent =
                            new ParkingPositionContent(XP, YP);
                    answerMsg.setContentObject((Serializable) parkingPositionContent);
                    send(answerMsg);
                    logger.LogSendMessage(answerMsg, myAgent);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else{
                block();
            }
        }

        public boolean done() {
            return false;
        }
        }

    private class AnswerFreeSpaceBehavior extends Behaviour{

        public void action(){
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchContent("Podaj liczbe wolnych miejsc")
            );
            ACLMessage msg = myAgent.receive(mt);
            if(msg!=null){
                logger.LogReciveMessage(msg, myAgent);
                ACLMessage answerMsg = msg.createReply();
                answerMsg.setPerformative(ACLMessage.INFORM);
                answerMsg.setLanguage("Polish");
                answerMsg.setContent(String.valueOf(freeSpaces));
                send(answerMsg);
                logger.LogSendMessage(answerMsg, myAgent);
            }else{
                block();
            }
        }

        public boolean done() {
            return false;
        }
    }

    }






