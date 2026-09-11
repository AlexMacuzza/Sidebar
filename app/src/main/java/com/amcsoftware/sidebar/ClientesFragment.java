package com.amcsoftware.sidebar;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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


public class ClientesFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener {
    //Variables locales
    EditText txtcliente,txtid,txtcelular,txtdir,txtcodeudor,txtcelcodeudor,txtobservacion;
    ImageButton btregistrar;
    JSONObject jsonObject = null;
    SweetAlertDialog dialogCargando;
    RequestQueue request, requestQueue;

    public ClientesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Intanciar objetos
        View vista      =  inflater.inflate(R.layout.fragment_clientes, container, false);
        txtcliente      =  vista.findViewById(R.id.lblcliente);
        txtid           =  vista.findViewById(R.id.txtid);
        txtcelular      =  vista.findViewById(R.id.txtcelular);
        txtdir          =  vista.findViewById(R.id.txtdir);
        txtcodeudor     =  vista.findViewById(R.id.txtcodeudor);
        txtcelcodeudor  =  vista.findViewById(R.id.txtcelcodeudor);
        txtobservacion  =  vista.findViewById(R.id.txtobservacion);
        btregistrar     =  vista.findViewById(R.id.btregistrar);

        request      = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET
        requestQueue = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo POST

        request.getCache().clear(); // Limpia el caché global

        //Evento click
        btregistrar.setOnClickListener(v -> guardarRegistro());

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
        String url = "https://www.wmcsoftware.net/apps/softpymes/guardarCliente.php";
        // Obtener los valores ingresados
        final String id          = txtid.getText().toString().trim();
        final String nombre      = txtcliente.getText().toString().trim();
        final String direccion   = txtdir.getText().toString().trim();
        final String celular     = txtcelular.getText().toString().trim();
        final String codeudor    = txtcodeudor.getText().toString().trim();
        final String celularcode = txtcelcodeudor.getText().toString().trim();
        final String observacion = txtobservacion.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (id.isEmpty() || nombre.isEmpty() || direccion.isEmpty()|| celular.isEmpty() || codeudor.isEmpty() || celularcode.isEmpty()) {
            AppUtils.alertAdvertencia(requireContext(), "Campos incompletos",
                    "Por favor, complete todos los campos.");
            return;
        }
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Guardando...", "Por favor espera.");
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    // Ocultar el diálogo de carga
                    AppUtils.cerrarCargando(dialogCargando);
                    // Manejar la respuesta del servidor
                    String mensaje = "Se ha registrado exitosamente!";
                    boolean ok = true;
                    try {
                        jsonObject = new JSONObject(response);
                        mensaje = jsonObject.optString("mensaje", mensaje);
                        ok = jsonObject.optBoolean("success", true);
                    } catch (JSONException e) {
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                    }
                    if (ok) {
                        AppUtils.alertExito(requireContext(), "Éxito", mensaje);
                        limpiar();
                    } else {
                        AppUtils.alertError(requireContext(), "Atención", mensaje);
                    }
                },
                error -> {
                    // Ocultar el diálogo de carga
                    AppUtils.cerrarCargando(dialogCargando);
                    // Manejar errores
                    Log.e("VOLLEY", "Error: " + error.getMessage());
                    AppUtils.alertError(requireContext(), "Error",
                            "No se pudo registrar el cliente. Intenta de nuevo.");
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("nombre", nombre);
                params.put("direccion", direccion);
                params.put("celular", celular);
                params.put("codeudor", codeudor);
                params.put("celularcode", celularcode);
                params.put("observacion", observacion);
                return params;
            }
        };

        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

    private void limpiar() {
        txtid.setText("");
        txtcliente.setText("");
        txtcelular.setText("");
        txtdir.setText("");
        txtcodeudor.setText("");
        txtcelcodeudor.setText("");
        txtobservacion.setText("");
    }


    @Override
    public void onResponse(JSONObject response) {
        AppUtils.cerrarCargando(dialogCargando);
        AppUtils.alertExito(requireContext(), "Éxito", "Se ha registrado exitosamente!");
        limpiar();
        btregistrar.setEnabled(true);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        AppUtils.cerrarCargando(dialogCargando);
        AppUtils.alertError(requireContext(), "Error", "No se pudo registrar el cliente.");
        Log.i("ERROR", error.toString());
        btregistrar.setEnabled(true);
    }


}
