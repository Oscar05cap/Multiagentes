package zumba;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import javax.swing.*;
import java.util.Random;

/**
 *
 * @author macuache
 */
public class Aspiradora extends Agent
{
    private Escenario gui;
    private  int x = 0, y = 0;  // Posición inicial en la cuadrícula
    private final int size = 15;     // Tamaño de la habitación
    private final int dirtRate = 15; // Porcentaje de suciedad
    private int dir = 0;
    private final ImageIcon high = new ImageIcon("Zumba/imagenes/buttercup.png");
    private final ImageIcon medium = new ImageIcon("Zumba/imagenes/bubbles.png");
    private final ImageIcon low = new ImageIcon("Zumba/imagenes/blossom.png");
    private final ImageIcon dead = new ImageIcon("Zumba/imagenes/skull.png");
    private ImageIcon face;
    private final int initialEnergy = 500;
    private int energy;
    private final int tick = 50; // 10 milisegundos
    private static int stationRow = -1;
    private static int stationColum = -1;
    private final Random aleatorio = new Random();
    private boolean moving = false;
    private int matrix[][];

    protected void setup()
    {
        face = high;
        energy = initialEnergy;
        gui = Escenario.getInstance(size, dirtRate); // Obtiene instancia del escenario
        gui.setVisible(true);
        gui.colocar(0,0, face);

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
                    if(content.equals("start") || content.equals("continue")) moving = true;
                    if(content.equals("stop") || content.equals("pause")) moving = false;
                }
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

    private boolean validPosition(int newI, int newJ){
        if(newI < 0 ||  newI >= size || newJ < 0 || newJ >= size){
            return false;
        }
        int object = gui.getObject(newI, newJ);
        return object != 1; // evita el obstáculo (1)
    }

    private void mover()
    {
        if(moving)
        {
            // Calcula dirección

            int yPre = y;
            int xPre = x;

            int newI = x;
            int newJ = y;

            dir = aleatorio.nextInt(1,5);

            // 1 - derecha
            // 2 - arriba
            // 3 - izquierda
            // 4 - abajo

            String mov = "";

            switch (dir) {
                case 1 -> { newI = x + 1; mov = "derecha";   }
                case 2 -> { newJ = y - 1; mov = "arriba";    }
                case 3 -> { newI = x - 1; mov = "izquierda"; }
                case 4 -> { newJ = y + 1; mov = "abajo";     }
            }

            if(validPosition(newI, newJ)) // Hay movimiento si la posición se considera válida
            {
                x = newI;
                y = newJ;
                System.out.println(this.getName()+ " Me muevo " + mov + " de "+ xPre + "," +yPre + " a " + x + "," + y);
                energy--;
            }
            else System.out.println(this.getName()+" NO me muevo, no se genero movimiento valido");


            if(energy < initialEnergy/2) face = medium;
            if(energy < initialEnergy/4) face = low;
            if(energy == 0) face=dead;

        }
    }

    private void recharge()
    {
    energy = initialEnergy;
    face = high;
    System.out.println(getName() + " AGENTE RECARGADO");
    }
}