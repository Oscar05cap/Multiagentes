package prac2;

import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import javax.swing.*;
import java.util.*;


public class Boss extends Agent
{
    private Escenario gui;
    private final int size = 15;
    private final int obsRate = 5; // porcentaje de obstáculos
    private int nAgents;
    private final int maxCoins = 20;
    private int nCoins = 0;
    private boolean busy[];
    private Map<String, int[]> workerPositions; // guarda las posiciones de los trabajdores
    private List<String> freeWorkers; // lista de trabajadores libres

    @Override
    protected void setup()
    {
        System.out.println(getLocalName() + " iniciando");

        workerPositions = new HashMap<>();
        freeWorkers = new ArrayList<>();

        this.addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if(msg != null){
                    String content = msg.getContent().toLowerCase(); // extrae el contenido del mensaje
                    String workerId = msg.getSender().getLocalName(); // obtiene el nombre del worker que envió el mensaje

                    if(content.contains("pick")){
                        // extrae las coordenadas usando substring()
                        int start = content.indexOf("(");
                        int end = content.indexOf(")") + 1;
                        String coordinates = content.substring(start, end);
                        String[] parts = coordinates.substring(1, coordinates.length() - 1).split(",");
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);

                        // guarda la posición del worker cuando recoge la moneda
                        workerPositions.put(workerId, new int[]{x,y});
                        System.out.println(getLocalName() + " dice que: "
                                + workerId
                                + " recogió la moneda en " + coordinates + " y se quedó quieto");

                        // marca al trabajador como libre
                        freeWorkers.add(workerId);
                        busy[Integer.parseInt(workerId.substring((workerId.length()) - 1)) -1] = false;
                    } else if (content.contains("position")) {
                        // actualiza la posición del trabajador
                        String[] parts = content.substring(content.indexOf("(") + 1, content.indexOf(")")).split(",");
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);
                        workerPositions.put(workerId, new int[]{x,y});
                        System.out.println(getLocalName() + " actualizó la posición de "
                                + workerId
                                + " a (" + x + ", " + y + ")");
                    }
                }
                block();
            }
        });

        // Agrega un comportamiento controlado por tiempo
        this.addBehaviour(new TickerBehaviour(this, 5000)
        {
            @Override
            protected void onTick() {
                drop();
                if(nCoins > maxCoins){
                    System.out.println("Deja de tirar varo");
                    stop();
                }
            }
        });

        gui = Escenario.getInstance(size, obsRate);
        gui.setVisible(true);

        // crea a los agentes trabajadores
        nAgents = Integer.parseInt(JOptionPane.showInputDialog("¿Cuántos agentes (máximo 3)?"));
        if(nAgents > 0 && nAgents <= 3){
            try{
                // obtiene el contener actual del agente
                ContainerController container = getContainerController();
                AgentController workers[] = new AgentController[nAgents];
                busy = new boolean[nAgents];

                for(int i = 0; i < nAgents; i++){
                    // crea y registra AgenteTrabajador
                    String workerId = "Trabajador_" + (i + 1);
                    workers[i] = container.createNewAgent(workerId, "prac2.Worker", null);
                    workers[i].start();
                    Thread.sleep(2000);
                    busy[i] = false; // inicialmente todos los trabajadores están libres
                    workerPositions.put(workerId, new int[]{0,0}) // posición inicial
                    freeWorkers.add(workerId); // añade a la lista de trabajadores libres
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            System.exit(0);
        }
        startSniffer();
    }

    @Override
    protected void takeDown()
    {
        System.out.println(getLocalName() + " se retira... ");
        System.exit(0);
    }

    private void drop()
    {
        Random rand = new Random();
        int x, y;
        boolean validPosition;

        do{
            x = rand.nextInt(0, size);
            y = rand.nextInt(0, size);
            validPosition = !gui.isObstacle(x,y) && !gui.isCoin(x,y);
        }while(!validPosition);

        gui.dirty(x,y);
        nCoins++;

        // asigna la moneda al trabajador más cercano
        String closestWorker = findClosestWorker(x,y);
        if(closestWorker != null){
            sendMessage(closestWorker, "coin(" + x + "," + y + ")");
            busy[Integer.parseInt(closestWorker.substring(closestWorker.length() - 1)) - 1] = true; // marca al trabajador como ocupado
            freeWorkers.remove(closestWorker);
        }
    }

    private String findClosestWorker(int x, int y){

    }

    // Método para enviar mensajes a otros agentes
    private void sendMessage(String receiver, String content) {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new jade.core.AID(receiver, jade.core.AID.ISLOCALNAME));
        message.setContent(content);
        send(message);
        System.out.println("Mensaje enviado a " + receiver + ": " + content);
    }

    private void startSniffer()
    {
        try
        {
            ContainerController container = getContainerController();
            AgentController sniffer = container.createNewAgent(
                    "Sniffer", "jade.tools.sniffer.Sniffer", new Object[]{});
            sniffer.start();

        } catch (StaleProxyException e) {e.printStackTrace();}
    }

}
