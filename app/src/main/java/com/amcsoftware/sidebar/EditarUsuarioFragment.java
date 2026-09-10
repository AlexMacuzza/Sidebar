package com.amcsoftware.sidebar;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

public class EditarUsuarioFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener, AdapterView.OnItemSelectedListener {
    //Variables locales
    ArrayAdapter<String> adapter;
    EditText usuario,clave,cclave;
    ImageButton btactualizar,bteliminar;
    int position;
    JSONObject jsonObject = null;
    ProgressBar progressBar;
    RequestQueue requestQueue;
    Spinner spnrperfil;//combobox
    String perfil;
    TextView iduser;

    public EditarUsuarioFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View vista = inflater.inflate(R.layout.fragment_editar_usuario, container, false);
        iduser       =  vista.findViewById(R.id.lbliduser);
        usuario      =  vista.findViewById(R.id.txtuser1);
        clave        =  vista.findViewById(R.id.txtclave1);
        cclave       =  vista.findViewById(R.id.txtcclave1);
        btactualizar  =  vista.findViewById(R.id.bteditar);
        spnrperfil   =  vista.findViewById(R.id.spnrperfil);
        bteliminar    =  vista.findViewById(R.id.bteliminar);
        progressBar     =  vista.findViewById(R.id.progressBar);
        requestQueue = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo POST
        // Obtener el adaptador del Spinner
        adapter = (ArrayAdapter<String>) spnrperfil.getAdapter();
        spnrperfil.setOnItemSelectedListener(this);//Selección de un item del spinner

        assert getArguments() != null;
        iduser.setText(getArguments().getString("idu"));
        usuario.setText(getArguments().getString("usuario"));
        perfil = getArguments().getString("perfil");

        // Buscar el índice del valor que quieres asignar
        position = adapter.getPosition(perfil);
        // Asignar el valor al Spinner
        spnrperfil.setSelection(position);

        btactualizar.setOnClickListener(v -> actualizarDatos());

        bteliminar.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());//Alert de confirmación
            builder.setMessage("Está seguro de eliminar este usuario?").setTitle("Softpymes");

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

      return  vista;
    }

    private void actualizarDatos(){
        String url = "https://www.wmcsoftware.net/apps/softpymes/actualizarUsuario.php";
        // Obtener los valores ingresados
        final String iduser1   = iduser.getText().toString().trim();
        final String usuario1  = usuario.getText().toString().trim();
        final String clave1    = clave.getText().toString().trim();
        final String clave2    = cclave.getText().toString().trim();
        final String perfil1   = perfil;

        // Validar que los campos no estén vacíos
        if (usuario1.isEmpty() || clave1.isEmpty() || clave2.isEmpty() || perfil1.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        } else if (!clave1.equals(clave2)) {
            Toast.makeText(getContext(), "Las claves no coinciden.", Toast.LENGTH_SHORT).show();
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
                params.put("idu", iduser1);
                params.put("nombre_usuario", usuario1);
                params.put("clave", clave1);
                params.put("perfil", perfil1);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

    private void eliminarDatos() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/eliminarUsuario.php";
        // Obtener los valores ingresados
        final String iduser1 = iduser.getText().toString().trim();
        // Validar que los campos no estén vacíos
        if (iduser1.isEmpty() ) {
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
                params.put("iduser", iduser1);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(JSONObject response) {

    }
    private void limpiar() {
        iduser.setText("");
        usuario.setText("");
        clave.setText("");
        cclave.setText("");
       // Buscar el índice del valor que quieres asignar
        position = adapter.getPosition("");
        // Asignar el valor al Spinner
        spnrperfil.setSelection(position);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        perfil  = spnrperfil.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        perfil = "";
    }
}
