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
import android.widget.ProgressBar;
import android.widget.Toast;
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


public class ClientesFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener {
    //Variables locales
    EditText txtcliente,txtid,txtcelular,txtdir,txtcodeudor,txtcelcodeudor,txtobservacion;
    ImageButton btregistrar;
    JSONObject jsonObject = null;
    ProgressBar progressBar;
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
        progressBar     =  vista.findViewById(R.id.progressBar);

        request      = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET
        requestQueue = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo POST

        request.getCache().clear(); // Limpia el caché global

        //Evento click
        btregistrar.setOnClickListener(v -> guardarRegistro());

        //Controlar botón atrás
        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                //Toast.makeText(requireContext(), "Botón en MyFragment", Toast.LENGTH_LONG).show();
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
            Toast.makeText(getContext(), "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Mostrar la ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    // Ocultar ProgressBar
                    progressBar.setVisibility(View.GONE);
                    // Manejar la respuesta del servidor
                    try {
                        jsonObject = new JSONObject(response);
                    } catch (JSONException e) {
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                    }
                    Toast.makeText(getContext(), jsonObject.optString("mensaje"), Toast.LENGTH_SHORT).show();
                    limpiar();

                },
                error -> {
                    // Ocultar ProgressBar
                    progressBar.setVisibility(View.GONE);
                    // Manejar errores
                    Log.e("VOLLEY", "Error: " + error.getMessage());
                    Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
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
        // Ocultar la ProgressBar
        progressBar.setVisibility(View.GONE);
        Toast.makeText((getContext()),"Se ha registrado exitosamente!",Toast.LENGTH_SHORT).show();
        txtid.setText("");
        txtcliente.setText("");
        txtcelular.setText("");
        txtdir.setText("");
        txtcodeudor.setText("");
        txtcelcodeudor.setText("");
        btregistrar.setEnabled(true);

    }
    @Override
    public void onErrorResponse(VolleyError error) {
        // Ocultar la ProgressBar
        progressBar.setVisibility(View.GONE);
        // Manejar la respuesta
        Toast.makeText((getContext()),"No se pudo registrar "+error.toString(),Toast.LENGTH_SHORT).show();
        Log.i("ERROR",error.toString());
        btregistrar.setEnabled(true);
    }


}