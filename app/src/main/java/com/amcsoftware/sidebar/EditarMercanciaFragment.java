package com.amcsoftware.sidebar;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class EditarMercanciaFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener {
    //Variables locales
    EditText desc, precioc, preciov, cantidad;
    ImageButton btactualizar, bteliminar;
    JSONObject jsonObject = null;
    TextView idp;
    // Declarar la ProgressBar
    ProgressBar progressBar;
    RequestQueue request, requestQueue;

    public EditarMercanciaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vista = inflater.inflate(R.layout.fragment_editar_mercancia, container, false);
        idp = vista.findViewById(R.id.lblidp);
        desc = vista.findViewById(R.id.txtdesc);
        precioc = vista.findViewById(R.id.txtprecioc);
        preciov = vista.findViewById(R.id.txtpreciov);
        cantidad = vista.findViewById(R.id.txtcantidad);
        btactualizar = vista.findViewById(R.id.bteditar);
        bteliminar = vista.findViewById(R.id.bteliminar);
        progressBar = vista.findViewById(R.id.progressBar);

        request = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET
        requestQueue = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo POST

        assert getArguments() != null;
        idp.setText(getArguments().getString("idp"));
        desc.setText(getArguments().getString("desc"));
        precioc.setText(getArguments().getString("precioc"));
        preciov.setText(getArguments().getString("preciov"));
        cantidad.setText(getArguments().getString("cantidad"));

        btactualizar.setOnClickListener(v -> actualizarDatos());

        bteliminar.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());//Alert de confirmación
            builder.setMessage("Está seguro de eliminar esta mercancia?").setTitle("Softpymes");

            builder.setPositiveButton("Si", (dialog, which) -> eliminarDatos());

            builder.setNegativeButton("No", (dialog, which) ->
                    Toast.makeText(getContext(),
                            "Eliminación cancelada",
                            Toast.LENGTH_SHORT).show());

            AlertDialog dialog = builder.create();
            dialog.show();//Mostrar el Alert
        });

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

    private void eliminarDatos() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/eliminarMercancia.php";
        // Obtener los valores ingresados
        final String idp1 = idp.getText().toString().trim();
        // Validar que los campos no estén vacíos
        if (idp1.isEmpty()) {
            Toast.makeText(getContext(), "El id no puede estar vacío.", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getContext(), jsonObject.optString("mensaje"), Toast.LENGTH_SHORT).show();
                    limpiar();

                },
                error -> {
                    // Ocultar ProgressBar
                    progressBar.setVisibility(View.GONE);
                    // Manejar errores
                    Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("idp", idp1);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);

    }

    private void limpiar() {
        idp.setText("");
        desc.setText("");
        precioc.setText("");
        preciov.setText("");
        cantidad.setText("");

    }

    private void actualizarDatos() {
        {
            String url = "https://www.wmcsoftware.net/apps/softpymes/actualizarMercancia.php";
            // Obtener los valores ingresados
            final String idp1       = idp.getText().toString().trim();
            final String desc1      = desc.getText().toString().trim();
            final String precioc1   = precioc.getText().toString().trim();
            final String preciov1   = preciov.getText().toString().trim();
            final String cantidad1  = cantidad.getText().toString().trim();

            // Validar que los campos no estén vacíos
            if (idp1.isEmpty() || desc1.isEmpty() || precioc1.isEmpty() || preciov1.isEmpty() || cantidad1.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Mostrar la ProgressBar
            progressBar.setVisibility(View.VISIBLE);
            // Crear la solicitud POST
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    url,
                    response -> {
                        // Ocultar ProgressBar
                        progressBar.setVisibility(View.GONE);
                        // Manejar la respuesta del servidor
                        try {
                            jsonObject = new JSONObject(response);
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(getContext(), jsonObject.optString("mensaje"), Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        // Ocultar ProgressBar
                        progressBar.setVisibility(View.GONE);
                        // Manejar errores
                        Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    // Enviar los parámetros al servidor
                    Map<String, String> params = new HashMap<>();
                    params.put("idp", idp1);
                    params.put("detalle", desc1);
                    params.put("precioc", precioc1);
                    params.put("preciov", preciov1);
                    params.put("cantidad", cantidad1);
                    return params;
                }
            };

            // Agregar la solicitud a la cola
            requestQueue.add(stringRequest);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(JSONObject response) {

    }
}