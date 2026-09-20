package cumbia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class Escenario extends JFrame {
    
    private static Escenario instance;
    
    private final JLabel[][] grid;
    private final ImageIcon obs  = new ImageIcon("Cumbia/imagenes/brick.png");
    private final ImageIcon coin  = new ImageIcon("Cumbia/imagenes/coin.png");
    private final BackGroundPanel scene = new BackGroundPanel(new ImageIcon("Cumbia/imagenes/surface.png"));
    private final int size;
    private int[][] matrix;   // 0 = libre, 1 = obstáculo, 2 = moneda
    
    public static Escenario getInstance(int size, int obsRate) // Será llamado por el jefe
    {
        if(instance == null)
        {
           instance = new Escenario(size, obsRate);         
        }
        return instance;
    }
    
    public static Escenario getInstance() // Será llamado por los trabajadores
    {
        return instance;
    }

    private Escenario(int size, int obsRate) 
    {
        this.size = size;
        setTitle("Simulador");
        setSize(50*size, 50*size);
        this.setContentPane(scene);
        setLayout(new GridLayout(size, size));
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        JMenuBar barraMenus = new JMenuBar();
        JMenu file = new JMenu("File");       
        JMenuItem exit   = new JMenuItem("Stop");
              
        this.setJMenuBar(barraMenus);
        barraMenus.add(file);
        file.add(exit);
        
        
        grid = new JLabel[size][size];
        Random aleatorio = new Random();
        matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = new JLabel();
                grid[i][j].setOpaque(false);
                
                int r = aleatorio.nextInt(0,100);
                
                if(r <= obsRate) {
                    grid[i][j].setIcon(obs);
                    matrix[i][j] = 1;
                }

                add(grid[i][j]);

                final int row = i;
                final int col = j;

                grid[i][j].addMouseListener(new MouseAdapter() // Este listener nos ayuda a agregar poner objetos en la rejilla
                {
                    @Override
                    public void mousePressed(MouseEvent e) 
                    {
                               obstacle(row, col);
                    }
                });
            }

        }
        
        exit.addActionListener(evt -> gestionaSalir(evt));
        System.out.println("Inicia escenario");

    }
    
    private void gestionaSalir(ActionEvent eventObject)
    {
        System.exit(0);
    }
     
    public void colocar(int i, int j, ImageIcon face)
    {
        grid[i][j].setIcon(face);
    }
    
    public void actualizarPosicion(ImageIcon face, int xPre, int yPre, int x, int y)
    {
        grid[yPre][xPre].setIcon(null);
        grid[y][x].setIcon(face);
    }

    private void obstacle(int row, int col) {
        grid[row][col].setIcon(obs);
        matrix[row][col] = 1;
    }

    public void dirty(int x, int y) {
        grid[y][x].setIcon(coin);
        matrix[y][x] = 2;
    }
    
    public int getRoomSize()
    {
        return size;
    }

    public int[][] getMatrix()
    {
        return matrix;
    }
}