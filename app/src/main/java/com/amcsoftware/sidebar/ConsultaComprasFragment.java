package com.amcsoftware.sidebar;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;
import com.amcsoftware.sidebar.Entidades.Factura_Compras;
import com.amcsoftware.sidebar.Entidades.Proveedores;
import com.amcsoftware.sidebar.adapter.FacturaComprasAdapter;
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

public class ConsultaComprasFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener,
        SearchView.OnQueryTextListener {
    ArrayList<Factura_Compras> listafacturacompras;
    ArrayList<Proveedores> listaProveedores;
    JSONObject jsonObject = null;
    JsonObjectRequest jsonObjectRequest;
    ImageButton btncompra;
    FacturaComprasAdapter adapter;
    ProveedoresAdapter adapter1;
    ProgressBar progressBar;
    RecyclerView recyclerFacturaCompras;
    RequestQueue request, requestQueue;
    SearchView txtbuscar;//buscador
    String nit,idfc;
    String [] vproveedores;

    public ConsultaComprasFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_consulta_compras, container, false);
        btncompra   = vista.findViewById(R.id.btncompra);
        txtbuscar        = vista.findViewById(R.id.txtbuscarc);
        listafacturacompras = new ArrayList<>();
        listaProveedores    = new ArrayList<>();
        recyclerFacturaCompras =  vista.findViewById(R.id.idRecycler);
        recyclerFacturaCompras.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerFacturaCompras.setHasFixedSize(true);
        adapter    = new FacturaComprasAdapter(listafacturacompras);
        adapter1   = new ProveedoresAdapter(listaProveedores);

        progressBar     = vista.findViewById(R.id.progressBar);
        request         = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET
        requestQueue    = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo POST
        adapter         = new FacturaComprasAdapter(listafacturacompras);
        nit            = " ";

        cargarWebService();

        txtbuscar.setOnQueryTextListener(this);
        txtbuscar.requestFocus();

        btncompra.setOnClickListener(v-> Navigation.findNavController(v).navigate(R.id.nuevaCompra));

        //Controlar botón atrás
        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                //Toast.makeText(requireContext(), "Botón en MyFragment", Toast.LENGTH_LONG).show();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        // Inflar la vista del fragment
        return vista;
    }

    private void cargarWebService() {
        // Mostrar la ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://www.wmcsoftware.net/apps/softpymes/consultaFacturaCompras.php";
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String text) {
        adapter.filtrado(text);
        return false;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // Ocultar la ProgressBar
        progressBar.setVisibility(View.GONE);
        // Manejar la respuesta
        Toast.makeText((getContext()),"No se pudo actualizar "+error.toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(JSONObject response) {
        Factura_Compras factura_compras;
        JSONArray json = response.optJSONArray("fcompras");
        JSONArray json1 = response.optJSONArray("proveedor");
        try {
            for (int i = 0; i< (json != null ? json.length() : 0); i++){
                factura_compras = new Factura_Compras();
                JSONObject jsonObject;
                jsonObject = json.getJSONObject(i);
                factura_compras.setIdfc(jsonObject.optString("idfc"));
                factura_compras.setProveedor(jsonObject.optString("proveedor"));
                factura_compras.setFecha(jsonObject.optString("fecha"));
                factura_compras.setObservacion(jsonObject.optString("detalle"));
                factura_compras.setMonto(jsonObject.optString("monto"));
                factura_compras.setSaldo(jsonObject.optString("saldo"));
                listafacturacompras.add(factura_compras);
            }

            vproveedores = new String[json1 != null ? json1.length() : 0];
            for (int i = 0; i< (json1 != null ? json1.length() : 0); i++){
                JSONObject jsonObject1;
                jsonObject1 = json1.getJSONObject(i);
                vproveedores[i]=jsonObject1.optString("nit")+"-"+jsonObject1.optString("rsocial");
            }

            // Ocultar la ProgressBar
            progressBar.setVisibility(View.GONE);
            adapter = new FacturaComprasAdapter(listafacturacompras);
            adapter.setOnClickListener(v->{
                //obtener el número de la factura
                idfc = listafacturacompras.get(recyclerFacturaCompras.getChildAdapterPosition(v)).getIdfc();
                nit =" ";
                //Mostrar inputbox para actualizar proveedor
                buscarProveedores();
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder
                        .setTitle("Softpymes")
                        .setPositiveButton("Aceptar", (dialog, which) -> {
                            //Actualizar proveedor en la venta
                            actualizarCompra(idfc,nit);
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> {

                        })

                        .setSingleChoiceItems(vproveedores, -1, (dialog, which) -> {
                            nit = vproveedores[which];
                            final  String[] parts =  nit.split("-");//Convertir en vector el string
                            nit = parts[0];//código del proveedor
                        });
                AlertDialog dialog = builder.create();
                dialog.show();//Mostrar el Alert
            });

            if (json1==null){
                recyclerFacturaCompras.setAdapter(adapter);
            }

        } catch (JSONException e) {
            // Ocultar la ProgressBar
            progressBar.setVisibility(View.GONE);
            // Manejar la respuesta
            Toast.makeText((getContext()),"No se pudo consultar!",Toast.LENGTH_SHORT).show();
        }

    }

    private void actualizarCompra(String idfc, String nit) {
        String url = "https://www.wmcsoftware.net/apps/softpymes/actualizarCompra.php";
        // Validar que los campos no estén vacíos
        if (nit.equals(" ")) {
            Toast.makeText(getContext(), "No ha seleccionado el proveedor!", Toast.LENGTH_SHORT).show();
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
                    listafacturacompras.clear();
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
                params.put("idfc", idfc);
                params.put("nit", nit);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

    private void buscarProveedores() {
        // Mostrar la ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaProveedores.php";
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }
}