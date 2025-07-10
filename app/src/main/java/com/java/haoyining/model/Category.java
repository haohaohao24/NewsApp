package com.java.haoyining.model;

import java.io.Serializable;
import java.util.Objects;

public class Category implements Serializable {
    public String name;
    public boolean isAdded;

    public Category(String name, boolean isAdded) {
        this.name = name;
        this.isAdded = isAdded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}