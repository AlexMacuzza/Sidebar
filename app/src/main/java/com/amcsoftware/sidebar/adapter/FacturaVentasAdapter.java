package com.amcsoftware.sidebar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.icu.util.Calendar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.amcsoftware.sidebar.Entidades.Factura_Ventas;
import com.amcsoftware.sidebar.R;
import com.amcsoftware.sidebar.utils.AppUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class FacturaVentasAdapter extends RecyclerView.Adapter<FacturaVentasAdapter.FacturaVentasViewHolder> implements View.OnClickListener {

    ArrayList<Factura_Ventas> listafacturaventas;
    List<Factura_Ventas> filteredData;//Buscar datos
    public Context context;
    private View.OnClickListener listener;
    private String filtroVendedor = "", filtroCliente = "";


    public FacturaVentasAdapter(ArrayList<Factura_Ventas> listafacturaventas, Context context) {
        this.listafacturaventas = listafacturaventas;
        this.context = context;
        filteredData = new ArrayList<>();
        filteredData.addAll(listafacturaventas);
    }

    @NonNull
    @Override
    public FacturaVentasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.ventas_list, parent, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        vista.setLayoutParams(layoutParams);

        vista.setOnClickListener(this);//Evento click

        return new FacturaVentasViewHolder(vista, parentContext);
    }

    @Override
    public void onBindViewHolder(@NonNull FacturaVentasViewHolder holder, int position) {
        Factura_Ventas facturaActual = listafacturaventas.get(position);
        holder.txtidfv.setText(facturaActual.getIdfv());
        holder.txtcliente.setText(facturaActual.getCliente());
        holder.txtdir.setText(facturaActual.getDircl());
        holder.txtfecha.setText(facturaActual.getFecha());
        holder.txtvendedor.setText(facturaActual.getVendedor());
        holder.txtcobrador.setText(facturaActual.getCobrador());
        holder.txtdesc.setText(facturaActual.getObservacion());
        holder.txtmonto.setText(facturaActual.getMonto());
        holder.txtmora.setText(facturaActual.getVmora());
        holder.txtsaldo.setText(facturaActual.getSaldo());
        holder.txtplazo.setText(facturaActual.getPlazo());
        holder.txtscore.setText(facturaActual.getScore());
        holder.txtncuotas.setText(facturaActual.getCuotas());
        holder.txtvcuotas.setText(facturaActual.getVcuotas());
        // *** Lógica para colorear la fila ***
        boolean fvencida = false;
        String fechaplazo = facturaActual.getPlazo(); // Asumiendo que es un String "YYYY-MM-DD"
        // Obtener la fecha actual
        Calendar calendarioActual = Calendar.getInstance();
        Date fechaActualDate      = calendarioActual.getTime();
        Date fechaVence;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            fechaVence = sdf.parse(fechaplazo);
            assert fechaVence != null;
            if (fechaVence.before(fechaActualDate)) {
                fvencida = true;
            }
        } catch (ParseException e) {
            Toast.makeText(context,"Error al procesar la fecha",Toast.LENGTH_SHORT).show();
        }

        if (fvencida){
            holder.txtidfv.setBackgroundColor(context.getResources().getColor(R.color.vencido, null));
        }else {
            holder.txtidfv.setBackgroundColor(context.getResources().getColor(R.color.normal, null));
        }
    }


    public Factura_Ventas getItemAtPosition(int position) {
        if (position >= 0 && position < listafacturaventas.size()){
            return listafacturaventas.get(position);
        }
        return null;
    }


    public void filtrarPorVendedor(String txtbuscar){
        filtroVendedor = txtbuscar;
        aplicarFiltros();
    }

    public void filtrarPorCliente(String txtbuscar){
        filtroCliente = txtbuscar;
        aplicarFiltros();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void aplicarFiltros(){
        List<Factura_Ventas> coleccion = filteredData.stream()
                .filter(i -> i.getVendedor().toLowerCase().contains(filtroVendedor.toLowerCase())
                        && i.getCliente().toLowerCase().contains(filtroCliente.toLowerCase()))
                .collect(Collectors.toList());
        listafacturaventas.clear();
        listafacturaventas.addAll(coleccion);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listafacturaventas.size();
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

    public static class FacturaVentasViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public Context context;
        TextView txtidfv, txtfecha, txtvendedor, txtcliente, txtdir, txtcobrador, txtdesc, txtmonto,
                txtmora, txtsaldo, txtplazo, txtscore, txtncuotas, txtvcuotas;
        ImageButton  bteliminar;
        JSONObject   jsonObject = null;
        SweetAlertDialog pDialog;
        RequestQueue requestQueue;

        public FacturaVentasViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context =  context;
            txtidfv      = itemView.findViewById(R.id.txtidfv);
            txtfecha    = itemView.findViewById(R.id.txtfecha);
            txtvendedor = itemView.findViewById(R.id.txtvendedor);
            txtcobrador = itemView.findViewById(R.id.txtcobrador);
            txtcliente  = itemView.findViewById(R.id.txtcliente);
            txtdir      = itemView.findViewById(R.id.txtdircl);
            txtdesc     = itemView.findViewById(R.id.txtdesc);
            txtmonto    = itemView.findViewById(R.id.txtmonto);
            txtmora     = itemView.findViewById(R.id.txtmora);
            txtsaldo    = itemView.findViewById(R.id.txtsaldo);
            txtplazo    = itemView.findViewById(R.id.txtplazo);
            txtscore    = itemView.findViewById(R.id.txtscore);
            txtncuotas  = itemView.findViewById(R.id.txtncuotas);
            txtvcuotas  = itemView.findViewById(R.id.txtvcuotas);
            bteliminar  = itemView.findViewById(R.id.bteliminar);
            requestQueue = Volley.newRequestQueue(context);//Respuesta de las peticiones metódo POST

            bteliminar.setOnClickListener(v->{
                final String idfv =  String.valueOf(txtidfv.getText());
                AppUtils.alertConfirmar(context,
                        "Confirmar eliminación",
                        "¿Está seguro de eliminar la venta N° " + idfv + "?",
                        "Sí, eliminar",
                        "Cancelar",
                        () -> eliminarDatos(idfv, v));
            });

        }

        private void eliminarDatos(String idfv,View v) {
            String url = "https://www.wmcsoftware.net/apps/softpymes/eliminarVenta.php";
            pDialog = AppUtils.mostrarCargando(context, "Eliminando...", "Por favor espera.");
            // Crear la solicitud POST
            StringRequest stringRequest =  new StringRequest(
                    Request.Method.POST,
                    url,
                    response -> {
                        AppUtils.cerrarCargando(pDialog);
                        // Manejar la respuesta del servidor
                        try {
                            jsonObject = new JSONObject(response);
                        } catch (JSONException e) {
                            AppUtils.alertError(context, "Atención", "Error al eliminar!");
                            return;
                        }
                        if (jsonObject.optBoolean("success")) {
                            // Navegar (refrescar el fragment) solo después de que el usuario
                            // confirme el mensaje de éxito
                            AppUtils.alertExito(context, "Éxito", jsonObject.optString("mensaje"),
                                    () -> Navigation.findNavController(v).navigate(R.id.recargarConsultaV));
                        } else {
                            AppUtils.alertError(context, "Atención", jsonObject.optString("mensaje"));
                        }
                    },
                    error -> {
                        // Manejar errores
                        AppUtils.cerrarCargando(pDialog);
                        AppUtils.alertError(context, "Atención", error.toString());
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    // Enviar los parámetros al servidor
                    Map<String, String> params = new HashMap<>();
                    params.put("idfv", idfv);
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