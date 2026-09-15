package com.amcsoftware.sidebar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.amcsoftware.sidebar.Entidades.Agenda;
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
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class AgendaAdapter extends RecyclerView.Adapter<AgendaAdapter.AgendaHolder>
        implements View.OnClickListener {

    private final List<Agenda> listaAgenda;
    private final List<Agenda> listaCompleta;   // copia para filtrado/restauración
    private View.OnClickListener listener;

    // Colores del chip de prioridad
    private static final int COLOR_ALTA  = Color.parseColor("#D32F2F"); // rojo
    private static final int COLOR_MEDIA = Color.parseColor("#F57C00"); // naranja
    private static final int COLOR_BAJA  = Color.parseColor("#388E3C"); // verde
    private static final int COLOR_DEF   = Color.parseColor("#757575"); // gris (fallback)

    public AgendaAdapter(List<Agenda> listaAgenda) {
        this.listaAgenda   = listaAgenda;
        this.listaCompleta = new ArrayList<>(listaAgenda);
    }

    @NonNull
    @Override
    public AgendaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.agenda_list, parent, false);

        vista.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        vista.setOnClickListener(this);
        return new AgendaHolder(vista, parentContext);
    }

    @Override
    public void onBindViewHolder(@NonNull AgendaHolder holder, int position) {
        Agenda item = listaAgenda.get(position);

        holder.txtid.setText(item.getIdt());
        holder.txtdescripcion.setText(item.getDescripcion());
        holder.txtfecha.setText(item.getFechat());
        holder.txtprioridad.setText(item.getPrioridad());

        // Color del chip según prioridad
        holder.txtprioridad.getBackground()
                .setTint(colorPrioridad(item.getPrioridad()));
    }

    @Override
    public int getItemCount() { return listaAgenda.size(); }

    // Sincroniza la copia de respaldo tras recargar listaAgenda desde el servidor
    @SuppressLint("NotifyDataSetChanged")
    public void actualizarLista() {
        listaCompleta.clear();
        listaCompleta.addAll(listaAgenda);
        notifyDataSetChanged();
    }


    @SuppressLint("NotifyDataSetChanged")
    public void filtrado(String txtBuscar) {
        listaAgenda.clear();
        if (txtBuscar == null || txtBuscar.isEmpty()) {
            listaAgenda.addAll(listaCompleta);
        } else {
            String query = txtBuscar.toLowerCase(Locale.ROOT).trim();
            List<Agenda> filtrados = listaCompleta.stream()
                    .filter(a -> a.getDescripcion().toLowerCase(Locale.ROOT).contains(query))
                    .collect(Collectors.toList());
            listaAgenda.addAll(filtrados);
        }
        notifyDataSetChanged();
    }

    // Agrega un ítem al tope sin recargar toda la lista
    public void agregarItem(Agenda agenda) {
        listaAgenda.add(0, agenda);
        listaCompleta.add(0, agenda);
        notifyItemInserted(0);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if (listener != null) listener.onClick(v);
    }

    private int colorPrioridad(String prioridad) {
        if (prioridad == null) return COLOR_DEF;
        switch (prioridad.trim()) {
            case "Alta":  return COLOR_ALTA;
            case "Media": return COLOR_MEDIA;
            case "Baja":  return COLOR_BAJA;
            default:      return COLOR_DEF;
        }
    }

    public static class AgendaHolder extends RecyclerView.ViewHolder {
        public Context context;
        TextView txtid, txtdescripcion, txtfecha, txtprioridad;
        ImageButton bteliminar;
        JSONObject   jsonObject = null;
        RequestQueue requestQueue;

        public AgendaHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context =  context;
            txtid          = itemView.findViewById(R.id.txtidagenda);
            txtdescripcion = itemView.findViewById(R.id.txtdescagenda);
            txtfecha       = itemView.findViewById(R.id.txtfechaagenda);
            txtprioridad   = itemView.findViewById(R.id.txtprioridadagenda);
            bteliminar  = itemView.findViewById(R.id.bteliminar);
            requestQueue = Volley.newRequestQueue(context);

            bteliminar.setOnClickListener(v-> {
                final String idt = String.valueOf(txtid.getText());
                AppUtils.alertConfirmar(context, "Softpymes",
                        "¿Está seguro de eliminar la tarea N° " + idt + "?", "Sí", "No",
                        () -> eliminarDatos(idt, v));
            });
        }

        private void eliminarDatos(String idt, View v) {
            String url = "https://www.wmcsoftware.net/apps/softpymes/eliminarTarea.php";
            // Crear la solicitud POST
            StringRequest stringRequest =  new StringRequest(
                    Request.Method.POST,
                    url,
                    response -> {
                        // Manejar la respuesta del servidor
                        String msj = "Tarea eliminada.";
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
                        Navigation.findNavController(v).navigate(R.id.recargarConsultaT);//Refrescar el fragment
                    },
                    error -> AppUtils.alertError(context, "Error de red", "No se pudo eliminar la tarea.")) {
                @Override
                protected Map<String, String> getParams() {
                    // Enviar los parámetros al servidor
                    Map<String, String> params = new HashMap<>();
                    params.put("idt", idt);
                    return params;
                }
            };
            // Agregar la solicitud a la cola
            requestQueue.add(stringRequest);
        }
    }
}
