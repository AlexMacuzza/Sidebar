package com.amcsoftware.sidebar;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amcsoftware.sidebar.Entidades.Cliente;
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

import cn.pedant.SweetAlert.SweetAlertDialog;


public class BuscarClientesFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener {

    EditText txtcedula;
    TextView lblcliente,lblcelular;
    Button btbuscar;
    SweetAlertDialog dialogCargando;
    RequestQueue request;
    JsonObjectRequest jsonObjectRequest;

    public BuscarClientesFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Intanciar objetos
        View vista       =  inflater.inflate(R.layout.fragment_buscar_clientes, container, false);

        txtcedula       =  vista.findViewById(R.id.txtid);
        lblcliente      =  vista.findViewById(R.id.lblcliente);
        lblcelular      =  vista.findViewById(R.id.lblcelular);
        btbuscar        =  vista.findViewById(R.id.btbuscar);
        request      = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET

        btbuscar.setOnClickListener(v -> cargarWebService());

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
        // Obtener los valores ingresados
        final String id  = txtcedula.getText().toString().trim();
        // Validar que los campos no estén vacíos
        if (id.isEmpty() ) {
            AppUtils.alertAdvertencia(requireContext(), "Dato requerido", "Por favor, ingrese la cédula.");
            return;
        }
        //desactivar botón guardar
        btbuscar.setEnabled(false);
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Consultando...", "Por favor espera.");
        String url = "https://www.wmcsoftware.net/apps/softpymes/consultarCliente.php?id="+id;
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }

    @Override
    public void onResponse(JSONObject response) {
        // Ocultar el diálogo de carga
        AppUtils.cerrarCargando(dialogCargando);

        Cliente miCliente = new Cliente();//Clase donde estan los campos de la tabla clientes

        JSONArray  json        = response.optJSONArray("cliente");
        JSONObject jsonObject;

        if (json != null && json.length() > 0) {
            try {
                jsonObject = json.getJSONObject(0);//Recorrer el array
                miCliente.setNombre(jsonObject.optString("nombre"));
                miCliente.setCelular(jsonObject.optString("celular"));
            } catch (JSONException e) {
                Log.i("ERROR", e.toString());
            }
            lblcliente.setText(miCliente.getNombre());
            lblcelular.setText(miCliente.getCelular());
        } else {
            lblcliente.setText("");
            lblcelular.setText("");
            AppUtils.alertError(requireContext(), "Sin resultados",
                    "No se encontró un cliente con esa cédula.");
        }
        btbuscar.setEnabled(true);
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        // Ocultar el diálogo de carga
        AppUtils.cerrarCargando(dialogCargando);
        // Manejar la respuesta
        AppUtils.alertError(requireContext(), "Error", "No se pudo consultar el cliente. Intenta de nuevo.");
        Log.i("ERROR",error.toString());
        lblcliente.setText("");
        lblcelular.setText("");
        btbuscar.setEnabled(true);
    }


}
