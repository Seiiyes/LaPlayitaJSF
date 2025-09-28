package service;

import dao.CategoriaDAO;
import dao.ProveedorDAO;
import dao.ReabastecimientoDAO;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import model.Categoria;
import model.DetalleReabastecimiento;
import model.EstadoReabastecimiento;
import model.Producto;
import model.Proveedor;
import model.Reabastecimiento;

@ApplicationScoped
public class ReabastecimientoService implements java.io.Serializable {

    @Inject
    private transient ReabastecimientoDAO reabastecimientoDAO;
    @Inject
    private transient ProveedorDAO proveedorDAO;
    @Inject
    private transient CategoriaDAO categoriaDAO;
    
    // Inyectamos el servicio de productos para manejar el inventario
    @Inject
    private ProductoService productoService;

    public List<Reabastecimiento> listar() throws SQLException {
        return reabastecimientoDAO.findAllMasters();
    }

    public Reabastecimiento obtenerCompleto(int idReabastecimiento) throws SQLException {
        Reabastecimiento reab = reabastecimientoDAO.findMasterById(idReabastecimiento);
        if (reab != null) {
            List<DetalleReabastecimiento> detalles = reabastecimientoDAO.findDetailsByReabastecimientoId(idReabastecimiento);
            reab.setDetalles(detalles);
        }
        return reab;
    }

    public List<Proveedor> listarProveedores() throws SQLException {
        return proveedorDAO.findAll();
    }

    public List<Categoria> listarCategorias() throws SQLException {
        return categoriaDAO.findAll();
    }

    public void guardarMaestroDetalle(Reabastecimiento reab) throws SQLException, IllegalArgumentException {
        if (reab.getProveedor() == null || reab.getProveedor().getIdProveedor() == 0) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor.");
        }
        if (reab.getDetalles() == null || reab.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La compra debe tener al menos un producto.");
        }

        boolean esNuevo = reab.getIdReabastecimiento() == 0;
        Reabastecimiento estadoAnterior = null;
        if (!esNuevo) {
            estadoAnterior = obtenerCompleto(reab.getIdReabastecimiento());
        }

        // Guardar la compra (maestro y detalle)
        reabastecimientoDAO.saveMaestroDetalle(reab);

        // Lógica de inventario post-guardado
        // Si es una compra nueva que se marca como RECIBIDO
        if (esNuevo && reab.getEstado() == EstadoReabastecimiento.RECIBIDO) {
            for (DetalleReabastecimiento det : reab.getDetalles()) {
                String desc = "Entrada por nueva compra #" + reab.getIdReabastecimiento();
                productoService.registrarMovimientoInventario(det.getProducto().getIdProducto(), det.getCantidad(), "ENTRADA", desc);
            }
        }
        // Si se está editando una compra
        else if (!esNuevo) {
            // Caso 1: Se cancela una compra que estaba recibida -> Revertir stock (SALIDA)
            if (reab.getEstado() == EstadoReabastecimiento.CANCELADO && estadoAnterior.getEstado() == EstadoReabastecimiento.RECIBIDO) {
                for (DetalleReabastecimiento det : estadoAnterior.getDetalles()) {
                    String desc = "Salida por cancelación de compra #" + reab.getIdReabastecimiento();
                    productoService.registrarMovimientoInventario(det.getProducto().getIdProducto(), det.getCantidad(), "SALIDA", desc);
                }
            }
            // Caso 2: Se marca como RECIBIDO una compra que antes no lo estaba -> Sumar stock (ENTRADA)
            else if (reab.getEstado() == EstadoReabastecimiento.RECIBIDO && estadoAnterior.getEstado() != EstadoReabastecimiento.RECIBIDO) {
                for (DetalleReabastecimiento det : reab.getDetalles()) {
                    String desc = "Entrada por actualización de compra #" + reab.getIdReabastecimiento();
                    productoService.registrarMovimientoInventario(det.getProducto().getIdProducto(), det.getCantidad(), "ENTRADA", desc);
                }
            }
            // NOTA: La edición de detalles de una compra ya recibida es compleja y se ha omitido por simplicidad.
            // La implementación correcta requeriría calcular las diferencias de cantidades por producto.
        }
    }

    public void eliminar(int idReabastecimiento) throws SQLException {
        Reabastecimiento reab = obtenerCompleto(idReabastecimiento);
        if (reab != null && reab.getEstado() == EstadoReabastecimiento.RECIBIDO) {
            // Revertir el stock creando un movimiento de SALIDA por cada producto
            for (DetalleReabastecimiento det : reab.getDetalles()) {
                String desc = "Salida por eliminación de compra #" + idReabastecimiento;
                productoService.registrarMovimientoInventario(det.getProducto().getIdProducto(), det.getCantidad(), "SALIDA", desc);
            }
        }
        reabastecimientoDAO.delete(idReabastecimiento);
    }

    public void procesarCargaMasiva(InputStream inputStream, int idProveedor) throws SQLException, java.io.IOException {
        List<DetalleReabastecimiento> detalles = new java.util.ArrayList<>();
        
        try (org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(inputStream)) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Empezar en 1 para saltar la cabecera
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;

                org.apache.poi.ss.usermodel.Cell idCell = row.getCell(0);
                org.apache.poi.ss.usermodel.Cell cantidadCell = row.getCell(1);

                if (idCell == null || cantidadCell == null) {
                     throw new IllegalArgumentException("Error en fila " + (i + 1) + ": Las celdas de ID y Cantidad no pueden estar vacías.");
                }

                int idProducto = (int) idCell.getNumericCellValue();
                int cantidad = (int) cantidadCell.getNumericCellValue();

                if (cantidad <= 0) {
                    throw new IllegalArgumentException("La cantidad para el producto ID " + idProducto + " debe ser positiva.");
                }

                Producto producto = productoService.findById(idProducto);
                if (producto == null) {
                    throw new IllegalArgumentException("El producto con ID " + idProducto + " no fue encontrado.");
                }

                DetalleReabastecimiento detalle = new DetalleReabastecimiento();
                detalle.setProducto(producto);
                detalle.setCantidad(cantidad);
                detalle.setCostoUnitario(producto.getPrecioUnitario());
                detalles.add(detalle);
            }
        } catch (Exception e) {
            throw new java.io.IOException("Error al procesar el archivo Excel. Verifique que el formato sea correcto (ID Producto en Columna A, Cantidad en Columna B) y que los valores sean numéricos.", e);
        }


        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("El archivo no contiene productos válidos para procesar.");
        }

        Reabastecimiento reab = new Reabastecimiento();
        Proveedor prov = new Proveedor();
        prov.setIdProveedor(idProveedor);
        reab.setProveedor(prov);
        reab.setDetalles(detalles);
        reab.setFecha(new java.util.Date());
        reab.setEstado(EstadoReabastecimiento.RECIBIDO);
        reab.setObservaciones("Carga masiva desde archivo.");

        java.math.BigDecimal costoTotal = java.math.BigDecimal.ZERO;
        for (DetalleReabastecimiento det : detalles) {
            costoTotal = costoTotal.add(det.getSubtotal());
        }
        reab.setCostoTotal(costoTotal);

        guardarMaestroDetalle(reab);
    }
}