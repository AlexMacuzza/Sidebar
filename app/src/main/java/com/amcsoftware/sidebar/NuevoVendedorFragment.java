package com.amcsoftware.sidebar;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.util.Log;
import com.amcsoftware.sidebar.utils.AppUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class NuevoVendedorFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener {
    //Variables locales
    EditText txtvendedor,txtcedvd,txtcelvd,txtdirvd;
    ImageButton btguardar;
    JSONObject jsonObject = null;
    // Declarar la ProgressBar
    SweetAlertDialog dialogCargando;
    RequestQueue requestQueue;

    public NuevoVendedorFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_nuevo_vendedor, container, false);
        txtvendedor  =  vista.findViewById(R.id.txtvendedor);
        txtcedvd     =  vista.findViewById(R.id.txtcedvd );
        txtcelvd     =  vista.findViewById(R.id.txtcelvd);
        txtdirvd     =  vista.findViewById(R.id.txtdirvd);
        btguardar    =  vista.findViewById(R.id.btregistrar);

        requestQueue = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo POST

        btguardar.setOnClickListener(v -> guardarRegistro());

        //Controlar botón atrás
        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {

            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        // Inflate the layout for this fragment
        return vista;
    }

    private void guardarRegistro() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/registrarVendedor.php";
        // Obtener los valores ingresados
        final String nombre    = txtvendedor.getText().toString().trim();
        final String cedula    = txtcedvd.getText().toString().trim();
        final String celular   = txtcelvd.getText().toString().trim();
        final String direccion = txtdirvd.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (nombre.isEmpty() || cedula.isEmpty() || celular.isEmpty()|| direccion.isEmpty()) {
            AppUtils.alertAdvertencia(requireContext(), "Campos incompletos", "Por favor, complete todos los campos.");
            return;
        }
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Guardando...", "Por favor espera.");
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    String msj = "Registro guardado.";
                    boolean ok = true;
                    try {
                        jsonObject = new JSONObject(response);
                        msj = jsonObject.optString("mensaje", msj);
                        ok = jsonObject.optBoolean("success", true);
                    } catch (JSONException e) {
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                    }
                    if (ok) {
                        AppUtils.alertExito(requireContext(), "Éxito", msj);
                        limpiar();
                    } else {
                        AppUtils.alertError(requireContext(), "Atención", msj);
                    }
                },
                error -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    AppUtils.alertError(requireContext(), "Error de red", "No se pudo guardar el registro.");
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("nombre", nombre);
                params.put("cedula", cedula);
                params.put("celular", celular);
                params.put("direccion", direccion);
                return params;
            }
        };

        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

    private void limpiar() {
        txtcedvd.setText("");
        txtcelvd.setText("");
        txtdirvd.setText("");
        txtvendedor.setText("");
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(JSONObject response) {

    }
}