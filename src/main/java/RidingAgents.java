import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.InternalError;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.lang.reflect.Array;
import java.util.*;


public class RidingAgents extends Agent {

    private StartAgent sA = new StartAgent();
    private Random rndm = new Random();
    private int _startPoint;
    private int _finalPoint;
    private ArrayList<AID> _needToSpeak = new ArrayList<AID>();
    private List<Integer> route = new ArrayList<Integer>();
    private ArrayList<AID> _readyToChange = new ArrayList<AID>();
    private ArrayList<AID> _spoken = new ArrayList<AID>();
    private int willreceive=0;
    private boolean _isFree = true;
    private int maxAgent=7;
    private HashMap<Integer,List<Integer>> my_routes= new HashMap<Integer, List<Integer>>();
    private Integer _minimum;
    private String purpose = "Никто не согласился. Прощай, жесткоий мир :С";
    private boolean haveManToChange = false;



    private int _stock;
   /*private  class send(Agent a, long period) extends TickerBehaviour{

           protected void onTick() {
               Boolean sended = false;
               DFAgentDescription[] searchResult = searchCompanion();
               for (DFAgentDescription sR : searchResult) {
                   Iterator itr = sR.getAllServices();
                   while (itr.hasNext()) {
                       ServiceDescription wayInfo = (ServiceDescription) itr.next();
                       String homeP = wayInfo.getName().split(" ")[1];
                       AID aid = sR.getName();
                       if (!_needToSpeak.contains(aid)) {
                           _needToSpeak.add(aid);
                           ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
                           message.addReceiver(aid);
                           message.setContent(costOfWay(Integer.parseInt(homeP)).toString());
                           myAgent.send(message);
                           System.out.println("\n" + getLocalName() + " send propose to " + aid.getLocalName() + " with cost " + costOfWay(Integer.parseInt(homeP)).toString());
                       }
                   }
               }
           }

   }*/
    private class receiveMessage extends CyclicBehaviour {
        public void action() {

            ACLMessage reply = myAgent.receive();
            if (reply != null) {
                if (reply.getSender().getLocalName().equals("ams")) {
                    return;
                }

                if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {


                String content=reply.getContent();
                String[] parts = content.split(" ",2);
                Integer m_cost=Integer.parseInt(parts[0]);
                Integer home=Integer.parseInt(parts[1]);

                    //System.out.print("\n" + getLocalName() + " привезу этому: " + reply.getSender().getLocalName() +" за "+m_cost );
                    willreceive+=m_cost;
                    route=my_routes.get(home);
                    /*System.out.print("\n" + "по этому маршруту: ");
                            for (Integer v :route){
                                System.out.print(" "+v+" ");
                            }*/

                }


            }
        }
    }

    protected void setup() {
        sA.CreateMap(0);
        _startPoint = (Integer) getArguments()[0];
        _finalPoint = (Integer) getArguments()[1];
        _stock = (Integer) getArguments()[2];



        System.out.println("I was born! My name is " + getLocalName() +
                "\nMy Home: " + getArguments()[0] +
                "\nMy Work: " + getArguments()[1] );

     /*   register();
        System.out.println("Now, " + getLocalName() + " registrated in YellowPages!");
        doWait(5000);*/


        WakerBehaviour die = new WakerBehaviour(this, 100000) {
            @Override
            protected void onWake() {
                System.out.println("\n"+getLocalName()+" получит "+willreceive);
                System.out.print("\n" + "поеду по этому маршруту: ");
                            for (Integer v :route){
                                System.out.print(" "+v+" ");
                            }
                System.out.println("\n"+getLocalName()+" отклонится от маршрута на  "+deviation());
                doDelete();

                super.onWake();
            }
        };
        addBehaviour(die);

        TickerBehaviour parseYP = new TickerBehaviour(this, 14000) {
            @Override
            protected void onTick() {
                Boolean sended = false;
                DFAgentDescription[] searchResult = searchCompanion();
                if (searchResult.length==0)
                {   System.out.println("\n"+getLocalName()+" получит "+willreceive);
                    System.out.print("\n" + "поеду по этому маршруту: ");
                    for (Integer v :route){
                        System.out.print(" "+v+" ");
                    }
                    System.out.println("\n"+getLocalName()+" отклонится от маршрута на  "+deviation());
                    takeDown();
                    myAgent.doDelete();

                }
                for (DFAgentDescription sR : searchResult) {
                    Iterator itr = sR.getAllServices();
                    while (itr.hasNext()) {
                        ServiceDescription wayInfo = (ServiceDescription) itr.next();
                        String homeP = wayInfo.getName().split(" ")[1];
                        AID aid = sR.getName();
                        if (!_needToSpeak.contains(aid)) {
                            _needToSpeak.add(aid);
                            ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
                            message.addReceiver(aid);
                            message.setContent(costOfWay(Integer.parseInt(homeP)).toString());
                            myAgent.send(message);
                            System.out.println("\n" + getLocalName() + " send propose to " + aid.getLocalName() + " with cost " + costOfWay(Integer.parseInt(homeP)).toString());
                        }
                    }
                }
                _needToSpeak.clear();
            }
        };
        addBehaviour(parseYP);
        addBehaviour(new receiveMessage());
    }


    private void register() {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        dfd.setName(getAID());
        sd.setType("Rider");
        sd.setName(getLocalName() + " " + _startPoint + " " + _finalPoint);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Registered " + getLocalName() + " as: " + getLocalName() + " " + _startPoint + " " + _finalPoint);
    }


    private DFAgentDescription[] searchCompanion() {
        DFAgentDescription[] searchResult = null;

        DFAgentDescription agentTemplate = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("Home");
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
    private Integer deviation () {


        int sum=0;
        if (route.size()!=0) {
            return (sA.minD(route)-sA.minDist(route.get(0),route.get(route.size()-1)));
        }
        else {return 0;}

    }
    private  Integer costOfWay(int houseMan) {

        int n=0;
        int sum=0;
        if (route.size()==0)
        {
            Integer rez1 =sA.minD (Arrays.asList(_startPoint,_stock,houseMan,_finalPoint));

            Integer rez2 =sA.minD(Arrays.asList(_finalPoint,_stock,houseMan,_startPoint));
            if (rez1<rez2){
                my_routes.put(houseMan,Arrays.asList(_startPoint,_stock,houseMan,_finalPoint));

                rez1=rez1-sA.minD(Arrays.asList(_startPoint,_stock,_finalPoint));
                return (rez1*45+300);

            }
            else {
                my_routes.put(houseMan,Arrays.asList(_finalPoint,_stock,houseMan,_startPoint));
                rez2-=sA.minD(Arrays.asList(_finalPoint,_stock,_startPoint));
                return (rez2*45+300);

            }

        }
        else {

            int min=10000;

            for (int i=1;i<(route.size()-1);i++)
            {
                sum=sA.minD(Arrays.asList(route.get(i), houseMan,route.get(i+1)));
                if (sum<min){
                    min=sum;
                    n=i;
                }


            }
            List<Integer> result =new ArrayList<Integer>();
            for (int k=0;k<=n;k++)
            {
                result.add(route.get(k));
            }


            result.add(houseMan);
            for (int k=n+1;k<route.size();k++)
            {
                result.add(route.get(k));
            }
            //result.addAll(route.subList(n+1,route.size()-1));

            int cost= sA.minD(result)-sA.minD(route);
            my_routes.put(houseMan,result);
            return (cost*45+300);

            }
    }
}





       /* HashMap<Integer,List<Integer>> dist= new HashMap<Integer, List<Integer>>();
        
        List<Integer> distances = new ArrayList<Integer>();

        Integer rez1 = (sA.minDist(_startPoint, _stock) + sA.minDist(_stock, houseMan)+sA.minDist(houseMan,_finalPoint)-sA.minDist(_startPoint,_stock)-sA.minDist(_stock,_finalPoint) )*45+300;
        Integer rez2 =(sA.minDist(_finalPoint, _stock) + sA.minDist(_stock, houseMan)+sA.minDist(houseMan,_startPoint)-sA.minDist(_finalPoint,_stock)-sA.minDist(_stock,_startPoint))*45+300;
        Integer rez3 =(sA.minDist(_startPoint,_stock)+sA.minDist(_stock,houseMan)+sA.minDist(houseMan,_startPoint)-sA.minDist(_startPoint,_stock)-sA.minDist(_stock,_startPoint))*45+300;
        Integer rez4=(sA.minDist(_finalPoint,_stock)+sA.minDist(_stock,houseMan)+sA.minDist(houseMan,_finalPoint)-sA.minDist(_finalPoint,_stock)-sA.minDist(_stock,_finalPoint))*45+300;
        distances.add(rez1);
        distances.add(rez2);
        distances.add(rez3);
        distances.add(rez4);
        List<Integer> r1=new ArrayList<Integer>();
        List<Integer> r2=new ArrayList<Integer>();
        List<Integer> r3=new ArrayList<Integer>();
        List<Integer> r4=new ArrayList<Integer>();
        r1.add(_startPoint);r1.add(_stock);r1.add(houseMan);r1.add(_finalPoint);
        r2.add(_finalPoint);r2.add(_stock);r2.add(houseMan);r2.add(_startPoint);
        r3.add(_finalPoint);r3.add(_stock);r3.add(houseMan);r3.add(_finalPoint);
        r4.add(_startPoint);r4.add(_stock);r4.add(houseMan);r4.add(_startPoint);
        dist.put(rez1,r1);dist.put(rez2,r2);dist.put(rez3,r3);dist.put(rez4,r4);


        int min = distances.get(0);
        for (int i : distances){
            min = min < i ? min : i;
        }
        my_routes.put(houseMan,dist.get(min));

        return min;
    }*/



