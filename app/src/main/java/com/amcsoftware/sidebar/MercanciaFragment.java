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
import com.amcsoftware.sidebar.Entidades.Mercancia;
import com.amcsoftware.sidebar.adapter.MercanciaAdapter;
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


public class MercanciaFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener,SearchView.OnQueryTextListener{
    ArrayList<Mercancia> listaMercancia;
    JsonObjectRequest jsonObjectRequest;
    RecyclerView recyclerMercancia;
    SweetAlertDialog dialogCargando;
    RequestQueue request;
    SearchView txtbuscar;//buscador
    ImageButton btnmercancia;
    MercanciaAdapter adapter;

    public MercanciaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Intanciar objetos
        View vista  =  inflater.inflate(R.layout.fragment_mercancia, container, false);
        btnmercancia = vista.findViewById(R.id.btnproducto);
        txtbuscar   = vista.findViewById(R.id.txtbuscar_mercancia);
        listaMercancia    = new ArrayList<>();
        recyclerMercancia =  vista.findViewById(R.id.idRecycler);
        recyclerMercancia.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerMercancia.setHasFixedSize(true);
        adapter         = new MercanciaAdapter(listaMercancia);
        request         = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET

        cargarWebService();

        txtbuscar.setOnQueryTextListener(this);
        btnmercancia.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.registrarMercancia));

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
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaMercancia.php";
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // Ocultar el diálogo de carga
        AppUtils.cerrarCargando(dialogCargando);
        // Manejar la respuesta
        AppUtils.alertError(requireContext(), "Error", "No se pudo consultar la mercancía.");
    }

    @Override
    public void onResponse(JSONObject response) {
        Mercancia mercancia;
        JSONArray json = response.optJSONArray("mercancia");
        try {
            for (int i = 0; i< (json != null ? json.length() : 0); i++){
                mercancia = new Mercancia();
                JSONObject jsonObject;
                jsonObject = json.getJSONObject(i);

                mercancia.setIdp(jsonObject.optString("idp"));
                mercancia.setReferencia(jsonObject.optString("detalle"));
                mercancia.setPreciov(jsonObject.optString("precio_venta"));
                mercancia.setPrecioc(jsonObject.optString("precio_compra"));
                mercancia.setCantidad(jsonObject.optString("saldo"));
                listaMercancia.add(mercancia);
            }
            // Ocultar el diálogo de carga
            AppUtils.cerrarCargando(dialogCargando);
            adapter = new MercanciaAdapter(listaMercancia);
            recyclerMercancia.setAdapter(adapter);
        } catch (JSONException e) {
            // Ocultar el diálogo de carga
            AppUtils.cerrarCargando(dialogCargando);
            // Manejar la respuesta
            AppUtils.alertError(requireContext(), "Error", "No se pudo consultar la mercancía.");
        }
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
    @Override
    public boolean onQueryTextChange(String s) {
        adapter.filtrado(s);
        return false;
    }
}