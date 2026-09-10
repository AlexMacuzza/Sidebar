package com.amcsoftware.sidebar;

import android.content.SharedPreferences;
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
import com.amcsoftware.sidebar.Entidades.Cliente;
import com.amcsoftware.sidebar.adapter.ClientesImagenAdapter;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ConsultaListaClientesImagenFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener,
        SearchView.OnQueryTextListener {
    //variables locales
    ClientesImagenAdapter adapter;
    ArrayList<Cliente> listaClientes;
    ImageButton btncliente,btruta, btrefresh;
    RecyclerView recyclerClientes;
    ProgressBar progressBar;
    JSONObject jsonObject = null;
    JsonObjectRequest jsonObjectRequest;
    RequestQueue requestQueue,request;
    String idcb,perfil;
    StringBuilder idcl;
    SearchView txtbuscar;//buscador

    public ConsultaListaClientesImagenFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = requireContext().getSharedPreferences("sesion",0);
        idcb   = sp.getString("codperfil", "General");
        perfil = sp.getString("perfil", "General");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Intanciar objetos
        View vista = inflater.inflate(R.layout.fragment_consulta_lista_clientes_imagen, container, false);

        btncliente       = vista.findViewById(R.id.btncliente);
        btruta           = vista.findViewById(R.id.btruta);
        btrefresh        = vista.findViewById(R.id.btrefresh);
        listaClientes    = new ArrayList<>();
        txtbuscar        = vista.findViewById(R.id.txtbuscar);
        recyclerClientes =  vista.findViewById(R.id.idRecycler);
        recyclerClientes.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerClientes.setHasFixedSize(true);
        adapter         = new ClientesImagenAdapter(listaClientes);
        progressBar     = vista.findViewById(R.id.progressBar);
        request         = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET
        requestQueue    = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo POST
        idcl            = new StringBuilder();

        if (perfil.equals("Administrador") || perfil.equals("Vendedor") ){
            idcb = " ";
            btncliente.setVisibility(View.VISIBLE);
            btruta.setVisibility(View.GONE);
        }else{
            btncliente.setVisibility(View.GONE);
            btruta.setVisibility(View.VISIBLE);
        }

        cargarWebService();
        txtbuscar.setOnQueryTextListener(this);

        btncliente.setOnClickListener(v->{
            Navigation.findNavController(v).navigate(R.id.nuevoClienteFoto);//Navegación dinámica
        });
        btrefresh.setOnClickListener(v-> {
            listaClientes.clear();
            cargarWebService();
        });
        btruta.setOnClickListener(v-> showClientsAndPositions());
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

    private void cargarWebService() {
        // Mostrar la ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaClientesImagen.php?idcb="+idcb;
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }
    private void msgBox(String mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(mensaje).setTitle("Softpymes");
        AlertDialog dialog = builder.create();
        dialog.show();//Mostrar el Alert
    }

    private void showClientsAndPositions() {
        if (adapter != null && adapter.getItemCount() > 0) {
            for (int i = 0; i < adapter.getItemCount(); i++) {
                Cliente client = adapter.getItemAtPosition(i);
                if (client != null) {
                    // Agrega la información del cliente y la posición al StringBuilder
                    idcl.append(client.getIdcl()).append("-");
                }
            }
            //Alert de confirmación
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setMessage("¿Está seguro de realizar esta operación?").setTitle("Softpymes");
            builder.setPositiveButton("Si", (dialog, which) -> reordenarLista());
            builder.setNegativeButton("No", (dialog, which) ->
                    Toast.makeText(getContext(),
                            "Reordenamiento cancelado",
                            Toast.LENGTH_SHORT).show());
            AlertDialog dialog = builder.create();
            dialog.show();//Mostrar el Alert

        } else {
            // Mensaje si la lista está vacía
            Toast.makeText(getContext(), "El RecyclerView está vacío.", Toast.LENGTH_SHORT).show();
        }
    }

    private void reordenarLista() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/ordenarListaClientes.php";
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
                    idcl.setLength(0);
                    if(jsonObject.optBoolean("success")) {
                        listaClientes.clear();
                        cargarWebService();
                    }
                },
                error -> {
                    // Ocultar ProgressBar
                    progressBar.setVisibility(View.GONE);
                    // Manejar errores
                    String errorMessage = "Error desconocido al guardar cliente.";
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        String responseData = new String(error.networkResponse.data, StandardCharsets.UTF_8);

                        errorMessage = "Error del servidor (" + statusCode + "): ";

                        if (responseData.isEmpty()) {
                            errorMessage += "Respuesta vacía o error interno del servidor.";
                        } else {
                            // Intenta parsear la respuesta si no está vacía (podría ser HTML de error)
                            errorMessage += "Detalles: " + responseData;
                        }
                    } else if (error.getMessage() != null && error.getMessage().equals("End of input at character 0 of ")) {
                        errorMessage = "El servidor no envió una respuesta. Verifica tu script PHP.";
                    } else if (error instanceof NoConnectionError) {
                        errorMessage = "No hay conexión a Internet. Por favor, verifica tu conexión.";
                    } else if (error instanceof TimeoutError) {
                        errorMessage = "Tiempo de espera agotado. El servidor no respondió a tiempo.";
                    } else {
                        errorMessage += error.getMessage(); // Mensaje genérico de Volley
                    }
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("OrdernarListaClientes", "Error: " + errorMessage, error); // Para ver el stack trace completo
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("idcl", String.valueOf(idcl).trim());
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

    @Override
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
        // Manejar la respuesta
        progressBar.setVisibility(View.GONE); // Asegurar que la barra de progreso se oculte en caso de error
        // Capturar cualquier otra excepción inesperada
        Toast.makeText(getContext(), "Error inesperado!!" , Toast.LENGTH_LONG).show();
        Log.e("ClienteFragment", "Error inesperado en onResponse: " + error.getMessage());
    }

    @Override
    public void onResponse(JSONObject response) {
        // Ocultar la ProgressBar
        progressBar.setVisibility(View.GONE);
        Cliente cliente;
        JSONArray json = response.optJSONArray("cliente");
        try {
            boolean success = response.optBoolean("success", false);
            String  mensaje = response.optString("mensaje","");
            if (success) {
                for (int i = 0; i < (json != null ? json.length() : 0); i++) {
                    cliente = new Cliente();
                    JSONObject jsonObject;
                    jsonObject = json.getJSONObject(i);
                    cliente.setIdcl(jsonObject.optString("idc"));
                    cliente.setCedula(jsonObject.optString("id"));
                    cliente.setNombre(jsonObject.optString("nombre"));
                    cliente.setCelular(jsonObject.optString("celular"));
                    cliente.setDireccion(jsonObject.optString("direccion"));
                    cliente.setCodeudor(jsonObject.optString("codeudor"));
                    cliente.setCelularcode(jsonObject.optString("celularcode"));
                    cliente.setObservacion(jsonObject.optString("observacion"));
                    cliente.setRfoto(jsonObject.optString("rfoto"));
                    cliente.setScore(jsonObject.optString("score"));
                    cliente.setDato(jsonObject.optString("foto"));
                    listaClientes.add(cliente);
                }

                adapter = new ClientesImagenAdapter(listaClientes);

                adapter.setOnClickListener(v -> {
                    //Actualizar y/o eliminar
                    if (perfil.equals("Administrador") || perfil.equals("Vendedor") ) {
                        final String idc        = listaClientes.get(recyclerClientes.getChildAdapterPosition(v)).getIdcl();
                        final String cedula     = listaClientes.get(recyclerClientes.getChildAdapterPosition(v)).getCedula();
                        final String nombre     = listaClientes.get(recyclerClientes.getChildAdapterPosition(v)).getNombre();
                        final String celular    = listaClientes.get(recyclerClientes.getChildAdapterPosition(v)).getCelular();
                        final String dir        = listaClientes.get(recyclerClientes.getChildAdapterPosition(v)).getDireccion();
                        final String codeudor   = listaClientes.get(recyclerClientes.getChildAdapterPosition(v)).getCodeudor();
                        final String celcode    = listaClientes.get(recyclerClientes.getChildAdapterPosition(v)).getCelularcode();
                        final String obs        = listaClientes.get(recyclerClientes.getChildAdapterPosition(v)).getObservacion();
                        final String foto       = listaClientes.get(recyclerClientes.getChildAdapterPosition(v)).getDato();
                        final String rfoto      = listaClientes.get(recyclerClientes.getChildAdapterPosition(v)).getRfoto();
                        Bundle bundle = new Bundle();
                        bundle.putString("idc", idc);
                        bundle.putString("id", cedula);
                        bundle.putString("nombre", nombre);
                        bundle.putString("celular", celular);
                        bundle.putString("dir", dir);
                        bundle.putString("codeudor", codeudor);
                        bundle.putString("celcode", celcode);
                        bundle.putString("obs", obs);
                        bundle.putString("foto", foto);
                        bundle.putString("rfoto", rfoto);
                        Navigation.findNavController(v).navigate(R.id.editarClienteFoto, bundle);//Navegación dinámica
                    }
                });
                recyclerClientes.setAdapter(adapter);
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
            }
            else {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
            listaClientes.clear(); // Limpiar la lista en caso de error
        }

        } catch (JSONException e) {
            // Ocultar la ProgressBar
            progressBar.setVisibility(View.GONE);
            // Manejar la respuesta
            Toast.makeText((getContext()),"No se pudo consultar!",Toast.LENGTH_SHORT).show();
        }

    }
}