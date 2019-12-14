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
    List<Parking> parkings = new ArrayList<Parking>();

    private double calcDist (int xp, int yp, int xu, int yu){
        return Math.sqrt((xp-xu)*(xp-xu)+(yp-yu)*(yp-yu));
    }

    private double[] getDistAll (int x){
        return new double[] {45.45};
    }

    @Override
    protected void setup(){

        System.out.println("Agent: "+getLocalName());

        DFAgentDescription dfad = new DFAgentDescription();
        dfad.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("FindFreeSlotsType");
        sd.setName("FindFreeSlots");
        dfad.addServices(sd);
        try {
            DFService.register(this, dfad);
        }
        catch (FIPAException ex) {
            System.out.println("Dupa :( (nie udalo sie zarejestrowac uslugi)");
        }

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType("GetCordsType");
        template.addServices(sd1);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            for (DFAgentDescription parking:result){
                //TODO

            }
        }
        catch (FIPAException ex) {
            System.out.println("Dupa :( (nie ma uslugi)");
        }

        //----------------------------------------------------------------------------
        //https://www.iro.umontreal.ca/~vaucher/Agents/Jade/primer4.html#1
        AMSAgentDescription [] agents = null;
        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults (new Long(-1));
            agents = AMSService.search( this, new AMSAgentDescription (), c );
        }
        catch (Exception e) {
            System.out.println( "Problem searching AMS: " + e );
            e.printStackTrace();
        }
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent( "" );

        for (int i=0; i<agents.length;i++)
            msg.addReceiver( agents[i].getName() );

        send(msg);

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
        //----------------------------------------------------------------------------
    }
}
