import jade.core.Agent;
import jade.wrapper.*;

import javax.swing.plaf.multi.MultiListUI;
import java.util.ArrayList;
import java.util.Random;
import java.util.*;
import static java.lang.Math.min;
import static java.util.Arrays.fill;
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


public class StartAgent extends Agent{
    private int mapSize = 24;
    private int map[][] = new int[mapSize][mapSize];
    private int INF = Integer.MAX_VALUE / 2;
    private ArrayList<Integer[]> Cargos = new ArrayList<Integer[]>();
    public ArrayList<String> result = new ArrayList<String>();

    protected void setup() {
        Cargos.add(new Integer[]{3, 1});
        Cargos.add(new Integer[]{3, 2});
        Cargos.add(new Integer[]{3, 4});
        Cargos.add(new Integer[]{3, 21});
        Cargos.add(new Integer[]{3, 7});
        Cargos.add(new Integer[]{3, 18});
        Cargos.add(new Integer[]{3, 11});
        CreateMap(0);
        drawMap();
        startAngents();
        doDelete();
    }


    public void CreateMap(int start)
    {



        int adjacencyList[][] = {{0, 2}, {0, 3},{0,7},{0,15}, {0, 11},{1,5}, {1, 7},{1,14},{1,16},{1,20}, {2, 4}, {2, 5}, {2, 6}, {2,12},{2,15},{2,17},{2,22},{3, 6},
                                {3, 7},{3,15},{3,18},{3,19},{4,5}, {4, 8},{4,10},{4,15},{4,13},{4,17},{4,19},{5,7}, {5, 9},{5,15},{5,13},{5,18},{5,20}, {6, 8}, {6, 9},{6,13},{6,18},{6,22}, {7, 10},{7,13},{7,19},{7,22},
                {8, 9}, {8, 11},{8,15},{8,17},{8,23},{9, 10}, {9, 15},{9,19},{9,22}, {10, 14}, {10, 17},{10,23}, {11, 13},{11,20},{11,18},
                {12, 16},{12,22}, {13, 15},{13,16},{13,23},{14,17},{14,21},{14,23},{15,17},{15,20},{15,23},{16,18},{16,21},{17,19},{17,23},
                {18,19},{18,22},{19,20},{20,23},{21,22},{22,23}};

        for(int r = 0; r < mapSize; r++)
        {
            for (int c = 0; c < mapSize; map[r][c] = INF, c++);
        }

        for(int i = 0; i < mapSize; map[i][i] = 0, i++);

        Random rnd = new Random();
        for(int i = 0; i < adjacencyList.length; i++)
        {
            int p1 = adjacencyList[i][0];
            int p2 = adjacencyList[i][1];
            map[p1][p2] =  rnd.nextInt(19)+1;
            map[p2][p1] = rnd.nextInt(19)+1;
        }

        System.out.println();
    }

    private void drawMap()
    {
        System.out.println("Map: ");
        for(int r = 0; r < mapSize; r++)
        {
            for (int c = 0; c < mapSize;  c++)
            {
                if(map[r][c]!= INF) {
                    System.out.print(map[r][c] + " ");
                }
                else {
                    System.out.print("* ");
                }
            }
            System.out.println();
        }

        System.out.println();
        System.out.println("Min. distance between points on map:");
        for(int r = 0; r < mapSize; r++)
        {
            for (int c = 0; c < mapSize; System.out.print(minDist(r, c) + "   "), c++);
            System.out.println();
        }
        System.out.println();
    }

    private void startAngents()
    {
        Random rndm = new Random();
        ContainerController container = getContainerController();
        AgentController agentController;
        int [][] coords =  {{1,8}, {5,11},
                            {6,14}, {10,17},{12,18},
                            {15,23}, {19,22},
                            };

        int [] coordsHome =  {0,4,7,13,16,20,21};

        try {
            for (int i = 0; i < 7; i++) {
                Object[] riderArgs = {coordsHome[i], 10000};
                agentController = container.createNewAgent("Home_" + i, "HomeAgent", riderArgs);
                agentController.start();
                doWait(1000);
            }
            for (int i = 0; i < 7; i++) {
                Object[] riderArgs = {coords[i][0], coords[i][1],  3};
                agentController = container.createNewAgent("Man_" + i, "RidingAgents", riderArgs);
                agentController.start();
                doWait(1000);
            }
            doWait(1000);
        }
        catch (StaleProxyException e) {
            e.printStackTrace();
        }


    }

     public int minDist (int start, int end) {
        boolean[] used = new boolean [mapSize];
        int[] dist = new int [mapSize];

        fill(dist, INF);
        dist[start] = 0;

        for (;;) {
         int v = -1;
         for (int nv = 0; nv < mapSize; nv++)
             if (!used[nv] && dist[nv] < INF && (v == -1 || dist[v] > dist[nv]))
                 v = nv;

         if (v == -1) break;
         used[v] = true;
         for (int nv = 0; nv < mapSize; nv++)
             if (!used[nv] && map[v][nv] < INF)
                 dist[nv] = min(dist[nv], dist[v] + map[v][nv]);
        }

        return (dist[end]);
     }

    public int minD(List<Integer> list){
        int min=0;
        if (list.size()==0){
            return min;

        }
        else {
        for (int i=0;i<(list.size()-1);i++){
            min+=minDist(list.get(i),list.get(i+1));
        }
        return min;}

    };

}