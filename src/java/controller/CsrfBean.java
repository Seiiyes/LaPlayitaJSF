package controller;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

@Named("csrfBean")
@SessionScoped
public class CsrfBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String CSRF_TOKEN_SESSION_ATTR_NAME = "CSRF_TOKEN";

    public String getToken() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        if (session != null) {
            return (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTR_NAME);
        }
        return null;
    }

    // Getter para el nombre del parámetro, útil para el atributo 'name' en h:inputHidden
    public String getParamName() {
        return filter.CsrfFilter.CSRF_TOKEN_REQUEST_PARAM_NAME;
    }
}
