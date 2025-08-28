package model;

import java.io.Serializable;

public class Rol implements Serializable {
    private int idRol;
    private String descripcion; // ADMIN, VENDEDOR, PRACTICANTE

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
