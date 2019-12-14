import jade.core.Agent;

import java.util.Random;

public class ParkingTracker extends Agent {
    private static double XP;
    private static double YP;
    private static int CAPACITY;
    private double freeSpaces;

    @Override
    protected void setup(){
        Random generator = new Random();
        XP = generator.nextDouble()*100;
        YP = generator.nextDouble()*100;
        CAPACITY = generator.nextInt(200);
        freeSpaces = 0.3 * generator.nextInt(CAPACITY);
        System.out.println("Agent: "+getLocalName()+ " is ready for work!");
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
}