package com.amcsoftware.sidebar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amcsoftware.sidebar.Entidades.Agenda;
import com.amcsoftware.sidebar.adapter.AgendaAdapter;
import com.amcsoftware.sidebar.utils.AppUtils;
import com.amcsoftware.sidebar.utils.RecordatorioScheduler;

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

    // Permiso de notificaciones (Android 13+); debe registrarse antes de CREATED.
    private final ActivityResultLauncher<String> permisoNotificaciones =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // Si se niega, los recordatorios se siguen programando igual;
                // simplemente el sistema no mostrará la notificación.
            });

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

        solicitarPermisoNotificacionesSiHaceFalta();

        return vista;
    }

    // Android 13+ exige permiso en tiempo de ejecución para mostrar notificaciones.
    private void solicitarPermisoNotificacionesSiHaceFalta() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    // Dialog: nueva tarea
    @SuppressLint("DefaultLocale")
    private void mostrarDialogNuevaTarea() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("");
        View viewInflada = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_input_agenda, null);

        final EditText    desc         = viewInflada.findViewById(R.id.txtdesc);
        final EditText    fecha        = viewInflada.findViewById(R.id.txtfecha);
        final EditText    hora         = viewInflada.findViewById(R.id.txthora);
        final Spinner     cmbpr        = viewInflada.findViewById(R.id.spnprt);
        final Spinner     cmbrepetir   = viewInflada.findViewById(R.id.spnrepetir);
        final View        rowintervalo = viewInflada.findViewById(R.id.rowintervalo);
        final EditText    intervalo    = viewInflada.findViewById(R.id.txtintervalodias);
        final ImageButton btcerrar     = viewInflada.findViewById(R.id.btcerrar);
        final ImageButton btlimpiar    = viewInflada.findViewById(R.id.btlimpiar);
        final ImageButton btguardar    = viewInflada.findViewById(R.id.btguardar);

        btcerrar.setOnClickListener(v -> alertDialog.dismiss());

        btlimpiar.setOnClickListener(v -> {
            desc.setText("");
            fecha.setText("");
            hora.setText("");
            cmbpr.setSelection(0);
            cmbrepetir.setSelection(0);
            intervalo.setText("");
            rowintervalo.setVisibility(View.GONE);
        });

        fecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (view, year, month, day) ->
                            fecha.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        hora.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(requireContext(),
                    (view, hourOfDay, minute) ->
                            hora.setText(String.format("%02d:%02d", hourOfDay, minute)),
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
            ).show();
        });

        // "Cada cuántos días" solo aplica cuando se elige repetición Personalizada
        cmbrepetir.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean personalizado = "Personalizado".equals(cmbrepetir.getSelectedItem());
                rowintervalo.setVisibility(personalizado ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btguardar.setOnClickListener(v -> {
            String sDesc      = desc.getText().toString().trim();
            String sFecha     = fecha.getText().toString().trim();
            String sHora      = hora.getText().toString().trim();
            String sPrioridad = cmbpr.getSelectedItem() != null
                    ? cmbpr.getSelectedItem().toString() : "";
            String sRepetir   = cmbrepetir.getSelectedItem() != null
                    ? cmbrepetir.getSelectedItem().toString() : "Ninguna";
            String sIntervalo = intervalo.getText().toString().trim();

            if (sDesc.isEmpty() || sFecha.isEmpty() || sPrioridad.isEmpty()) {
                AppUtils.alertAdvertencia(requireContext(), "Campos incompletos",
                        "Completa todos los campos antes de guardar.");
                return;
            }
            if ("Personalizado".equals(sRepetir) && sIntervalo.isEmpty()) {
                AppUtils.alertAdvertencia(requireContext(), "Campo incompleto",
                        "Indica cada cuántos días se debe repetir el recordatorio.");
                return;
            }
            // Verificar conectividad antes del POST
            if (!AppUtils.hayConectividad(requireContext())) {
                AppUtils.alertSinInternet(requireContext());
                return;
            }
            // Las alarmas exactas (Android 12+) requieren un permiso especial que el
            // usuario concede en Ajustes; sin él, el recordatorio igual se programa
            // pero de forma inexacta (ver RecordatorioScheduler.programar).
            if (!RecordatorioScheduler.tienePermisoAlarmaExacta(requireContext())) {
                AppUtils.alertConfirmar(requireContext(), "Permiso necesario",
                        "Para que el recordatorio suene puntual, activa \"Alarmas y recordatorios\" para esta app.",
                        "Ir a Ajustes", "Ahora no",
                        () -> RecordatorioScheduler.solicitarPermisoAlarmaExacta(requireContext()));
            }
            guardarTarea(sDesc, sFecha, sHora, sPrioridad, sRepetir, sIntervalo);
            alertDialog.dismiss();
        });

        builder.setView(viewInflada);
        alertDialog = builder.create();
        alertDialog.show();
    }

    // Volley: guardar tarea (POST con parámetros form)
    private void guardarTarea(String descripcion, String fecha, String hora, String prioridad,
                               String repetir, String intervalodias) {
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
                            Agenda nuevaTarea = new Agenda(nuevoId, descripcion, fecha, prioridad,
                                    hora, repetir, intervalodias);
                            adapter.agregarItem(nuevaTarea);
                            recyclerView.scrollToPosition(0);
                            RecordatorioScheduler.programar(requireContext(), nuevaTarea);
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
                params.put("descripcion",   descripcion);
                params.put("fecha",         fecha);
                params.put("hora",          hora);
                params.put("prioridad",     prioridad);
                params.put("repetir",       repetir);
                params.put("intervalodias", intervalodias);
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
                            Agenda tarea = new Agenda(
                                    o.getString("id"),
                                    o.getString("descripcion"),
                                    o.getString("fecha"),
                                    o.getString("prioridad"),
                                    o.optString("hora"),
                                    o.optString("repetir", "Ninguna"),
                                    o.optString("intervalodias")
                            );
                            listaAgenda.add(tarea);
                            // Reprograma la alarma por si se creó/editó desde otro
                            // dispositivo, o si esta instalación aún no la tenía.
                            RecordatorioScheduler.programar(requireContext(), tarea);
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
