/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comercio;

import jade.core.AID;
import java.io.Serializable;

/**
 * Representa la propuesta económica de un Proveedor.
 * Se enviará dentro del mensaje PROPOSE como respuesta a una solicitud.
 */
public class Cotizacion implements Serializable {
    private SolicitudCotizacion solicitud;
    private AID proveedor;
    private double precio;
    private int tiempoEntrega;

    public Cotizacion( SolicitudCotizacion solicitud, AID proveedor, double precio, int tiempoEntrega) 
    {
        this.solicitud = solicitud;
        this.proveedor = proveedor;
        this.precio = precio;
        this.tiempoEntrega = tiempoEntrega;
    }

    public SolicitudCotizacion getSolicitud() { 
        return solicitud; 
    }
    public AID getProveedor() { 
        return proveedor; 
    }
    public double getPrecio() { 
        return precio; 
    }
    public int getTiempoEntrega() {
        return tiempoEntrega; 
    }
}
