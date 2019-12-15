import jade.core.Agent;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

import jade.core.AID;

import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.core.behaviours.*;
import jade.domain.FIPAException;
import jade.lang.acl.*;

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


class GetParkingsBehaviour extends OneShotBehaviour{
    @Override
    public void action(){

    }
}

public class TaskManager extends Agent {
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


        GetParkingPosition();
    }

    @Override
    protected void takeDown(){
        doDelete();
    }


    private void GetParkingPosition(){
        // Dowiedz się jakie pozycje mają parkingi
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Parking");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            for (int i = 0; i < result.length; i++){
                AID nameReciver = result[i].getName();
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(nameReciver);
                msg.setLanguage("Polish");
                msg.setContent("Podaj pozycje");
                send(msg);
            }
        }
        catch (FIPAException ex) {
            ex.printStackTrace();
        }
    }

}
