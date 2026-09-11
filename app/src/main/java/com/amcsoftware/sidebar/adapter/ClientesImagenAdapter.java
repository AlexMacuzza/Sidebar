package com.amcsoftware.sidebar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.amcsoftware.sidebar.Entidades.Cliente;
import com.amcsoftware.sidebar.R;
import com.amcsoftware.sidebar.utils.AppUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientesImagenAdapter extends RecyclerView.Adapter<ClientesImagenAdapter.ClientesImagenHolder>
        implements View.OnClickListener
{
    List<Cliente> listaClientes;
    List<Cliente> filteredData;//Buscar datos

    public Context context;

    private View.OnClickListener listener;

    public ClientesImagenAdapter(List<Cliente> listaClientes){
        this.listaClientes = listaClientes;
        filteredData = new ArrayList<>();
        filteredData.addAll(listaClientes);
    }

    @NonNull
    @Override
    public ClientesImagenHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.buscar_clientes_list_image,parent,false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        vista.setLayoutParams(layoutParams);

        vista.setOnClickListener(this);//Evento click

        return new ClientesImagenHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientesImagenHolder holder, int position) {
        holder.txtidcl.setText(listaClientes.get(position).getIdcl());
        holder.txtid.setText(listaClientes.get(position).getCedula());
        holder.txtcliente.setText(listaClientes.get(position).getNombre());
        holder.txtcelular.setText(listaClientes.get(position).getCelular());
        holder.txtdir.setText(listaClientes.get(position).getDireccion());
        holder.txtcodeudor.setText(listaClientes.get(position).getCodeudor());
        holder.txtcelcodeudor.setText(listaClientes.get(position).getCelularcode());
        holder.txtobs.setText(listaClientes.get(position).getObservacion());
        holder.txtscore.setText(listaClientes.get(position).getScore());
        holder.btnUp.setEnabled(position > 0);
        holder.btnDown.setEnabled(position < listaClientes.size() - 1);
        //IMAGEN BLOB
        if (listaClientes.get(position).getFoto()!=null){
            holder.imagen.setImageBitmap(listaClientes.get(position).getFoto());
        }else{
            holder.imagen.setImageResource(R.drawable.clientes);
        }
        //IMAGEN A TRAVES DE URL
        //URL BASE DE TU SERVIDOR
        String BASE_URL = "https://www.wmcsoftware.net/";
        String rutaImagen = listaClientes.get(position).getRfoto();
        if (rutaImagen != null && !rutaImagen.isEmpty()) {
            String urlCompleta = BASE_URL + rutaImagen;
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(urlCompleta)
                    .placeholder(R.drawable.clientes) // mientras carga
                    .error(R.drawable.clientes)       // si falla
                    .into(holder.imagen);
        } else {
            holder.imagen.setImageResource(R.drawable.clientes);
        }
        //////////////////////////////////////////////////////////////////
        holder.btnUp.setOnClickListener(v -> {
            // Lógica para mover hacia arriba
            // Deberás mover el elemento en la 'posición' un lugar hacia arriba
            // Llama a un método en tu adaptador o pasa un callback a la actividad/fragmento
            if (position > 0) {
                // Intercambia elementos en tu lista de datos
                Collections.swap(listaClientes, position, position - 1);
                notifyItemMoved(position, position - 1);
                // Opcionalmente, si necesitas volver a enlazar las vistas debido a cambios de índice:
                notifyItemChanged(position);
                notifyItemChanged(position - 1);
            }
        });

        holder.btnDown.setOnClickListener(v -> {
            // Lógica para mover hacia abajo
            // Deberás mover el elemento en la 'posición' un lugar hacia abajo
            // Llama a un método en tu adaptador o pasa un callback a la actividad/fragmento
            if (position < listaClientes.size() - 1) {
                // Intercambia elementos en tu lista de datos
                Collections.swap(listaClientes, position, position + 1);
                notifyItemMoved(position, position + 1);
                // Opcionalmente, si necesitas volver a enlazar las vistas debido a cambios de índice:
                notifyItemChanged(position);
                notifyItemChanged(position + 1);
            }
        });
        // --- Lógica del botón "Mover al Principio" ---
        if (holder.btnMoveToStart != null) { // Asegúrate de que el botón exista en tu ViewHolder
            holder.btnMoveToStart.setOnClickListener(v -> {
                int currentPosition = holder.getBindingAdapterPosition();
                moveItemToStart(currentPosition); // Llama al método actualizado
            });
        }

        // --- Lógica del botón "Mover al Final" ---
        if (holder.btnMoveToEnd != null) { // Asegúrate de que el botón exista en tu ViewHolder
            holder.btnMoveToEnd.setOnClickListener(v -> {
                int currentPosition = holder.getBindingAdapterPosition();
                moveItemToEnd(currentPosition); // Llama al método actualizado
            });
        }

    }

    // --- Método para Mover un ítem al Principio de la Lista ---
    public void moveItemToStart(int currentPosition) {
        if (currentPosition > 0 && currentPosition < listaClientes.size()) {
            Cliente itemToMove = listaClientes.remove(currentPosition);
            notifyItemRemoved(currentPosition);

            listaClientes.add(0, itemToMove);
            notifyItemInserted(0);

            // Si necesitas asegurar que las vistas se re-dibujen correctamente
            notifyItemRangeChanged(0, currentPosition + 1); // Notifica un rango afectado
        }
    }

    // --- Método para Mover un ítem al Final de la Lista ---
    public void moveItemToEnd(int currentPosition) {
        if (currentPosition >= 0 && currentPosition < listaClientes.size() - 1) {
            Cliente itemToMove = listaClientes.remove(currentPosition);
            notifyItemRemoved(currentPosition);

            listaClientes.add(itemToMove);
            notifyItemInserted(listaClientes.size() - 1);

            // Si necesitas asegurar que las vistas se re-dibujen correctamente
            notifyItemRangeChanged(currentPosition, listaClientes.size() - currentPosition); // Notifica un rango afectado
        }
    }
    
    @SuppressLint("NotifyDataSetChanged")
    public void filtrado(String txtbuscar){
        int longitud = txtbuscar.length();
        if (longitud==0){
            listaClientes.clear();
            listaClientes.addAll(filteredData);
        }else{
            List<Cliente> collection = listaClientes.stream().
                    filter(i -> i.getNombre().toLowerCase().contains(txtbuscar.toLowerCase()))
                    .collect(Collectors.toList());
            listaClientes.clear();
            listaClientes.addAll(collection);
        }
        notifyDataSetChanged();
    }

    // Método para acceder a los ítems por posición
    public Cliente getItemAtPosition(int position) {
        if (position >= 0 && position < listaClientes.size()) {
            return listaClientes.get(position);
        }
        return null;
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

    @Override
    public int getItemCount() {
        return listaClientes.size();
    }



    public static class ClientesImagenHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Context context;
        TextView txtid,txtidcl,txtcliente,txtcelular,txtcodeudor,txtcelcodeudor,txtdir,
                txtobs, txtscore;
        ImageButton btnUp,btnDown,btnMoveToStart,btnMoveToEnd, btncoment;
        ImageView imagen;
        String idcb,perfil;
        JSONObject   jsonObject = null;
        RequestQueue requestQueue;
        cn.pedant.SweetAlert.SweetAlertDialog dialogCargando;

        public ClientesImagenHolder(View itemView) {
            super(itemView);
            this.context =  itemView.getContext();
            txtidcl = itemView.findViewById(R.id.txtidcl1);
            txtid = itemView.findViewById(R.id.txtid1);
            txtcliente = itemView.findViewById(R.id.txtcliente1);
            txtcelular = itemView.findViewById(R.id.txtcelular1);
            txtcodeudor = itemView.findViewById(R.id.txtcodeudor1);
            txtcelcodeudor = itemView.findViewById(R.id.txtcelcodeudor1);
            txtdir = itemView.findViewById(R.id.txtdir1);
            txtobs = itemView.findViewById(R.id.txtobservacion1);
            txtscore = itemView.findViewById(R.id.txtscore1);
            imagen = itemView.findViewById(R.id.idfoto);
            btnUp = itemView.findViewById(R.id.btn_up);
            btnDown = itemView.findViewById(R.id.btn_down);
            btnMoveToStart = itemView.findViewById(R.id.btn_move_to_start);
            btnMoveToEnd = itemView.findViewById(R.id.btn_move_to_end);
            btncoment  = itemView.findViewById(R.id.btnscore);
            requestQueue = Volley.newRequestQueue(context);//Respuesta de las peticiones método POST

            SharedPreferences sp = itemView.getContext().getSharedPreferences("sesion",0);
            idcb   = sp.getString("codperfil", "General");
            perfil = sp.getString("perfil", "General");
            //Ocultar los botones mover arriba o hacia abajo
            if (perfil.equals("Administrador") || perfil.equals("Vendedor") ){
                btnUp.setVisibility(View.GONE);
                btnDown.setVisibility(View.GONE);
                btnMoveToStart.setVisibility(View.GONE);
                btnMoveToEnd.setVisibility(View.GONE);
                btncoment.setVisibility(View.GONE);
            }

            btncoment.setOnClickListener(v->{
                final String idc =  String.valueOf(txtidcl.getText());
                AlertDialogRButtons(idc,v);
                /*AlertDialog.Builder builder = new AlertDialog.Builder(context);//Alert de confirmación
                builder.setMessage("¿Desea este comentario para N° "+txtcliente.getText()+ "?").setTitle("Softpymes");

                builder.setPositiveButton("Si", (dialog, which) -> agregarComentario(idc,v,"Excelente"));

                builder.setNegativeButton("No", (dialog, which) ->
                        Toast.makeText(context,
                                "Operación cancelada",
                                Toast.LENGTH_SHORT).show());

                AlertDialog dialog = builder.create();
                dialog.show();//Mostrar el Alert*/
            });


        }

        public void AlertDialogRButtons(String idc, View v) {
            final String[] opciones = {"Excelente", "Aceptable", "Malo"};
            final int[] selectedItemIndex = {-1};
            AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
            builder.setTitle("Calificar cliente:");
            builder.setSingleChoiceItems(opciones, -1, (dialog, which) -> selectedItemIndex[0] = which);
            builder.setPositiveButton("Aceptar", (dialog, which) -> {
                if (selectedItemIndex[0] != -1) {
                    String seleccion = opciones[selectedItemIndex[0]];
                    agregarComentario(idc,v,seleccion);
                } else {
                    AppUtils.alertAdvertencia(this.context, "Atención", "No se seleccionó ninguna opción.");
                }
            });
            // Crea y muestra el AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    private void agregarComentario(String idc, View v, String seleccion) {
        String url = "https://www.wmcsoftware.net/apps/softpymes/calificarCliente.php";
        dialogCargando = AppUtils.mostrarCargando(context, "Guardando...", "Por favor espera.");
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    String msj = "Calificación registrada.";
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
                    Navigation.findNavController(v).navigate(R.id.recargarConsultaCl);//Refrescar el fragment
                },
                error -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    AppUtils.alertError(context, "Error de red", "No se pudo registrar la calificación.");
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("idc", idc);
                params.put("score", seleccion);
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
