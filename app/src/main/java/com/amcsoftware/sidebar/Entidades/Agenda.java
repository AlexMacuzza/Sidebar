package com.amcsoftware.sidebar.Entidades;

public class Agenda {
    private String idt;
    private String descripcion;
    private String fechat;
    private String prioridad;
    private String hora;          // HH:mm, hora del recordatorio (alarma)
    private String repetir;       // "Ninguna", "Diario", "Semanal", "Mensual" o "Personalizado"
    private String intervalodias; // Solo aplica cuando repetir = "Personalizado"

    public Agenda(String id, String descripcion, String fecha, String prioridad) {
        this(id, descripcion, fecha, prioridad, "", "Ninguna", "");
    }

    public Agenda(String id, String descripcion, String fecha, String prioridad,
                  String hora, String repetir, String intervalodias) {
        this.idt           = id;
        this.descripcion   = descripcion;
        this.fechat        = fecha;
        this.prioridad     = prioridad;
        this.hora          = hora;
        this.repetir       = repetir;
        this.intervalodias = intervalodias;
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

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getRepetir() {
        return repetir;
    }

    public void setRepetir(String repetir) {
        this.repetir = repetir;
    }

    public String getIntervalodias() {
        return intervalodias;
    }

    public void setIntervalodias(String intervalodias) {
        this.intervalodias = intervalodias;
    }
}
