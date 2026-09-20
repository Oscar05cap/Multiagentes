/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package cumbia;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

/**
 *
 * @author 
 */
public class Cumbia  
{
    public static void main(String[] args) 
    {
        try {
            // Obtener la instancia de Runtime de JADE
            jade.core.Runtime rt = jade.core.Runtime.instance();

            // Configurar el perfil del contenedor principal
            Profile p = new ProfileImpl();
            p.setParameter(Profile.GUI,"true");
            p.setParameter(Profile.MAIN_HOST, "localhost");

            // Crear el contenedor principal
            AgentContainer mainContainer = rt.createMainContainer(p);

                                               
            // Iniciar el agente coordinador dentro del contenedor
            AgentController coordinator = mainContainer.createNewAgent(
                "Jefe", "cumbia.Boss", null);
            coordinator.start();
                                 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
