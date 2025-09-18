package controller;

import dao.ClienteDAO;
import dao.ProductoDAO;
import model.Cliente;
import model.DetalleVenta;
import model.Producto;
import model.Usuario;
import model.Venta;
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

@ManagedBean
@ViewScoped
public class VentaBean implements Serializable {

    // Claves para guardar el carrito en la sesión HTTP
    private static final String CART_PRODUCTS_KEY = "session.cart.products";
    private static final String CART_QUANTITIES_KEY = "session.cart.quantities";

    private List<Producto> productosDisponibles;
    private List<Cliente> clientes;
    private int idClienteSeleccionado;
    private BigDecimal totalVenta;

    private ProductoDAO productoDAO;
    private ClienteDAO clienteDAO;
    private VentaService ventaService;

    @PostConstruct
    public void init() {
        productoDAO = new ProductoDAO();
        clienteDAO = new ClienteDAO();
        ventaService = new VentaService();
        totalVenta = BigDecimal.ZERO;

        // Cargar datos que no cambian con cada acción
        try {
            productosDisponibles = productoDAO.findAll();
        } catch (SQLException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los productos.");
            productosDisponibles = new ArrayList<>();
        }
        clientes = clienteDAO.listarTodos();
        
        // Recalcular el total basado en el carrito actual en la sesión
        calcularTotal();
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

    public Map<Integer, Integer> getCarritoCantidades() {
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        Map<Integer, Integer> carrito = (Map<Integer, Integer>) sessionMap.get(CART_QUANTITIES_KEY);
        if (carrito == null) {
            carrito = new LinkedHashMap<>();
            sessionMap.put(CART_QUANTITIES_KEY, carrito);
        }
        return carrito;
    }

    // --- Lógica de la Venta ---

    public void agregarProductoPorId(int idProducto) {
        Map<Integer, Producto> carritoProds = getCarritoProductos();
        Map<Integer, Integer> carritoCant = getCarritoCantidades();

        Producto productoAAgregar = findProductoById(idProducto);
        if (productoAAgregar == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Producto no encontrado.");
            return;
        }

        int cantidadActual = carritoCant.getOrDefault(idProducto, 0);
        carritoCant.put(idProducto, cantidadActual + 1);
        carritoProds.putIfAbsent(idProducto, productoAAgregar);

        calcularTotal();
    }

    public void quitarProducto(int idProducto) {
        getCarritoProductos().remove(idProducto);
        getCarritoCantidades().remove(idProducto);
        calcularTotal();
    }

    public void onCantidadChange(int idProducto) {
        Integer cantidad = getCarritoCantidades().get(idProducto);
        if (cantidad != null && cantidad <= 0) {
            quitarProducto(idProducto);
        } else {
            calcularTotal();
        }
    }

    public void registrarVenta() {
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
        venta.setTotal(totalVenta);
        long now = System.currentTimeMillis();
        venta.setFechaVenta(new Date(now));
        venta.setHoraVenta(new Time(now));

        List<DetalleVenta> detalles = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : getCarritoCantidades().entrySet()) {
            Integer idProducto = entry.getKey();
            Integer cantidad = entry.getValue();
            Producto p = getCarritoProductos().get(idProducto);
            if (p != null && cantidad > 0) {
                BigDecimal subtotal = p.getPrecioUnitario().multiply(new BigDecimal(cantidad));
                detalles.add(new DetalleVenta(p.getIdProducto(), 0, cantidad, subtotal));
            }
        }

        try {
            ventaService.realizarVenta(venta, detalles);
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Venta registrada correctamente.");
            limpiarFormulario();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "Error Crítico", "No se pudo registrar la venta: " + e.getMessage());
        }
    }

    private void calcularTotal() {
        totalVenta = BigDecimal.ZERO;
        for (Map.Entry<Integer, Integer> entry : getCarritoCantidades().entrySet()) {
            Integer idProducto = entry.getKey();
            Integer cantidad = entry.getValue();
            Producto p = getCarritoProductos().get(idProducto);
            if (p != null) {
                totalVenta = totalVenta.add(p.getPrecioUnitario().multiply(new BigDecimal(cantidad)));
            }
        }
    }

    private void limpiarFormulario() {
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        sessionMap.remove(CART_PRODUCTS_KEY);
        sessionMap.remove(CART_QUANTITIES_KEY);
        idClienteSeleccionado = 0;
        totalVenta = BigDecimal.ZERO;
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
        return totalVenta;
    }

    public List<Producto> getCarritoAsList() {
        return new ArrayList<>(getCarritoProductos().values());
    }
}
