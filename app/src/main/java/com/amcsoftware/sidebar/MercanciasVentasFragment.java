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
import com.amcsoftware.sidebar.adapter.MercanciasVentasAdapter;
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
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MercanciasVentasFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener,SearchView.OnQueryTextListener {
    ArrayList<Mercancia> listaMercancia;
    boolean bandera;
    ImageButton btmerc;
    int n;
    JsonObjectRequest jsonObjectRequest;
    RecyclerView recyclerMercancia;
    SweetAlertDialog dialogCargando;
    RequestQueue request;
    SearchView txtbuscar;//buscador
    MercanciasVentasAdapter adapter;

    public MercanciasVentasFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista =  inflater.inflate(R.layout.fragment_mercancias_ventas, container, false);

        txtbuscar         = vista.findViewById(R.id.txtbuscar);
        btmerc            = vista.findViewById(R.id.btnmerc);
        listaMercancia    = new ArrayList<>();
        recyclerMercancia =  vista.findViewById(R.id.idRecycler);
        recyclerMercancia.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerMercancia.setHasFixedSize(true);
        adapter          = new MercanciasVentasAdapter(listaMercancia);
        request          = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET

        cargarWebService();


        btmerc.setOnClickListener(v->
            //Alert de confirmación
            AppUtils.alertConfirmar(requireContext(), "Softpymes",
                    "¿Está seguro de la selección de estos productos?", "Sí", "No", () -> {
                List<Mercancia> selectedItems = adapter.getSelectedDataOnly();
                //Recorrer 'Items Seleccionados'
                StringBuilder message = new StringBuilder("Elementos seleccionados: ");
                if (selectedItems.isEmpty()) {
                    message.append("Ninguno");
                    bandera=false;
                } else {
                    bandera=true;
                    n   = selectedItems.size();
                    final  String[] idp1     = new String[n];
                    final  String[] desc1    = new String[n];
                    final  String[] precio1  = new String[n];
                    final  String[] cant1    = new String[n];
                    final  String[] subt1    = new String[n];

                    for (int i = 0; i < selectedItems.size(); i++) {
                        idp1[i]    = selectedItems.get(i).getIdp();
                        desc1[i]   = selectedItems.get(i).getDescripcion();
                        precio1[i] = selectedItems.get(i).getPreciov();
                        cant1[i]   = selectedItems.get(i).getCantidad();
                        subt1[i]   = selectedItems.get(i).getSubtotal();
                        message.append(idp1[i]).append(" ").append(desc1[i]).append(" ").append(precio1[i]).append(" ").append(cant1[i]).append(" ").append(subt1[i]);
                        if (i < selectedItems.size() - 1) {
                            message.append(", ");
                        }
                    }
                    Bundle bundle = new Bundle();
                    bundle.putStringArray("idp", idp1);
                    bundle.putStringArray("desc", desc1);
                    bundle.putStringArray("precio", precio1);
                    bundle.putStringArray("cant", cant1);
                    bundle.putStringArray("subt", subt1);
                    bundle.putString("n",String.valueOf(n));
                    getParentFragmentManager().setFragmentResult("mercancia", bundle);
                }
                Navigation.findNavController(v).popBackStack();
            }));


        txtbuscar.setOnQueryTextListener(this);

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
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        adapter.filtrado(s);
        return false;
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
                mercancia.setDescripcion(jsonObject.optString("detalle"));
                mercancia.setPrecioc(jsonObject.optString("precio_compra"));
                mercancia.setPreciov(jsonObject.optString("precio_venta"));
                mercancia.setCantidad(jsonObject.optString("cantidad_inicial"));
                listaMercancia.add(mercancia);
            }
            // Ocultar el diálogo de carga
            AppUtils.cerrarCargando(dialogCargando);
            adapter = new MercanciasVentasAdapter(listaMercancia);
            //Retornar valores
            adapter.setOnClickListener(v -> {

            });
            recyclerMercancia.setAdapter(adapter);
        } catch (JSONException e) {
            // Ocultar el diálogo de carga
            AppUtils.cerrarCargando(dialogCargando);
            // Manejar la respuesta
            AppUtils.alertError(requireContext(), "Error", "No se pudo consultar la mercancía.");
        }
    }
}
