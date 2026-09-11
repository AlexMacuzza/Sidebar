package com.amcsoftware.sidebar;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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

public class NuevoUsuarioFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener,
        AdapterView.OnItemSelectedListener, View.OnFocusChangeListener {
    //Variables locales
    EditText txtusuario,txtclave,txtcclave,txtpermisos,txtsucursal;
    ImageButton btguardar;
    JSONObject jsonObject = null;
    SweetAlertDialog dialogCargando;
    RequestQueue requestQueue;
    Spinner spnrperfil;//combobox
    String perfil;

    public NuevoUsuarioFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista   = inflater.inflate(R.layout.fragment_nuevo_usuario, container, false);
        txtusuario   =  vista.findViewById(R.id.txtuser);
        txtclave     =  vista.findViewById(R.id.txtclave);
        txtcclave    =  vista.findViewById(R.id.txtcclave);
        spnrperfil   =  vista.findViewById(R.id.spnrperfil);
        txtpermisos  =  vista.findViewById(R.id.txtpermisos);
        txtsucursal  =  vista.findViewById(R.id.txtsuc);
        btguardar    =  vista.findViewById(R.id.btregistrar);

        requestQueue = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo POST

        spnrperfil.setOnItemSelectedListener(this);//Selección de un item del spinner
        spnrperfil.setOnFocusChangeListener(this);//Detecta cuando pierde el foco
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
        String url = "https://www.wmcsoftware.net/apps/softpymes/guardarUsuario.php";
        // Obtener los valores ingresados
        final String usuario1  = txtusuario.getText().toString().trim();
        final String clave1    = txtclave.getText().toString().trim();
        final String clave2    = txtcclave.getText().toString().trim();
        final String perfil1   = perfil;
        final String permisos1 = txtpermisos.getText().toString().trim();
        final String sucursal1 = txtsucursal.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (usuario1.isEmpty() || clave1.isEmpty() || clave2.isEmpty() || perfil1.isEmpty() || permisos1.isEmpty()) {
            AppUtils.alertAdvertencia(requireContext(), "Campos incompletos", "Por favor, complete todos los campos.");
            return;
        } else if (!clave1.equals(clave2)) {
            AppUtils.alertAdvertencia(requireContext(), "Atención", "Las claves no coinciden.");
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
                    String msj = "Usuario guardado.";
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
                    AppUtils.alertError(requireContext(), "Error de red", "No se pudo guardar el usuario.");
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("usuario", usuario1);
                params.put("clave", clave1);
                params.put("permisos", permisos1);
                params.put("perfil", perfil1);
                params.put("sucursal",sucursal1);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

    private void limpiar() {
        txtusuario.setText("");
        txtclave.setText("");
        txtcclave.setText("");
        spnrperfil.setOnItemSelectedListener(this);
        txtpermisos.setText("");
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(JSONObject response) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
      perfil  = spnrperfil.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        perfil = "";
    }

    public void onFocusChange(View v, boolean hasFocus) {

    }
}