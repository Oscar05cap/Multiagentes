package cumbia;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import javax.swing.*;
import java.util.List;
import java.util.Random;

/**
 *
 * @author
 */
public class Worker extends Agent
{
    private Escenario gui;
    private  int x = 0, y = 0;  // Posición inicial en la cuadrícula
    private int size;     // Tamaño de la habitación
    private int dir = 0;
    private ImageIcon face;
    private final int tick = 50; // 50 milisegundos
    private final ImageIcon[] faces = new ImageIcon[3];
    private final Random aleatorio = new Random();
    private boolean moving = false;
    private int targetX, targetY; // coordenadas objetivo
    private List<Node> path; // ruta de A*
    private int pathIndex = 0; // índice del siguiente nodo a visitar
    private jade.core.AID bossAID;   // se guarda al recibir las monedas


    @Override
    protected void setup()
    {
        faces[0]= new ImageIcon("Cumbia/imagenes/mario.png");
        faces[1]= new ImageIcon("Cumbia/imagenes/luigi.png");
        faces[2]= new ImageIcon("Cumbia/imagenes/toad.png");
        gui = Escenario.getInstance(); // Obtiene instancia del escenario
        gui.setVisible(true);
        size = gui.getRoomSize();

        face = faces[(Integer.parseInt(getLocalName().substring(getLocalName().length()-1)))-1];

        gui.colocar(0, 0, face);

        System.out.println("Agente trabajador iniciado: " + getLocalName());

        // Agrega un comportamiento cíclico

        this.addBehaviour(new CyclicBehaviour(this)
        {
            @Override
            public void action()
            {
                ACLMessage msg = receive();  // Espera un mensaje
                if(msg!=null) // Verifica si se recibió mensaje
                {
                    String content = msg.getContent().toLowerCase();  // Extrae el contenido del mensaje


                    if (content.contains("coin")) {
                        bossAID = msg.getSender();

                        int start = content.indexOf("(");
                        int end = content.indexOf(")") + 1;
                        String coordinates = content.substring(start, end);
                        String[] parts = coordinates.substring(1, coordinates.length() - 1).split(",");
                        targetX = Integer.parseInt(parts[0]);
                        targetY = Integer.parseInt(parts[1]);

                        System.out.println(getLocalName() + " dice: a recoger moneda en " + coordinates);

                        path = aStarSearch(x, y, targetX, targetY);

                        if (path == null || path.size() <= 1) {
                            System.out.println(getLocalName() + " sin ruta hacia " + coordinates);
                            path = null;
                            moving = false;
                        } else {
                            pathIndex = 1;
                            moving = true;
                            System.out.println(getLocalName() + " ruta calculada con " + path.size() + " nodos");
                        }

                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("moving " + coordinates);
                        send(reply);
                    }
                }
                block();
            }
        });

        // Agrega un comportamiento controlado por tiempo

        this.addBehaviour(new TickerBehaviour(this, tick) {
            @Override
            protected void onTick() {
                mover();
            }
        });
    }

    private void mover() {
        if (!moving || path == null) return;

        int xPre = x;
        int yPre = y;

        Node next = path.get(pathIndex);

        x = next.y;
        y = next.x;
        pathIndex++;

        System.out.println(getLocalName() + " me muevo a (" + x + "," + y + ")  ["
                + pathIndex + "/" + path.size() + "]");

        gui.actualizarPosicion(face, xPre, yPre, x, y);

        if (pathIndex >= path.size()) {
            System.out.println(getLocalName() + " llegué a (" + x + "," + y + ")");

            // Avisa al boss que ya llegó
            if (bossAID != null) {
                ACLMessage done = new ACLMessage(ACLMessage.INFORM);
                done.addReceiver(bossAID);
                done.setContent("picked (" + x + "," + y + ")");
                send(done);
            }

            moving = false;
            path = null;
            pathIndex = 0;
            return;
        }
    }

    private List<Node> aStarSearch(int wStartX, int wStartY, int wGoalX, int wGoalY) {
        int[][] matrix = gui.getMatrix();
        AStar astar = new AStar(
                matrix,
                wStartY,
                wStartX,
                wGoalY,
                wGoalX
        );
        return astar.findPath();
    }
}