package zumba;

import javax.swing.*;
import java.awt.*;
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

    public static Escenario getInstance(int size, int dirtRate)
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

                if(r <= dirtRate) {
                    grid[i][j].setIcon(dirt);
                    matrix[i][j] = 3;
                }

                add(grid[i][j]);
                final int row = i;
                final int col = j;
                grid[i][j].addMouseListener(new MouseAdapter() // Este listener nos ayuda a agregar poner objetos en la rejilla
                {
                    @Override
                    public void mousePressed(MouseEvent e)
                    {
                               insertObject(row,col);
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
            if(xPre == x && yPre == y) return;
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

    public void insertObject(int row, int col){
        if(actualIcon == null) return;

        JLabel box = grid[row][col];

        if(actualIcon == stationIcon){
            if(stationYN){
                JOptionPane.showMessageDialog(this, "Ya existe una estación de recarga");
                return;
            }
            box.setIcon(stationIcon);
            matrix[row][col] = 2;
            stationYN = true;
            stationRow = row;
            stationColumn = col;
        }
        else if(actualIcon == obstacleIcon){
            box.setIcon(obstacleIcon);
            matrix[row][col] = 1;
        }
        else if(actualIcon == dirt){
            box.setIcon(dirt);
            matrix[row][col] = 3;
        }
         /*
        Obstáculos = 1
        Hojas = 3
        Nada = 0
        */
    }

    public int getObject(int x, int y){
        if(x < 0 || x >= matrix.length || y < 0 || y >= matrix[0].length){
            return 1;
        }
        return matrix[y][x];
    }

    public void cleanBox(int row, int column){
        matrix[row][column] = 0;
    }

}