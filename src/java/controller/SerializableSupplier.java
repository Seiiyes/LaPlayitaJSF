package controller;

import java.io.Serializable;

public interface SerializableSupplier<T> extends Serializable {
    T get();
}
