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
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.amcsoftware.sidebar.Entidades.Proveedores;
import com.amcsoftware.sidebar.adapter.ProveedoresAdapter;
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

public class ProveedoresFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener,SearchView.OnQueryTextListener {
    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;
    private ArrayList<Proveedores> listaProveedores;
    ImageButton btnproveedor;
    JsonObjectRequest jsonObjectRequest;
    private JSONObject jsonObject = null;
    private RecyclerView recyclerProveedores;
    private ProgressBar progressBar;
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
        progressBar     = vista.findViewById(R.id.progressBar);
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
                Toast.makeText(getContext(), "Debe completar todos los campos!", Toast.LENGTH_SHORT).show();
            }else{
                //Confirmar operación
                AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                builder1.setMessage("¿Está seguro de registrar este proveedor?").setTitle("Softpymes");
                builder1.setPositiveButton("Si", (dialog1, which1) -> {
                    nit1        = nit.getText().toString().trim();
                    rsocial1    = rsocial.getText().toString().trim();
                    tel1        = tel.getText().toString().trim();
                    cel1        = cel.getText().toString().trim();
                    dir1        = dir.getText().toString().trim();
                    ciudad1     = ciudad.getText().toString().trim();
                    guardarRegistro();
                });
                builder1.setNegativeButton("No", (dialog1, which1) ->
                        Toast.makeText(getContext(),
                                "Operación cancelada",
                                Toast.LENGTH_SHORT).show());
                AlertDialog dialog1 = builder1.create();
                dialog1.show();//Mostrar
            }
        });

        //Inflar la vista del alert
        builder.setView(viewInflada);
        //builder.show();
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
        btneliminar.setOnClickListener(v->{
            //Confirmar operación
            AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
            builder1.setMessage("¿Está seguro de eliminar este proveedor?").setTitle("Softpymes");
            builder1.setPositiveButton("Si", (dialog1, which1) -> {
                idpr1       = idpr.getText().toString().trim();
                eliminarRegistro();
                alertDialog.dismiss();
            });
            builder1.setNegativeButton("No", (dialog1, which1) ->
                    Toast.makeText(getContext(),
                            "Operación cancelada",
                            Toast.LENGTH_SHORT).show());
            AlertDialog dialog1 = builder1.create();
            dialog1.show();//Mostrar
        });
        //Guardar el nuevo proveedor
        btguardar.setOnClickListener(v->{
            if (nit.getText().toString().isEmpty() || rsocial.getText().toString().isEmpty() ||
                    tel.getText().toString().isEmpty() || cel.getText().toString().isEmpty() || ciudad.getText().toString().isEmpty() ||
                    dir.getText().toString().isEmpty()){
                Toast.makeText(getContext(), "Debe completar todos los campos!", Toast.LENGTH_SHORT).show();
            }else{
                //Confirmar operación
                AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                builder1.setMessage("¿Está seguro de actualizar este proveedor?").setTitle("Softpymes");
                builder1.setPositiveButton("Si", (dialog1, which1) -> {
                    idpr1       = idpr.getText().toString().trim();
                    nit1        = nit.getText().toString().trim();
                    rsocial1    = rsocial.getText().toString().trim();
                    tel1        = tel.getText().toString().trim();
                    cel1        = cel.getText().toString().trim();
                    dir1        = dir.getText().toString().trim();
                    ciudad1     = ciudad.getText().toString().trim();
                    actualizarRegistro();
                });
                builder1.setNegativeButton("No", (dialog1, which1) ->
                        Toast.makeText(getContext(),
                                "Operación cancelada",
                                Toast.LENGTH_SHORT).show());
                AlertDialog dialog1 = builder1.create();
                dialog1.show();//Mostrar
            }
        });

        //Inflar la vista del alert
        builder.setView(viewInflada);
        //builder.show();
        alertDialog = builder.create();
        alertDialog.show();
    }
    //FUNCIONES
    private void actualizarRegistro() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/actualizarProveedor.php";
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
                        Toast.makeText(getContext(), jsonObject.optString("mensaje"), Toast.LENGTH_SHORT).show();
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                    }
                    Toast.makeText(getContext(), jsonObject.optString("mensaje"), Toast.LENGTH_SHORT).show();
                    if (jsonObject.optBoolean("success")) {
                        alertDialog.dismiss();
                        listaProveedores.clear();
                        cargarWebService();
                    }
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
                        Toast.makeText(getContext(), jsonObject.optString("mensaje"), Toast.LENGTH_SHORT).show();
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                    }
                    Toast.makeText(getContext(), jsonObject.optString("mensaje"), Toast.LENGTH_SHORT).show();
                    listaProveedores.clear();
                    cargarWebService();
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
                params.put("idpr", idpr1);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }
    private void guardarRegistro() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/guardarProveedor.php";
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
                        Toast.makeText(getContext(), jsonObject.optString("mensaje"), Toast.LENGTH_SHORT).show();
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                    }
                    Toast.makeText(getContext(), jsonObject.optString("mensaje"), Toast.LENGTH_SHORT).show();
                    if (jsonObject.optBoolean("success")) {
                        alertDialog.dismiss();
                        listaProveedores.clear();
                        cargarWebService();
                    }
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
        // Mostrar la ProgressBar
        progressBar.setVisibility(View.VISIBLE);
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
        // Ocultar la ProgressBar
        progressBar.setVisibility(View.GONE);
        // Manejar la respuesta
        Toast.makeText(getContext(), "No se pudo consultar, ocurrió un error inesperado!" , Toast.LENGTH_LONG).show();
        Log.e("ProveedoresFragment", "Error inesperado en onResponse: " + error.getMessage());

    }

    @Override
    public void onResponse(JSONObject response) {
        Proveedores proveedor;
        JSONArray json = response.optJSONArray("proveedor");
        try {
            final String  mensaje = response.optString("mensaje","");
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
            // Ocultar la ProgressBar
            progressBar.setVisibility(View.GONE);
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
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            // Ocultar la ProgressBar
            progressBar.setVisibility(View.GONE);
            // Manejar la respuesta
            Toast.makeText((getContext()),"No se pudo consultar!",Toast.LENGTH_SHORT).show();
        }

    }


}