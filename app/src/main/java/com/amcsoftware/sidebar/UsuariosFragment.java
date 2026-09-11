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
import com.amcsoftware.sidebar.Entidades.Usuario;
import com.amcsoftware.sidebar.adapter.UsuariosAdapter;
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

public class UsuariosFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener, SearchView.OnQueryTextListener{
    ArrayList<Usuario> listaUsuarios;
    JsonObjectRequest jsonObjectRequest;
    RecyclerView recyclerUsuarios;
    SweetAlertDialog dialogCargando;
    RequestQueue request;
    SearchView txtbuscar;//buscador por nombre de usuario
    ImageButton btnusuario;
    UsuariosAdapter adapter;


    public UsuariosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_usuarios, container, false);
        btnusuario = vista.findViewById(R.id.btnusuario);
        txtbuscar   = vista.findViewById(R.id.txtbuscar);
        listaUsuarios    = new ArrayList<>();
        recyclerUsuarios =  vista.findViewById(R.id.idRecycler);
        recyclerUsuarios.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerUsuarios.setHasFixedSize(true);
        adapter         = new UsuariosAdapter(listaUsuarios);
        request         = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET

        cargarWebService();

        txtbuscar.setOnQueryTextListener(this);
        btnusuario.setOnClickListener(v-> Navigation.findNavController(v).navigate(R.id.registrarUsuario));

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
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaUsuarios.php";
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
        AppUtils.alertError(requireContext(), "Error", "No se pudo consultar los usuarios.");
    }

    @Override
    public void onResponse(JSONObject response) {
        Usuario usuario;
        JSONArray json = response.optJSONArray("usuario");
        try {
            for (int i = 0; i< (json != null ? json.length() : 0); i++){
                usuario = new Usuario();
                JSONObject jsonObject;
                jsonObject = json.getJSONObject(i);
                usuario.setIdu(jsonObject.optString("idu"));
                usuario.setNombre(jsonObject.optString("nombre_usuario"));
                usuario.setCodperfil(jsonObject.optString("codperfil"));
                usuario.setPerfil(jsonObject.optString("perfil"));
                usuario.setPermisos(jsonObject.optString("permisos"));
                usuario.setSucursal(jsonObject.optString("nombre_sucursal"));
                listaUsuarios.add(usuario);
            }
            // Ocultar el diálogo de carga
            AppUtils.cerrarCargando(dialogCargando);
            adapter = new UsuariosAdapter(listaUsuarios);
            recyclerUsuarios.setAdapter(adapter);
        } catch (JSONException e) {
            // Ocultar el diálogo de carga
            AppUtils.cerrarCargando(dialogCargando);
            AppUtils.alertError(requireContext(), "Error", "No se pudo consultar los usuarios.");
        }

    }
}