package comercio;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.util.Random;

/**
 * Agente que publica su servicio en el DF y responde a las solicitudes (CFP)
 * con una propuesta (PROPOSE) o un rechazo (REFUSE)
 */
public class Proveedor extends Agent {
    private RegistroMensajes gui;
    private Random random = new Random();

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            gui = (RegistroMensajes) args[0];
        }

        // 1. Publicar el servicio en el Directory Facilitator (DF)
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("proveedor-productos");
        sd.setName("cotizacion-productos");
        dfd.addServices(sd);
        
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // 2. Escuchar mensajes CFP que sigan el protocolo Contract Net
        MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        
        addBehaviour(new ContractNetResponder(this, template) {
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) {
                try {
                    // Leer la solicitud enviada por el Shopper
                    SolicitudCotizacion sc = (SolicitudCotizacion) cfp.getContentObject();
                    gui.registrarMensaje(myAgent.getLocalName(), "CFP recibido (" + sc.getIdSolicitud() + ")");
                    
                    // Lógica simulada: 80% de probabilidad de tener el producto en inventario
                    boolean tieneInventario = random.nextDouble() > 0.2;

                    if (tieneInventario) {
                        // Generar precios y tiempos aleatorios para que compitan
                        double precio = 50 + (350 * random.nextDouble());
                        int tiempoEntrega = 1 + random.nextInt(5);
                        
                        Cotizacion cotizacion = new Cotizacion(sc, getAID(), precio, tiempoEntrega);

                        // Responder con PROPOSE incluyendo el objeto Cotizacion
                        ACLMessage propose = cfp.createReply();
                        propose.setPerformative(ACLMessage.PROPOSE);
                        propose.setContentObject(cotizacion);
                        
                        gui.registrarMensaje(myAgent.getLocalName(), "PROPOSE: " + sc.getProducto().getNombre() + " : $" + String.format("%.2f", precio));
                        return propose;
                    } else {
                        // Responder con REFUSE si no hay inventario
                        ACLMessage refuse = cfp.createReply();
                        refuse.setPerformative(ACLMessage.REFUSE);
                        gui.registrarMensaje(myAgent.getLocalName(), "REFUSE: " + sc.getProducto().getNombre() + " (sin existencia)");
                        return refuse;
                    }
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
                gui.registrarMensaje(myAgent.getLocalName(), "ACCEPT_PROPOSAL recibido.");
                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
            }

            @Override
            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                gui.registrarMensaje(myAgent.getLocalName(), "REJECT_PROPOSAL recibido.");
            }
        });
    }

    @Override
    protected void takeDown() {
        // Eliminar el registro del DF al destruir el agente
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}