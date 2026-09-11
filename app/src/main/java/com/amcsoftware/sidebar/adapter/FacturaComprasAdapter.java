package com.amcsoftware.sidebar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.amcsoftware.sidebar.Entidades.Factura_Compras;
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

public class FacturaComprasAdapter extends RecyclerView.Adapter<FacturaComprasAdapter.FacturaComprasViewHolder> implements View.OnClickListener {

    ArrayList<Factura_Compras> listafacturacompras;
    List<Factura_Compras> filteredData;//Buscar datos
    public Context context;
    private View.OnClickListener listener;


    public FacturaComprasAdapter(ArrayList<Factura_Compras> listafacturacompras) {
        this.listafacturacompras = listafacturacompras;
        filteredData = new ArrayList<>();
        filteredData.addAll(listafacturacompras);
    }

    @NonNull
    @Override
    public FacturaComprasAdapter.FacturaComprasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.compras_list, parent, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        vista.setLayoutParams(layoutParams);

        vista.setOnClickListener(this);//Evento click

        return new FacturaComprasAdapter.FacturaComprasViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull FacturaComprasAdapter.FacturaComprasViewHolder holder, int position) {
        holder.txtidfc.setText(listafacturacompras.get(position).getIdfc());
        holder.txtproveedor.setText(listafacturacompras.get(position).getProveedor());
        holder.txtfecha.setText(listafacturacompras.get(position).getFecha());
        holder.txtdesc.setText(listafacturacompras.get(position).getObservacion());
        holder.txtmonto.setText(listafacturacompras.get(position).getMonto());
        holder.txtsaldo.setText(listafacturacompras.get(position).getSaldo());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filtrado(String txtbuscar){
        int longitud = txtbuscar.length();
        if (longitud==0){
            listafacturacompras.clear();
            listafacturacompras.addAll(filteredData);
        }else{
            List<Factura_Compras> colletion = listafacturacompras.stream().
                    filter(i -> i.getProveedor().toLowerCase().contains(txtbuscar.toLowerCase()))
                    .collect(Collectors.toList());
            listafacturacompras.clear();
            listafacturacompras.addAll(colletion);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listafacturacompras.size();
    }

    public void setOnClickListener(View.OnClickListener listener){

        this.listener = listener;

    }

    @Override
    public void onClick(View vista) {
        if (listener!=null){
            listener.onClick(vista);
        }

    }

    public static class FacturaComprasViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public Context context;
        TextView txtidfc,txtfecha,txtproveedor,txtcobrador,txtdesc,txtmonto,txtsaldo;
        ImageButton bteliminar;
        JSONObject jsonObject = null;
        SweetAlertDialog dialogCargando;
        RequestQueue requestQueue;

        public FacturaComprasViewHolder(@NonNull View itemView) {
            super(itemView);
            context =  itemView.getContext();
            txtidfc     = itemView.findViewById(R.id.txtidfc);
            txtfecha    = itemView.findViewById(R.id.txtfecha);
            txtproveedor  = itemView.findViewById(R.id.txtproveedor);
            txtdesc     = itemView.findViewById(R.id.txtdesc);
            txtmonto    = itemView.findViewById(R.id.txtmonto);
            txtsaldo    = itemView.findViewById(R.id.txtsaldo);
            bteliminar  = itemView.findViewById(R.id.bteliminar);
            requestQueue = Volley.newRequestQueue(context);//Respuesta de las peticiones metódo POST

            bteliminar.setOnClickListener(v-> {
                final String idfc = String.valueOf(txtidfc.getText());
                AppUtils.alertConfirmar(context, "Softpymes",
                        "¿Está seguro de eliminar la compra N° " + idfc + "?", "Sí", "No",
                        () -> eliminarDatos(idfc, v));
            });

        }

        private void eliminarDatos(String idfc,View v) {
            String url = "https://www.wmcsoftware.net/apps/softpymes/eliminarCompra.php";
            dialogCargando = AppUtils.mostrarCargando(context, "Eliminando...", "Por favor espera.");
            // Crear la solicitud POST
            StringRequest stringRequest =  new StringRequest(
                    Request.Method.POST,
                    url,
                    response -> {
                        AppUtils.cerrarCargando(dialogCargando);
                        String msj = "Compra eliminada.";
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
                        Navigation.findNavController(v).navigate(R.id.recargarConsultaC);//Refrescar el fragment
                    },
                    error -> {
                        AppUtils.cerrarCargando(dialogCargando);
                        AppUtils.alertError(context, "Error de red", "No se pudo eliminar la compra.");
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    // Enviar los parámetros al servidor
                    Map<String, String> params = new HashMap<>();
                    params.put("idfc", idfc);
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
