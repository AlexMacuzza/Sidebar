package com.amcsoftware.sidebar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.amcsoftware.sidebar.Entidades.Factura_Compras;
import com.amcsoftware.sidebar.R;
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
        // Declarar la ProgressBar
        ProgressBar progressBar;
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
            progressBar = itemView.findViewById(R.id.progressBar);
            requestQueue = Volley.newRequestQueue(context);//Respuesta de las peticiones metódo POST

            bteliminar.setOnClickListener(v->{
                final String idfc =  String.valueOf(txtidfc.getText());
                AlertDialog.Builder builder = new AlertDialog.Builder(context);//Alert de confirmación
                builder.setMessage("¿Está seguro de eliminar la compra N° "+idfc+"?").setTitle("Softpymes");

                builder.setPositiveButton("Si", (dialog, which) -> eliminarDatos(idfc,v));

                builder.setNegativeButton("No", (dialog, which) ->
                        Toast.makeText(context,
                                "Eliminación cancelada",
                                Toast.LENGTH_SHORT).show());

                AlertDialog dialog = builder.create();
                dialog.show();//Mostrar el Alert

            });

        }

        private void eliminarDatos(String idfc,View v) {
            String url = "https://www.wmcsoftware.net/apps/softpymes/eliminarCompra.php";
            // Crear la solicitud POST
            StringRequest stringRequest =  new StringRequest(
                    Request.Method.POST,
                    url,
                    response -> {
                        // Manejar la respuesta del servidor
                        try {
                            jsonObject = new JSONObject(response);
                        } catch (JSONException e) {
                            Toast.makeText((context),"Error al eliminar!",Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(context, jsonObject.optString("mensaje"), Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(v).navigate(R.id.recargarConsultaC);//Refrescar el fragment
                    },
                    error -> {
                        // Manejar errores
                        Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
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
