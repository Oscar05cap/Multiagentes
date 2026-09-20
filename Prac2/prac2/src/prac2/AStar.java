package prac2;

import java.util.*;

class Node implements Comparable<Node>
{
    int x, y, g, h, f; // coordenadas x,y costo acumulado, heurística y función
    Node parent; // nodo padre desde donde se construirá la ruta

    public Node(int i, int j)
    {
        this.x = i;
        this.y = j;
    }

    @Override
    public int compareTo(Node other) // método inherente a comparable
    {
        return Integer.compare(this.f, other.f); // compara un objeto actual con otro elemento de la misma clase
    }

    @Override
    public boolean equals(Object obj)
    {
        if(this == obj) return true;
        if(!(obj instanceof Node)) return false;
        Node other = (Node) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(x,y);
    }
}

public class AStar
{
    private int[][] matrix;
    private int startX, startY, endX, endY;
    private int[][] directions = {{0, 1}, {1,0}, {0,-1}, {-1, 0}};
    private int width, height;

    public AStar(int[][] grid, int startX, int startY, int endX, int endY)
    {
        this.matrix = grid;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.width = grid.length;
        this.height = grid[0].length;
    }

    public List<Node> findPath()
    {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<Node, Node> parents = new HashMap<>();
        Map<Node, Integer> gCosts = new HashMap<>();

        Node startNode = new Node(startX, startY);
        startNode.g = 0; // inicialización del coste
        startNode.h = heuristic(startNode); // costo estimado
        startNode.f = startNode.g + startNode.h; // costo estimado total

        openSet.add(startNode); // se comienza evualuando desde el nodo inicial
        gCosts.put(startNode, 0); // costo desde el nodo de inicio

        while(!openSet.isEmpty()){
            Node current = openSet.poll(); // recupera el primer elemento de la cola

            if(current.x == endX && current.y == endY){
                return buildPath(parents, current);
            }
            for(int[] dir : directions) {
                int newX = current.x + dir[0]; // mueve una casilla en una dirección
                int newY = current.y + dir[1];

                if(newX < 0 || newX >= width || newY < 0 || newY >= height || matrix[newX][newY] == 1){
                    continue; // filtra posiciones inválidas
                }

                int newCost = current.g + 1;
                Node neighbor = new Node(newX, newY);
                neighbor.h = heuristic(neighbor); // calcula una heurística preeliminar al nodo vecino

                if(!gCosts.containsKey(neighbor) || newCost < gCosts.get(neighbor)){
                    neighbor.parent = current;
                    parents.put(neighbor, current); // guarda el nodo padre en el mapa
                    gCosts.put(neighbor, newCost); // registra el nuevo costo
                    neighbor.g = newCost;
                    neighbor.f = neighbor.g + neighbor.h; // se recalcula la heurística
                    openSet.add(neighbor);
                }
            }
        }
        return null;
    }

    private List<Node> buildPath(Map<Node, Node> parents, Node currentNode)
    {
        List<Node> route = new ArrayList<>();
        while(currentNode != null){
            route.add(currentNode);
            currentNode = parents.get(currentNode);
        }
        Collections.reverse(route);
        return route;
    }

    private int heuristic(Node current)
    {
        return Math.abs(current.x - endX) + Math.abs(current.y - endY); // distancia manhattan como función heurística
    }
}
