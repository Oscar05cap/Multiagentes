/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comercio;

import java.util.List;

/**
 * Interfaz O2A (Object-to-Agent) que expone los métodos públicos del agente Shopper.
 * Permite que la GUI invoque la funcionalidad de cotización directamente.
 */
public interface ShopperInterface {
    void solicitarCotizaciones(List<SolicitudCotizacion> solicitudes);
}