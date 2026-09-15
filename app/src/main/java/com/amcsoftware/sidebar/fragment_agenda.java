package com.amcsoftware.sidebar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amcsoftware.sidebar.Entidades.Agenda;
import com.amcsoftware.sidebar.adapter.AgendaAdapter;
import com.amcsoftware.sidebar.utils.AppUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class fragment_agenda extends Fragment implements SearchView.OnQueryTextListener {

    private static final String URL_GUARDAR = "https://www.wmcsoftware.net/apps/softpymes/guardarAgenda.php";
    private static final String URL_LISTA   = "https://www.wmcsoftware.net/apps/softpymes/listaAgenda.php";

    private RecyclerView recyclerView;

    private AgendaAdapter adapter;
    private final List<Agenda> listaAgenda = new ArrayList<>();

    private RequestQueue requestQueue; // una sola instancia por fragmento

    private AlertDialog alertDialog;              // diálogo de entrada "nueva tarea"
    private SweetAlertDialog dialogCargando;      // spinner SweetAlert

    SearchView searchView;

    public fragment_agenda() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_agenda, container, false);

        ImageButton btntarea = vista.findViewById(R.id.btntarea);
        ImageButton btrefresh = vista.findViewById(R.id.btrefresh);
        recyclerView = vista.findViewById(R.id.idRecycler);
        searchView = vista.findViewById(R.id.txtbuscar);

        // Cola de Volley
        requestQueue = Volley.newRequestQueue(requireContext());

        // RecyclerView
        adapter = new AgendaAdapter(listaAgenda);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Búsqueda
        searchView.setOnQueryTextListener(this);

        btntarea.setOnClickListener(v -> mostrarDialogNuevaTarea());
        btrefresh.setOnClickListener(v -> {
            if (AppUtils.hayConectividad(requireContext())) {
                cargarTareas();
            } else {
                AppUtils.alertSinInternet(requireContext());
            }
        });

        // Carga inicial con verificación de red
        if (AppUtils.hayConectividad(requireContext())) {
            cargarTareas();
        } else {
            AppUtils.alertSinInternet(requireContext());
        }
        return vista;
    }

    // Dialog: nueva tarea
    @SuppressLint("DefaultLocale")
    private void mostrarDialogNuevaTarea() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("");
        View viewInflada = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_input_agenda, null);

        final EditText    desc      = viewInflada.findViewById(R.id.txtdesc);
        final EditText    fecha     = viewInflada.findViewById(R.id.txtfecha);
        final Spinner     cmbpr     = viewInflada.findViewById(R.id.spnprt);
        final ImageButton btcerrar  = viewInflada.findViewById(R.id.btcerrar);
        final ImageButton btlimpiar = viewInflada.findViewById(R.id.btlimpiar);
        final ImageButton btguardar = viewInflada.findViewById(R.id.btguardar);

        btcerrar.setOnClickListener(v -> alertDialog.dismiss());

        btlimpiar.setOnClickListener(v -> {
            desc.setText("");
            fecha.setText("");
            cmbpr.setSelection(0);
        });

        fecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (view, year, month, day) ->
                            fecha.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        btguardar.setOnClickListener(v -> {
            String sDesc      = desc.getText().toString().trim();
            String sFecha     = fecha.getText().toString().trim();
            String sPrioridad = cmbpr.getSelectedItem() != null
                    ? cmbpr.getSelectedItem().toString() : "";

            if (sDesc.isEmpty() || sFecha.isEmpty() || sPrioridad.isEmpty()) {
                AppUtils.alertAdvertencia(requireContext(), "Campos incompletos",
                        "Completa todos los campos antes de guardar.");
                return;
            }
            // Verificar conectividad antes del POST
            if (!AppUtils.hayConectividad(requireContext())) {
                AppUtils.alertSinInternet(requireContext());
                return;
            }
            guardarTarea(sDesc, sFecha, sPrioridad);
            alertDialog.dismiss();
        });

        builder.setView(viewInflada);
        alertDialog = builder.create();
        alertDialog.show();
    }

    // Volley: guardar tarea (POST con parámetros form)
    private void guardarTarea(String descripcion, String fecha, String prioridad) {
        mostrarCargando("Guardando...");
        // StringRequest permite enviar parámetros POST clásicos (application/x-www-form-urlencoded)
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL_GUARDAR,
                response -> {
                    cerrarCargando();
                    try {
                        JSONObject resp    = new JSONObject(response);
                        boolean    success = resp.getBoolean("success");
                        String     mensaje = resp.getString("mensaje");

                        if (success) {
                            String nuevoId = resp.optString("id", "");
                            adapter.agregarItem(new Agenda(nuevoId, descripcion, fecha, prioridad));
                            recyclerView.scrollToPosition(0);
                            AppUtils.alertExito(requireContext(), "¡Guardado!", mensaje);
                        } else {
                            AppUtils.alertError(requireContext(), "Error", mensaje);
                        }
                    } catch (Exception e) {
                        AppUtils.alertError(requireContext(), "Error",
                                "No se pudo procesar la respuesta del servidor.");
                    }
                },
                error -> {
                    cerrarCargando();
                    AppUtils.alertError(requireContext(), "Error de red",
                            "No se pudo conectar al servidor.\n" + error.getMessage());
                }
        ) {
            // Parámetros POST
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("descripcion", descripcion);
                params.put("fecha",       fecha);
                params.put("prioridad",   prioridad);
                return params;
            }
        };

        requestQueue.add(request);
    }

    // Volley: cargar lista de tareas (GET -> JSON)
    private void cargarTareas() {
        mostrarCargando("Cargando tareas...");
        // JsonObjectRequest parsea el JSON automáticamente
        @SuppressLint("NotifyDataSetChanged") JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                URL_LISTA,
                null,   // body nulo para GET
                response -> {
                    cerrarCargando();
                    try {
                        if (!response.getBoolean("success")) {
                            AppUtils.alertError(requireContext(), "Softpymes",
                                    response.getString("mensaje"));
                            return;
                        }
                        listaAgenda.clear();
                        JSONArray arr = response.getJSONArray("agenda");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            listaAgenda.add(new Agenda(
                                    o.getString("id"),
                                    o.getString("descripcion"),
                                    o.getString("fecha"),
                                    o.getString("prioridad")
                            ));
                        }
                        adapter.actualizarLista();
                    } catch (Exception e) {
                        AppUtils.alertError(requireContext(), "Softpymes",
                                "Error al procesar los datos.");
                    }
                },
                error -> {
                    cerrarCargando();
                    AppUtils.alertError(requireContext(), "Error de red",
                            "No se pudo conectar al servidor.\n" + error.getMessage());
                }
        );

        requestQueue.add(request);
    }

    // Helpers de carga (SweetAlert) — delegan en AppUtils
    private void mostrarCargando(String titulo) {
        if (getContext() == null) return;
        dialogCargando = AppUtils.mostrarCargando(requireContext(), titulo, "Por favor espera.");
    }

    private void cerrarCargando() {
        AppUtils.cerrarCargando(dialogCargando);
    }

    // Ciclo de vida
    @Override
    public void onStop() {
        super.onStop();
        if (requestQueue != null) requestQueue.cancelAll(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cerrarCargando();
        if (alertDialog != null && alertDialog.isShowing()) alertDialog.dismiss();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String Text) {
        adapter.filtrado(Text);
        return false;
    }
}
