//Recibe la respuesta del servidor.
package com.amcsoftware.sidebar.model;

import com.amcsoftware.sidebar.Entidades.Cliente;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ResponseModel {

    @SerializedName("success")
    private boolean success;

    @SerializedName("mensaje")
    private String mensaje;

    @SerializedName("cliente")
    private List<Cliente> cliente;

    public boolean isSuccess() {
        return success;
    }

    public String getMensaje() {
        return mensaje;
    }
    public List<Cliente> getCliente() {
        return cliente;
    }
}
