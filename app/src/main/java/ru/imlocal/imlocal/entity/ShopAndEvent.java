package ru.imlocal.imlocal.entity;

import java.util.List;

public class ShopAndEvent {
    private List<Shop> shops;
    private List<Action> actions;

    public ShopAndEvent(List<Shop> shops, List<Action> actions) {
        this.shops = shops;
        this.actions = actions;
    }

    public ShopAndEvent() {
    }

    @Override
    public String toString() {
        return "ShopAndEvent{" +
                "shops=" + shops +
                ", actions=" + actions +
                '}';
    }

    public List<Shop> getShops() {
        return shops;
    }

    public void setShops(List<Shop> shops) {
        this.shops = shops;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }
}
