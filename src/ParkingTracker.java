import jade.core.Agent;

import java.util.Random;

public class ParkingTracker extends Agent {
    private static double XP;
    private static double YP;
    private static int CAPACITY;
    private int freeSpaces;

    @Override
    protected void setup(){
        Random generator = new Random();
        XP = generator.nextDouble()*100;
        YP = generator.nextDouble()*100;
        CAPACITY = generator.nextInt(200);
        freeSpaces = (int) 0.3 * generator.nextInt(CAPACITY);

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        for (int i=0; i<agents.length;i++)
            msg.addReceiver( agents[i].getName() );

        msg.setLanguage("Polski");
        msg.setOntology("Geolokalizacja");

        System.out.println("Agent: "+getLocalName()+ " jest gotowy do pracy!");
        addBehaviour(new TickerBehaviour(this, 10000){
            protected void onTick(){
                if (freeSpaces != 0) {
                    msg.setContent("" + freeSpaces + " są dostępne na ");
                }
                else if (freeSpaces == 0) {
                    msg.setContent("Miejsca parkingowe nie są dostępne");
                }
                send(msg);
            }
        })

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg= receive();
                if (msg!=null)
                    System.out.println( "== Odpowiedź" + " <- " +  msg.getContent() + " od " +  msg.getSender().getName());
                block();
            }
        });
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
        System.out.println("Agent: "+getLocalName()+ " skończył pracę.");
    }
}