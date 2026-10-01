/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comercio;

/**
 * Interfaz implementada por la GUI (ShopperGUI).
 * Permite que Shopper y los Proveedores actualicen sus áreas de texto y la tabla
 * de mejores cotizaciones en tiempo real de forma local.
 */
public interface RegistroMensajes {
    void registrarMensaje(
            String agente, 
            String mensaje
    );
    void agregarMejorCotizacion(
            String producto, 
            int cantidad, 
            double precio, 
            String proveedor);
}