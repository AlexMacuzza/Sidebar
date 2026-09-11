package com.amcsoftware.sidebar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import com.amcsoftware.sidebar.Entidades.Proveedores;
import com.amcsoftware.sidebar.adapter.ProveedoresAdapter;
import com.amcsoftware.sidebar.utils.AppUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProveedoresFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener,SearchView.OnQueryTextListener {
    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;
    private ArrayList<Proveedores> listaProveedores;
    ImageButton btnproveedor;
    JsonObjectRequest jsonObjectRequest;
    private JSONObject jsonObject = null;
    private RecyclerView recyclerProveedores;
    private SweetAlertDialog dialogCargando;
    private RequestQueue request, requestQueue;
    private ProveedoresAdapter adapter;
    String idpr1,nit1,rsocial1,tel1,cel1,dir1,ciudad1;
    SearchView txtbuscar;//buscador

    public ProveedoresFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista =  inflater.inflate(R.layout.fragment_proveedores, container, false);
        btnproveedor = vista.findViewById(R.id.btnproveedor);
        txtbuscar    = vista.findViewById(R.id.txtbuscar);
        listaProveedores    = new ArrayList<>();
        recyclerProveedores =  vista.findViewById(R.id.idRecycler);
        recyclerProveedores.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerProveedores.setHasFixedSize(true);
        adapter = new ProveedoresAdapter(listaProveedores);
        request         = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET
        requestQueue = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo POST

        cargarWebService();

        txtbuscar.setOnQueryTextListener(this);

        builder = new AlertDialog.Builder(requireContext());

        btnproveedor.setOnClickListener(v -> intputboxNProveedor());

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

    private void intputboxNProveedor() {
        builder.setTitle("");
        // Inflar el layout personalizado con el EditText
        View viewInflada = LayoutInflater.from(getContext()).inflate(R.layout.dialog_input_proveedor, null);
        //Instanciar objetos
        final TextView idpr       = viewInflada.findViewById(R.id.lblidpr);
        final TextView nit        = viewInflada.findViewById(R.id.txtnit);
        final EditText rsocial    = viewInflada.findViewById(R.id.txtrsocial);
        final EditText tel        = viewInflada.findViewById(R.id.txttel);
        final EditText cel        = viewInflada.findViewById(R.id.txtcel);
        final EditText dir        = viewInflada.findViewById(R.id.txtdir);
        final EditText ciudad     = viewInflada.findViewById(R.id.txtciudad);
        final ImageButton btcerrar   = viewInflada.findViewById(R.id.btcerrar);
        final ImageButton btlimpiar  = viewInflada.findViewById(R.id.btlimpiar);
        final ImageButton btguardar  = viewInflada.findViewById(R.id.btguardar);
        final ImageButton btneliminar = viewInflada.findViewById(R.id.bteliminar);
        btneliminar.setVisibility(View.GONE);
        //cerrar el alert
        btcerrar.setOnClickListener(v-> alertDialog.dismiss());
        //limpiar campos
        btlimpiar.setOnClickListener(v->{
            idpr.setText("");
            nit.setText("");
            rsocial.setText("");
            tel.setText("");
            cel.setText("");
            dir.setText("");
            ciudad.setText("");
            nit.requestFocus();
        });
        //Guardar el nuevo proveedor
        btguardar.setOnClickListener(v->{
            if (nit.getText().toString().isEmpty() || rsocial.getText().toString().isEmpty() ||
                    tel.getText().toString().isEmpty() || cel.getText().toString().isEmpty() || ciudad.getText().toString().isEmpty() ||
                    dir.getText().toString().isEmpty()){
                AppUtils.alertAdvertencia(requireContext(), "Campos incompletos", "Debe completar todos los campos.");
            }else{
                //Confirmar operación
                AppUtils.alertConfirmar(requireContext(), "Softpymes",
                        "¿Está seguro de registrar este proveedor?", "Sí", "No", () -> {
                    nit1        = nit.getText().toString().trim();
                    rsocial1    = rsocial.getText().toString().trim();
                    tel1        = tel.getText().toString().trim();
                    cel1        = cel.getText().toString().trim();
                    dir1        = dir.getText().toString().trim();
                    ciudad1     = ciudad.getText().toString().trim();
                    guardarRegistro();
                });
            }
        });

        //Inflar la vista del alert
        builder.setView(viewInflada);
        alertDialog = builder.create();
        alertDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void intputboxActProveedor() {
        builder.setTitle("");
        // Inflar el layout personalizado con el EditText
        View viewInflada = LayoutInflater.from(getContext()).inflate(R.layout.dialog_input_proveedor, null);
        //Instanciar objetos
        final TextView lbltitulo  = viewInflada.findViewById(R.id.lbltitle);
        final TextView idpr       = viewInflada.findViewById(R.id.lblidpr);
        final TextView nit        = viewInflada.findViewById(R.id.txtnit);
        final EditText rsocial    = viewInflada.findViewById(R.id.txtrsocial);
        final EditText tel        = viewInflada.findViewById(R.id.txttel);
        final EditText cel        = viewInflada.findViewById(R.id.txtcel);
        final EditText dir        = viewInflada.findViewById(R.id.txtdir);
        final EditText ciudad     = viewInflada.findViewById(R.id.txtciudad);
        final ImageButton btcerrar  = viewInflada.findViewById(R.id.btcerrar);
        final ImageButton btlimpiar = viewInflada.findViewById(R.id.btlimpiar);
        final ImageButton btguardar = viewInflada.findViewById(R.id.btguardar);
        final ImageButton btneliminar = viewInflada.findViewById(R.id.bteliminar);
        lbltitulo.setText("Actualizar Proveedor");
        btneliminar.setVisibility(View.VISIBLE);
        //Asignar datos del recyclerview
        idpr.setText(idpr1);
        nit.setText(nit1);
        rsocial.setText(rsocial1);
        tel.setText(tel1);
        cel.setText(cel1);
        dir.setText(dir1);
        ciudad.setText(ciudad1);
        //cerrar el alert
        btcerrar.setOnClickListener(v-> alertDialog.dismiss());
        //limpiar campos
        btlimpiar.setOnClickListener(v->{
            nit.setText("");
            rsocial.setText("");
            tel.setText("");
            cel.setText("");
            dir.setText("");
            ciudad.setText("");
            nit.requestFocus();
        });
        //Eliminar proveedor
        btneliminar.setOnClickListener(v->
            //Confirmar operación
            AppUtils.alertConfirmar(requireContext(), "Softpymes",
                    "¿Está seguro de eliminar este proveedor?", "Sí", "No", () -> {
                idpr1       = idpr.getText().toString().trim();
                eliminarRegistro();
                alertDialog.dismiss();
            }));
        //Guardar el nuevo proveedor
        btguardar.setOnClickListener(v->{
            if (nit.getText().toString().isEmpty() || rsocial.getText().toString().isEmpty() ||
                    tel.getText().toString().isEmpty() || cel.getText().toString().isEmpty() || ciudad.getText().toString().isEmpty() ||
                    dir.getText().toString().isEmpty()){
                AppUtils.alertAdvertencia(requireContext(), "Campos incompletos", "Debe completar todos los campos.");
            }else{
                //Confirmar operación
                AppUtils.alertConfirmar(requireContext(), "Softpymes",
                        "¿Está seguro de actualizar este proveedor?", "Sí", "No", () -> {
                    idpr1       = idpr.getText().toString().trim();
                    nit1        = nit.getText().toString().trim();
                    rsocial1    = rsocial.getText().toString().trim();
                    tel1        = tel.getText().toString().trim();
                    cel1        = cel.getText().toString().trim();
                    dir1        = dir.getText().toString().trim();
                    ciudad1     = ciudad.getText().toString().trim();
                    actualizarRegistro();
                });
            }
        });

        //Inflar la vista del alert
        builder.setView(viewInflada);
        alertDialog = builder.create();
        alertDialog.show();
    }
    //FUNCIONES
    private void actualizarRegistro() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/actualizarProveedor.php";
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Actualizando...", "Por favor espera.");
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    boolean ok = false;
                    String msj = "No se pudo procesar la respuesta del servidor.";
                    try {
                        jsonObject = new JSONObject(response);
                        msj = jsonObject.optString("mensaje", msj);
                        ok = jsonObject.optBoolean("success");
                    } catch (JSONException e) {
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                    }
                    if (ok) {
                        AppUtils.alertExito(requireContext(), "Éxito", msj);
                        alertDialog.dismiss();
                        listaProveedores.clear();
                        cargarWebService();
                    } else {
                        AppUtils.alertError(requireContext(), "Atención", msj);
                    }
                },
                error -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    AppUtils.alertError(requireContext(), "Error de red", "No se pudo actualizar el proveedor.");
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("idpr", idpr1);
                params.put("nit", nit1);
                params.put("rsocial",rsocial1);
                params.put("tel",tel1);
                params.put("cel", cel1);
                params.put("dir", dir1);
                params.put("ciudad", ciudad1);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }
    private void eliminarRegistro(){
        String url = "https://www.wmcsoftware.net/apps/softpymes/eliminarProveedor.php";
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Eliminando...", "Por favor espera.");
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    boolean ok = true;
                    String msj = "Proveedor eliminado.";
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
                    listaProveedores.clear();
                    cargarWebService();
                },
                error -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    AppUtils.alertError(requireContext(), "Error de red", "No se pudo eliminar el proveedor.");
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("idpr", idpr1);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }
    private void guardarRegistro() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/guardarProveedor.php";
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Guardando...", "Por favor espera.");
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    boolean ok = false;
                    String msj = "No se pudo procesar la respuesta del servidor.";
                    try {
                        jsonObject = new JSONObject(response);
                        msj = jsonObject.optString("mensaje", msj);
                        ok = jsonObject.optBoolean("success");
                    } catch (JSONException e) {
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                    }
                    if (ok) {
                        AppUtils.alertExito(requireContext(), "Éxito", msj);
                        alertDialog.dismiss();
                        listaProveedores.clear();
                        cargarWebService();
                    } else {
                        AppUtils.alertError(requireContext(), "Atención", msj);
                    }
                },
                error -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    AppUtils.alertError(requireContext(), "Error de red", "No se pudo guardar el proveedor.");
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("nit", nit1);
                params.put("rsocial",rsocial1);
                params.put("tel",tel1);
                params.put("cel", cel1);
                params.put("dir", dir1);
                params.put("ciudad", ciudad1);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

    private void cargarWebService() {
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Cargando...", "Por favor espera.");
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaProveedores.php";
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String Text) {
        adapter.filtrado(Text);
        return false;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        AppUtils.cerrarCargando(dialogCargando);
        AppUtils.alertError(requireContext(), "Error", "No se pudo consultar los proveedores.");
        Log.e("ProveedoresFragment", "Error inesperado en onResponse: " + error.getMessage());
    }

    @Override
    public void onResponse(JSONObject response) {
        Proveedores proveedor;
        JSONArray json = response.optJSONArray("proveedor");
        try {
            for (int i = 0; i< (json != null ? json.length() : 0); i++){
                proveedor = new Proveedores();
                JSONObject jsonObject;
                jsonObject = json.getJSONObject(i);

                proveedor.setIdpr(jsonObject.optString("idpr"));
                proveedor.setNit(jsonObject.optString("nit"));
                proveedor.setRsocial(jsonObject.optString("rsocial"));
                proveedor.setTelefono(jsonObject.optString("telefono"));
                proveedor.setCelular(jsonObject.optString("celular"));
                proveedor.setDireccion(jsonObject.optString("direccion"));
                proveedor.setCiudad(jsonObject.optString("ciudad"));
                listaProveedores.add(proveedor);
            }
            AppUtils.cerrarCargando(dialogCargando);
            adapter = new ProveedoresAdapter(listaProveedores);
            adapter.setOnClickListener(v->{
                idpr1     = listaProveedores.get(recyclerProveedores.getChildAdapterPosition(v)).getIdpr();
                nit1      = listaProveedores.get(recyclerProveedores.getChildAdapterPosition(v)).getNit();
                rsocial1  = listaProveedores.get(recyclerProveedores.getChildAdapterPosition(v)).getRsocial();
                tel1      = listaProveedores.get(recyclerProveedores.getChildAdapterPosition(v)).getTelefono();
                cel1      = listaProveedores.get(recyclerProveedores.getChildAdapterPosition(v)).getCelular();
                dir1      = listaProveedores.get(recyclerProveedores.getChildAdapterPosition(v)).getDireccion();
                ciudad1   = listaProveedores.get(recyclerProveedores.getChildAdapterPosition(v)).getCiudad();
                intputboxActProveedor();
            });
            recyclerProveedores.setAdapter(adapter);
        } catch (JSONException e) {
            AppUtils.cerrarCargando(dialogCargando);
            AppUtils.alertError(requireContext(), "Error", "No se pudo consultar los proveedores.");
        }

    }


}
