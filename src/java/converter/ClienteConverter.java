package converter;

import controller.PedidoBean;
import model.Cliente;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.Optional;

@FacesConverter(value = "clienteConverter", forClass = Cliente.class)
public class ClienteConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty() || value.equals("null")) {
            return null;
        }

        PedidoBean bean = context.getApplication().evaluateExpressionGet(context, "#{pedidoBean}", PedidoBean.class);

        try {
            Integer idCliente = Integer.valueOf(value);
            Optional<Cliente> cliente = bean.getClientes().stream()
                    .filter(c -> c.getIdCliente() == idCliente)
                    .findFirst();
            return cliente.orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Cliente) {
            Cliente cliente = (Cliente) value;
            return String.valueOf(cliente.getIdCliente());
        } else {
            return String.valueOf(value);
        }
    }
}
