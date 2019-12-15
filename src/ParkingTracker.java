import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import messageTemplate.FreeSlotsPositionContent;

import java.io.IOException;
import java.util.Random;

public class ParkingTracker extends Agent {
    private static int XP;
    private static int YP;
    private static int CAPACITY;
    private int freeSpaces;

    private boolean IsGetPositionBehaviorProcessing = false;

    @Override
    protected void setup(){

        // Inicjalizacja parametrów parkingu
        Random generator = new Random();
        XP = generator.nextInt()*100;
        YP = generator.nextInt()*100;
        CAPACITY = generator.nextInt(200);
        freeSpaces = (int) 0.3 * generator.nextInt(CAPACITY);
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
        addBehaviour(new CyclicBehaviour(this)
        {
            public void action() {
                ACLMessage msg= receive();
                if (msg!=null)
                    System.out.println( "== Answer" + " <- "
                            +  msg.getContent() + " from "
                            +  msg.getSender().getName() );
                block();
            }
        });
        addBehaviour(new GetPositionBehavior());


    }

    public boolean isFreeSpaceAvailable() {
        if (freeSpaces == 0) {
            return false;
        }
        return true;
    }

    public static double[] getLocalization() {
        double[] coordinates = {XP, YP};
        return coordinates;
    }

    protected void takeDown(){
        System.out.println("Agent: "+getLocalName()+ " is done.");
    }



    public class GetPositionBehavior extends Behaviour{

        public void action(){
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchContent("Podaj pozycje")
            );
            ACLMessage msg = myAgent.receive(mt);
            if(msg!=null){
                IsGetPositionBehaviorProcessing = true;
                try{
                    ACLMessage answerMsg = new ACLMessage(ACLMessage.REQUEST);
                    answerMsg.addReceiver(msg.getSender());
                    answerMsg.setLanguage("Polish");
                    answerMsg.setContent("Oto moja pozycja");
                    answerMsg.setContentObject(new FreeSlotsPositionContent(XP, YP));
                    send(answerMsg);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else{
                block();
            }

        }

        public boolean done() {
            if (!IsGetPositionBehaviorProcessing) return true; else return false;
        }

        }

    }


