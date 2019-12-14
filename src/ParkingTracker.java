import jade.core.Agent;

public class ParkingTracker extends Agent {
    private static final double XP;
    private static final double YP;
    private static final int CAPACITY;
    private int freeSpaces;

    @Override
    protected void setup(){
        Random generator = new Random();
        XP = generator.nextDouble()*100;
        YP = generator.nextDouble()*100;
        CAPACITY = generator.nexInt(200);
        freeSpaces = 0.3 * generator.nextInt(CAPACITY);
        System.out.println("Agent: "+getLocalName()+ " is ready for work!");
    }

    public boolean isFreeSpaceAvailable() {
        if (freeSpaces == 0) {
            return false;
        }
        return true;
    }

    public static int[] getLocalization() {
        int[] coordinates = {XP, YP};
        return coordinates;
    }

    protected void takeDown(){
        System.out.println("Agent: "+getLocalName()+ " is done.");
    }
}