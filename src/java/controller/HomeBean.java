package controller;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 * Backing bean para gestionar la navegación desde el panel de inicio de los usuarios.
 */
@Named("homeBean")
@RequestScoped
public class HomeBean implements Serializable {

    // --- Vendedor (Rol 2) ---
    public String irRegistrarVenta() {
        return "/ventas/registrar?faces-redirect=true";
    }

    public String irGestionPedidos() {
        return "/pedidos/gestion?faces-redirect=true";
    }

    public String irCrearPqrc() {
        return "/pqrc/crear?faces-redirect=true";
    }

    // --- Practicante (Rol 3) ---
    public String irReporteCompras() {
        return "/compras/reportes?faces-redirect=true";
    }

    public String irHistorialVentas() {
        return "/ventas/historial?faces-redirect=true";
    }
}