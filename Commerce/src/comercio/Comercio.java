
package comercio;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

/**
 * Clase principal que inicializa JADE, construye la GUI y lanza los agentes
 */
public class Comercio {
    public static void main(String[] args) {
        try {
            // Inicializar el Runtime y crear el contenedor principal
            Runtime rt = Runtime.instance();
            Profile p = new ProfileImpl();
            p.setParameter(Profile.GUI, "true");
            ContainerController cc = rt.createMainContainer(p);

            // Construir la interfaz gráfica
            ShopperGUI gui = new ShopperGUI();
            gui.setVisible(true);

            // Crear y arrancar tres agentes Proveedor, pasándoles la referencia de la GUI para logs
            AgentController prov1 = cc.createNewAgent("Proveedor1", Proveedor.class.getName(), new Object[]{gui});
            AgentController prov2 = cc.createNewAgent("Proveedor2", Proveedor.class.getName(), new Object[]{gui});
            AgentController prov3 = cc.createNewAgent("Proveedor3", Proveedor.class.getName(), new Object[]{gui});
            prov1.start();
            prov2.start();
            prov3.start();

            // Crear y arrancar el agente intermediario Shopper
            AgentController shopper = cc.createNewAgent("Shopper", Shopper.class.getName(), new Object[]{gui});
            shopper.start();

            // Pequeña pausa para asegurar que Shopper.setup() complete la línea registerO2AInterface()
            Thread.sleep(1500);

            // Recuperar la interfaz O2A del agente Shopper y vincularla a la GUI
            ShopperInterface shopperInt = shopper.getO2AInterface(ShopperInterface.class);
            gui.setShopperInterface(shopperInt);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}