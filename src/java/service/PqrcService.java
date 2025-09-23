package service;

import dao.PqrcDAO;
import model.Pqrc;

import java.sql.SQLException;
import java.util.List;

public class PqrcService {

    private PqrcDAO pqrcDAO;

    public PqrcService() {
        this.pqrcDAO = new PqrcDAO();
    }

    public void registrarPqrc(Pqrc pqrc) throws SQLException {
        // Lógica de negocio antes de registrar, si es necesaria.
        // Por ejemplo, validaciones complejas.
        pqrcDAO.registrar(pqrc);
    }

    public List<Pqrc> obtenerTodosPqrc() {
        return pqrcDAO.listarTodas();
    }

    public void actualizarEstadoPqrc(int idPqrc, String nuevoEstado) throws SQLException {
        // Lógica de negocio antes de actualizar, si es necesaria.
        pqrcDAO.actualizarEstado(idPqrc, nuevoEstado);
    }

    public void eliminarPqrc(int idPqrc) throws SQLException {
        pqrcDAO.eliminar(idPqrc);
    }
}

