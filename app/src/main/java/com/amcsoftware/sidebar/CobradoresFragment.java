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
import com.amcsoftware.sidebar.utils.AppUtils;
import com.amcsoftware.sidebar.Entidades.Cobrador;
import com.amcsoftware.sidebar.adapter.CobradoresAdapter;
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

public class CobradoresFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener,SearchView.OnQueryTextListener {
    ArrayList<Cobrador> listaCobradores;
    JsonObjectRequest jsonObjectRequest;
    RecyclerView recyclerCobradores;
    SweetAlertDialog dialogCargando;
    RequestQueue request;
    SearchView txtbuscar;//buscador
    ImageButton btncobrador;
    CobradoresAdapter adapter;

    public CobradoresFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista =  inflater.inflate(R.layout.fragment_cobradores, container, false);
        btncobrador = vista.findViewById(R.id.btncobrador);
        txtbuscar   = vista.findViewById(R.id.txtbuscar);
        listaCobradores    = new ArrayList<>();
        recyclerCobradores =  vista.findViewById(R.id.idRecycler);
        recyclerCobradores.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerCobradores.setHasFixedSize(true);
        adapter         = new CobradoresAdapter(listaCobradores);
        request         = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET

        cargarWebService();

        txtbuscar.setOnQueryTextListener(this);
        btncobrador.setOnClickListener(v->{
            Navigation.findNavController(v).navigate(R.id.registrarCobrador);//Navegación dinámica
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
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Cargando...", "Por favor espera.");
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaCobradores.php";
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
        // Ocultar el diálogo de carga
        AppUtils.cerrarCargando(dialogCargando);
        AppUtils.alertError(requireContext(), "Error", "No se pudo consultar los cobradores.");

    }

    @Override
    public void onResponse(JSONObject response) {
        Cobrador cobrador;
        JSONArray json = response.optJSONArray("cobrador");
        try {
            for (int i = 0; i< (json != null ? json.length() : 0); i++){
                cobrador = new Cobrador();
                JSONObject jsonObject;
                jsonObject = json.getJSONObject(i);
                cobrador.setIdcob(jsonObject.optString("idcobrador"));
                cobrador.setCedula(jsonObject.optString("cedula"));
                cobrador.setNombre(jsonObject.optString("nombre"));
                cobrador.setCelular(jsonObject.optString("celular"));
                cobrador.setDireccion(jsonObject.optString("direccion"));
                listaCobradores.add(cobrador);
            }
            // Ocultar el diálogo de carga
            AppUtils.cerrarCargando(dialogCargando);
            adapter = new CobradoresAdapter(listaCobradores);
            recyclerCobradores.setAdapter(adapter);
        } catch (JSONException e) {
            // Ocultar el diálogo de carga
            AppUtils.cerrarCargando(dialogCargando);
            AppUtils.alertError(requireContext(), "Error", "No se pudo consultar los cobradores.");
        }

    }
}