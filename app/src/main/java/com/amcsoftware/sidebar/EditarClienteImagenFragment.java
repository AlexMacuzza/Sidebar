package com.amcsoftware.sidebar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amcsoftware.sidebar.api.ApiService;
import com.amcsoftware.sidebar.api.RetrofitClient;
import com.amcsoftware.sidebar.model.ResponseModel;
import com.amcsoftware.sidebar.utils.AppUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class EditarClienteImagenFragment extends Fragment  implements Response.Listener<JSONObject>,Response.ErrorListener{
    // Variables locales
    private  boolean bandera;
    TextView lblidcl;
    EditText txtcliente, txtid, txtcelular, txtdir, txtcodeudor, txtcelcodeudor, txtobservacion;
    ImageButton btregistrar,btfoto,bteliminar;
    ImageView foto;
    JSONObject jsonResponse = null;
    private SweetAlertDialog dialogCargando;
    RequestQueue request, requestQueue;
    JsonObjectRequest jsonObjectRequest;
    private Bitmap bitmap;
    private String idc;
    private String nombre;
    private String id;
    private String direccion;
    private String celular;
    private String codeudor;
    private String celularcode;
    private String observacion;
    private String imagen;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri>    cameraLauncher;
    private Uri currentCameraPhotoUri,imageUri,selectedImageUri; // Para almacenar la URI de la foto tomada con la cámara.
    private ImageDecoder.Source source;
    private ContentResolver contentResolver;

    public EditarClienteImagenFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contentResolver = requireContext().getContentResolver();

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        foto.setImageURI(imageUri);
                        selectedImageUri = imageUri; // Almacenar la URI seleccionada
                        try {
                            assert imageUri != null;
                            source = ImageDecoder.createSource(contentResolver, imageUri);
                            bitmap = ImageDecoder.decodeBitmap(source);
                            foto.setImageBitmap(bitmap);
                        }catch (IOException e){
                            AppUtils.alertError(requireContext(), "Error", "No se pudo abrir la imagen seleccionada.");
                        }
                        Log.d("ClientesImagenFragment", "Imagen de galería seleccionada: " + imageUri);
                        Toast.makeText(getContext(), "¡Imagen de galería seleccionada!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("ClientesImagenFragment", "Selección de galería cancelada.");
                        Toast.makeText(getContext(), "Selección de galería cancelada.", Toast.LENGTH_SHORT).show();
                        selectedImageUri = null; // Limpiar la URI si la selección es cancelada
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success) {
                        foto.setImageURI(currentCameraPhotoUri);
                        selectedImageUri = currentCameraPhotoUri; // Almacenar la URI de la foto tomada
                        try {
                            assert currentCameraPhotoUri != null;
                            source =  ImageDecoder.createSource(contentResolver, currentCameraPhotoUri);
                            bitmap =  ImageDecoder.decodeBitmap(source);
                            foto.setImageBitmap(bitmap);
                        }catch (IOException e){
                            AppUtils.alertError(requireContext(), "Error", "No se pudo abrir la foto tomada.");
                        }
                        Log.d("ClientesImagenFragment", "Foto tomada y guardada en: " + currentCameraPhotoUri);
                        Toast.makeText(getContext(), "¡Foto tomada y guardada!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("ClientesImagenFragment", "Captura de cámara cancelada o fallida.");
                        Toast.makeText(getContext(), "Captura de cámara cancelada.", Toast.LENGTH_SHORT).show();
                        selectedImageUri = null; // Limpiar la URI si la captura es cancelada
                    }
                }
        );

    }

    // ActivityResultLauncher para solicitar permisos
    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean allGranted = true;
                for (String permission : permissions.keySet()) {
                    if (Boolean.FALSE.equals(permissions.get(permission))) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    Toast.makeText(requireContext(), "Permisos concedidos.", Toast.LENGTH_SHORT).show();
                } else {
                    AppUtils.alertAdvertencia(requireContext(), "Permisos denegados",
                            "La funcionalidad de la cámara puede no estar disponible.");
                }
            }
    );

    private boolean allPermissionsGranted() {
        List<String> permissions = new ArrayList<>();
        permissions.add(android.Manifest.permission.CAMERA);
        for (String permission : permissions) {
            // Usa requireContext() para obtener el contexto del fragmento
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        permissionsToRequest.add(Manifest.permission.CAMERA);
        // Lanza el ActivityResultLauncher para solicitar múltiples permisos
        requestPermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
    }

    private Bitmap redimensionarImagen(Bitmap bitmap) {
        int ancho=bitmap.getWidth();
        int alto=bitmap.getHeight();

        if(ancho> (float) 600 || alto> (float) 800){
            float escalaAncho= (float) 600 /ancho;
            float escalaAlto= (float) 800 /alto;

            Matrix matrix=new Matrix();
            matrix.postScale(escalaAncho,escalaAlto);

            return Bitmap.createBitmap(bitmap,0,0,ancho,alto,matrix,false);

        }else{
            return bitmap;
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_editar_cliente_imagen, container, false);
        lblidcl = vista.findViewById(R.id.lblidcl);
        txtid = vista.findViewById(R.id.txtid);
        txtcliente = vista.findViewById(R.id.lblcliente);
        txtcelular = vista.findViewById(R.id.txtcelular);
        txtdir = vista.findViewById(R.id.txtdir);
        txtcodeudor = vista.findViewById(R.id.txtcodeudor);
        txtcelcodeudor = vista.findViewById(R.id.txtcelcodeudor);
        txtobservacion = vista.findViewById(R.id.txtobservacion);
        btregistrar = vista.findViewById(R.id.btaceptar);
        bteliminar  = vista.findViewById(R.id.bteliminar);
        btfoto = vista.findViewById(R.id.btfotocl);
        foto   = vista.findViewById(R.id.imgFoto);
        //Inicializar
        request      = Volley.newRequestQueue(requireContext());
        requestQueue = Volley.newRequestQueue(requireContext());

        request.getCache().clear();

        // Verifica y solicita permisos al crear la vista del fragmento
        if (!allPermissionsGranted()) {
            requestPermissions();
        }

        //Capturar datos del recyclerview
        assert getArguments() != null;
        lblidcl.setText(getArguments().getString("idc"));
        txtid.setText(getArguments().getString("id"));
        txtcliente.setText(getArguments().getString("nombre"));
        txtcelular.setText(getArguments().getString("celular"));
        txtdir.setText(getArguments().getString("dir"));
        txtcodeudor.setText(getArguments().getString("codeudor"));
        txtcelcodeudor.setText(getArguments().getString("celcode"));
        txtobservacion.setText(getArguments().getString("obs"));

        String rutaImagen = getArguments().getString("rfoto"); // o de donde obtengas la ruta

        if (rutaImagen != null && !rutaImagen.isEmpty()) {
            String BASE_URL = "https://www.wmcsoftware.net/";
            String urlCompleta = BASE_URL + rutaImagen;
            Glide.with(requireContext())
                    .load(urlCompleta)
                    .placeholder(R.drawable.clientes) // mientras carga
                    .error(R.drawable.clientes)       // si falla
                    .into(foto);
        } else {
            foto.setImageResource(R.drawable.clientes);
        }

        //Actualizar cliente
        btregistrar.setOnClickListener(v->{
            // Obtener los valores ingresados
            bandera     = true;//Indica si / no , necesario actualizar la foto
            idc         = lblidcl.getText().toString().trim();
            id          = txtid.getText().toString().trim();
            nombre      = txtcliente.getText().toString().trim();
            direccion   = txtdir.getText().toString().trim();
            celular     = txtcelular.getText().toString().trim();
            codeudor    = txtcodeudor.getText().toString().trim();
            celularcode = txtcelcodeudor.getText().toString().trim();
            observacion = txtobservacion.getText().toString().trim();

            // Validar que los campos no estén vacíos
            if (id.isEmpty() || nombre.isEmpty() || direccion.isEmpty()|| celular.isEmpty()
                    || codeudor.isEmpty() || celularcode.isEmpty() || observacion.isEmpty()) {
                AppUtils.alertAdvertencia(requireContext(), "Campos incompletos",
                        "Por favor, complete todos los campos.");
                return;
            }else if (selectedImageUri == null || bitmap == null){
                bandera = false;
            }
            //Alert de confirmación
            AppUtils.alertConfirmar(requireContext(), "Softpymes",
                    "¿Está seguro de realizar esta operación?", "Sí", "No",
                    this::actualizarClienteConFoto);

        });
        //Eliminar cliente
        bteliminar.setOnClickListener(v->
                AppUtils.alertConfirmar(requireContext(), "Softpymes",
                        "¿Está seguro de eliminar este cliente?", "Sí", "No",
                        this::eliminarClienteFoto));
        btfoto.setOnClickListener(v -> mostrarDialogOpciones());

        //Controlar botón atrás
        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        // Inflate the layout for this fragment
        return vista ;
    }

    private void eliminarClienteFoto(){
        // Obtener los valores ingresados
        idc  = lblidcl.getText().toString().trim();
        // Validar que los campos no estén vacíos
        if (idc.isEmpty() ) {
            AppUtils.alertAdvertencia(requireContext(), "Dato requerido", "Por favor, ingrese el id.");
            return;
        }
        //desactivar botones
        btregistrar.setEnabled(false);
        bteliminar.setEnabled(false);
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Eliminando...", "Por favor espera.");
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.eliminarCliente(
                idc
        ).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseModel> call, @NonNull retrofit2.Response<ResponseModel> response) {
                btregistrar.setEnabled(true);//activar botones
                bteliminar.setEnabled(false);
                AppUtils.cerrarCargando(dialogCargando);
                if (response.isSuccessful()) {
                    AppUtils.alertExito(requireContext(), "Éxito", "Cliente eliminado.");
                    limpiar();
                } else {
                    AppUtils.alertError(requireContext(), "Error", "No se pudo eliminar el cliente.");
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponseModel> call, @NonNull Throwable t) {
                AppUtils.cerrarCargando(dialogCargando);
                btregistrar.setEnabled(true);
                bteliminar.setEnabled(true);
                AppUtils.alertError(requireContext(), "Error de red",
                        "No se pudo eliminar el cliente.\n" + t.getMessage());
            }
        });
    }

    private void eliminarDatos() {
        // Obtener los valores ingresados
        idc  = lblidcl.getText().toString().trim();
        // Validar que los campos no estén vacíos
        if (idc.isEmpty() ) {
            AppUtils.alertAdvertencia(requireContext(), "Dato requerido", "Por favor, ingrese el id.");
            return;
        }
        //desactivar botón eliminar
        bteliminar.setEnabled(false);
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Eliminando...", "Por favor espera.");
        String url = "https://www.wmcsoftware.net/apps/softpymes/eliminarCliente.php?idc="+idc;
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }
    //Actualizar Cliente con Retrofit
    private void actualizarClienteConFoto() {
        String fotoBase64 = "";
        String bandera1 = String.valueOf(bandera);
        if (bandera) {
            // Solo convertimos el bitmap si realmente hay una foto nueva
            fotoBase64 = convertirImagenBase64(bitmap);
        }
        //desactivar botones
        btregistrar.setEnabled(false);
        bteliminar.setEnabled(false);
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Actualizando...", "Por favor espera.");
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.actualizarCliente(
                idc,
                id,
                nombre,
                direccion,
                celular,
                codeudor,
                celularcode,
                observacion,
                fotoBase64,
                bandera1
        ).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseModel> call, @NonNull retrofit2.Response<ResponseModel> response) {
                //activar botones
                btregistrar.setEnabled(true);
                bteliminar.setEnabled(true);
                AppUtils.cerrarCargando(dialogCargando);
                if (response.isSuccessful()) {
                    AppUtils.alertExito(requireContext(), "Éxito", "Cliente actualizado.");
                } else {
                    AppUtils.alertError(requireContext(), "Error", "No se pudo actualizar el cliente.");
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponseModel> call, @NonNull Throwable t) {
                AppUtils.cerrarCargando(dialogCargando);
                btregistrar.setEnabled(true);
                bteliminar.setEnabled(true);
                AppUtils.alertError(requireContext(), "Error de red",
                        "No se pudo actualizar el cliente.\n" + t.getMessage());
            }
        });
    }
    //Libreria Volley
    private void guardarRegistro() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/actualizarClienteImagen.php";
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Guardando...", "Por favor espera.");
        //Petición POST
        StringRequest stringRequest = new StringRequest(
                com.android.volley.Request.Method.POST,
                url,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    boolean ok = false;
                    String msj = "No se pudo procesar la respuesta del servidor.";
                    try {
                        jsonResponse = new JSONObject(response);
                        msj = jsonResponse.optString("mensaje", msj);
                        ok = jsonResponse.optBoolean("success");
                    } catch (JSONException e) {
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                    }
                    if (ok) {
                        AppUtils.alertExito(requireContext(), "Éxito", msj);
                        limpiar();
                    } else {
                        AppUtils.alertError(requireContext(), "Atención", msj);
                    }
                },
                error -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    AppUtils.alertError(requireContext(), "Error de red", "No se pudo conectar al servidor.");
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                imagen = "";
                if (bandera) {
                    bitmap = redimensionarImagen(bitmap);
                    imagen = convertirImgString(bitmap);
                }
                // Añadir los datos de tu formulario
                params.put("idc", idc);
                params.put("id", id);
                params.put("nombre", nombre);
                params.put("dir", direccion);
                params.put("celular", celular);
                params.put("codeudor", codeudor);
                params.put("celularcode", celularcode);
                params.put("obs", observacion);
                params.put("bandera", String.valueOf(bandera));
                params.put("foto", imagen); // Enviar la imagen codificada en Base6
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void limpiar() {
        bandera = false;
        lblidcl.setText("");
        txtid.setText("");
        txtcliente.setText("");
        txtdir.setText("");
        txtcelular.setText("");
        txtcodeudor.setText("");
        txtcelcodeudor.setText("");
        txtobservacion.setText("");
        foto.setImageURI(Uri.parse("@drawable/clientesfoto"));
        txtcliente.requestFocus();
    }



    private String convertirImgString(Bitmap bitmap) {
        ByteArrayOutputStream array=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,array);
        byte[] imagenByte=array.toByteArray();

        return Base64.encodeToString(imagenByte,Base64.DEFAULT);
    }

    private void mostrarDialogOpciones() {
        final CharSequence[] opciones = {"Tomar Foto", "Elegir de Galeria", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Elige una Opción");
        builder.setItems(opciones, (dialogInterface, i) -> {
            if (opciones[i].equals("Tomar Foto")) {
                abriCamara();
            } else {
                if (opciones[i].equals("Elegir de Galeria")) {
                    abriGaleria();
                } else {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private void abriCamara() {
        if (allPermissionsGranted()) {
            try {
                Uri photoUri = createImageFileUri();
                if (photoUri != null) {
                    currentCameraPhotoUri = photoUri;
                    cameraLauncher.launch(currentCameraPhotoUri);
                }
            } catch (Exception ex) {
                AppUtils.alertError(requireContext(), "Error",
                        "No se pudo preparar la imagen: " + ex.getMessage());
            }
        } else {
            AppUtils.alertAdvertencia(requireContext(), "Permisos requeridos",
                    "Por favor, concede los permisos necesarios para usar la cámara.");
            requestPermissions(); // Vuelve a solicitar si no se concedieron
        }

    }

    private void abriGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        galleryLauncher.launch(Intent.createChooser(intent, "Selecciona una imagen"));
    }

    private Uri createImageFileUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";

        ContentValues contentValues = getContentValues(imageFileName);

        // Inserta la imagen en MediaStore y obtiene su URI
        // Usa requireContext() para obtener el ContentResolver
        return requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }


    @NonNull
    private static ContentValues getContentValues(String imageFileName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        return contentValues;
    }

    private String convertirImagenBase64(Bitmap bitmap) {

        if (bitmap == null) return "";

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Comprimir imagen (JPEG recomendado)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        AppUtils.cerrarCargando(dialogCargando);
        AppUtils.alertError(requireContext(), "Error", "No se pudo eliminar el cliente.");
        Log.i("ERROR",error.toString());
        bteliminar.setEnabled(true);
    }

    @Override
    public void onResponse(JSONObject response) {
        AppUtils.cerrarCargando(dialogCargando);
        String  mensaje = response.optString("mensaje","");
        AppUtils.alertExito(requireContext(), "Softpymes",
                mensaje.isEmpty() ? "Operación realizada." : mensaje);
        bteliminar.setEnabled(true);
        limpiar();
    }
}
