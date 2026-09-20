package cumbia;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

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

                    if (content.contains("picked")) {
                        // llegó al objetivo y se marca como libre
                        String[] parts = content.substring(content.indexOf("(") + 1,
                                content.indexOf(")")).split(",");
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);

                        workerPositions.put(workerId, new int[]{x, y});
                        freeWorkers.add(workerId);
                        busy[Integer.parseInt(workerId.substring(workerId.length() - 1)) - 1] = false;

                        System.out.println(getLocalName() + ": " + workerId
                                + " recogió moneda en (" + x + "," + y + ")");

                    } else if (content.contains("moving")) {
                        System.out.println(getLocalName() + ": " + workerId
                                + " va en camino a " + content);
                        // no lo marques libre aquí

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
                    doDelete();
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
                    workers[i] = container.createNewAgent(workerId, "cumbia.Worker", null);
                    workers[i].start();
                    Thread.sleep(2000);
                    busy[i] = false; // inicialmente todos los trabajadores están libres
                    workerPositions.put(workerId, new int[]{0,0}); // posición inicial
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
    }

    private void drop()
    {
        if (freeWorkers.isEmpty()) {
            System.out.println("Sin workers libres, no genero moneda");
            return;
        }

        int[][] m = gui.getMatrix();
        Random rand = new Random();
        int x, y;
        boolean validPosition;

        // busca una celda libre, descartando obstáculos (1) y monedas ya colocadas (2)
        do {
            x = rand.nextInt(0, size);
            y = rand.nextInt(0, size);
            validPosition = m[y][x] != 1 && m[y][x] != 2;
        } while (!validPosition);

        // pinta una moneda y la registra en la matriz
        gui.dirty(x, y);
        nCoins++;

        // asigna la moneda al trabajador más cercano
        String closestWorker = findClosestWorker(x, y);
        if (closestWorker != null) {
            sendMessage(closestWorker, "coin(" + x + "," + y + ")");
            // toma el índice del último dígito del nombre que tiene el trabajador
            busy[Integer.parseInt(closestWorker.substring(closestWorker.length() - 1)) - 1] = true;
            // marca al trabajador como no disponible mientras realiza la tarea
            freeWorkers.remove(closestWorker);
        }
    }

    private String findClosestWorker(int targetX, int targetY)
    {
        // se guardan todos los candidatos empatados a la distancia mínima
        List<String> candidatos = new ArrayList<>();
        int bestDist = Integer.MAX_VALUE;

        for (String workerId : freeWorkers) {
            int[] pos = workerPositions.get(workerId);
            if (pos == null) continue; // se descarta si existe una posición desconocida

            int dist = Math.abs(pos[0] - targetX) + Math.abs(pos[1] - targetY);

            if (dist < bestDist) {
                // si se encuentra un nuevo minímo los candidatos anteriores quedan descartados
                bestDist = dist;
                candidatos.clear();
                candidatos.add(workerId);
            } else if (dist == bestDist) {
                // empata con el mínimo actual y se añade como candidato
                candidatos.add(workerId);
            }
        }

        if (candidatos.isEmpty()) return null; // ningún trabajador libre con posición
        return candidatos.get(new Random().nextInt(candidatos.size()));
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
            /* Se especifican los nombres de los agentes a snifear separados por punto y coma
            El boss debe llamarse estrictamente "Boss" para que sea snifeado automáticamente
             */
            AgentController sniffer = container.createNewAgent(
                    "Sniffer", "jade.tools.sniffer.Sniffer", new Object[]{"Boss;Trabajador_1;Trabajador_2;Trabajador_3"});
            sniffer.start();

        } catch (StaleProxyException e) {e.printStackTrace();}
    }

}