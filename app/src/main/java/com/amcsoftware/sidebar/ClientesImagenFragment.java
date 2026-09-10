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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import android.util.Base64; // Importar Base64 para la codificación
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amcsoftware.sidebar.Entidades.Cliente;
import com.amcsoftware.sidebar.api.ApiService;
import com.amcsoftware.sidebar.api.RetrofitClient;
import com.amcsoftware.sidebar.model.ResponseModel;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.StringRequest; // Para enviar datos de imagen
import com.bumptech.glide.Glide;

import org.json.JSONException; // Para errores de parsing JSON
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap; // Para parámetros
import java.util.List;
import java.util.Locale;
import java.util.Map; // Para parámetros
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;


public class ClientesImagenFragment extends Fragment {
    // Variables locales
    EditText txtcliente, txtid, txtcelular, txtdir, txtcodeudor, txtcelcodeudor, txtobservacion;
    ImageButton btregistrar, btfoto, btbuscarcl;
    ImageView foto;
    JSONObject jsonResponse = null;
    private ProgressBar progressBar;
    RequestQueue request, requestQueue;
    private Bitmap bitmap;
    private String nombre,id,direccion,celular,codeudor,celularcode,
            observacion, mensaje, rutaImagen;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri>    cameraLauncher;
    private Uri currentCameraPhotoUri,imageUri,selectedImageUri; // Para almacenar la URI de la foto tomada con la cámara.
    private ImageDecoder.Source source;
    private ContentResolver contentResolver;

    public ClientesImagenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
                            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(requireContext(), "Permisos denegados. La funcionalidad de la cámara puede no estar disponible.", Toast.LENGTH_LONG).show();
                }
            }
    );

    private boolean allPermissionsGranted() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
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
        View vista = inflater.inflate(R.layout.fragment_clientes_imagen, container, false);
        txtcliente = vista.findViewById(R.id.lblcliente);
        txtid = vista.findViewById(R.id.txtid);
        txtcelular = vista.findViewById(R.id.txtcelular);
        txtdir = vista.findViewById(R.id.txtdir);
        txtcodeudor = vista.findViewById(R.id.txtcodeudor);
        txtcelcodeudor = vista.findViewById(R.id.txtcelcodeudor);
        txtobservacion = vista.findViewById(R.id.txtobservacion);
        btregistrar = vista.findViewById(R.id.btaceptar);
        btfoto      = vista.findViewById(R.id.btfotocl);
        btbuscarcl  = vista.findViewById(R.id.btbuscarcl);
        foto   = vista.findViewById(R.id.imgFoto);
        progressBar = vista.findViewById(R.id.progressBar);
        //Inicializar
        request      = Volley.newRequestQueue(requireContext());
        requestQueue = Volley.newRequestQueue(requireContext());

        request.getCache().clear();

        // Verifica y solicita permisos al crear la vista del fragmento
        if (!allPermissionsGranted()) {
            requestPermissions();
        }
        //Buscar cliente
        btbuscarcl.setOnClickListener(v-> {
            //mensaje = "Buscar cliente";
            //msgBox(mensaje);
            cargarWebService();
        });
        //Guardar el cliente
        btregistrar.setOnClickListener(v -> {
            // Obtener los valores ingresados
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
                Toast.makeText(getContext(), "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }else if (selectedImageUri == null || bitmap == null){
                Toast.makeText(getContext(), "Por favor, selecciona o toma una foto.", Toast.LENGTH_SHORT).show();
                return;
            }
            //Alert de confirmación
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
            builder.setMessage("¿Está seguro de realizar esta operación?").setTitle("Softpymes");
            builder.setPositiveButton("Si", (dialog, which) -> guardarClienteConFoto());
            builder.setNegativeButton("No", (dialog, which) ->
                    Toast.makeText(getContext(),
                            "Operación cancelada",
                            Toast.LENGTH_SHORT).show());
            androidx.appcompat.app.AlertDialog dialog = builder.create();
            dialog.show();//Mostrar el Alert
        });
        //Capturar imagen
        btfoto.setOnClickListener(v -> mostrarDialogOpciones());

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
    //Buscar  Cliente
    private void cargarWebService() {
        final String id = txtid.getText().toString().trim();
        if (id.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, ingrese la cedula.", Toast.LENGTH_SHORT).show();
            return;
        }
        btbuscarcl.setEnabled(false);
        btbuscarcl.setImageResource(R.drawable.buscarcliente1);
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ResponseModel> call = apiService.getCliente(id);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseModel> call, @NonNull Response<ResponseModel> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ResponseModel data = response.body();
                    if (data.isSuccess()) {
                        if (data.getCliente() != null && !data.getCliente().isEmpty()) {
                            Cliente cliente = data.getCliente().get(0);
                            txtcliente.setText(cliente.getNombre());
                            txtcelular.setText(cliente.getCelular());
                            txtdir.setText(cliente.getDireccion());
                            txtcodeudor.setText(cliente.getCodeudor());
                            txtcelcodeudor.setText(cliente.getCelularcode());
                            txtobservacion.setText(cliente.getObservacion());
                            rutaImagen = cliente.getRfoto();
                            if (rutaImagen != null && !rutaImagen.isEmpty()) {
                                String BASE_URL = "https://www.wmcsoftware.net/";
                                String urlCompleta = BASE_URL + rutaImagen;
                                Glide.with(requireContext())
                                        .load(urlCompleta)
                                        .placeholder(R.drawable.clientes) // mientras carga
                                        .error(R.drawable.clientes)       // si falla
                                        .into(foto);
                            } else {
                                foto.setImageResource(R.drawable.clientesfoto);
                            }
                            enabledEditext();
                            mensaje = "Cliente registrado."+"\n"+"Score:" + cliente.getScore();
                            msgBox(mensaje);
                        } else {
                            clearEditext();
                            Toast.makeText(getContext(), data.getMensaje(), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(getContext(), data.getMensaje(), Toast.LENGTH_SHORT).show();
                        clearEditext();
                    }

                } else {
                    Toast.makeText(getContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show();
                }
                btbuscarcl.setEnabled(true);
                btbuscarcl.setImageResource(R.drawable.buscarcliente);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseModel> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                clearEditext();
                btbuscarcl.setEnabled(true);
                btbuscarcl.setImageResource(R.drawable.buscarcliente);
            }
        });
    }
     //Guardar Cliente con Retrofit
    private void guardarClienteConFoto() {
        if(bitmap == null){
            Toast.makeText(getContext(),"Debe seleccionar una foto",Toast.LENGTH_SHORT).show();
            return;
        }
        String fotoBase64 = convertirImagenBase64(bitmap);
        progressBar.setVisibility(View.VISIBLE);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.guardarCliente(
                id,
                nombre,
                direccion,
                celular,
                codeudor,
                celularcode,
                observacion,
                fotoBase64
        ).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseModel> call, @NonNull Response<ResponseModel> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Cliente guardado", Toast.LENGTH_SHORT).show();
                    limpiar();
                } else {
                    Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponseModel> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    //Guardar Cliente con libreria Volley
    private void guardarRegistro() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/guardarClienteImagen.php";
        // Mostrar la ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        //Petición POST
        StringRequest stringRequest = new StringRequest(
                com.android.volley.Request.Method.POST,
                url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        jsonResponse = new JSONObject(response);
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Error de respuesta del servidor.", Toast.LENGTH_LONG).show();
                    }
                    Toast.makeText(getContext(), jsonResponse.optString("mensaje"), Toast.LENGTH_SHORT).show();
                    if (jsonResponse.optBoolean("success")) {
                        limpiar(); // limpiar después de una operación exitosa
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error de red: ", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                bitmap = redimensionarImagen(bitmap);
                final String imagen = convertirImgString(bitmap);
                Map<String, String> params = new HashMap<>();
                // Añadir los datos de tu formulario
                params.put("id", id);
                params.put("nombre", nombre);
                params.put("direccion", direccion);
                params.put("celular", celular);
                params.put("codeudor", codeudor);
                params.put("celularcode", celularcode);
                params.put("observacion", observacion);
                params.put("foto", imagen); // Enviar la imagen codificada en Base64
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void limpiar() {
        txtid.setText("");
        txtcliente.setText("");
        txtdir.setText("");
        txtcelular.setText("");
        txtcodeudor.setText("");
        txtcelcodeudor.setText("");
        txtobservacion.setText("");
        foto.setImageURI(Uri.parse("@drawable/clientesfoto"));
        txtid.requestFocus();
    }
    private void clearEditext() {
        txtcliente.setText("");
        txtcelular.setText("");
        txtdir.setText("");
        txtcodeudor.setText("");
        txtcelcodeudor.setText("");
        txtobservacion.setText("");
        txtcliente.setEnabled(true);
        txtcelular.setEnabled(true);
        txtdir.setEnabled(true);
        txtcodeudor.setEnabled(true);
        txtcelcodeudor.setEnabled(true);
        txtobservacion.setEnabled(true);
        btregistrar.setEnabled(true);
        btfoto.setEnabled(true);
        btregistrar.setImageResource(R.drawable.aceptar);
        btfoto.setImageResource(R.drawable.camara);
        foto.setImageResource(R.drawable.clientesfoto);
        txtcliente.requestFocus();
    }
    private void enabledEditext(){
        txtcliente.setEnabled(false);
        txtcelular.setEnabled(false);
        txtdir.setEnabled(false);
        txtcodeudor.setEnabled(false);
        txtcelcodeudor.setEnabled(false);
        txtobservacion.setEnabled(false);
        btregistrar.setEnabled(false);
        btfoto.setEnabled(false);
        btregistrar.setImageResource(R.drawable.aceptar1);
        btfoto.setImageResource(R.drawable.camara1);
    }

    private void msgBox(String mensaje) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setMessage(mensaje).setTitle("Softpymes");
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();//Mostrar el Alert
    }

    private String convertirImagenBase64(Bitmap bitmap) {
        if (bitmap == null) return "";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Comprimir imagen (JPEG recomendado)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
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
                Toast.makeText(requireContext(), "Error al preparar la imagen: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(requireContext(), "Por favor, concede los permisos necesarios para usar la cámara.", Toast.LENGTH_SHORT).show();
            requestPermissions(); // Vuelve a solicitar si no se concedieron
        }

    }

    private void abriGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        galleryLauncher.launch(Intent.createChooser(intent, "Selecciona una imagen:"));
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

}