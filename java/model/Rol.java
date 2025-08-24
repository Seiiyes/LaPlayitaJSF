package model;

public class Rol {
    private int id;
    private String descripcion; // ADMIN, VENDEDOR, PRACTICANTE
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
