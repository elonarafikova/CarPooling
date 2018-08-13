import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.time.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by Nikita on 05.06.2018.
 */
public class HomeAgent extends Agent {

    private StartAgent sA = new StartAgent();
    private Random rndm = new Random();
    public int _homePoint;
    private AID giveto;
    private AID driver;
    private int minCost=10000;
    private ArrayList<AID> _needToSpeak = new ArrayList<AID>();
    private ArrayList<AID> _readyToChange = new ArrayList<AID>();
    private ArrayList<AID> _spoken = new ArrayList<AID>();
    private ArrayList<AID> _needToReject = new ArrayList<AID>();
    private boolean _isFree = true;
    private boolean notfirst=false;
    private int maxAgent=7;
    private Integer agree_cost=300;
    private long start;

    private Integer _money;

    private Integer _minimum;
    private String purpose = "Никто не согласился. Прощай, жесткоий мир :С";
    private boolean haveManToChange = false;

    private class receiveMessage extends CyclicBehaviour {
        public void action() {

            ACLMessage reply = myAgent.receive();
            if (reply != null && (reply.getPerformative()==ACLMessage.PROPOSE)) {
                if (reply.getSender().getLocalName().equals("ams")) {
                    return;
                }
                maxAgent--;
                Integer cost = Integer.parseInt(reply.getContent());
                ACLMessage answer = reply.createReply();
                if (cost<=agree_cost) {
                    answer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    answer.setContent(Integer.toString(cost)+" "+Integer.toString(_homePoint));
                    myAgent.send(answer);
                    System.out.print("\n" + getLocalName() + " мне привезет " + reply.getSender().getLocalName() +" за "+cost);

                    unregister();
                    takeDown();
                    myAgent.doDelete();



                }

                else {
                    answer.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    myAgent.send(answer);
                    //System.out.print("\n" + getLocalName() + " reject " + reply.getSender().getLocalName());
                }



            }
            else {
                if (maxAgent==0 || ((System.currentTimeMillis()-start)==50000)) {

                    //System.out.print("\n" + "all send or time");
                    maxAgent=6;
                    agree_cost+=200;
                    start=System.currentTimeMillis();
                    //unregister();
                    //takeDown();
                    //myAgent.doDelete();

                }
            }

        }

    }

    protected void setup() {
        sA.CreateMap(0);
        _homePoint = (Integer) getArguments()[0];
        _money = (Integer) getArguments()[1];

        System.out.println("I was born! My name is " + getLocalName() +
                "\nMy home: " + getArguments()[0] );

        register();
        System.out.println("Now, " + getLocalName() + " registrated in YellowPages!");
        //doWait(5000);


        WakerBehaviour die = new WakerBehaviour(this, 100000) {
            @Override
            protected void onWake() {
                doDelete();
                System.out.println("Мне никто не помог :(");
                super.onWake();
            }
        };
        addBehaviour(die);
        start = System.currentTimeMillis();
        addBehaviour(new HomeAgent.receiveMessage());
        /*TickerBehaviour parseYP = new TickerBehaviour(this, 3000) {
            @Override
            protected void onTick() {
                Boolean sended = false;
                DFAgentDescription[] searchResult = searchCompanion();
                for (DFAgentDescription sR : searchResult) {
                    Iterator itr = sR.getAllServices();
                    while (itr.hasNext()) {
                        ServiceDescription wayInfo = (ServiceDescription) itr.next();
                        String startP = wayInfo.getName().split(" ")[1];
                        String finalP = wayInfo.getName().split(" ")[2];
                        AID aid = sR.getName();
                        if (!checkForAid(aid.toString(), getLocalName()) && !_needToSpeak.contains(aid)) {
                            _needToSpeak.add(aid);
                            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                            message.addReceiver(aid);
                            message.setContent("0," + _manNum.toString());
                            myAgent.send(message);
                        }
                    }
                }
            }
        };
        addBehaviour(parseYP);*/

    }


    private void register() {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        dfd.setName(getAID());
        sd.setType("Home");
        sd.setName(getLocalName() + " " + _homePoint);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Registered " + getLocalName() + " as: " + getLocalName() + " " + _homePoint);
    }



    private void unregister() {
        //System.out.println("Trying to deregister " + name + " as: " + name + " " + sP + " " + fP + "!!!");
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        dfd.setName(getAID());
        //sd.setType("Rider");
        //sd.setName(name + " " + sP + " " + fP);
        //dfd.addServices(sd);
        DFAgentDescription[] searchResult = null;
        try{
            searchResult = DFService.search(this, dfd);
        }
        catch (FIPAException fe){
            fe.printStackTrace();
        }

        if(searchResult.length == 0 || searchResult == null) {
            //System.out.println("No need to deregister " + name + "!!!");
            return;
        }
        try {
            DFService.deregister(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        //System.out.println("Successfully deregistered " + name + "!!!");
    }


    private DFAgentDescription[] searchCompanion() {
        DFAgentDescription[] searchResult = null;

        DFAgentDescription agentTemplate = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("Rider");
        agentTemplate.addServices(serviceDescription);
        try {
            searchResult = DFService.search(this, agentTemplate);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return searchResult;
    }

    private boolean checkForAid(String Aid, String Name) {
        return Aid.contains(Name);
    }

}

