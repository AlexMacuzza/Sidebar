package com.amcsoftware.sidebar.Entidades;

public class Factura_Ventas {
    private String cliente;
    private String cobrador;
    private String cuotas;
    private String dircl;
    private String fecha;
    private String idfv;
    private String monto;
    private String observacion;
    private String plazo;
    private String saldo;
    private String vendedor;
    private String vmora;
    private String score;
    private String vcuotas;

    public String getScore() {return score;}
    public void setScore(String score) {this.score = score;}
    public String getVcuotas() {return vcuotas;}
    public void setVcuotas(String vcuotas) {this.vcuotas = vcuotas;}
    public String getDircl() { return dircl;}
    public void setDircl(String dircl) { this.dircl = dircl;}
    public String getVmora() { return vmora;}
    public void setVmora(String vmora) { this.vmora = vmora; }
    public String getCuotas() { return cuotas; }
    public void setCuotas(String cuotas) { this.cuotas = cuotas; }
    public String getPlazo() { return plazo;}
    public void setPlazo(String plazo) { this.plazo = plazo;}
    public String getCliente() {
        return cliente;
    }
    public void setCliente(String cliente) {
        this.cliente = cliente;
    }
    public String getIdfv() {
        return idfv;
    }
    public void setIdfv(String idfv) {
        this.idfv = idfv;
    }
    public String getObservacion() {
        return observacion;
    }
    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
    public String getSaldo() {
        return saldo;
    }
    public void setSaldo(String saldo) {
        this.saldo = saldo;
    }
    public String getMonto() {
        return monto;
    }
    public void setMonto(String monto) {
        this.monto = monto;
    }
    public String getCobrador() {
        return cobrador;
    }
    public void setCobrador(String cobrador) {
        this.cobrador = cobrador;
    }
    public String getVendedor() {
        return vendedor;
    }
    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }
    public String getFecha() {
        return fecha;
    }
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
