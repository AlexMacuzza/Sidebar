package com.amcsoftware.sidebar;

import static com.amcsoftware.sidebar.utils.AppUtils.alertAdvertencia;
import static com.amcsoftware.sidebar.utils.AppUtils.alertExito;
import static com.amcsoftware.sidebar.utils.AppUtils.alertError;
import static com.amcsoftware.sidebar.utils.AppUtils.alertSinInternet;
import static com.amcsoftware.sidebar.utils.AppUtils.alertConfirmar;
import static com.amcsoftware.sidebar.utils.AppUtils.hayConectividad;
import static com.amcsoftware.sidebar.utils.AppUtils.mostrarCargando;
import static com.amcsoftware.sidebar.utils.AppUtils.cerrarCargando;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;
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


public class PrincipalActivity extends AppCompatActivity implements Response.Listener<JSONObject>, Response.ErrorListener {

    private EditText txtcorreo, txtclave;
    private Button btnlogon;
    private SweetAlertDialog dialogCargando;
    RequestQueue request;
    JsonObjectRequest jsonObjectRequest;
    String rta, id, user, codperfil, perfil, permisos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        txtcorreo = findViewById(R.id.edtMail);
        txtclave = findViewById(R.id.edtPassword);
        btnlogon = findViewById(R.id.btnLogin);
        rta = "";
        request = Volley.newRequestQueue(this);//Respuesta de las peticiones metódo GET

        btnlogon.requestFocus();

        btnlogon.setOnClickListener(v -> cargarWebService());

        //Controlar botón atrás
        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                // Alerta de confirmación (SweetAlert) antes de cerrar la aplicación.
                alertConfirmar(PrincipalActivity.this,
                        "Softpymes",
                        "¿Quiere cerrar la aplicación?",
                        "Si",
                        "No",
                        () -> finishAffinity());//Cerrar todas las activitys
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void cargarWebService() {
        // Obtener los valores ingresados
        final String correo = txtcorreo.getText().toString().trim();
        final String clave = txtclave.getText().toString().trim();
        // Validar que los campos no estén vacíos
        if (correo.isEmpty() || clave.isEmpty()) {
            alertAdvertencia(this,"ADVERTENCIA","Por favor, ingrese los campos solicitados!");
            return;
        }
        // Verificar conectividad antes de consultar el webservice
        if (!hayConectividad(this)) {
            alertSinInternet(this);
            return;
        }
        //desactivar botón guardar
        btnlogon.setEnabled(false);
        // Mostrar diálogo de carga (SweetAlert)
        dialogCargando = mostrarCargando(this, "Espere un momento", "Verificando credenciales...");

        String url = "https://www.wmcsoftware.net/apps/softpymes/consultarUsuario.php?usuario=" + correo + "&clave=" + clave;

        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, this, this);
        // El login nunca debe servirse desde caché en disco; además así queda
        // inmune a una posible corrupción del DiskBasedCache de Volley.
        jsonObjectRequest.setShouldCache(false);
        request.add(jsonObjectRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // Ocultar el diálogo de carga
        cerrarCargando(dialogCargando);
        btnlogon.setEnabled(true);
        // --- DIAGNÓSTICO TEMPORAL: mostrar la causa real del fallo ---
        String causa = error.getClass().getSimpleName();
        if (error.getCause() != null) {
            causa += " / " + error.getCause().getClass().getSimpleName()
                    + ": " + error.getCause().getMessage();
        }
        if (error.networkResponse != null) {
            causa += " (HTTP " + error.networkResponse.statusCode + ")";
        }
        Log.e("LOGIN", "Fallo Volley: " + causa, error);
        // Manejar la respuesta según si hay o no conectividad
        if (!hayConectividad(this)) {
            alertSinInternet(this);
        } else {
            alertError(this, "ERROR", "No se pudo consultar el servidor.\n\nDetalle: " + causa);
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        // Ocultar el diálogo de carga
        cerrarCargando(dialogCargando);
        Usuario miUsuario = new Usuario();//Clase donde estan los campos de la tabla clientes
        String mensaje = "No se pudo validar el usuario. Intente nuevamente.";

        JSONArray json = response.optJSONArray("usuario");
        JSONObject jsonObject;

        if (json != null && json.length() > 0) {
            try {
                jsonObject = json.getJSONObject(0);//Recorrer el array
                rta = jsonObject.optString("success");
                mensaje = jsonObject.optString("mensaje", mensaje);
                miUsuario.setIdu(jsonObject.optString("idu"));
                miUsuario.setNombre(jsonObject.optString("nombre_usuario"));
                miUsuario.setClave(jsonObject.optString("clave"));
                miUsuario.setCodperfil(jsonObject.optString("codperfil"));
                miUsuario.setPerfil(jsonObject.optString("perfil"));
                miUsuario.setPermisos(jsonObject.optString("permisos"));
            } catch (JSONException e) {
                rta = "";
            }
        } else {
            rta = "";
        }


        if (rta.equals("true")) {
            alertExito(this, "Softpymes",
                    "Usuario Registrado! Bienvenido: " + miUsuario.getNombre(),
                    () -> {
                        id = miUsuario.getIdu();
                        codperfil = miUsuario.getCodperfil();
                        perfil = miUsuario.getPerfil();
                        permisos = miUsuario.getPermisos();
                        user = miUsuario.getNombre();
                        // Guardar sesión
                        SharedPreferences.Editor editor = getSharedPreferences("sesion", MODE_PRIVATE).edit();
                        editor.putBoolean("sesionIniciada", true);
                        editor.putString("id", id);
                        editor.putString("usuario", user);
                        editor.putString("codperfil", codperfil);
                        editor.putString("perfil", perfil);
                        editor.putString("permisos", permisos);
                        editor.apply();
                        // Navegar a MainActivity
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        // Limpiar campos
                        txtcorreo.setText("");
                        txtclave.setText("");
                        txtcorreo.requestFocus();
                    });
        }else{
            alertError(this,"ERROR", mensaje);
        }
        btnlogon.setEnabled(true);
    }
}