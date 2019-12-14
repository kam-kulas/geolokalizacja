import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;

public class Main {

    public static void main(String[] args){
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
                p.setParameter(Profile.MAIN_HOST, "localhost");
                p.setParameter(Profile.GUI, "true");
        ContainerController cc = rt.createMainContainer(p);
    }
}
