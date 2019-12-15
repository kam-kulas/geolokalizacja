package messageTemplate;
import java.io.Serializable;

public class GiveFreeSlotsContent implements Serializable{
    private static double XP;
    private static double YP;

    public GiveFreeSlotsContent(double XP, double YP){
        this.XP = XP;
        this.XY = XY;
    }

    public double getXP() {
        return XP;
    }
    public double getXY() {
        return XY;
    }

}