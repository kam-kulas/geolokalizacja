import jade.core.Agent;

public class ParkingTracker extends Agent {

    @Override
    protected void setup(){
        System.out.println("Agent: "+getLocalName());
    }
}
