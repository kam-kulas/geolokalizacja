import jade.core.Agent;

public class TaskManager extends Agent {

    @Override
    protected void setup(){
        System.out.println("Agent: "+getLocalName());
    }
}
