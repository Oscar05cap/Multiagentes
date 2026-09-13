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
        if(newI < 0 || newI >= matrix.length || newJ >0 || newJ >= matrix[0].length){
            return false;
        }
        return matrix[newI][newJ] != 1;
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
                        
            if(xPre != x || yPre != y) // Hay movimiento
            {
                System.out.println(this.getName()+ " Me muevo " + mov + " de "+ xPre + "," +yPre + " a " + x + "," + y);
                energy--;
            }
            else System.out.println(this.getName()+" NO me muevo, no se genero movimiento valido");

            
            if(energy < initialEnergy/2) face = medium;
            if(energy < initialEnergy/4) face = low;
            if(energy == 0) face=dead; 
            
            gui.actualizarPosicion(face, xPre, yPre, x, y);
            if(energy == 0) moving = false;
        }
    }

    private void recharge()
    {

    }
}