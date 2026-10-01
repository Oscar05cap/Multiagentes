package comercio;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.util.List;
import java.util.Vector;

/**
 * Agente intermediario que recibe solicitudes de la GUI, descubre proveedores
 * y ejecuta el protocolo Contract Net para obtener la mejor cotización
 */
public class Shopper extends Agent implements ShopperInterface {
    private RegistroMensajes gui;
    private int solicitudCounter = 1;

    @Override
    protected void setup() {
        // Recuperar la referencia a la GUI pasada como argumento desde Comercio
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            gui = (RegistroMensajes) args[0];
        }

        // Registrar esta clase como una interfaz O2A para que la GUI pueda llamarla
        registerO2AInterface(ShopperInterface.class, this);
        gui.registrarMensaje(getLocalName(), "Sistema iniciado y listo.");
    }

    /**
     * Método invocado por la GUI a través de O2A
     * Debe incorporar un Behaviour al agente para mantener la asincronía de JADE
     */
    @Override
    public void solicitarCotizaciones(List<SolicitudCotizacion> solicitudes) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                try {
                    // 1. Descubrir Proveedores registrados en el Directory Facilitator (DF)
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("proveedor-productos");
                    template.addServices(sd);
                    
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    AID[] proveedores = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        proveedores[i] = result[i].getName();
                    }

                    // 2. Generar una negociación Contract Net INDEPENDIENTE por cada producto
                    for (SolicitudCotizacion sc : solicitudes) {
                        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                        for (AID p : proveedores) {
                            cfp.addReceiver(p);
                        }
                        
                        // Enviar el objeto SolicitudCotizacion serializado
                        cfp.setContentObject(sc);
                        cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                        cfp.setReplyByDate(new java.util.Date(System.currentTimeMillis() + 10000));
                        
                        gui.registrarMensaje(getLocalName(), "CFP enviado a proveedores para " + sc.getProducto().getNombre());
                        
                        // Iniciar el comportamiento ContractNetInitiator para este producto específico
                        myAgent.addBehaviour(new GestorContractNet(myAgent, cfp, sc));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Clase interna que maneja el protocolo Contract Net desde el lado del iniciador (Shopper)
     */
    private class GestorContractNet extends ContractNetInitiator {
        private SolicitudCotizacion sc;

        public GestorContractNet(Agent a, ACLMessage cfp, SolicitudCotizacion sc) {
            super(a, cfp);
            this.sc = sc;
        }

        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            gui.registrarMensaje(myAgent.getLocalName(), "Seleccionando mejor cotización para: " + sc.getProducto().getNombre());
            
            double mejorPrecio = Double.MAX_VALUE;
            int mejorTiempo = Integer.MAX_VALUE;
            ACLMessage mejorRespuesta = null;
            Cotizacion mejorCotizacion = null;

            // Evaluar todas las propuestas recibidas de los proveedores
            for (Object obj : responses) {
                ACLMessage msg = (ACLMessage) obj;
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    // Por defecto, preparamos el rechazo para todas las propuestas
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acceptances.add(reply); 

                    try {
                        // Extraer la cotización serializada
                        Cotizacion c = (Cotizacion) msg.getContentObject();
                        
                        // Criterio principal: menor precio. Criterio de desempate: menor tiempo de entrega
                        if (c.getPrecio() < mejorPrecio || (c.getPrecio() == mejorPrecio && c.getTiempoEntrega() < mejorTiempo)) {
                            mejorPrecio = c.getPrecio();
                            mejorTiempo = c.getTiempoEntrega();
                            mejorRespuesta = reply; // Guardamos la referencia para cambiarla a ACCEPT más adelante
                            mejorCotizacion = c;
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }

            // Si hubo al menos una propuesta válida, aceptar la mejor
            if (mejorRespuesta != null) {
                mejorRespuesta.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                
                // Actualizar la tabla final en la GUI
                gui.agregarMejorCotizacion(sc.getProducto().getNombre(), sc.getCantidad(), mejorCotizacion.getPrecio(), mejorCotizacion.getProveedor().getLocalName());
                gui.registrarMensaje(myAgent.getLocalName(), "Ganador para " + sc.getProducto().getNombre() + ": " + mejorCotizacion.getProveedor().getLocalName());
            } else {
                gui.registrarMensaje(myAgent.getLocalName(), "Ningún proveedor tiene " + sc.getProducto().getNombre());
            }
        }
    }
}