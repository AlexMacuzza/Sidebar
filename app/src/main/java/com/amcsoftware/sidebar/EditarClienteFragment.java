package com.amcsoftware.sidebar;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.amcsoftware.sidebar.utils.AppUtils;
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

import cn.pedant.SweetAlert.SweetAlertDialog;


public class EditarClienteFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener {
    //Variables locales
    EditText nombre,cedula,celular,dir,codeudor,celcode,obs;
    ImageButton btactualizar,bteliminar;
    JSONObject jsonObject = null;
    TextView idcl;
    SweetAlertDialog dialogCargando;
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

        bteliminar.setOnClickListener(v ->
                AppUtils.alertConfirmar(requireContext(), "Softpymes",
                        "¿Está seguro de eliminar este cliente?", "Sí", "No",
                        this::eliminarDatos));

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

    private void eliminarDatos() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/eliminarCliente.php";
        // Obtener los valores ingresados
        final String idcl1 = idcl.getText().toString().trim();
        // Validar que los campos no estén vacíos
        if (idcl1.isEmpty() ) {
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
                    // Manejar la respuesta del servidor
                    String msj = "Cliente eliminado.";
                    boolean ok = true;
                    try {
                        jsonObject = new JSONObject(response);
                        msj = jsonObject.optString("mensaje", msj);
                        ok = jsonObject.optBoolean("success", true);
                    } catch (JSONException e) {
                        ok = false;
                        msj = "No se pudo procesar la respuesta del servidor.";
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
                    AppUtils.alertError(requireContext(), "Error de red", "No se pudo eliminar el cliente.");
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
                AppUtils.alertAdvertencia(requireContext(), "Campos incompletos",
                        "Por favor, complete todos los campos.");
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
                        // Manejar la respuesta del servidor
                        String msj = "Cliente actualizado.";
                        boolean ok = true;
                        try {
                            jsonObject = new JSONObject(response);
                            msj = jsonObject.optString("mensaje", msj);
                            ok = jsonObject.optBoolean("success", true);
                        } catch (JSONException e) {
                            ok = false;
                            msj = "No se pudo procesar la respuesta del servidor.";
                        }
                        if (ok) {
                            AppUtils.alertExito(requireContext(), "Éxito", msj);
                        } else {
                            AppUtils.alertError(requireContext(), "Atención", msj);
                        }
                    },
                    error -> {
                        AppUtils.cerrarCargando(dialogCargando);
                        AppUtils.alertError(requireContext(), "Error de red", "No se pudo actualizar el cliente.");
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
        AppUtils.cerrarCargando(dialogCargando);
        JSONArray json = response.optJSONArray("reponse");
        try {
            JSONObject jsonObject;
            assert json != null;
            jsonObject = json.getJSONObject(0);
            final String rta = jsonObject.optString("mensaje");
            AppUtils.alertExito(requireContext(), "Éxito", rta);
        } catch (JSONException e) {
            AppUtils.alertError(requireContext(), "Error", "No se pudo actualizar el cliente.");
        }
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        AppUtils.cerrarCargando(dialogCargando);
        AppUtils.alertError(requireContext(), "Error", "No se pudo actualizar el cliente.");
    }


}
