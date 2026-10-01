/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comercio;

import java.io.Serializable;

/**
 * Clase que encapsula la solicitud de cotización de un producto y su cantidad.
 * Se enviará dentro del mensaje CFP los proveedores.
 */
public class SolicitudCotizacion implements Serializable {
    private String idSolicitud;
    private Producto producto;
    private int cantidad;

    public SolicitudCotizacion(String idSolicitud, Producto producto, int cantidad) {
        this.idSolicitud = idSolicitud;
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public String getIdSolicitud() { 
        return idSolicitud; 
    }
    public Producto getProducto() { 
        return producto; 
    }
    public int getCantidad() { 
        return cantidad; 
    }
}