package controller;

import model.ProductoVendido;
import service.VentaService;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("reporteBean")
@ViewScoped
public class ReporteBean implements Serializable {

    @Inject
    private VentaService ventaService;

    private List<ProductoVendido> bestSellers;

    private int limit = 10; // Default limit

    @PostConstruct
    public void init() {
        loadBestSellers();
    }

    public void loadBestSellers() {
        bestSellers = ventaService.getBestSellingProducts(limit);
    }

    public List<ProductoVendido> getBestSellers() {
        return bestSellers;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
