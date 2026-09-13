package zumba;

import jade.tools.sniffer.MMCanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class Escenario extends JFrame {
    
    private static Escenario instance;

    private int matrix[][];
    private boolean stationYN;
    private static int stationRow = -1;
    private static int stationColumn = -1;

    private final JLabel[][] grid;
    private final ImageIcon dirt  = new ImageIcon("Zumba/imagenes/leaf.png");
    private final ImageIcon obstacleIcon = new ImageIcon("Zumba/imagenes/obstacle.jpg");
    private final ImageIcon stationIcon = new ImageIcon("Zumba/imagenes/station.jpg");
    private ImageIcon actualIcon;
    private final BackGroundPanel floor = new BackGroundPanel(new ImageIcon("Zumba/imagenes/floor.jpg"));

    private final JMenu generate = new JMenu("Random generate");
    private final JMenu settings = new JMenu("Settings");
    private final JRadioButtonMenuItem obstacle = new JRadioButtonMenuItem("Obstacle");
    private final JRadioButtonMenuItem leaf = new JRadioButtonMenuItem("Leaf");
    private final JRadioButtonMenuItem station = new JRadioButtonMenuItem("Station");
    private final JMenuItem randomObstacle = new JMenuItem("Random Obstacle");
    private final JMenuItem randomLeaf = new JMenuItem("Random leaves");

    public static Escenario getInstance(int size, int dirtRate, int matrix[][])
    {
        if(instance == null)
           instance = new Escenario(size, dirtRate);
        return instance;
    }

    private Escenario(int size, int dirtRate) {
        setTitle("Probando JADE (MyName)");
        setSize(50*size, 50*size);
        this.setContentPane(floor);
        setLayout(new GridLayout(size, size));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ButtonGroup settingOptions = new ButtonGroup();
        settingOptions.add(leaf);
        settingOptions.add(obstacle);
        settingOptions.add(station);

        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        menuBar.add(settings);
        menuBar.add(generate);
        settings.add(station);
        settings.add(obstacle);
        settings.add(leaf);
        generate.add(randomObstacle);
        generate.add(randomLeaf);

        grid = new JLabel[size][size];
        matrix = new int[size][size];
        Random aleatorio = new Random();

        leaf.addItemListener(evt -> leafSet(evt));
        obstacle.addItemListener(evt -> obstacleSet(evt));
        station.addItemListener(evt -> stationSet(evt));

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = 0;
                grid[i][j] = new JLabel();
                grid[i][j].setOpaque(false);
                
                int r = aleatorio.nextInt(0,100);
                
                if(r <= dirtRate)
                    grid[i][j].setIcon(dirt);

                add(grid[i][j]);
                grid[i][j].addMouseListener(new MouseAdapter() // Este listener nos ayuda a agregar poner objetos en la rejilla
                {
                    @Override
                    public void mousePressed(MouseEvent e) 
                    {
                               insertObject(e);
                    }   
                
                    @Override
                    public void mouseReleased(MouseEvent e) 
                    {
                                insertObject(e);
                    }   


                });
            }
        }

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

    private void leafSet(ItemEvent eventObject){
        JRadioButtonMenuItem opt = (JRadioButtonMenuItem) eventObject.getSource();
        actualIcon = opt.isSelected() ? dirt : null;
    }

    private void obstacleSet(ItemEvent eventObject)
    {
        JRadioButtonMenuItem opt = (JRadioButtonMenuItem) eventObject.getSource();
        actualIcon = opt.isSelected() ? obstacleIcon : null;
    }

    private void stationSet(ItemEvent eventObject)
    {
        JRadioButtonMenuItem opt = (JRadioButtonMenuItem) eventObject.getSource();
        actualIcon = opt.isSelected() ? stationIcon : null;
    }

    public void insertObject(MouseEvent e){
        JLabel box = (JLabel) e.getSource();
        int row = (box.getY() - 15) / 50;
        int column = (box.getX() - 15) / 50;

        if(actualIcon == stationIcon && matrix[row][column] != 2){
            if(!stationYN){
                box.setIcon(stationIcon);
                stationYN = true;
                matrix[row][column] = 2;
                stationRow = row;
                stationColumn = column;
            }else{
                JOptionPane.showMessageDialog(this, "Ya existe una estación de recarga");
            }
        }
        box.setIcon(actualIcon);
        matrix[row][column] = (actualIcon == obstacleIcon) ? 1 : (actualIcon == dirt) ? 3 : 0;
    }

}