package ru.imlocal.imlocal.entity;

public class Category {
    private String name;
    private int dayOfWeek;
    private boolean isSelected;

    public Category(String name, int dayOfWeek) {
        this.name = name;
        this.dayOfWeek = dayOfWeek;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
