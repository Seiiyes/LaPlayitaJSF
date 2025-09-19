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
import model.EstadoReabastecimiento; // Importar el enum
import model.Producto;
import model.Proveedor;
import model.Reabastecimiento;
import org.primefaces.event.RowEditEvent;
import service.ReabastecimientoService;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

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
    private List<EstadoReabastecimiento> estadosReabastecimiento;

    @PostConstruct
    public void init() {
        try {
            listaReabastecimientos = service.listar();
            listaProveedores = service.listarProveedores();
            listaProductos = service.listarProductos(); // This populates listaProductos
            estadosReabastecimiento = Arrays.asList(EstadoReabastecimiento.values()); // Initialize the list of enum values

            // --- DEBUGGING: Check if products are loaded ---
            System.out.println("DEBUG: listaProductos size: " + listaProductos.size());
            for (Producto p : listaProductos) {
                System.out.println("DEBUG: Producto ID: " + p.getIdProducto() + ", Nombre: " + p.getNombreProducto());
            }
            // --- END DEBUGGING ---

            seleccionado = new Reabastecimiento(); 
            seleccionado.setEstado(EstadoReabastecimiento.RECIBIDO); // Inicializar con el enum
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los datos iniciales."));
            e.printStackTrace();
        }
    }

    public void prepararNuevo() {
        seleccionado = new Reabastecimiento();
        seleccionado.setEstado(EstadoReabastecimiento.RECIBIDO); // Inicializar con el enum
    }

    public void prepararEdicion(Reabastecimiento reab) {
        try {
            this.seleccionado = service.obtenerCompleto(reab.getIdReabastecimiento());
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo cargar el detalle de la compra."));
            e.printStackTrace();
        }
    }

    public void agregarDetalle() {
        this.seleccionado.getDetalles().add(new DetalleReabastecimiento());
    }

    public void quitarDetalle(DetalleReabastecimiento detalle) {
        this.seleccionado.getDetalles().remove(detalle);
    }

    public void onRowEdit(RowEditEvent<DetalleReabastecimiento> event) {
        DetalleReabastecimiento det = event.getObject();
        
        // Actualizar el objeto Producto completo en el detalle (versión Java 7)
        Producto productoSeleccionado = null;
        for (Producto p : listaProductos) {
            if (p.getIdProducto() == det.getProducto().getIdProducto()) {
                productoSeleccionado = p;
                break;
            }
        }
        
        if (productoSeleccionado != null) {
            det.setProducto(productoSeleccionado);
        }

        FacesMessage msg = new FacesMessage("Fila Editada", "Producto: " + det.getProducto().getNombreProducto());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void guardarMaestroDetalle() {
        try {
            // Asegurarse de que el costoTotal calculado se establezca en el objeto antes de guardar
            seleccionado.setCostoTotal(seleccionado.getCostoTotal());
            service.guardarMaestroDetalle(seleccionado);
            listaReabastecimientos = service.listar(); // Recargar la lista
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Compra guardada correctamente."));
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage("formDialog", new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("formDialog", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al guardar la compra. " + e.getMessage()));
            e.printStackTrace();
        }
    }

    public void eliminar(Reabastecimiento item) {
        try {
            service.eliminar(item.getIdReabastecimiento());
            listaReabastecimientos.remove(item);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro eliminado."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el registro. " + e.getMessage()));
            e.printStackTrace();
        }
    }

    // --- New method for debugging state change ---
    public void onEstadoChange() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Estado cambiado a", seleccionado.getEstado().getDisplayValue()));
        System.out.println("DEBUG: Estado cambiado a: " + seleccionado.getEstado().getDisplayValue());
    }

    // --- Getters y Setters ---

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

    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public Reabastecimiento getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Reabastecimiento seleccionado) {
        this.seleccionado = seleccionado;
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

    public void handleFileUpload(FileUploadEvent event) {
        FacesMessage message = null;
        try {
            UploadedFile file = event.getFile();
            if (file != null) {
                // Validate selectedProveedorForUploadId
                if (selectedProveedorForUploadId == null || selectedProveedorForUploadId == 0) {
                    throw new IllegalArgumentException("Debe seleccionar un proveedor para la carga masiva.");
                }

                // Create a new Reabastecimiento object
                Reabastecimiento newReabastecimiento = new Reabastecimiento();
                newReabastecimiento.setFecha(new Date()); // Current date
                newReabastecimiento.setHora(new Time(System.currentTimeMillis())); // Current time
                newReabastecimiento.setEstado(EstadoReabastecimiento.RECIBIDO); // Default to RECIBIDO

                // Set the selected supplier
                Proveedor selectedProveedor = null;
                for (Proveedor p : listaProveedores) {
                    if (p.getIdProveedor() == selectedProveedorForUploadId) {
                        selectedProveedor = p;
                        break;
                    }
                }
                if (selectedProveedor == null) {
                    throw new IllegalArgumentException("Proveedor seleccionado no encontrado.");
                }
                newReabastecimiento.setProveedor(selectedProveedor);

                // Process the Excel file
                Workbook workbook = new XSSFWorkbook(file.getInputStream()); // For .xlsx files
                // For .xls files, use HSSFWorkbook
                // Workbook workbook = new HSSFWorkbook(file.getInputStream());

                Sheet sheet = workbook.getSheetAt(0); // Get the first sheet

                // Assuming the first row is the header
                boolean firstRow = true;
                for (Row row : sheet) {
                    if (firstRow) {
                        firstRow = false;
                        continue; // Skip header row
                    }

                    // Assuming columns: Producto (String), Cantidad (int), Costo Unitario (BigDecimal)
                    Cell productCell = row.getCell(0);
                    Cell quantityCell = row.getCell(1);
                    Cell unitCostCell = row.getCell(2);

                    if (productCell == null || quantityCell == null || unitCostCell == null) {
                        // Skip empty or incomplete rows
                        continue;
                    }

                    String productName = productCell.getStringCellValue();
                    int quantity = (int) quantityCell.getNumericCellValue();
                    BigDecimal unitCost = BigDecimal.valueOf(unitCostCell.getNumericCellValue());

                    // Find the product by name
                    Producto product = null;
                    for (Producto p : listaProductos) {
                        if (p.getNombreProducto().equalsIgnoreCase(productName)) {
                            product = p;
                            break;
                        }
                    }

                    if (product == null) {
                        // Handle case where product is not found
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "Producto '" + productName + "' no encontrado. Se omitirá esta fila."));
                        continue;
                    }

                    DetalleReabastecimiento detalle = new DetalleReabastecimiento();
                    detalle.setProducto(product);
                    detalle.setCantidad(quantity);
                    detalle.setCostoUnitario(unitCost);

                    newReabastecimiento.getDetalles().add(detalle);
                }

                // Set the calculated total cost before saving
                newReabastecimiento.setCostoTotal(newReabastecimiento.getCostoTotal()); 

                // Save the new replenishment
                service.guardarMaestroDetalle(newReabastecimiento);
                listaReabastecimientos = service.listar(); // Refresh the list

                message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", file.getFileName() + " cargado y procesado correctamente.");
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
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
}
