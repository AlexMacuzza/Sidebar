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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class EditarVendedorFragment extends Fragment {
    //Variables locales
    EditText nombre,cedula,celular,dir;
    ImageButton btactualizar,bteliminar;
    JSONObject jsonObject = null;
    TextView idvd;
    // Declarar la ProgressBar
    ProgressBar progressBar;
    RequestQueue request, requestQueue;

    public EditarVendedorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_editar_vendedor, container, false);
        idvd   = vista.findViewById(R.id.lblidvd);
        cedula = vista.findViewById(R.id.txtcedvd1);
        nombre = vista.findViewById(R.id.txtvendedor1);
        celular  = vista.findViewById(R.id.txtcelvd1);
        dir      = vista.findViewById(R.id.txtdirvd1);
        btactualizar  =  vista.findViewById(R.id.bteditar);
        bteliminar    =  vista.findViewById(R.id.bteliminar);
        progressBar     =  vista.findViewById(R.id.progressBar);
        request      = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET
        requestQueue = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo POST

        assert getArguments() != null;
        idvd.setText(getArguments().getString("idvd"));
        cedula.setText(getArguments().getString("cedula"));
        nombre.setText(getArguments().getString("nombre"));
        celular.setText(getArguments().getString("celular"));
        dir.setText(getArguments().getString("dir"));

        btactualizar.setOnClickListener(v -> actualizarDatos());

        bteliminar.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());//Alert de confirmación
            builder.setMessage("Está seguro de eliminar este vendedor?").setTitle("Softpymes");

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
        String url = "https://www.wmcsoftware.net/apps/softpymes/eiliminarVendedor.php";
        // Obtener los valores ingresados
        final String idvd1 = idvd.getText().toString().trim();
        // Validar que los campos no estén vacíos
        if (idvd1.isEmpty() ) {
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
                params.put("idvd", idvd1);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

    private void limpiar() {
        idvd.setText("");
        cedula.setText("");
        nombre.setText("");
        celular.setText("");
        dir.setText("");
    }

    private void actualizarDatos() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/actualizarVendedor.php";
        // Obtener los valores ingresados
        final String idvd1        = idvd.getText().toString().trim();
        final String cedula1      = cedula.getText().toString().trim();
        final String nombre1      = nombre.getText().toString().trim();
        final String dir1         = dir.getText().toString().trim();
        final String celular1     = celular.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (idvd1.isEmpty() || nombre1.isEmpty() || dir1.isEmpty()|| celular1.isEmpty()) {
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
                params.put("idvd", idvd1);
                params.put("cedula", cedula1);
                params.put("nombre", nombre1);
                params.put("dir", dir1);
                params.put("celular", celular1);
                return params;
            }
        };

        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }
}