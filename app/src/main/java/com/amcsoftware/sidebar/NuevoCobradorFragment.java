package com.amcsoftware.sidebar;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

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

public class NuevoCobradorFragment extends Fragment {
    EditText txtcobrador, txtcedcob, txtcelcob, txtdircob;
    ImageButton btguardar;
    SweetAlertDialog dialogCargando;
    RequestQueue requestQueue;

    public NuevoCobradorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_nuevo_cobrador, container, false);
        txtcobrador  = vista.findViewById(R.id.txtcobrador);
        txtcedcob    = vista.findViewById(R.id.txtcedcob);
        txtcelcob    = vista.findViewById(R.id.txtcelcob);
        txtdircob    = vista.findViewById(R.id.txtdircob);
        btguardar    = vista.findViewById(R.id.btregistrar);

        requestQueue = Volley.newRequestQueue(requireContext());

        btguardar.setOnClickListener(v -> guardarRegistro());

        // Controlar botón atrás
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Sin acción por ahora
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return vista;
    }

    private void guardarRegistro() {
        final String nombre    = txtcobrador.getText().toString().trim();
        final String cedula    = txtcedcob.getText().toString().trim();
        final String celular   = txtcelcob.getText().toString().trim();
        final String direccion = txtdircob.getText().toString().trim();

        // Validar campos vacíos
        if (nombre.isEmpty() || cedula.isEmpty() || celular.isEmpty() || direccion.isEmpty()) {
            AppUtils.alertAdvertencia(requireContext(),
                    "Campos incompletos",
                    "Por favor, complete todos los campos.");
            return;
        }

        // Validar conectividad antes de enviar
        if (!AppUtils.hayConectividad(requireContext())) {
            AppUtils.alertSinInternet(requireContext());
            return;
        }

        // Confirmación antes de registrar
        AppUtils.alertConfirmar(requireContext(),
                "¿Registrar cobrador?",
                "Se creará el cobrador \"" + nombre + "\" con cédula " + cedula + ".",
                "Sí, registrar",
                "Cancelar",
                () -> enviarRegistro(nombre, cedula, celular, direccion));
    }

    private void enviarRegistro(String nombre, String cedula, String celular, String direccion) {
        String url = "https://www.wmcsoftware.net/apps/softpymes/registrarCobrador.php";
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Guardando...", "Por favor espera.");

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
                            AppUtils.alertExito(requireContext(), "¡Guardado!", mensaje, this::limpiar);
                        } else {
                            // Ej: cédula ya registrada, o no hay sucursal configurada
                            AppUtils.alertAdvertencia(requireContext(), "No se pudo guardar", mensaje);
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
                params.put("nombre", nombre);
                params.put("cedula", cedula);
                params.put("celular", celular);
                params.put("direccion", direccion);
                // Si en el futuro el usuario puede elegir/cambiar la sucursal de trabajo
                // desde la app, envía aquí su id: params.put("idsucursal", idSucursalSeleccionada);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void limpiar() {
        txtcobrador.setText("");
        txtcedcob.setText("");
        txtcelcob.setText("");
        txtdircob.setText("");
    }
}
