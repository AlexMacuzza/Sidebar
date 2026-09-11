package com.amcsoftware.sidebar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.amcsoftware.sidebar.Entidades.Pagos_Ventas;
import com.amcsoftware.sidebar.R;
import com.amcsoftware.sidebar.utils.AppUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PagosVentasAdapter extends RecyclerView.Adapter<PagosVentasAdapter.PagosVentasViewHolder> implements View.OnClickListener {

    ArrayList<Pagos_Ventas> listapagosventas;
    List<Pagos_Ventas> filteredData;//Buscar datos
    public Context context;
    private View.OnClickListener listener;


    public PagosVentasAdapter(ArrayList<Pagos_Ventas> listapagosventas) {
        this.listapagosventas = listapagosventas;
        filteredData = new ArrayList<>();
        filteredData.addAll(listapagosventas);
    }

    @NonNull
    @Override
    public PagosVentasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.pagos_list, parent, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        vista .setLayoutParams(layoutParams);

        vista.setOnClickListener(this);//Evento click

        return new PagosVentasViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull PagosVentasViewHolder holder, int position) {
        holder.txtidpago.setText(listapagosventas.get(position).getCodpago());
        holder.txtidfv.setText(listapagosventas.get(position).getIdfv());
        holder.txtcliente.setText(listapagosventas.get(position).getCliente());
        holder.txtcelular.setText(listapagosventas.get(position).getCelular());
        holder.txtfecha.setText(listapagosventas.get(position).getFecha());
        holder.txtfechac.setText(listapagosventas.get(position).getFechacobro());
        holder.txtvalor.setText(listapagosventas.get(position).getValor());
        holder.txtvcuota.setText(listapagosventas.get(position).getVcuota());
        holder.txtsaldo.setText(listapagosventas.get(position).getSaldo());
        holder.txtdesc.setText(listapagosventas.get(position).getRef());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filtrado(String txtbuscar){
        int longitud = txtbuscar.length();
        if (longitud==0){
            listapagosventas.clear();
            listapagosventas.addAll(filteredData);
        }else{
            List<Pagos_Ventas> colletion = listapagosventas.stream().
                    filter(i -> i.getCliente().toLowerCase().contains(txtbuscar.toLowerCase()))
                    .collect(Collectors.toList());
            listapagosventas.clear();
            listapagosventas.addAll(colletion);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listapagosventas.size();
    }

    public void setOnClickListener(View.OnClickListener listener){

        this.listener = listener;

    }

    //Método para acceder a los ítems por posición
    public Pagos_Ventas getItemAtPosition(int position) {
        if (position >= 0 && position < listapagosventas.size()) {
            return listapagosventas.get(position);
        }
        return null;
    }

    @Override
    public void onClick(View vista) {
        if (listener!=null){
            listener.onClick(vista);
        }

    }

    public static class PagosVentasViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public Context context;
        TextView txtidpago,txtidfv,txtfecha,txtcliente,txtcelular,txtdesc,
                txtvalor,txtvcuota,txtsaldo,txtfechac;
        ImageButton  btpagos, bteliminar;
        JSONObject   jsonObject = null;
        SweetAlertDialog dialogCargando;
        RequestQueue requestQueue;
        String perfil;

        public PagosVentasViewHolder(@NonNull View itemView) {
            super(itemView);
            context     =  itemView.getContext();
            txtidpago   = itemView.findViewById(R.id.txtidpago);
            txtidfv     = itemView.findViewById(R.id.txtidfv);
            txtfecha    = itemView.findViewById(R.id.txtfecha);
            txtfechac   = itemView.findViewById(R.id.txtfechac);
            txtcliente  = itemView.findViewById(R.id.txtcliente);
            txtcelular  = itemView.findViewById(R.id.txtcelular);
            txtdesc     = itemView.findViewById(R.id.txtdesc);
            txtvalor    = itemView.findViewById(R.id.txtvalor);
            txtvcuota   = itemView.findViewById(R.id.txtvcuota);
            txtsaldo    = itemView.findViewById(R.id.txtsaldo);
            btpagos     = itemView.findViewById(R.id.btpagos);
            bteliminar  = itemView.findViewById(R.id.bteliminar);
            requestQueue = Volley.newRequestQueue(context);//Respuesta de las peticiones metódo POST
            //Consultar el perfil de usuario
            SharedPreferences sp = itemView.getContext().getSharedPreferences("sesion",0);
            perfil = sp.getString("perfil", "General");
            //Mostrar el botón eliminar
            if (perfil.equals("Administrador")){
                bteliminar.setVisibility(View.VISIBLE);
            }

            bteliminar.setOnClickListener(v-> {
                final String idfv = String.valueOf(txtidfv.getText());
                final String idpago = String.valueOf(txtidpago.getText());
                AppUtils.alertConfirmar(context, "Softpymes",
                        "¿Está seguro de eliminar el pago N° " + idpago + "?", "Sí", "No",
                        () -> eliminarPagos(idfv, idpago, v));
            });
            btpagos.setOnClickListener(v->{
            });

        }

        private void eliminarPagos(String idfv, String idpago, View v) {
            String url = "https://www.wmcsoftware.net/apps/softpymes/eliminarPago.php";
            dialogCargando = AppUtils.mostrarCargando(context, "Eliminando...", "Por favor espera.");
            // Crear la solicitud POST
            StringRequest stringRequest =  new StringRequest(
                    Request.Method.POST,
                    url,
                    response -> {
                        AppUtils.cerrarCargando(dialogCargando);
                        String msj = "Pago eliminado.";
                        boolean ok = true;
                        try {
                            jsonObject = new JSONObject(response);
                            msj = jsonObject.optString("mensaje", msj);
                            ok = jsonObject.optBoolean("success", true);
                        } catch (JSONException e) {
                            ok = false;
                            msj = "No se pudo procesar la respuesta del servidor.";
                        }
                        if (ok) {
                            AppUtils.alertExito(context, "Éxito", msj);
                        } else {
                            AppUtils.alertError(context, "Atención", msj);
                        }
                        Navigation.findNavController(v).navigate(R.id.recargarConsultaP);//Refrescar el fragment
                    },
                    error -> {
                        AppUtils.cerrarCargando(dialogCargando);
                        AppUtils.alertError(context, "Error de red", "No se pudo eliminar el pago.");
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    // Enviar los parámetros al servidor
                    Map<String, String> params = new HashMap<>();
                    params.put("idfv", idfv);
                    params.put("codpago", idpago);
                    return params;
                }
            };
            // Agregar la solicitud a la cola
            requestQueue.add(stringRequest);

        }

        @Override
        public void onClick(View v) {

        }


    }
}
