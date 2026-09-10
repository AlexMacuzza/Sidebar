package com.amcsoftware.sidebar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.amcsoftware.sidebar.Entidades.Usuario;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InicioActivity extends AppCompatActivity implements Response.Listener<JSONObject>,Response.ErrorListener{
    EditText txtcorreo,txtclave;
    Button btnlogon;
    ProgressBar progressBar;
    RequestQueue request;
    JsonObjectRequest jsonObjectRequest;
    String rta,user;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtcorreo    = findViewById(R.id.edtMail);
        txtclave     = findViewById(R.id.edtPassword);
        btnlogon     = findViewById(R.id.btnLogin);
        progressBar  =  findViewById(R.id.progressBar);
        rta          = "";
        request      = Volley.newRequestQueue(this);//Respuesta de las peticiones metódo GET

        btnlogon.setOnClickListener(v-> cargarWebService());

    }

    private void cargarWebService() {
        // Obtener los valores ingresados
        final String correo  = txtcorreo.getText().toString().trim();
        final String clave   = txtclave.getText().toString().trim();
        // Validar que los campos no estén vacíos
        if (correo.isEmpty() || clave.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese los campos solicitados.", Toast.LENGTH_SHORT).show();
            return;
        }
        //desactivar botón guardar
        btnlogon.setEnabled(false);
        // Mostrar la ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        String url = "https://www.wmcsoftware.net/apps/softpymes/consultarUsuario.php?usuario="+correo+"&clave="+clave;

        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // Ocultar la ProgressBar
        progressBar.setVisibility(View.GONE);
        // Manejar la respuesta
        Toast.makeText(this,"No se pudo consultar "+error.toString(),Toast.LENGTH_SHORT).show();

        btnlogon.setEnabled(true);

    }

    @Override
    public void onResponse(JSONObject response) {
        // Ocultar la ProgressBar
        progressBar.setVisibility(View.GONE);
        Usuario miUsuario = new Usuario();//Clase donde estan los campos de la tabla clientes

        JSONArray  json        = response.optJSONArray("usuario");
        JSONObject jsonObject;

        try {
            assert json != null;
            jsonObject = json.getJSONObject(0);//Recorrer el array
            rta = jsonObject.optString("success");
            miUsuario.setIdu(jsonObject.optString("idu"));
            miUsuario.setNombre(jsonObject.optString("nombre_usuario"));
            miUsuario.setClave(jsonObject.optString("clave"));
            miUsuario.setPerfil(jsonObject.optString("perfil"));
            miUsuario.setPermisos(jsonObject.optString("permisos"));

        } catch (JSONException e) {
            //throw new RuntimeException(e);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//Alert de confirmación

        if (rta.equals("true")) {
            builder.setMessage("Usuario Registrado! Bienvenido:"+miUsuario.getNombre()).setTitle("Softpymes");

            builder.setPositiveButton("Aceptar", (dialog, which) ->
                    user  = miUsuario.getIdu());

            AlertDialog dialog = builder.create();
            dialog.show();//Mostrar el Alert

        } else {
            builder.setMessage("No existe este usuario!").setTitle("Softpymes");
            AlertDialog dialog = builder.create();
            dialog.show();//Mostrar el Alert
        }
        btnlogon.setEnabled(true);
    }
}
