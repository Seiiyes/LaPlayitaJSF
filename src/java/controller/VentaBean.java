package controller;

import model.Cliente;
import model.DetalleVenta;
import model.Producto;
import model.Usuario;
import model.Venta;
import service.EmailService;
import service.FacturaService;
import service.ProductoService;
import service.VentaService;
import service.ClienteService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.PrimeFaces;

@Named("ventaBean")
@ViewScoped
public class VentaBean implements Serializable {

    private static final String CART_PRODUCTS_KEY = "session.cart.products";
    private static final String CART_QUANTITIES_KEY = "session.cart.quantities";

    @Inject
    private ProductoService productoService;
    @Inject
    private ClienteService clienteService;
    @Inject
    private VentaService ventaService;
    @Inject
    private FacturaService facturaService;
    @Inject
    private EmailService emailService;

    private List<Producto> productosDisponibles;
    private List<Cliente> clientes;
    private int idClienteSeleccionado;

    private StreamedContent file;
    private Venta ultimaVentaRegistrada;
    private Cliente clienteDeUltimaVenta;
    private List<DetalleVenta> detallesDeUltimaVenta;
    private boolean mostrarBotonDescarga;

    @PostConstruct
    public void init() {
        mostrarBotonDescarga = false;
        refrescarProductos();
        try {
            clientes = clienteService.listarTodos();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los clientes.");
            clientes = new ArrayList<>();
        }
    }

    private void refrescarProductos() {
        try {
            productosDisponibles = productoService.obtenerTodosConStock();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los productos.");
            productosDisponibles = new ArrayList<>();
        }
    }

    // --- Métodos para manejar el carrito en la SESIÓN ---

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
        
        // Validar stock antes de agregar al carrito
        if (productoAAgregar.getStockActual() < (cantidadActual + 1)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Stock Insuficiente", "No hay más stock para: " + productoAAgregar.getNombreProducto());
            return;
        }

        carritoCant.put(idProducto, cantidadActual + 1);
        carritoProds.putIfAbsent(idProducto, productoAAgregar);
    }

    public void quitarProducto(int idProducto) {
        getCarritoProductos().remove(idProducto);
        getCarritoCantidades().remove(idProducto);
    }

    public void onCantidadChange(int idProducto) {
        Object cantidadObj = getCarritoCantidades().get(idProducto);
        int cantidadDeseada = getCantidadAsInt(cantidadObj);
        Producto producto = getCarritoProductos().get(idProducto);

        if (cantidadDeseada <= 0) {
            quitarProducto(idProducto);
            return;
        }

        if (producto != null && producto.getStockActual() < cantidadDeseada) {
            // Si la cantidad excede el stock, se ajusta al máximo disponible
            getCarritoCantidades().put(idProducto, producto.getStockActual());
            addMessage(FacesMessage.SEVERITY_WARN, "Stock Máximo", "La cantidad para '" + producto.getNombreProducto() + "' se ajustó al stock disponible: " + producto.getStockActual());
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

        // 1. Validar stock ANTES de intentar registrar la venta
        for (Map.Entry<Integer, Object> entry : getCarritoCantidades().entrySet()) {
            Producto p = getCarritoProductos().get(entry.getKey());
            int cantidadPedida = getCantidadAsInt(entry.getValue());
            if (p.getStockActual() < cantidadPedida) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Stock Insuficiente", "No hay suficiente stock para: " + p.getNombreProducto() + ". Disponible: " + p.getStockActual());
                return; // Detener la venta
            }
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
            Producto p = getCarritoProductos().get(entry.getKey());
            int cantidad = getCantidadAsInt(entry.getValue());
            if (p != null && cantidad > 0) {
                BigDecimal subtotal = p.getPrecioUnitario().multiply(new BigDecimal(cantidad));
                detalles.add(new DetalleVenta(p.getIdProducto(), 0, cantidad, subtotal));
            }
        }

        try {
            // 2. Realizar la venta (insertar en DB)
            ventaService.realizarVenta(venta, detalles);

            // 3. Descontar el stock (registrar movimientos de SALIDA)
            for (DetalleVenta detalle : detalles) {
                String desc = "Salida por Venta #" + venta.getIdVenta();
                productoService.registrarMovimientoInventario(detalle.getIdProducto(), detalle.getCantidad(), "SALIDA", desc);
            }

            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Venta registrada correctamente.");

            this.ultimaVentaRegistrada = venta;
            this.clienteDeUltimaVenta = findClienteById(idClienteSeleccionado);
            this.detallesDeUltimaVenta = detalles;

            // 4. Refrescar la lista de productos para mostrar el nuevo stock
            refrescarProductos();

            // 5. Lógica de facturación y correo (sin cambios)
            if (clienteDeUltimaVenta != null && clienteDeUltimaVenta.getCorreo() != null && !clienteDeUltimaVenta.getCorreo().isEmpty()) {
                try {
                    byte[] pdfBytes = facturaService.generarFacturaConIText(ultimaVentaRegistrada, detallesDeUltimaVenta, clienteDeUltimaVenta, getCarritoProductos());
                    emailService.enviarFactura(clienteDeUltimaVenta, ultimaVentaRegistrada, pdfBytes);
                    addMessage(FacesMessage.SEVERITY_INFO, "Correo Enviado", "Factura enviada a: " + clienteDeUltimaVenta.getCorreo());
                } catch (Exception emailEx) {
                    addMessage(FacesMessage.SEVERITY_WARN, "Error de Correo", "La venta se registró, pero no se pudo enviar la factura.");
                    emailEx.printStackTrace();
                }
            }

            if (generarFactura) {
                mostrarBotonDescarga = true;
                PrimeFaces.current().ajax().addCallbackParam("facturaLista", true);
            } else {
                limpiarFormulario();
            }

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "Error Crítico", "No se pudo registrar la venta: " + e.getMessage());
            e.printStackTrace();
            mostrarBotonDescarga = false;
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
        file = null;
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
    
    private Producto findProductoById(int idProducto) {
        return productosDisponibles.stream().filter(p -> p.getIdProducto() == idProducto).findFirst().orElse(null);
    }

    private Cliente findClienteById(int idCliente) {
        return clientes.stream().filter(c -> c.getIdCliente() == idCliente).findFirst().orElse(null);
    }

    private int getCantidadAsInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
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
            Producto p = getCarritoProductos().get(entry.getKey());
            int cantidad = getCantidadAsInt(entry.getValue());
            if (p != null && cantidad > 0) {
                calculatedTotal = calculatedTotal.add(p.getPrecioUnitario().multiply(new BigDecimal(cantidad)));
            }
        }
        return calculatedTotal;
    }

    public StreamedContent getFile() {
        if (mostrarBotonDescarga && ultimaVentaRegistrada != null) {
            try {
                byte[] pdfBytes = facturaService.generarFacturaConIText(ultimaVentaRegistrada, detallesDeUltimaVenta, clienteDeUltimaVenta, getCarritoProductos());
                InputStream stream = new ByteArrayInputStream(pdfBytes);
                String idVentaStr = String.valueOf(ultimaVentaRegistrada.getIdVenta());
                return DefaultStreamedContent.builder()
                        .name("factura_" + idVentaStr + ".pdf")
                        .contentType("application/pdf")
                        .stream(() -> stream)
                        .build();
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_FATAL, "Error Factura", "No se pudo generar la factura: " + e.getMessage());
                e.printStackTrace();
                return null;
            } finally {
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
