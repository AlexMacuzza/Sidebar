package com.amcsoftware.sidebar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amcsoftware.sidebar.Entidades.Agenda;
import com.amcsoftware.sidebar.adapter.AgendaAdapter;

// ── Volley ────────────────────────────────────────────────────────────────────
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

    // ── URLs ─────────────────────────────────────────────────────────────────
    private static final String URL_GUARDAR = "https://www.wmcsoftware.net/apps/softpymes/guardarAgenda.php";
    private static final String URL_LISTA   = "https://www.wmcsoftware.net/apps/softpymes/listaAgenda.php";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    // ── Datos ─────────────────────────────────────────────────────────────────
    private AgendaAdapter adapter;
    private final List<Agenda> listaAgenda = new ArrayList<>();

    // ── Volley queue (una sola instancia por fragmento) ───────────────────────
    private RequestQueue requestQueue;

    // ── Dialog ────────────────────────────────────────────────────────────────
    private AlertDialog alertDialog;
    private SweetAlertDialog dialogCargando;

    SearchView searchView;

    public fragment_agenda() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_agenda, container, false);

        // ── Vistas ────────────────────────────────────────────────────────────────
        ImageButton btntarea = vista.findViewById(R.id.btntarea);
        ImageButton btrefresh = vista.findViewById(R.id.btrefresh);
        recyclerView = vista.findViewById(R.id.idRecycler);
        progressBar  = vista.findViewById(R.id.progressBar);
        searchView = vista.findViewById(R.id.txtbuscar);

        // Cola de Volley
        requestQueue = Volley.newRequestQueue(requireContext());

        // RecyclerView
        adapter = new AgendaAdapter(listaAgenda);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Búsqueda
        searchView.setOnQueryTextListener(this);
        /*searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { adapter.filtrado(q); return true; }
            @Override public boolean onQueryTextChange(String q) { adapter.filtrado(q); return true; }
        });*/

        btntarea.setOnClickListener(v -> mostrarDialogNuevaTarea());
        btrefresh.setOnClickListener(v -> {
            if (hayConectividad()) {
                cargarTareas();
            } else {
                alertSinInternet();
            }
        });

        // Carga inicial con verificación de red
        if (hayConectividad()) {
            cargarTareas();
        } else {
            alertSinInternet();
        }
        return vista;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CONECTIVIDAD
    // ════════════════════════════════════════════════════════════════════════
    /**
     * Comprueba si el dispositivo tiene conexión activa a Internet.
     * Usa NetworkCapabilities (API 23+), compatible con Android 6 en adelante.
     */
    private boolean hayConectividad() {
        ConnectivityManager cm = (ConnectivityManager)
                requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        android.net.Network red = cm.getActiveNetwork();
        if (red == null) return false;

        NetworkCapabilities caps = cm.getNetworkCapabilities(red);
        return caps != null && (
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        );
    }

    /** SweetAlert específico para ausencia de internet */
    private void alertSinInternet() {
        if (getContext() == null) return;
        new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Sin conexión")
                .setContentText("Verifica tu conexión a Internet e intenta de nuevo.")
                .setConfirmText("Entendido")
                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DIALOG – Nueva tarea
    // ════════════════════════════════════════════════════════════════════════
    @SuppressLint("DefaultLocale")
    private void mostrarDialogNuevaTarea() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("");
        View viewInflada = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_input_agenda, null);
        /*View viewInflada = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_input_agenda, null);*/

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
                alertAdvertencia();
                return;

            }
            // Verificar conectividad antes del POST
            if (!hayConectividad()) {
                alertSinInternet();
                return;
            }
            //dialogTarea.dismissWithAnimation();
            guardarTarea(sDesc, sFecha, sPrioridad);
            alertDialog.dismiss();
        });

        builder.setView(viewInflada);
        alertDialog = builder.create();
        alertDialog.show();
        //dialogTarea.show();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  VOLLEY – Guardar tarea (POST con parámetros form)
    // ════════════════════════════════════════════════════════════════════════
    private void guardarTarea(String descripcion, String fecha, String prioridad) {
        mostrarCargando("Guardando...");
        //mostrarProgress(true);
        // StringRequest permite enviar parámetros POST clásicos (application/x-www-form-urlencoded)
        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL_GUARDAR,
                response -> {
                    //mostrarProgress(false);
                    cerrarCargando();
                    try {
                        JSONObject resp    = new JSONObject(response);
                        boolean    success = resp.getBoolean("success");
                        String     mensaje = resp.getString("mensaje");

                        if (success) {
                            String nuevoId = resp.optString("id", "");
                            adapter.agregarItem(new Agenda(nuevoId, descripcion, fecha, prioridad));
                            recyclerView.scrollToPosition(0);
                            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
                            //alertExito("¡Guardado!", mensaje);
                        } else {
                            //mostrarMensaje(mensaje);
                            alertError("Error", mensaje);
                        }
                    } catch (Exception e) {
                        alertError("Error", "No se pudo procesar la respuesta del servidor.");
                        //mostrarMensaje("Error al procesar la respuesta.");
                    }
                },
                error -> {
                    //mostrarProgress(false);
                    cerrarCargando();
                    //mostrarMensaje("Error de red: " + error.getMessage());
                    alertError("Error de red", "No se pudo conectar al servidor.\n" + error.getMessage());
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

    // ════════════════════════════════════════════════════════════════════════
    //  VOLLEY – Cargar lista de tareas (GET → JSON)
    // ════════════════════════════════════════════════════════════════════════
    private void cargarTareas() {
        //mostrarProgress(true);
        mostrarCargando("Cargando tareas...");
        // JsonObjectRequest parsea el JSON automáticamente
        @SuppressLint("NotifyDataSetChanged") JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                URL_LISTA,
                null,   // body nulo para GET
                response -> {
                    //mostrarProgress(false);
                    cerrarCargando();
                    try {
                        if (!response.getBoolean("success")) {
                            mostrarMensaje(response.getString("mensaje"));
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
                        mostrarMensaje("Error al procesar los datos.");
                    }
                },
                error -> {
                    //mostrarProgress(false);
                    cerrarCargando();
                    mostrarMensaje("Error de red: " + error.getMessage());
                }
        );

        requestQueue.add(request);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════════════

    private void mostrarMensaje(String mensaje) {
        if (getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Softpymes")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    /** Spinner de carga con título y subtítulo */
    private void mostrarCargando(String titulo) {
        if (getContext() == null) return;
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        dialogCargando = new SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE);
        dialogCargando.setTitleText(titulo);
        dialogCargando.setContentText("Por favor espera.");
        dialogCargando.setCancelable(false);
        dialogCargando.show();
    }

    private void cerrarCargando() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (dialogCargando != null && dialogCargando.isShowing())
            dialogCargando.dismissWithAnimation();
    }

    //Transacción exitosa
    /*private void alertExito(String titulo, String mensaje) {
        if (getContext() == null) return;
        new SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(titulo)
                .setContentText(mensaje)
                .setConfirmText("OK")
                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }*/

    //Error en la transacción
    private void alertError(String titulo, String mensaje) {
        if (getContext() == null) return;
        new SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                .setTitleText(titulo)
                .setContentText(mensaje)
                .setConfirmText("Cerrar")
                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    private void alertAdvertencia() {
        if (getContext() == null) return;
        new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Campos incompletos")
                .setContentText("Completa todos los campos antes de guardar.")
                .setConfirmText("OK")
                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Ciclo de vida
    // ════════════════════════════════════════════════════════════════════════
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
