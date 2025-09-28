package controller;

import model.Categoria;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import model.DetalleReabastecimiento;
import model.EstadoReabastecimiento;
import model.Producto;
import model.Proveedor;
import model.Reabastecimiento;
import org.primefaces.event.FileUploadEvent;
import service.ProductoService;
import service.ReabastecimientoService;

@Named("reabastecimientoBean")
@ViewScoped
public class ReabastecimientoBean implements Serializable {

    @Inject
    private ReabastecimientoService reabastecimientoService;

    @Inject
    private ProductoService productoService;

    private List<Reabastecimiento> listaReabastecimientos;
    private List<Reabastecimiento> listaFiltrada; 
    private List<Proveedor> listaProveedores;
    private List<Producto> listaProductos;
    private List<Categoria> listaCategorias;

    private Reabastecimiento seleccionado;
    private Producto nuevoProducto;
    private int stockInicialNuevoProducto;
    private List<EstadoReabastecimiento> estadosReabastecimiento;
    private int selectedProveedorForUploadId;

    @PostConstruct
    public void init() {
        try {
            listaReabastecimientos = reabastecimientoService.listar();
            listaProveedores = reabastecimientoService.listarProveedores();
            listaCategorias = reabastecimientoService.listarCategorias();
            refrescarProductos();
            estadosReabastecimiento = Arrays.asList(EstadoReabastecimiento.values());
            prepararNuevo();
            nuevoProducto = new Producto(); 
        } catch (Exception e) {
            String errorMessage = "Error al cargar datos iniciales: " + e.getMessage();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", errorMessage));
            e.printStackTrace();
        }
    }

    public void refrescarProductos() {
        try {
            listaProductos = productoService.obtenerTodosConStock();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo recargar la lista de productos."));
        }
    }

    public void refrescarProveedores() {
        try {
            listaProveedores = reabastecimientoService.listarProveedores();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo recargar la lista de proveedores."));
        }
    }

    public void prepararNuevo() {
        seleccionado = new Reabastecimiento();
        seleccionado.setDetalles(new java.util.ArrayList<>());
    }

    public void prepararEdicion(Reabastecimiento reab) {
        this.seleccionado = reab;
    }

    public void prepararNuevoProducto() {
        nuevoProducto = new Producto();
        nuevoProducto.setPrecioUnitario(BigDecimal.ZERO);
        this.stockInicialNuevoProducto = 0;
    }

    public void guardarNuevoProducto() {
        try {
            productoService.crearProducto(nuevoProducto, stockInicialNuevoProducto);
            refrescarProductos();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                            "Producto '" + nuevoProducto.getNombreProducto() + "' guardado."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("formProducto:productoMessages",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            e.printStackTrace();
        }
    }

    public void guardarMaestroDetalle() {
        try {
            if (seleccionado.getDetalles() == null || seleccionado.getDetalles().isEmpty()) {
                throw new IllegalArgumentException("Debe agregar al menos un detalle antes de guardar.");
            }
            reabastecimientoService.guardarMaestroDetalle(seleccionado);
            listaReabastecimientos = reabastecimientoService.listar();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Compra guardada correctamente."));
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage("formDialog:dialogMessages",
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("formDialog:dialogMessages",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Ocurrió un error al guardar la compra: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    public void eliminar(Reabastecimiento reab) {
        try {
            reabastecimientoService.eliminar(reab.getIdReabastecimiento());
            listaReabastecimientos.remove(reab);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Compra eliminada."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al eliminar la compra."));
            e.printStackTrace();
        }
    }

    public void agregarDetalle() {
        if (seleccionado.getDetalles() == null) {
            seleccionado.setDetalles(new java.util.ArrayList<>());
        }
        seleccionado.getDetalles().add(new DetalleReabastecimiento());
        recalcularTotal();
    }

    public void quitarDetalle(DetalleReabastecimiento detalle) {
        if (seleccionado != null && seleccionado.getDetalles() != null) {
            seleccionado.getDetalles().remove(detalle);
            recalcularTotal();
        }
    }

    public void onProductoChange(DetalleReabastecimiento detalle) {
        if (detalle != null && detalle.getProducto() != null 
                && detalle.getProducto().getPrecioUnitario() != null) {
            detalle.setCostoUnitario(detalle.getProducto().getPrecioUnitario());
        } else {
            detalle.setCostoUnitario(BigDecimal.ZERO);
        }
        recalcularTotal();
    }

    public void recalcularTotal() {
        if (seleccionado != null && seleccionado.getDetalles() != null) {
            BigDecimal total = BigDecimal.ZERO;
            for (DetalleReabastecimiento det : seleccionado.getDetalles()) {
                if (det != null && det.getSubtotal() != null) {
                    total = total.add(det.getSubtotal());
                }
            }
            seleccionado.setCostoTotal(total);
        }
    }

    public void handleFileUpload(FileUploadEvent event) {
        try {
            if (selectedProveedorForUploadId == 0) {
                FacesContext.getCurrentInstance().addMessage("formUpload:uploadMessages",
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "Debe seleccionar un proveedor."));
                return;
            }

            reabastecimientoService.procesarCargaMasiva(event.getFile().getInputStream(), selectedProveedorForUploadId);

            listaReabastecimientos = reabastecimientoService.listar(); 
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Archivo '" + event.getFile().getFileName() + "' cargado y procesado."));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("formUpload:uploadMessages",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al procesar el archivo: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    // --- Getters y Setters ---
    public List<Reabastecimiento> getListaReabastecimientos() { return listaReabastecimientos; }
    public void setListaReabastecimientos(List<Reabastecimiento> listaReabastecimientos) { this.listaReabastecimientos = listaReabastecimientos; }

    public List<Reabastecimiento> getListaFiltrada() { return listaFiltrada; }
    public void setListaFiltrada(List<Reabastecimiento> listaFiltrada) { this.listaFiltrada = listaFiltrada; }

    public List<Proveedor> getListaProveedores() { return listaProveedores; }
    public void setListaProveedores(List<Proveedor> listaProveedores) { this.listaProveedores = listaProveedores; }

    public List<Producto> getListaProductos() { return listaProductos; }
    public void setListaProductos(List<Producto> listaProductos) { this.listaProductos = listaProductos; }

    public List<Categoria> getListaCategorias() { return listaCategorias; }
    public void setListaCategorias(List<Categoria> listaCategorias) { this.listaCategorias = listaCategorias; }

    public Reabastecimiento getSeleccionado() { return seleccionado; }
    public void setSeleccionado(Reabastecimiento seleccionado) { this.seleccionado = seleccionado; }

    public Producto getNuevoProducto() { return nuevoProducto; }
    public void setNuevoProducto(Producto nuevoProducto) { this.nuevoProducto = nuevoProducto; }

    public int getStockInicialNuevoProducto() { return stockInicialNuevoProducto; }
    public void setStockInicialNuevoProducto(int stockInicialNuevoProducto) { this.stockInicialNuevoProducto = stockInicialNuevoProducto; }

    public List<EstadoReabastecimiento> getEstadosReabastecimiento() { return estadosReabastecimiento; }

    public int getSelectedProveedorForUploadId() { return selectedProveedorForUploadId; }
    public void setSelectedProveedorForUploadId(int selectedProveedorForUploadId) { this.selectedProveedorForUploadId = selectedProveedorForUploadId; }
}
