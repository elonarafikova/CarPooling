import jade.Boot;

public class Main {

    public static void main(String[] args){
        String[] s = {"-gui", "start:StartAgent"};
        Boot.main(s);
        System.out.println("Let's get it started!");
    }
}