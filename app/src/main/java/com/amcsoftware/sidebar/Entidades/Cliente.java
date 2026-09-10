package com.amcsoftware.sidebar.Entidades;

/* ajustar tamaño de la foto*/
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.Serializable;

public class Cliente implements Serializable {
    private Bitmap foto;
    private String cedula;
    private String celular;
    private String celularcode;
    private String ciudad;
    private String codeudor;
    private String correo;
    private String dato;
    private String direccion;
    private String estado;
    private String idcl;
    private String nombre;
    private String observacion;
    private String rfoto;
    private String score;


    public String getScore() { return score;}

    public void setScore(String score) { this.score = score;}

    public String getIdcl() {
        return idcl;
    }

    public void setIdcl(String idcl) {
        this.idcl = idcl;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getDato() {
        return dato;
    }

    public void setDato(String dato) {
        this.dato = dato;

        try {
            byte[] byteCode= Base64.decode(dato,Base64.DEFAULT);

            int alto=100;//alto en pixeles
            int ancho=150;//ancho en pixeles

            Bitmap foto= BitmapFactory.decodeByteArray(byteCode,0,byteCode.length);
            this.foto=Bitmap.createScaledBitmap(foto,alto,ancho,true);


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCodeudor() {
        return codeudor;
    }

    public void setCodeudor(String codeudor) {
        this.codeudor = codeudor;
    }

    public String getCelularcode() {
        return celularcode;
    }

    public void setCelularcode(String celularcode) {
        this.celularcode = celularcode;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Bitmap getFoto() {
        return foto;
    }

    public void setFoto(Bitmap foto) {
        this.foto = foto;
    }


    public String getRfoto() {
        return rfoto;
    }

    public void setRfoto(String rfoto) {
        this.rfoto = rfoto;
    }
}
