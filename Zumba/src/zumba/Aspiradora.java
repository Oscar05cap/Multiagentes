package zumba;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;


import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
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
    private static final int tickRate = 10;
    private int rechargeCounter = 0;

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

            if (gui.getObject(x, y) == 2 && energy < initialEnergy) {
                recharge();
                return;   // no se mueve mientras recarga
            }
            int yPre = y;
            int xPre = x;

            int newI = x;
            int newJ = y;

            dir = chooseDir();

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

            if(gui.getObject(x,y) == 2){
                recharge();
            }
            ImageIcon base = (energy == 0)             ? dead
                    : (energy < initialEnergy/4) ? low
                    : (energy < initialEnergy/2) ? medium
                    : high;
            face = battery(base, energy, initialEnergy);

            gui.actualizarPosicion(face, xPre, yPre, x, y);
            if(energy == 0) moving = false;
        }
    }

    private void recharge()
    {
        rechargeCounter++;
        energy += initialEnergy /tickRate; // 25 unidades por cada tick
        if(energy > initialEnergy) energy = initialEnergy;

        if (energy > initialEnergy / 2)      face = battery(high,   energy, initialEnergy);
        else if (energy > initialEnergy / 4) face = battery(medium, energy, initialEnergy);
        else                                 face = battery(low,    energy, initialEnergy);
        System.out.println(getName() + " recargando " + energy + "/" + initialEnergy);

        // repinta la cara en la misma posición
        gui.actualizarPosicion(face, x, y, x, y);

        if(energy >= initialEnergy){
            rechargeCounter = 0;
        }
        System.out.println(getName() + " AGENTE RECARGADO");
    }

    private int chooseDir()
    {
        boolean lowEnergy = energy < initialEnergy*0.30;
        boolean stationLoc = Escenario.stationExistence();

        if(lowEnergy && stationLoc){
            int stationRow = Escenario.getStationRow();
            int stationColumn = Escenario.getStationColumn();

            // compara las distancias que hay a la ubicación de la estación
            int difX = Integer.compare(stationColumn, x); // -1 (<), 0 (=), +1 (>)
            int difY = Integer.compare(stationRow, y);

            if(aleatorio.nextInt(100) < 70) {
                if (difX > 0) return 1; // derecha
                if (difX < 0) return 3; // izquierda
                if (difY > 0) return 4; // abajo
                if (difY < 0) return 2; // arriba
            }
        }
        return aleatorio.nextInt(1,5);
    }
    private ImageIcon battery(ImageIcon base, int energy, int max) {
        int w = base.getIconWidth();
        int h = base.getIconHeight();

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(base.getImage(), 0, 0, null);

        int barX = 2, barY = h - 7, barW = w - 4, barH = 5;
        g.setColor(Color.BLACK);
        g.fillRect(barX - 1, barY - 1, barW + 2, barH + 2);
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barW, barH);

        float pct = Math.min(1f, (float) energy / max);
        Color c = pct > 0.5f ? new Color(60, 200, 60)
                : pct > 0.25f ? new Color(240, 160, 0)
                : new Color(220, 50, 50);
        g.setColor(c);
        g.fillRect(barX, barY, (int)(barW * pct), barH);

        g.dispose();
        return new ImageIcon(img);
    }
}