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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;


public class EditarClienteFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener {
    //Variables locales
    EditText nombre,cedula,celular,dir,codeudor,celcode,obs;
    ImageButton btactualizar,bteliminar;
    JSONObject jsonObject = null;
    TextView idcl;
    // Declarar la ProgressBar
    ProgressBar progressBar;
    RequestQueue request, requestQueue;


    public EditarClienteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista      =  inflater.inflate(R.layout.fragment_editar_cliente, container, false);

        idcl   = vista.findViewById(R.id.lblidcl);
        cedula = vista.findViewById(R.id.txtid);
        nombre = vista.findViewById(R.id.txtcliente);
        celular  = vista.findViewById(R.id.txtcelular);
        dir      = vista.findViewById(R.id.txtdir);
        codeudor = vista.findViewById(R.id.txtcodeudor);
        celcode  = vista.findViewById(R.id.txtcelcodeudor);
        obs      = vista.findViewById(R.id.txtobservacion);
        btactualizar  =  vista.findViewById(R.id.bteditar);
        bteliminar    =  vista.findViewById(R.id.bteliminar);

        progressBar     =  vista.findViewById(R.id.progressBar);

        request      = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET
        requestQueue = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo POST


        assert getArguments() != null;
        idcl.setText(getArguments().getString("idcl"));
        cedula.setText(getArguments().getString("idc"));
        nombre.setText(getArguments().getString("nombre"));
        celular.setText(getArguments().getString("celular"));
        dir.setText(getArguments().getString("dir"));
        codeudor.setText(getArguments().getString("codeudor"));
        celcode.setText(getArguments().getString("celcode"));
        obs.setText(getArguments().getString("obs"));

        //Evento click
        btactualizar.setOnClickListener(v -> actualizarDatos());

        bteliminar.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());//Alert de confirmación
            builder.setMessage("Está seguro de eliminar este cliente?").setTitle("Softpymes");

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
        String url = "https://www.wmcsoftware.net/apps/softpymes/eliminarCliente.php";
        // Obtener los valores ingresados
        final String idcl1 = idcl.getText().toString().trim();
        // Validar que los campos no estén vacíos
        if (idcl1.isEmpty() ) {
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
                params.put("idc", idcl1);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

    private void actualizarDatos() {
            String url = "https://www.wmcsoftware.net/apps/softpymes/actualizarCliente.php";
            // Obtener los valores ingresados
            final String idcl1        = idcl.getText().toString().trim();
            final String cedula1      = cedula.getText().toString().trim();
            final String nombre1      = nombre.getText().toString().trim();
            final String dir1         = dir.getText().toString().trim();
            final String celular1     = celular.getText().toString().trim();
            final String codeudor1    = codeudor.getText().toString().trim();
            final String celularcode1 = celcode.getText().toString().trim();
            final String obs1         = obs.getText().toString().trim();

            // Validar que los campos no estén vacíos
            if (idcl1.isEmpty() || nombre1.isEmpty() || dir1.isEmpty()|| celular1.isEmpty() || codeudor1.isEmpty() || celularcode1.isEmpty()) {
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
                            Toast.makeText(getContext(),"mensaje 1:"+e, Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(getContext(), jsonObject.optString("mensaje"), Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        // Ocultar ProgressBar
                        progressBar.setVisibility(View.GONE);
                        // Manejar errores
                        Toast.makeText(getContext(), "mensaje 2:"+error.toString(), Toast.LENGTH_LONG).show();
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    // Enviar los parámetros al servidor
                    Map<String, String> params = new HashMap<>();
                    params.put("idc", idcl1);
                    params.put("id", cedula1);
                    params.put("nombre", nombre1);
                    params.put("dir", dir1);
                    params.put("celular", celular1);
                    params.put("codeudor", codeudor1);
                    params.put("celularcode", celularcode1);
                    params.put("obs", obs1);
                    return params;
                }
            };

            // Agregar la solicitud a la cola
            requestQueue.add(stringRequest);
        }


    private void limpiar() {
        idcl.setText("");
        cedula.setText("");
        nombre.setText("");
        dir.setText("");
        celular.setText("");
        codeudor.setText("");
        celcode.setText("");
        obs.setText("");
    }

    @Override
    public void onResponse(JSONObject response) {
        // Ocultar la ProgressBar
        progressBar.setVisibility(View.GONE);
        JSONArray json = response.optJSONArray("reponse");
        try {
            JSONObject jsonObject;
            assert json != null;
            jsonObject = json.getJSONObject(0);
            final String rta = jsonObject.optString("mensaje");
            Toast.makeText((getContext()),rta,Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Toast.makeText((getContext()),"No se pudo actualizar!",Toast.LENGTH_SHORT).show();
        }


    }
        @Override
    public void onErrorResponse(VolleyError error) {
            // Ocultar la ProgressBar
            progressBar.setVisibility(View.GONE);
            // Manejar la respuesta
            Toast.makeText((getContext()),"No se pudo actualizar. "+error.toString(),Toast.LENGTH_SHORT).show();
    }


}