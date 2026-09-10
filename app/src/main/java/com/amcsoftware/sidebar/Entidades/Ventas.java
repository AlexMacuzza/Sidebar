package com.amcsoftware.sidebar.Entidades;

public class Ventas {
    private String idp;
    private String desc;
    private String preciov;
    private String cantidad;
    private String subtotal;

    public Ventas(String idp,String desc, String preciov, String cantidad, String subtotal){
        this.idp       = idp;
        this.desc      = desc;
        this.preciov   = preciov;
        this.cantidad  = cantidad;
        this.subtotal  = subtotal;
    }

    public String getIdp() {
        return idp;
    }

    public void setIdp(String idp) {
        this.idp = idp;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public String getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }
}
