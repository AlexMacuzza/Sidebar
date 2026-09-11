package com.amcsoftware.sidebar;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amcsoftware.sidebar.utils.AppUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class EditarCobradorFragment extends Fragment {
    // Variables locales
    EditText nombre, cedula, celular, dir;
    ImageButton btactualizar, bteliminar;
    TextView idcob;
    SweetAlertDialog dialogCargando;
    RequestQueue requestQueue;


    public EditarCobradorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_editar_cobrador, container, false);
        idcob        = vista.findViewById(R.id.lblidcob);
        cedula       = vista.findViewById(R.id.txtcedcob);
        nombre       = vista.findViewById(R.id.txtcobrador);
        celular      = vista.findViewById(R.id.txtcelcob);
        dir          = vista.findViewById(R.id.txtdircob);
        btactualizar = vista.findViewById(R.id.bteditar);
        bteliminar   = vista.findViewById(R.id.bteliminar);
        requestQueue = Volley.newRequestQueue(requireContext());

        assert getArguments() != null;
        idcob.setText(getArguments().getString("idcob"));
        cedula.setText(getArguments().getString("idcedcob"));
        nombre.setText(getArguments().getString("nombre"));
        celular.setText(getArguments().getString("celular"));
        dir.setText(getArguments().getString("dir"));

        btactualizar.setOnClickListener(v -> confirmarActualizar());

        bteliminar.setOnClickListener(v -> confirmarEliminar());

        // Controlar botón atrás
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Sin acción por ahora
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        // Inflate the layout for this fragment
        return vista;
    }

    // ════════════════════════════════════════════════════════════════════
    //  ELIMINAR
    // ════════════════════════════════════════════════════════════════════

    private void confirmarEliminar() {
        final String idcob1 = idcob.getText().toString().trim();

        if (idcob1.isEmpty()) {
            AppUtils.alertAdvertencia(requireContext(), "Dato faltante", "El id no puede estar vacío.");
            return;
        }

        AppUtils.alertConfirmar(requireContext(),
                "¿Eliminar cobrador?",
                "Esta acción no se puede deshacer.",
                "Sí, eliminar",
                "Cancelar",
                this::eliminarDatos);
    }

    private void eliminarDatos() {
        if (!AppUtils.hayConectividad(requireContext())) {
            AppUtils.alertSinInternet(requireContext());
            return;
        }

        String url = "https://www.wmcsoftware.net/apps/softpymes/eliminarCobrador.php";
        final String idcob1 = idcob.getText().toString().trim();

        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Eliminando...", "Por favor espera.");
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.optBoolean("success", false);
                        String mensaje  = jsonObject.optString("mensaje", "Ocurrió un error inesperado.");

                        if (success) {
                            AppUtils.alertExito(requireContext(), "¡Eliminado!", mensaje, this::limpiar);
                        } else {
                            AppUtils.alertAdvertencia(requireContext(), "No se pudo eliminar", mensaje);
                        }
                    } catch (JSONException e) {
                        AppUtils.alertError(requireContext(), "Error", "Respuesta inválida del servidor.");
                    }
                },
                error -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    AppUtils.alertError(requireContext(), "Error de conexión",
                            "No se pudo comunicar con el servidor. Intenta de nuevo.");
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("idcob", idcob1);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void limpiar() {
        idcob.setText("");
        cedula.setText("");
        nombre.setText("");
        celular.setText("");
        dir.setText("");
    }
    // ════════════════════════════════════════════════════════════════════
    //  ACTUALIZAR
    // ════════════════════════════════════════════════════════════════════
    private void confirmarActualizar() {
        final String idcob1    = idcob.getText().toString().trim();
        final String nombre1   = nombre.getText().toString().trim();
        final String dir1      = dir.getText().toString().trim();
        final String celular1  = celular.getText().toString().trim();

        if (idcob1.isEmpty() || nombre1.isEmpty() || dir1.isEmpty() || celular1.isEmpty()) {
            AppUtils.alertAdvertencia(requireContext(),
                    "Campos incompletos",
                    "Por favor, complete todos los campos.");
            return;
        }

        AppUtils.alertConfirmar(requireContext(),
                "¿Actualizar cobrador?",
                "Se guardarán los cambios realizados sobre \"" + nombre1 + "\".",
                "Sí, actualizar",
                "Cancelar",
                this::actualizarDatos);
    }

    private void actualizarDatos() {
        if (!AppUtils.hayConectividad(requireContext())) {
            AppUtils.alertSinInternet(requireContext());
            return;
        }

        String url = "https://www.wmcsoftware.net/apps/softpymes/actualizarCobrador.php";
        final String idcob1    = idcob.getText().toString().trim();
        final String cedula1   = cedula.getText().toString().trim();
        final String nombre1   = nombre.getText().toString().trim();
        final String dir1      = dir.getText().toString().trim();
        final String celular1  = celular.getText().toString().trim();

        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Actualizando...", "Por favor espera.");
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.optBoolean("success", false);
                        String mensaje  = jsonObject.optString("mensaje", "Ocurrió un error inesperado.");

                        if (success) {
                            AppUtils.alertExito(requireContext(), "¡Actualizado!", mensaje, null);
                        } else {
                            AppUtils.alertAdvertencia(requireContext(), "No se pudo actualizar", mensaje);
                        }
                    } catch (JSONException e) {
                        AppUtils.alertError(requireContext(), "Error", "Respuesta inválida del servidor.");
                    }
                },
                error -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    AppUtils.alertError(requireContext(), "Error de conexión",
                            "No se pudo comunicar con el servidor. Intenta de nuevo.");
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("idcob", idcob1);
                params.put("cedula", cedula1);
                params.put("nombre", nombre1);
                params.put("dir", dir1);
                params.put("celular", celular1);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
}