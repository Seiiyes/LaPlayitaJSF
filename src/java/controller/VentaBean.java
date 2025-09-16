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
import java.util.AbstractMap;
import java.util.Map;
import org.primefaces.PrimeFaces; // Added import

@ManagedBean
@ViewScoped
public class VentaBean implements Serializable {

    private List<Producto> productosDisponibles;
    private List<Cliente> clientes;
    private Map<Integer, Integer> carritoCantidades; // idProducto -> cantidad
    private Map<Integer, Producto> carritoProductos; // idProducto -> Producto
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
        carritoCantidades = new LinkedHashMap<>();
        carritoProductos = new LinkedHashMap<>();
        totalVenta = BigDecimal.ZERO;

        try {
            productosDisponibles = productoDAO.findAll();
        } catch (SQLException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los productos.");
            productosDisponibles = new ArrayList<>();
        }

        clientes = clienteDAO.listarTodos();
    }

    public void agregarProductoPorId(int idProducto) {
        Producto productoAAgregar = null;
        for (Producto p : productosDisponibles) {
            if (p.getIdProducto() == idProducto) {
                productoAAgregar = p;
                break;
            }
        }

        if (productoAAgregar != null) {
            int cantidadActual = carritoCantidades.getOrDefault(idProducto, 0);
            carritoCantidades.put(idProducto, cantidadActual + 1);
            carritoProductos.put(idProducto, productoAAgregar); // Store the product object

            calcularTotal();
            PrimeFaces.current().ajax().update("formVenta:panelCarrito", "formVenta:growl");
        } else {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Producto no encontrado.");
            PrimeFaces.current().ajax().update("formVenta:growl");
        }
    }

    public void quitarProducto(Producto producto) {
        int idProducto = producto.getIdProducto();
        carritoCantidades.remove(idProducto);
        carritoProductos.remove(idProducto);
        calcularTotal();
        PrimeFaces.current().ajax().update("formVenta:panelCarrito", "formVenta:growl");
    }

    public void actualizarCantidad(Producto producto) {
        int idProducto = producto.getIdProducto();
        Integer cantidad = carritoCantidades.get(idProducto); // Get the potentially updated quantity

        if (cantidad != null && cantidad <= 0) {
            quitarProducto(producto);
        } else {
            calcularTotal();
        }
        PrimeFaces.current().ajax().update("formVenta:panelCarrito", "formVenta:growl");
    }

    public void registrarVenta() {
        if (idClienteSeleccionado == 0) {
            addMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un cliente.");
            PrimeFaces.current().ajax().update("formVenta:growl");
            return;
        }
        if (carritoCantidades.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Atención", "El carrito está vacío.");
            PrimeFaces.current().ajax().update("formVenta:growl");
            return;
        }

        Usuario vendedor = (Usuario) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("usuario");
        if (vendedor == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error de Sesión", "No se pudo identificar al vendedor. Por favor, inicie sesión de nuevo.");
            PrimeFaces.current().ajax().update("formVenta:growl");
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
        for (Map.Entry<Integer, Integer> entry : carritoCantidades.entrySet()) {
            Integer idProducto = entry.getKey();
            Integer cantidad = entry.getValue();
            Producto p = carritoProductos.get(idProducto); // Get the product object
            if (p != null) {
                BigDecimal subtotal = p.getPrecioUnitario().multiply(new BigDecimal(cantidad));
                detalles.add(new DetalleVenta(p.getIdProducto(), 0, cantidad, subtotal)); // idVenta se setea en el DAO
            }
        }

        try {
            ventaService.realizarVenta(venta, detalles);
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Venta registrada correctamente.");
            limpiarFormulario();
            PrimeFaces.current().ajax().update("formVenta", "formVenta:growl");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "Error Crítico", "No se pudo registrar la venta: " + e.getMessage());
            PrimeFaces.current().ajax().update("formVenta:growl");
        }
    }

    private void calcularTotal() {
        totalVenta = BigDecimal.ZERO;
        for (Map.Entry<Integer, Integer> entry : carritoCantidades.entrySet()) {
            Integer idProducto = entry.getKey();
            Integer cantidad = entry.getValue();
            Producto p = carritoProductos.get(idProducto);
            if (p != null) {
                totalVenta = totalVenta.add(p.getPrecioUnitario().multiply(new BigDecimal(cantidad)));
            }
        }
    }

    private void limpiarFormulario() {
        carritoCantidades.clear();
        carritoProductos.clear();
        idClienteSeleccionado = 0;
        totalVenta = BigDecimal.ZERO;
        // Recargar productos por si el stock cambió
        try {
            productosDisponibles = productoDAO.findAll();
        } catch (SQLException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron recargar los productos.");
        }
        PrimeFaces.current().ajax().update("formVenta"); // Update entire form after clearing
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters y Setters

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

    public List<Map.Entry<Producto, Integer>> getCarritoAsList() {
        List<Map.Entry<Producto, Integer>> list = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : carritoCantidades.entrySet()) {
            Integer idProducto = entry.getKey();
            Integer cantidad = entry.getValue();
            Producto p = carritoProductos.get(idProducto);
            if (p != null) {
                list.add(new AbstractMap.SimpleEntry<>(p, cantidad));
            }
        }
        return list;
    }
}
