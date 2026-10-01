/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comercio;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShopperGUI extends JFrame implements RegistroMensajes {
    private ShopperInterface shopper;
    
    private DefaultTableModel carritoModel;
    private JTable carritoTable;
    private DefaultTableModel mejoresCotizacionesModel;
    
    private JTextArea txtShopper, txtProv1, txtProv2, txtProv3;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private int requestCounter = 1;

    public ShopperGUI() {
        setTitle("Shopper - Cotizador multiagente");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Finaliza la app al cerrar la ventana
        setLayout(new BorderLayout());
        
        // Colores base para la interfaz 
        Color colorFondo = new Color(240, 248, 255); // Azul muy claro
        Color colorBtnAgregar = new Color(46, 204, 113); // Verde
        Color colorBtnEliminar = new Color(231, 76, 60); // Rojo
        Color colorBtnSolicitar = new Color(52, 152, 219); // Azul
        
        getContentPane().setBackground(colorFondo);

        // PANEL IZQUIERDO: Carrito y Catálogo
        JPanel panelIzquierdo = new JPanel(new BorderLayout(0, 10));
        panelIzquierdo.setPreferredSize(new Dimension(500, 600)); 
        panelIzquierdo.setBackground(colorFondo);
        panelIzquierdo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Zona del Carrito
        JPanel panelCarrito = new JPanel(new BorderLayout());
        panelCarrito.setBackground(Color.WHITE);
        panelCarrito.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Carrito de cotización"));
        
        carritoModel = new DefaultTableModel(new Object[]{"Producto", "Cant."}, 0);
        carritoTable = new JTable(carritoModel);
        carritoTable.setRowHeight(25);
        panelCarrito.add(new JScrollPane(carritoTable), BorderLayout.CENTER);

        // Botones del carrito
        JPanel panelBotonesCarrito = new JPanel(new GridLayout(1, 3, 5, 0));
        panelBotonesCarrito.setBackground(Color.WHITE);
        panelBotonesCarrito.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JButton btnVaciar = configurarBoton("Vaciar", Color.DARK_GRAY, Color.WHITE);
        JButton btnEliminar = configurarBoton("Eliminar", colorBtnEliminar, Color.WHITE);
        JButton btnSolicitar = configurarBoton("Solicitar cotizaciones", colorBtnSolicitar, Color.WHITE);
        
        btnVaciar.addActionListener(e -> carritoModel.setRowCount(0));
        btnEliminar.addActionListener(e -> {
            int row = carritoTable.getSelectedRow();
            if (row >= 0) carritoModel.removeRow(row);
        });
        btnSolicitar.addActionListener(e -> enviarSolicitudes());

        panelBotonesCarrito.add(btnVaciar);
        panelBotonesCarrito.add(btnEliminar);
        panelBotonesCarrito.add(btnSolicitar);
        panelCarrito.add(panelBotonesCarrito, BorderLayout.SOUTH);

        // 2. Zona del Catálogo
        JPanel panelCatalogo = new JPanel(new GridLayout(2, 2, 10, 10));
        panelCatalogo.setBackground(colorFondo);
        panelCatalogo.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Catálogo de productos"));
        
        Producto[] productosMock = {
                new Producto("P1", "Libro Harry Potter", "Libro de la saga Harry Potter"),
                new Producto("P2", "Computadora", "Computadora de escritorio"),
                new Producto("P3", "Peluche", "Peluche suave de Pikachu"),
                new Producto("P4", "Lentes", "Lentes Versace")
        };
        // Rutas de las imágenes
        String[] rutasImagenes = {
                "img/harry.jpg", 
                "img/pc.png", 
                "img/peluche.png", 
                "img/lentes.png"
        };
        
        for (int i = 0; i < productosMock.length; i++) {
            Producto p = productosMock[i];
            
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            
            // Imagen del producto y título
            JLabel lblImagen = cargarIconoRedimensionado(rutasImagenes[i]);
            JLabel lblNombre = new JLabel(p.getNombre(), SwingConstants.CENTER);
            lblNombre.setFont(new Font("Arial", Font.BOLD, 12));
            
            JPanel panelSuperiorTarjeta = new JPanel(new BorderLayout());
            panelSuperiorTarjeta.setBackground(Color.WHITE);
            panelSuperiorTarjeta.add(lblImagen, BorderLayout.CENTER);
            panelSuperiorTarjeta.add(lblNombre, BorderLayout.SOUTH);
            
            JSpinner spnCant = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
            JButton btnAgregar = configurarBoton("Agregar", colorBtnAgregar, Color.WHITE);
            
            btnAgregar.addActionListener(e -> carritoModel.addRow(new Object[]{p, spnCant.getValue()}));
            
            JPanel bottomCard = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            bottomCard.setBackground(Color.WHITE);
            bottomCard.add(new JLabel("Cant:"));
            bottomCard.add(spnCant);
            
            JPanel panelInferiorTarjeta = new JPanel(new BorderLayout());
            panelInferiorTarjeta.setBackground(Color.WHITE);
            panelInferiorTarjeta.add(bottomCard, BorderLayout.CENTER);
            panelInferiorTarjeta.add(btnAgregar, BorderLayout.SOUTH);
            
            card.add(panelSuperiorTarjeta, BorderLayout.CENTER);
            card.add(panelInferiorTarjeta, BorderLayout.SOUTH);
            panelCatalogo.add(card);
        }

        panelIzquierdo.add(panelCarrito, BorderLayout.NORTH);
        panelIzquierdo.add(panelCatalogo, BorderLayout.CENTER);
        add(panelIzquierdo, BorderLayout.WEST);

        // PANEL DERECHO: Seguimiento y Resultados
        JPanel panelDerecho = new JPanel(new BorderLayout(0, 10));
        panelDerecho.setBackground(colorFondo);
        panelDerecho.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));

        JPanel panelSeguimiento = new JPanel(new GridLayout(1, 4, 5, 5));
        panelSeguimiento.setBackground(colorFondo);
        panelSeguimiento.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Seguimiento de agentes"));
        
        txtShopper = crearTextArea();
        txtProv1 = crearTextArea();
        txtProv2 = crearTextArea();
        txtProv3 = crearTextArea();
        
        panelSeguimiento.add(crearPanelConTitulo("Shopper", txtShopper));
        panelSeguimiento.add(crearPanelConTitulo("Proveedor 1", txtProv1));
        panelSeguimiento.add(crearPanelConTitulo("Proveedor 2", txtProv2));
        panelSeguimiento.add(crearPanelConTitulo("Proveedor 3", txtProv3));
        
        // Resultados
        JPanel panelResultados = new JPanel(new BorderLayout());
        panelResultados.setBackground(Color.WHITE);
        panelResultados.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Mejores cotizaciones"));
        
        mejoresCotizacionesModel = new DefaultTableModel(new Object[]{"Producto", "Cantidad", "Precio", "Proveedor"}, 0);
        JTable tablaResultados = new JTable(mejoresCotizacionesModel);
        tablaResultados.setRowHeight(25);
        tablaResultados.getTableHeader().setBackground(new Color(220, 230, 240));
        panelResultados.add(new JScrollPane(tablaResultados), BorderLayout.CENTER);

        panelDerecho.add(panelSeguimiento, BorderLayout.CENTER);
        panelDerecho.add(panelResultados, BorderLayout.SOUTH);
        add(panelDerecho, BorderLayout.CENTER);
    }

    // Método auxiliar para dar estilo a los botones
    private JButton configurarBoton(String texto, Color fondo, Color textoColor) {
        JButton btn = new JButton(texto);
        btn.setBackground(fondo);
        btn.setForeground(textoColor);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        return btn;
    }

    // Método auxiliar para cargar y escalar las imágenes
    private JLabel cargarIconoRedimensionado(String ruta) {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        try {
            // Si en el paso anterior usaste getClass().getResource, mantén esa estructura aquí
            ImageIcon icon = new ImageIcon(ruta);
            if (icon.getIconWidth() > 0) {
                // Escalar la imagen a 100x100 pixeles
                Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
            } else {
                label.setText("[Imagen no hallada]");
            }
        } catch (Exception e) {
            label.setText("[Sin Imagen]");
        }
        // Aumentar el espacio reservado para la imagen
        label.setPreferredSize(new Dimension(120, 120));
        return label;
    }

    public void setShopperInterface(ShopperInterface shopper) {
        this.shopper = shopper;
    }

    private void enviarSolicitudes() {
        if (shopper != null && carritoModel.getRowCount() > 0) {
            mejoresCotizacionesModel.setRowCount(0);
            List<SolicitudCotizacion> solicitudes = new ArrayList<>();
            for (int i = 0; i < carritoModel.getRowCount(); i++) {
                Producto p = (Producto) carritoModel.getValueAt(i, 0);
                int c = (int) carritoModel.getValueAt(i, 1);
                solicitudes.add(new SolicitudCotizacion("SC-00" + requestCounter++, p, c));
            }
            shopper.solicitarCotizaciones(solicitudes);
        }
    }

    private JTextArea crearTextArea() {
        JTextArea txt = new JTextArea();
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setFont(new Font("Monospaced", Font.PLAIN, 14));
        return txt;
    }

    private JPanel crearPanelConTitulo(String titulo, JTextArea txtArea) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setOpaque(true);
        lblTitulo.setBackground(new Color(220, 230, 240));
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 12));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        p.add(lblTitulo, BorderLayout.NORTH);
        p.add(new JScrollPane(txtArea), BorderLayout.CENTER);
        return p;
    }

    @Override
    public void registrarMensaje(String agente, String mensaje) {
        SwingUtilities.invokeLater(() -> {
            String time = "[" + sdf.format(new Date()) + "] ";
            String log = time + mensaje + "\n";
            if (agente.equals("Shopper")) txtShopper.append(log);
            else if (agente.equals("Proveedor1")) txtProv1.append(log);
            else if (agente.equals("Proveedor2")) txtProv2.append(log);
            else if (agente.equals("Proveedor3")) txtProv3.append(log);
        });
    }

    @Override
    public void agregarMejorCotizacion(String producto, int cantidad, double precio, String proveedor) {
        SwingUtilities.invokeLater(() -> {
            mejoresCotizacionesModel.addRow(new Object[]{producto, cantidad, String.format("$%.2f", precio), proveedor});
        });
    }
}