package com.amcsoftware.sidebar;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;
import com.amcsoftware.sidebar.Entidades.Cliente;
import com.amcsoftware.sidebar.adapter.ClientesAdapter;
import com.amcsoftware.sidebar.utils.AppUtils;
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

import cn.pedant.SweetAlert.SweetAlertDialog;


public class ConsultaListaClientesFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener,
        SearchView.OnQueryTextListener {

    //variables locales
    ClientesAdapter adapter;
    ArrayList<Cliente> listaClientes;
    ImageButton btncliente;
    RecyclerView recyclerClientes;
    SweetAlertDialog dialogCargando;
    RequestQueue request;
    SearchView txtbuscar;//buscador
    JsonObjectRequest jsonObjectRequest;

    public ConsultaListaClientesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Intanciar objetos
        View vista       =  inflater.inflate(R.layout.fragment_consulta_lista_clientes, container, false);

        btncliente       = vista.findViewById(R.id.btncliente);
        listaClientes    = new ArrayList<>();
        txtbuscar        = vista.findViewById(R.id.txtbuscar);
        recyclerClientes =  vista.findViewById(R.id.idRecycler);
        recyclerClientes.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerClientes.setHasFixedSize(true);
        adapter         = new ClientesAdapter(listaClientes);
        request         = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET

        cargarWebService();
        txtbuscar.setOnQueryTextListener(this);

        btncliente.setOnClickListener(v->{
            Navigation.findNavController(v).navigate(R.id.nuevoClienteFoto);//Navegación dinámica
        });

        //Controlar botón atrás
        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return vista;
    }

    private void cargarWebService() {
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Cargando...", "Por favor espera.");
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaClientes.php";
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // Ocultar el diálogo de carga
        AppUtils.cerrarCargando(dialogCargando);
        // Manejar la respuesta
        AppUtils.alertError(requireContext(), "Error", "No se pudo consultar la lista de clientes.");
        Log.i("ERROR",error.toString());
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
        // Ocultar el diálogo de carga
        AppUtils.cerrarCargando(dialogCargando);
        adapter = new ClientesAdapter(listaClientes);

            adapter.setOnClickListener(v -> {
                /*Toast.makeText(getContext(),
                        "Seleccion: "+listaClientes.get(recyclerClientes.getChildAdapterPosition(v))
                                .getNombre(),Toast.LENGTH_LONG).show();*/

            });
            recyclerClientes.setAdapter(adapter);
        } catch (JSONException e) {
            // Ocultar el diálogo de carga
            AppUtils.cerrarCargando(dialogCargando);
            // Manejar la respuesta
            AppUtils.alertError(requireContext(), "Error", "No se pudo consultar la lista de clientes.");
        }
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
}