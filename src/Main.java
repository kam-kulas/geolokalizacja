import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Main {

    public static void main(String[] args){
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
                p.setParameter(Profile.MAIN_HOST, "localhost");
                p.setParameter(Profile.GUI, "true");
        ContainerController cc = rt.createMainContainer(p);
        AgentController ac;
        try{
            ac = cc.createNewAgent("TaskManager", "TaskManager", null);
            ac.start();
        }catch (StaleProxyException e){
            e.printStackTrace();
        }
        for (int i = 0; i<10; i++){
            try{
                ac = cc.createNewAgent("ParkingTracker"+i, "ParkingTracker", null);
                ac.start();
            }catch (StaleProxyException e){
                e.printStackTrace();
            }
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
