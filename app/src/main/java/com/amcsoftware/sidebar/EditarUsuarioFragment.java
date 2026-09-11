package com.amcsoftware.sidebar;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
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

public class EditarUsuarioFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener, AdapterView.OnItemSelectedListener {
    //Variables locales
    ArrayAdapter<String> adapter;
    EditText usuario,clave,cclave;
    ImageButton btactualizar,bteliminar;
    int position;
    JSONObject jsonObject = null;
    SweetAlertDialog dialogCargando;
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

        bteliminar.setOnClickListener(v ->
                AppUtils.alertConfirmar(requireContext(), "Softpymes",
                        "¿Está seguro de eliminar este usuario?", "Sí", "No",
                        this::eliminarDatos));

        //Controlar botón atrás
        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
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
            AppUtils.alertAdvertencia(requireContext(), "Campos incompletos",
                    "Por favor, complete todos los campos.");
            return;
        } else if (!clave1.equals(clave2)) {
            AppUtils.alertAdvertencia(requireContext(), "Atención", "Las claves no coinciden.");
            return;
        }
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Actualizando...", "Por favor espera.");
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    String msj = "Usuario actualizado.";
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
                    AppUtils.alertError(requireContext(), "Error de red", "No se pudo actualizar el usuario.");
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
            AppUtils.alertAdvertencia(requireContext(), "Dato requerido", "El id no puede estar vacío.");
            return;
        }
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Eliminando...", "Por favor espera.");
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    String msj = "Usuario eliminado.";
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
                    AppUtils.alertError(requireContext(), "Error de red", "No se pudo eliminar el usuario.");
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
