package model;

import java.io.Serializable;

public class Rol implements Serializable {
    private int idRol;
    private String descripcionRol; 

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

    public String getDescripcionRol() { return descripcionRol; }
    public void setDescripcionRol(String descripcionRol) { this.descripcionRol = descripcionRol; }

    @Override
    public String toString() {
        return descripcionRol;
    }
}
