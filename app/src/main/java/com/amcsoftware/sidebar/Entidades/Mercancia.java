package com.amcsoftware.sidebar.Entidades;

public class Mercancia {
    private String idp;
    private String descripcion;
    private String referencia;
    private String precioc;
    private String preciov;
    private String cantidad;
    private String subtotal;
    private boolean isSelected;

    public String getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getIdp() {
        return idp;
    }

    public void setIdp(String idp) {
        this.idp = idp;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPrecioc() {
        return precioc;
    }

    public void setPrecioc(String precioc) {
        this.precioc = precioc;
    }

    public String getPreciov() {
        return preciov;
    }

    public void setPreciov(String preciov) {
        this.preciov = preciov;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

}
