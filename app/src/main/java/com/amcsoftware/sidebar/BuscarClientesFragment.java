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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amcsoftware.sidebar.Entidades.Cliente;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class BuscarClientesFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener {

    EditText txtcedula;
    TextView lblcliente,lblcelular;
    Button btbuscar;
    ProgressBar progressBar;
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
        progressBar     =  vista.findViewById(R.id.progressBar);
        request      = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET

        btbuscar.setOnClickListener(v -> cargarWebService());

        //Controlar botón atrás
        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                //Toast.makeText(requireContext(), "Botón en MyFragment", Toast.LENGTH_LONG).show();
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
            Toast.makeText(getContext(), "Por favor, ingrese la cedula.", Toast.LENGTH_SHORT).show();
            return;
        }
        //desactivar botón guardar
        btbuscar.setEnabled(false);
        // Mostrar la ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://www.wmcsoftware.net/apps/softpymes/consultarCliente.php?id="+id;
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }

    @Override
    public void onResponse(JSONObject response) {
        // Ocultar la ProgressBar
        progressBar.setVisibility(View.GONE);
        Toast.makeText((getContext()),"Mensaje: ",Toast.LENGTH_LONG).show();

        Cliente miCliente = new Cliente();//Clase donde estan los campos de la tabla clientes

        JSONArray  json        = response.optJSONArray("cliente");
        JSONObject jsonObject;

        try {
            assert json != null;
            jsonObject = json.getJSONObject(0);//Recorrer el array
            miCliente.setNombre(jsonObject.optString("nombre"));
            miCliente.setCelular(jsonObject.optString("celular"));
        } catch (JSONException e) {
            //throw new RuntimeException(e);
        }

        lblcliente.setText(miCliente.getNombre());
        lblcelular.setText(miCliente.getCelular());
        btbuscar.setEnabled(true);
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        // Ocultar la ProgressBar
        progressBar.setVisibility(View.GONE);
        // Manejar la respuesta
        Toast.makeText((getContext()),"No se pudo consultar "+error.toString(),Toast.LENGTH_SHORT).show();
        Log.i("ERROR",error.toString());
        lblcliente.setText("");
        lblcelular.setText("");
        btbuscar.setEnabled(true);
    }


}