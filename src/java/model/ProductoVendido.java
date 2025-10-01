package model;

public class ProductoVendido {

    private String nombreProducto;
    private int totalVendido;

    public ProductoVendido(String nombreProducto, int totalVendido) {
        this.nombreProducto = nombreProducto;
        this.totalVendido = totalVendido;
    }

    // Getters and setters
    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getTotalVendido() {
        return totalVendido;
    }

    public void setTotalVendido(int totalVendido) {
        this.totalVendido = totalVendido;
    }
}
