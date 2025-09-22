package converter;

import controller.ReabastecimientoBean;
import java.util.Optional;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import model.Producto;

@FacesConverter(value = "productoConverter", forClass = Producto.class)
public class ProductoConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty() || value.equals("null")) {
            return null;
        }

        // Obtenemos el bean directamente de la vista de JSF
        ReabastecimientoBean bean = context.getApplication().evaluateExpressionGet(context, "#{reabastecimientoBean}", ReabastecimientoBean.class);

        try {
            Integer idProducto = Integer.valueOf(value);

            // Buscamos el producto en la lista que ya está en memoria (muy eficiente)
            Optional<Producto> producto = bean.getListaProductos().stream()
                    .filter(p -> p.getIdProducto() != null && p.getIdProducto().equals(idProducto))
                    .findFirst();

            return producto.orElse(null);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return null; // Usar null en lugar de "" para que el itemValue="#{null}" funcione
        }
        if (value instanceof Producto) {
            Producto producto = (Producto) value;
            if (producto.getIdProducto() == null) {
                return null; // Un producto sin ID es considerado nulo para la selección
            }
            return String.valueOf(producto.getIdProducto());
        } else {
            return String.valueOf(value);
        }
    }
}
