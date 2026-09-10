package com.amcsoftware.sidebar.Entidades;

public class Agenda {
    private String idt;
    private String descripcion;
    private String fechat;
    private String prioridad;

    public Agenda(String id, String descripcion, String fecha, String prioridad) {
        this.idt          = id;
        this.descripcion = descripcion;
        this.fechat       = fecha;
        this.prioridad   = prioridad;
    }


    public String getIdt() {
        return idt;
    }

    public void setIdt(String idt) {
        this.idt = idt;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechat() {
        return fechat;
    }

    public void setFechat(String fechat) {
        this.fechat = fechat;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }
}
