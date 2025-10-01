package controller;

import model.MovimientoInventario;
import model.Producto;
import service.ProductoService;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("inventarioBean")
@ViewScoped
public class InventarioBean implements Serializable {

    @Inject
    private ProductoService productoService;

    private List<Producto> productos;

    @PostConstruct
    public void init() {
        productos = productoService.obtenerTodosConStock();
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public List<MovimientoInventario> getMovimientosPorProducto(int idProducto) {
        return productoService.obtenerMovimientosPorProducto(idProducto);
    }
}