import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Random;

public class ParkingTracker extends Agent {
    private static double XP;
    private static double YP;
    private static int CAPACITY;
    private int freeSpaces;

    private boolean IsGetPositionBehaviorProcessing = false;

    @Override
    protected void setup(){

        // Inicjalizacja parametrów parkingu
        Random generator = new Random();
        XP = generator.nextDouble()*100;
        YP = generator.nextDouble()*100;
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
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);


            ACLMessage msg = myAgent.receive();
            if(msg!=null){
                IsGetPositionBehaviorProcessing = true;

            }else{
                block();
            }

        }

        public boolean done() {
            if (!IsGetPositionBehaviorProcessing) return true; else return false;
        }

        }

    }


