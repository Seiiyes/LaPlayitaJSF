package controller;

import java.io.Serializable;
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
import service.ReabastecimientoService;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Named("reabastecimientoBean")
@ViewScoped
public class ReabastecimientoBean implements Serializable {

    private Integer selectedProveedorForUploadId;

    @Inject
    private ReabastecimientoService service;

    private List<Reabastecimiento> listaReabastecimientos;
    private List<Reabastecimiento> listaFiltrada;
    private List<Proveedor> listaProveedores;
    private List<Producto> listaProductos;

    private Reabastecimiento seleccionado;
    private Producto nuevoProducto;
    private List<EstadoReabastecimiento> estadosReabastecimiento;

    @PostConstruct
    public void init() {
        try {
            listaReabastecimientos = service.listar();
            listaProveedores = service.listarProveedores();
            refrescarProductos();
            estadosReabastecimiento = Arrays.asList(EstadoReabastecimiento.values());
            nuevoProducto = new Producto();
            // Inicializar un objeto 'seleccionado' vacío para evitar NullPointerException al inicio
            seleccionado = new Reabastecimiento();
            seleccionado.setDetalles(new ArrayList<>());
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los datos iniciales."));
            e.printStackTrace();
        }
    }

    public void refrescarProductos() {
        try {
            listaProductos = service.listarProductos();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo recargar la lista de productos."));
        }
    }

    public void prepararNuevo() {
        seleccionado = new Reabastecimiento();
        seleccionado.setDetalles(new ArrayList<>());
    }

    public void prepararNuevoProducto() {
        nuevoProducto = new Producto();
        nuevoProducto.setPrecioUnitario(BigDecimal.ZERO);
        nuevoProducto.setCantidadStock(0);
    }

    public void guardarNuevoProducto() {
        try {
            service.registrarProducto(nuevoProducto);
            refrescarProductos();
            PrimeFaces.current().executeScript("PF('dialogProducto').hide()");
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                            "Producto '" + nuevoProducto.getNombreProducto() + "' guardado."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("formProducto:productoMessages",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "No se pudo guardar el producto. " + e.getMessage()));
            e.printStackTrace();
        }
    }

    public void prepararEdicion(Reabastecimiento reab) {
        try {
            this.seleccionado = service.obtenerCompleto(reab.getIdReabastecimiento());
            if (this.seleccionado.getDetalles() == null) {
                this.seleccionado.setDetalles(new ArrayList<>());
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo cargar el detalle de la compra."));
            e.printStackTrace();
        }
    }

    public void agregarDetalle() {
        if (seleccionado.getDetalles() == null) {
            seleccionado.setDetalles(new ArrayList<>());
        }
        DetalleReabastecimiento nuevoDetalle = new DetalleReabastecimiento();
        nuevoDetalle.setProducto(null); // Correcto: empezar con null para el <f:selectItem ... itemValue="#{null}" />
        nuevoDetalle.setCantidad(1);
        nuevoDetalle.setCostoUnitario(BigDecimal.ZERO);
        this.seleccionado.getDetalles().add(nuevoDetalle);
        recalcularTotal();
    }

    public void quitarDetalle(DetalleReabastecimiento detalle) {
        if (seleccionado != null && seleccionado.getDetalles() != null) {
            this.seleccionado.getDetalles().remove(detalle);
            recalcularTotal();
        }
    }
    
    public void onProductoChange(DetalleReabastecimiento detalle) {
        if (detalle != null && detalle.getProducto() != null && detalle.getProducto().getPrecioUnitario() != null) {
            // El converter ya nos dio el objeto Producto completo.
            // Simplemente copiamos su precio al costo del detalle.
            detalle.setCostoUnitario(detalle.getProducto().getPrecioUnitario());
        } else {
            // Si el producto se des-selecciona (vuelve a null), reseteamos el costo.
            detalle.setCostoUnitario(BigDecimal.ZERO);
        }
        recalcularTotal();
    }

    public void recalcularTotal() {
        if (seleccionado != null && seleccionado.getDetalles() != null) {
            BigDecimal total = BigDecimal.ZERO;
            for (DetalleReabastecimiento det : seleccionado.getDetalles()) {
                total = total.add(det.getSubtotal()); // Usamos el método getSubtotal() del modelo
            }
            seleccionado.setCostoTotal(total);
        }
    }

    public void guardarMaestroDetalle() {
        try {
            if (seleccionado.getDetalles() == null || seleccionado.getDetalles().isEmpty()) {
                throw new IllegalArgumentException("Debe agregar al menos un detalle antes de guardar.");
            }
            // El total ya está calculado y actualizado, así que solo guardamos.
            service.guardarMaestroDetalle(seleccionado);
            listaReabastecimientos = service.listar();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Compra guardada correctamente."));
            PrimeFaces.current().ajax().update(":formPrincipal:tablaMaestra");
            PrimeFaces.current().executeScript("PF('dialogGestion').hide()");
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage("formDialog:dialogMessages",
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("formDialog:dialogMessages",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Ocurrió un error al guardar la compra. " + e.getMessage()));
            e.printStackTrace();
        }
    }

    public void eliminar(Reabastecimiento item) {
        try {
            service.eliminar(item.getIdReabastecimiento());
            listaReabastecimientos.remove(item);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro eliminado."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el registro. " + e.getMessage()));
            e.printStackTrace();
        }
    }

    public void onEstadoChange() {
        if (seleccionado != null && seleccionado.getEstado() != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Estado cambiado a", seleccionado.getEstado().getDisplayValue()));
        }
    }

    // --- Getters y Setters (Completos) ---
    public List<Reabastecimiento> getListaReabastecimientos() {
        return listaReabastecimientos;
    }

    public void setListaReabastecimientos(List<Reabastecimiento> listaReabastecimientos) {
        this.listaReabastecimientos = listaReabastecimientos;
    }

    public List<Reabastecimiento> getListaFiltrada() {
        return listaFiltrada;
    }

    public void setListaFiltrada(List<Reabastecimiento> listaFiltrada) {
        this.listaFiltrada = listaFiltrada;
    }

    public List<Proveedor> getListaProveedores() {
        return listaProveedores;
    }

    public void setListaProveedores(List<Proveedor> listaProveedores) {
        this.listaProveedores = listaProveedores;
    }

    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public void setListaProductos(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    public Reabastecimiento getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Reabastecimiento seleccionado) {
        this.seleccionado = seleccionado;
    }

    public Producto getNuevoProducto() {
        return nuevoProducto;
    }

    public void setNuevoProducto(Producto nuevoProducto) {
        this.nuevoProducto = nuevoProducto;
    }

    public Integer getSelectedProveedorForUploadId() {
        return selectedProveedorForUploadId;
    }

    public void setSelectedProveedorForUploadId(Integer selectedProveedorForUploadId) {
        this.selectedProveedorForUploadId = selectedProveedorForUploadId;
    }

    public List<EstadoReabastecimiento> getEstadosReabastecimiento() {
        return estadosReabastecimiento;
    }

    public void setEstadosReabastecimiento(List<EstadoReabastecimiento> estadosReabastecimiento) {
        this.estadosReabastecimiento = estadosReabastecimiento;
    }

    // --- File Upload ---
    public void handleFileUpload(FileUploadEvent event) {
        FacesMessage message = null;
        try {
            UploadedFile file = event.getFile();
            if (file != null) {
                if (selectedProveedorForUploadId == null || selectedProveedorForUploadId == 0) {
                    throw new IllegalArgumentException("Debe seleccionar un proveedor para la carga masiva.");
                }

                Reabastecimiento newReabastecimiento = new Reabastecimiento();
                newReabastecimiento.setFecha(new Date());
                newReabastecimiento.setHora(new Time(System.currentTimeMillis()));
                newReabastecimiento.setEstado(EstadoReabastecimiento.RECIBIDO);
                newReabastecimiento.setDetalles(new ArrayList<>());

                Proveedor selectedProveedor = listaProveedores.stream()
                        .filter(p -> p.getIdProveedor() != null && p.getIdProveedor().equals(selectedProveedorForUploadId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Proveedor seleccionado no encontrado."));

                newReabastecimiento.setProveedor(selectedProveedor);

                Workbook workbook = new XSSFWorkbook(file.getInputStream());
                Sheet sheet = workbook.getSheetAt(0);

                boolean firstRow = true;
                for (Row row : sheet) {
                    if (firstRow) {
                        firstRow = false;
                        continue;
                    }

                    Cell productCell = row.getCell(0);
                    Cell quantityCell = row.getCell(1);
                    Cell unitCostCell = row.getCell(2);

                    if (productCell == null || quantityCell == null || unitCostCell == null) {
                        continue;
                    }

                    String productName = productCell.getStringCellValue();
                    int quantity = (int) quantityCell.getNumericCellValue();
                    BigDecimal unitCost = BigDecimal.valueOf(unitCostCell.getNumericCellValue());

                    Producto product = listaProductos.stream()
                            .filter(p -> p.getNombreProducto() != null && p.getNombreProducto().equalsIgnoreCase(productName))
                            .findFirst()
                            .orElse(null);

                    if (product == null) {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                                        "Producto '" + productName + "' no encontrado. Se omitirá esta fila."));
                        continue;
                    }

                    DetalleReabastecimiento detalle = new DetalleReabastecimiento();
                    detalle.setProducto(product);
                    detalle.setCantidad(quantity);
                    detalle.setCostoUnitario(unitCost);

                    newReabastecimiento.getDetalles().add(detalle);
                }

                BigDecimal total = newReabastecimiento.getDetalles().stream()
                        .map(d -> d.getSubtotal())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                newReabastecimiento.setCostoTotal(total);

                service.guardarMaestroDetalle(newReabastecimiento);
                listaReabastecimientos = service.listar();

                message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                        file.getFileName() + " cargado y procesado correctamente.");
                PrimeFaces.current().executeScript("PF('uploadDialogWV').hide()");
            }
        } catch (IllegalArgumentException e) {
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", e.getMessage());
        } catch (IOException e) {
            message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error de lectura del archivo: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al procesar el archivo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (message != null) {
                FacesContext.getCurrentInstance().addMessage("formUpload:uploadMessages", message);
            }
        }
    }
}