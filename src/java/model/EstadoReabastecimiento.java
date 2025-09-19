package model;

public enum EstadoReabastecimiento {
    RECIBIDO("Recibido"),
    PEDIDO("Pedido"),
    CANCELADO("Cancelado");

    private final String displayValue;

    EstadoReabastecimiento(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    // Método estático para obtener el enum a partir de su displayValue
    public static EstadoReabastecimiento fromDisplayValue(String displayValue) {
        for (EstadoReabastecimiento estado : EstadoReabastecimiento.values()) {
            if (estado.displayValue.equalsIgnoreCase(displayValue)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("No se encontró un EstadoReabastecimiento con el valor: " + displayValue);
    }
}