package model;

public enum EstadoReabastecimiento {
    PEDIDO("Pedido"),
    RECIBIDO("Recibido"),
    CANCELADO("Cancelado");

    private final String displayValue;

    private EstadoReabastecimiento(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static EstadoReabastecimiento fromDisplayValue(String displayValue) {
        for (EstadoReabastecimiento estado : EstadoReabastecimiento.values()) {
            if (estado.displayValue.equalsIgnoreCase(displayValue)) {
                return estado;
            }
        }
        // Devolver un valor predeterminado o lanzar una excepción si el valor no se encuentra
        return PEDIDO; // Opcional: valor por defecto
    }
}
