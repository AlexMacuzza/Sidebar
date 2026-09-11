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
import android.widget.SearchView;
import com.amcsoftware.sidebar.Entidades.Vendedor;
import com.amcsoftware.sidebar.adapter.VendedoresAdapter;
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

public class VendedoresFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener, SearchView.OnQueryTextListener {
    ArrayList<Vendedor> listaVendedores;
    JsonObjectRequest jsonObjectRequest;
    RecyclerView recyclerVendedores;
    SweetAlertDialog dialogCargando;
    RequestQueue request;
    SearchView txtbuscar;//buscador
    ImageButton btnvendedor;
    VendedoresAdapter adapter;

    public VendedoresFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista  = inflater.inflate(R.layout.fragment_vendedores, container, false);
        btnvendedor = vista.findViewById(R.id.btnvendedor);
        txtbuscar   = vista.findViewById(R.id.txtbuscar);
        listaVendedores    = new ArrayList<>();
        recyclerVendedores =  vista.findViewById(R.id.idRecycler);
        recyclerVendedores.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerVendedores.setHasFixedSize(true);
        adapter         = new VendedoresAdapter(listaVendedores);
        request         = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET

        cargarWebService();

        txtbuscar.setOnQueryTextListener(this);
        btnvendedor.setOnClickListener(v->{
            //Navegación dinámica
            Navigation.findNavController(v).navigate(R.id.registrarVendedor);
        });

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

    private void cargarWebService() {
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Cargando...", "Por favor espera.");
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaVendedores.php";
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
        // Ocultar el diálogo de carga
        AppUtils.cerrarCargando(dialogCargando);
        // Manejar la respuesta
        AppUtils.alertError(requireContext(), "Error", "No se pudo consultar los vendedores.");
    }

    @Override
    public void onResponse(JSONObject response) {
        Vendedor vendedor;
        JSONArray json = response.optJSONArray("vendedor");
        try {
            for (int i = 0; i< (json != null ? json.length() : 0); i++){
                vendedor = new Vendedor();
                JSONObject jsonObject;
                jsonObject = json.getJSONObject(i);

                vendedor.setIdvd(jsonObject.optString("idvd"));
                vendedor.setCedula(jsonObject.optString("cedula"));
                vendedor.setNombre(jsonObject.optString("nombre"));
                vendedor.setCelular(jsonObject.optString("celular"));
                vendedor.setDireccion(jsonObject.optString("direccion"));
                listaVendedores.add(vendedor);
            }
            // Ocultar el diálogo de carga
            AppUtils.cerrarCargando(dialogCargando);
            adapter = new VendedoresAdapter(listaVendedores);
            recyclerVendedores.setAdapter(adapter);
        } catch (JSONException e) {
            // Ocultar el diálogo de carga
            AppUtils.cerrarCargando(dialogCargando);
            // Manejar la respuesta
            AppUtils.alertError(requireContext(), "Error", "No se pudo consultar los vendedores.");
        }

    }
}
