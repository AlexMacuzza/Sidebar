package com.amcsoftware.sidebar.api;

import com.amcsoftware.sidebar.model.ResponseModel;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @FormUrlEncoded
    @POST("guardarClienteFoto.php")
    Call<ResponseModel> guardarCliente(
            @Field("id") String id,
            @Field("nombre") String nombre,
            @Field("direccion") String direccion,
            @Field("celular") String celular,
            @Field("codeudor") String codeudor,
            @Field("celularcode") String celularcode,
            @Field("observacion") String observacion,
            @Field("foto") String foto
    );

    @FormUrlEncoded
    @POST("actualizarClienteFoto.php")
    Call<ResponseModel> actualizarCliente(
            @Field("idc") String idc,         // ID único (Primary Key) de la tabla
            @Field("id") String id,           // Documento de identidad
            @Field("nombre") String nombre,
            @Field("dir") String direccion,
            @Field("celular") String celular,
            @Field("codeudor") String codeudor,
            @Field("celularcode") String celularcode,
            @Field("obs") String observacion,
            @Field("foto") String foto,        // Nueva foto en Base64
            @Field("bandera") String bandera  // true si se envía foto nueva, false si no
    );

    @FormUrlEncoded
    @POST("eliminarClienteFoto.php")
    Call<ResponseModel> eliminarCliente(
            @Field("idc") String idc     // ID único (Primary Key) de la tabla
    );

    @GET("consultarCliente.php")
    Call<ResponseModel> getCliente(@Query("id") String id);
}


