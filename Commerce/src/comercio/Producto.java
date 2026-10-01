/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comercio;

import java.io.Serializable;

/**
 * Representa un artículo del catálogo del sistema.
 * Implementa Serializable para poder ser enviado en los mensajes ACL.
 */
public class Producto implements Serializable {
    private String id;
    private String nombre;
    private String descripcion;

    public Producto(String id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }

    @Override
    public String toString() { return nombre; }
}