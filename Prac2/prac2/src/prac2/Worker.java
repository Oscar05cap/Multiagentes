package prac2;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import javax.swing.*;
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

    @Override
    protected void setup()
    {
        faces[0]= new ImageIcon("imagenes/mario.png");
        faces[1]= new ImageIcon("imagenes/luigi.png");
        faces[2]= new ImageIcon("imagenes/toad.png");
        gui = Escenario.getInstance(); // Obtiene instancia del escenario
        gui.setVisible(true);
        size = gui.getRoomSize();

        face = faces[(Integer.parseInt(getLocalName().substring(getLocalName().length()-1)))-1];
        gui.colocar(x,y, face);

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


                    if(content.contains("coin"))
                    {
                        // Extraer las coordenadas usando substring()
                        int start = content.indexOf("(");
                        int end = content.indexOf(")")+1;
                        String coordinates = content.substring(start, end);
                        String[] parts = coordinates.substring(1, coordinates.length() - 1).split(",");
                        targetX = Integer.parseInt(parts[0]);
                        targetY = Integer.parseInt(parts[1]);
                        System.out.println(getLocalName()+ " dice: a recoger moneda en " + coordinates);

                        // se calcula la ruta usando a*
                        path = aStarSearch(x, y, targetX, targetY);
                        moving = true;

                        // responde al boss que se está moviendo hacia la moneda
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("pick"+" "+coordinates);
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

    private void mover()
    {
        if(moving)
        {
            // Calcula dirección

            int yPre = y;
            int xPre = x;

            dir = aleatorio.nextInt(1,5);

            // 1 - derecha
            // 2 - arriba
            // 3 - izquierda
            // 4 - abajo

            String mov = "";

            switch(dir)
            {
                case 1 -> {
                    if(x < size-1) x++; mov = "derecha";
                }
                case 2 -> {
                    if(y > 0) y--;  mov = "arriba";
                }
                case 3 -> {
                    if(x > 0) x--;  mov = "izquierda";
                }
                case 4 -> {
                    if(y < size-1) y++; mov = "abajo";
                }
            }


            gui.actualizarPosicion(face, xPre, yPre, x, y);

        }
    }

}