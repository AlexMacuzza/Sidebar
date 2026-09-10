package com.amcsoftware.sidebar;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;
import com.amcsoftware.sidebar.Entidades.Cliente;
import com.amcsoftware.sidebar.adapter.ClientesVentasAdapter;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class ClientesVentasFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener,SearchView.OnQueryTextListener {
    //Variables locales
    ClientesVentasAdapter adapter;
    ArrayList<Cliente> listaClientes;
    RecyclerView recyclerClientes;
    ProgressBar progressBar;
    RequestQueue request;
    SearchView txtbuscar;
    JsonObjectRequest jsonObjectRequest;
    ImageButton btncliente;
    
    public ClientesVentasFragment() {
        // Required empty public constructor
    }


       @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_clientes_ventas, container, false);

        btncliente       = vista.findViewById(R.id.btncliente);
        listaClientes    = new ArrayList<>();
        txtbuscar        = vista.findViewById(R.id.txtbuscarcl);
        recyclerClientes =  vista.findViewById(R.id.idRecycler);
        recyclerClientes.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerClientes.setHasFixedSize(true);
        adapter         = new ClientesVentasAdapter(listaClientes);
        progressBar     = vista.findViewById(R.id.progressBar);
        request         = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET

        cargarWebService();
        txtbuscar.setOnQueryTextListener(this);

        btncliente.setOnClickListener(v->{
               Navigation.findNavController(v).navigate(R.id.nuevoClienteVentas);//Navegación dinámica
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

    private void cargarWebService() {
        // Mostrar la ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaClientes.php";
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
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
        // Ocultar la ProgressBar
        progressBar.setVisibility(View.GONE);
        // Manejar la respuesta
        Toast.makeText((getContext()),"No se pudo consultar!",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(JSONObject response) {
        Cliente cliente;
        JSONArray json = response.optJSONArray("cliente");
        try {
            for (int i = 0; i< (json != null ? json.length() : 0); i++){
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
                listaClientes.add(cliente);
            }
            // Ocultar la ProgressBar
            progressBar.setVisibility(View.GONE);
            adapter = new ClientesVentasAdapter(listaClientes);

            adapter.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("idcl", listaClientes.get(recyclerClientes.getChildAdapterPosition(v))
                        .getCedula());
                bundle.putString("cliente", listaClientes.get(recyclerClientes.getChildAdapterPosition(v))
                        .getNombre());
                getParentFragmentManager().setFragmentResult("clientes", bundle);
                Navigation.findNavController(v).popBackStack();
            });

            recyclerClientes.setAdapter(adapter);
        } catch (JSONException e) {
            // Ocultar la ProgressBar
            progressBar.setVisibility(View.GONE);
            // Manejar la respuesta
            Toast.makeText((getContext()),"No se pudo consultar!",Toast.LENGTH_SHORT).show();
        }

    }
}