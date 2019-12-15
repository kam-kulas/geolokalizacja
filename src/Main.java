import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.Date;
import java.util.Random;

public class Main {

    public static void main(String[] args){
        long seed=new Date().getTime();
        Random generator = new Random(seed);

        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
                p.setParameter(Profile.MAIN_HOST, "localhost");
                p.setParameter(Profile.GUI, "true");
        ContainerController cc = rt.createMainContainer(p);
        AgentController ac;
        for (int i = 0; i<10; i++){
            try{
                Object[] object = new Object[] {
                        generator.nextInt(100),
                        generator.nextInt(100),
                        generator.nextInt(200),
                        generator.nextInt(100)
                };
                ac = cc.createNewAgent("ParkingTracker"+i, "ParkingTracker", object);
                ac.start();
            }catch (StaleProxyException e){
                e.printStackTrace();
            }
        }

        try{
            ac = cc.createNewAgent("TaskManager", "TaskManager", null);
            ac.start();
        }catch (StaleProxyException e){
            e.printStackTrace();
        }

        for (int i = 0; i<1 ; i++){
            try{
                ac = cc.createNewAgent("UserTracker"+i, "UserTracker", null);
                ac.start();
            }catch (StaleProxyException e){
                e.printStackTrace();
            }
        }

    }
}
