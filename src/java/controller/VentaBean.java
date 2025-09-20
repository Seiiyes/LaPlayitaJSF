package controller;

import dao.ClienteDAO;
import dao.ProductoDAO;
import model.Cliente;
import model.DetalleVenta;
import model.Producto;
import model.Usuario;
import model.Venta;
import service.FacturaService;
import service.VentaService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import controller.Supplier;
import org.primefaces.util.SerializableSupplier;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.PrimeFaces;

@ManagedBean
@ViewScoped
public class VentaBean implements Serializable {

    // Claves para guardar el carrito en la sesión HTTP
    private static final String CART_PRODUCTS_KEY = "session.cart.products";
    private static final String CART_QUANTITIES_KEY = "session.cart.quantities";

    private List<Producto> productosDisponibles;
    private List<Cliente> clientes;
    private int idClienteSeleccionado;

    private ProductoDAO productoDAO;
    private ClienteDAO clienteDAO;
    private VentaService ventaService;
    private FacturaService facturaService; // Nuevo servicio de factura

    // Propiedades para la descarga de factura
    private StreamedContent file;
    private Venta ultimaVentaRegistrada;
    private Cliente clienteDeUltimaVenta;
    private List<DetalleVenta> detallesDeUltimaVenta;
    private boolean mostrarBotonDescarga;

    @PostConstruct
    public void init() {
        productoDAO = new ProductoDAO();
        clienteDAO = new ClienteDAO();
        ventaService = new VentaService();
        facturaService = new FacturaService(); // Inicializar servicio de factura
        mostrarBotonDescarga = false;

        try {
            productosDisponibles = productoDAO.findAll();
        } catch (SQLException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los productos.");
            productosDisponibles = new ArrayList<>();
        }
        clientes = clienteDAO.listarTodos();
    }

    // --- Métodos para manejar los mapas del carrito en la SESIÓN ---

    private Map<Integer, Producto> getCarritoProductos() {
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        Map<Integer, Producto> carrito = (Map<Integer, Producto>) sessionMap.get(CART_PRODUCTS_KEY);
        if (carrito == null) {
            carrito = new LinkedHashMap<>();
            sessionMap.put(CART_PRODUCTS_KEY, carrito);
        }
        return carrito;
    }

    public Map<Integer, Object> getCarritoCantidades() {
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        Map<Integer, Object> carrito = (Map<Integer, Object>) sessionMap.get(CART_QUANTITIES_KEY);
        if (carrito == null) {
            carrito = new LinkedHashMap<>();
            sessionMap.put(CART_QUANTITIES_KEY, carrito);
        }
        return carrito;
    }

    // --- Lógica de la Venta ---

    public void agregarProductoPorId(int idProducto) {
        Map<Integer, Producto> carritoProds = getCarritoProductos();
        Map<Integer, Object> carritoCant = getCarritoCantidades();

        Producto productoAAgregar = findProductoById(idProducto);
        if (productoAAgregar == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Producto no encontrado.");
            return;
        }

        int cantidadActual = getCantidadAsInt(carritoCant.get(idProducto));
        carritoCant.put(idProducto, cantidadActual + 1);
        carritoProds.putIfAbsent(idProducto, productoAAgregar);
    }

    public void quitarProducto(int idProducto) {
        getCarritoProductos().remove(idProducto);
        getCarritoCantidades().remove(idProducto);
    }

    public void onCantidadChange(int idProducto) {
        Object cantidadObj = getCarritoCantidades().get(idProducto);
        if (getCantidadAsInt(cantidadObj) <= 0) {
            quitarProducto(idProducto);
        }
    }

    public void registrarVenta(boolean generarFactura) {
        if (idClienteSeleccionado == 0) {
            addMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un cliente.");
            return;
        }
        if (getCarritoCantidades().isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Atención", "El carrito está vacío.");
            return;
        }

        Usuario vendedor = (Usuario) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("usuario");
        if (vendedor == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error de Sesión", "No se pudo identificar al vendedor.");
            return;
        }

        Venta venta = new Venta();
        venta.setIdCliente(idClienteSeleccionado);
        venta.setIdUsuario(vendedor.getIdUsuario());
        venta.setTotal(getTotalVenta());
        long now = System.currentTimeMillis();
        venta.setFechaVenta(new Date(now));
        venta.setHoraVenta(new Time(now));

        List<DetalleVenta> detalles = new ArrayList<>();
        for (Map.Entry<Integer, Object> entry : getCarritoCantidades().entrySet()) {
            Integer idProducto = entry.getKey();
            int cantidad = getCantidadAsInt(entry.getValue());
            Producto p = getCarritoProductos().get(idProducto);
            if (p != null && cantidad > 0) {
                BigDecimal subtotal = p.getPrecioUnitario().multiply(new BigDecimal(cantidad));
                detalles.add(new DetalleVenta(p.getIdProducto(), 0, cantidad, subtotal));
            }
        }

        try {
            ventaService.realizarVenta(venta, detalles);
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Venta registrada correctamente.");

            // Almacenar datos de la venta para la factura
            this.ultimaVentaRegistrada = venta;
            this.clienteDeUltimaVenta = findClienteById(idClienteSeleccionado);
            this.detallesDeUltimaVenta = detalles;

            if (generarFactura) {
                mostrarBotonDescarga = true;
                addMessage(FacesMessage.SEVERITY_INFO, "Facturación", "La factura está lista para descargar.");
                // Añadir el parámetro de callback para que el frontend sepa que debe descargar
                if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
                    PrimeFaces.current().ajax().addCallbackParam("facturaLista", true);
                }
            } else {
                mostrarBotonDescarga = false;
                limpiarFormulario(); // Limpiar si no se va a descargar factura
            }

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "Error Crítico", "No se pudo registrar la venta: " + e.getMessage());
            mostrarBotonDescarga = false; // Asegurar que el botón no se muestre en caso de error
        }
    }

    private void limpiarFormulario() {
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        sessionMap.remove(CART_PRODUCTS_KEY);
        sessionMap.remove(CART_QUANTITIES_KEY);
        idClienteSeleccionado = 0;
        mostrarBotonDescarga = false;
        ultimaVentaRegistrada = null;
        clienteDeUltimaVenta = null;
        detallesDeUltimaVenta = null;
        file = null; // Limpiar el StreamedContent
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
    
    private Producto findProductoById(int idProducto) {
        for (Producto p : productosDisponibles) {
            if (p.getIdProducto() == idProducto) {
                return p;
            }
        }
        return null;
    }

    private Cliente findClienteById(int idCliente) {
        for (Cliente c : clientes) {
            if (c.getIdCliente() == idCliente) {
                return c;
            }
        }
        return null;
    }

    private int getCantidadAsInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                if (((String) value).isEmpty()) return 0;
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    // --- Getters y Setters para la VISTA ---

    public List<Producto> getProductosDisponibles() {
        return productosDisponibles;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public int getIdClienteSeleccionado() {
        return idClienteSeleccionado;
    }

    public void setIdClienteSeleccionado(int idClienteSeleccionado) {
        this.idClienteSeleccionado = idClienteSeleccionado;
    }

    public BigDecimal getTotalVenta() {
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (Map.Entry<Integer, Object> entry : getCarritoCantidades().entrySet()) {
            Integer idProducto = entry.getKey();
            int cantidad = getCantidadAsInt(entry.getValue());
            Producto p = getCarritoProductos().get(idProducto);
            if (p != null && cantidad > 0) {
                calculatedTotal = calculatedTotal.add(p.getPrecioUnitario().multiply(new BigDecimal(cantidad)));
            }
        }
        return calculatedTotal;
    }

    public StreamedContent getFile() {
        if (mostrarBotonDescarga && ultimaVentaRegistrada != null && clienteDeUltimaVenta != null && detallesDeUltimaVenta != null) {
            try {
                // Llamada al nuevo método de iText
                byte[] pdfBytes = facturaService.generarFacturaConIText(
                    ultimaVentaRegistrada, 
                    detallesDeUltimaVenta, 
                    clienteDeUltimaVenta,
                    getCarritoProductos() // Pasando el mapa de productos del carrito
                );
                
                InputStream stream = new ByteArrayInputStream(pdfBytes);
                
                // Asignar un ID de venta si es una venta nueva que aún no lo tiene del DAO
                String idVentaStr = (ultimaVentaRegistrada.getIdVenta() > 0) ? String.valueOf(ultimaVentaRegistrada.getIdVenta()) : "nueva";

                return new DefaultStreamedContent(
                        stream,
                        "application/pdf",
                        "factura_" + idVentaStr + ".pdf"
                );
            } catch (Exception e) { // Captura una excepción más genérica
                addMessage(FacesMessage.SEVERITY_FATAL, "Error Factura", "No se pudo generar la factura: " + e.getMessage());
                e.printStackTrace(); // Imprimir el stack trace para depuración
                return null;
            } finally {
                // Limpiar el formulario después de intentar la descarga
                limpiarFormulario();
            }
        }
        return null;
    }

    public boolean isMostrarBotonDescarga() {
        return mostrarBotonDescarga;
    }

    public List<Producto> getCarritoAsList() {
        return new ArrayList<>(getCarritoProductos().values());
    }
}