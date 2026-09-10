package com.amcsoftware.sidebar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.amcsoftware.sidebar.Entidades.Mercancia;
import com.amcsoftware.sidebar.Entidades.Proveedores;
import com.amcsoftware.sidebar.Entidades.Ventas;
import com.amcsoftware.sidebar.adapter.ProveedoresAdapter;
import com.amcsoftware.sidebar.adapter.MercanciasVentasAdapter;
import com.amcsoftware.sidebar.adapter.VentasAdapter;
import com.amcsoftware.sidebar.listener.OnSubtotalChangeListener;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ComprasFragment extends Fragment implements Response.Listener<JSONObject>,
        Response.ErrorListener, AdapterView.OnItemSelectedListener, OnSubtotalChangeListener {
    //Variables locales para instanciar objetos
    private android.app.AlertDialog alertDialog;
    private android.app.AlertDialog.Builder builder;
    private ArrayList<Proveedores>   listaProveedores;
    private ArrayList<Mercancia> listaMercancia;
    private ProveedoresAdapter adapter2;
    // 'subtotal' ahora se usará como la variable que mantiene el total
    // actual de los ítems en el RecyclerView.
    private double subtotal, total, saldo, vpagado;
    private EditText txtidfc,txttotal,txtsaldo,txtvpagado;
    private JSONObject jsonObject = null;
    private JsonObjectRequest jsonObjectRequest;
    int n;
    private MercanciasVentasAdapter adapter1;
    private RecyclerView recyclerProveedores;
    private ProgressBar progressBar;
    private RequestQueue requestQueue,request;
    private Spinner spnrfpago;
    private String idfc1,cliente, idcl, fpago, saldo1, total1, vpagado1, nit1, rsocial1, tel1,
            cel1, dir1, ciudad1;
    StringBuilder codpv,descv,preciov,cantv,subtv,
            obsv,auxcodpv,auxdescv,auxpreciov,auxcantv,auxsubtv,auxobsv;
    private VentasAdapter adapter;
    private TextView txtcliente;
    private TextView txtidcl;

    public ComprasFragment() {
        // Required empty public constructor
    }


    @SuppressLint({"MissingInflatedId", "SetTextI18n", "DefaultLocale"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflar la vista
        View vista = inflater.inflate(R.layout.fragment_compras, container, false);
        //Instanciar objetos
        ImageButton btborrar = vista.findViewById(R.id.btnborrar);
        ImageButton btmerc = vista.findViewById(R.id.btnmercancia);
        ImageButton btguardar = vista.findViewById(R.id.btregistrarv);
        spnrfpago  = vista.findViewById(R.id.spnrfpago);
        ImageButton btcliente = vista.findViewById(R.id.btbcliente);
        ImageButton btncliente = vista.findViewById(R.id.btncliente);
        txtidfc     = vista.findViewById(R.id.txtidfc);
        txtidcl    = vista.findViewById(R.id.txtidcl);
        txtcliente = vista.findViewById(R.id.txtcliente);
        txttotal   = vista.findViewById(R.id.txtventat); // Asegúrate que este es el EditText para el total de la venta
        txtsaldo      = vista.findViewById(R.id.txtsaldo);
        txtvpagado    = vista.findViewById(R.id.txtvpagado);
        progressBar   =  vista.findViewById(R.id.progressBar);
        RecyclerView recyclerVentasM = vista.findViewById(R.id.idRecycler);
        //Inicializar objetos
        builder = new android.app.AlertDialog.Builder(requireContext());
        listaProveedores     = new ArrayList<>();
        listaMercancia       = new ArrayList<>();
        ArrayList<Ventas> listaventas = new ArrayList<>();
        recyclerVentasM.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerVentasM.setHasFixedSize(true);
        // MODIFICACIÓN CLAVE 2: Pasar 'this' (el fragmento) como el listener a VentasAdapter.
        adapter     = new VentasAdapter(listaventas, this); // 'this' se refiere a esta instancia de VentasFragment
        recyclerVentasM.setAdapter(adapter); // Establece el adaptador al RecyclerView
        requestQueue    = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo POST
        request         = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET
        spnrfpago.setOnItemSelectedListener(this);//Selección de un item del spinner
        //Buscar datos en el servidor
        buscarMercancia();
        //Cargar datos iniciales
        loadData();
        //Buscar Cliente
        btcliente.setOnClickListener(v-> buscarProveedores());
        //Crear Cliente
        btncliente.setOnClickListener(v-> intputboxNProveedor());
        btmerc.setOnClickListener(v-> mostrarDialogoInput(getContext()));
        btguardar.setOnClickListener(v ->{
            // Obtener los valores ingresados
            idcl        = txtidcl.getText().toString().trim();
            // Usamos la variable 'subtotal' del fragmento que se mantiene actualizada.
            total1      = String.format(Locale.US, "%.0f", subtotal); // Asegurarse de que total1 tome el valor de 'subtotal'
            saldo1      = txtsaldo.getText().toString().trim();
            vpagado1    = txtvpagado.getText().toString().trim();
            idfc1       = txtidfc.getText().toString().trim();
            // Validar que los campos no estén vacíos
            if (idcl.isEmpty() || total1.isEmpty() || idfc1.isEmpty() || vpagado1.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }
            //Alert de confirmación
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setMessage("¿Está seguro de realizar esta operación?").setTitle("Softpymes");
            builder.setPositiveButton("Si", (dialog, which) -> guardarCompra());
            builder.setNegativeButton("No", (dialog, which) ->
                    Toast.makeText(getContext(),
                            "Compra cancelada",
                            Toast.LENGTH_SHORT).show());
            AlertDialog dialog = builder.create();
            dialog.show();//Mostrar el Alert
        });
        //limpiar objetos de los EditText de pagos/saldos (manteniendo el total de la venta)
        btborrar.setOnClickListener(v->{
            txtvpagado.setText("");
            txtsaldo.setText("");
            saldo    = 0;
            vpagado  = 0;
            txtvpagado.requestFocus();
        });
        //Calcular saldo
        txtvpagado.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    vpagado = Double.parseDouble(txtvpagado.getText().toString());
                    total   = Double.parseDouble(txttotal.getText().toString()); // txttotal se actualiza con 'subtotal'
                    saldo   = total - vpagado;
                    txtsaldo.setText(String.format(Locale.US, "%.0f", saldo)); // Usar Locale.US para evitar problemas de coma/punto decimal
                } catch (NumberFormatException e) {
                    txtvpagado.setText(""); // Limpiar si no es un número
                    txtsaldo.setText("");
                    Toast.makeText(getContext(), "Ingrese un valor numérico para el valor pagado", Toast.LENGTH_SHORT).show();
                }
            }
            return false;
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

    private void listaProveedores(Context context) {
        builder.setTitle("Seleccionar Cliente:");
        // Inflar el layout personalizado con los objetos
        View viewInflada = LayoutInflater.from(context).inflate(R.layout.fragment_clientes_ventas, null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        final SearchView searchView = viewInflada.findViewById(R.id.txtbuscarcl);
        final ImageButton adcl = viewInflada.findViewById(R.id.btncliente);//botón agregar Cliente
        adcl.setVisibility(View.GONE);
        recyclerProveedores =  viewInflada.findViewById(R.id.idRecycler);
        adapter2         = new ProveedoresAdapter(listaProveedores);
        recyclerProveedores.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerProveedores.setHasFixedSize(true);
        recyclerProveedores.setAdapter(adapter2);
        //Asignar cliente seleccionado
        adapter2.setOnClickListener(v->{
            txtidcl.setText(listaProveedores.get(recyclerProveedores.getChildAdapterPosition(v)).getNit());
            txtcliente.setText(listaProveedores.get(recyclerProveedores.getChildAdapterPosition(v)).getRsocial());
            alertDialog.dismiss();
        });
        //Configurar botones del dialogo
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.cancel();
            //Borrar el campo de búsqueda y actualizar el recylerview
            searchView.setQuery("",false);
            searchView.requestFocus();
        } );
        builder.setOnDismissListener(dialog -> {
            //Borrar el campo de búsqueda y actualizar el recylerview
            searchView.setQuery("",false);
            searchView.requestFocus();
        });
        //Texto de búsqueda
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String Text) {
                adapter2.filtrado(Text);
                return true;
            }

        });

        //Inflar la vista del alert
        builder.setView(viewInflada);
        //builder.show();
        alertDialog = builder.create();
        alertDialog.show();
    }



    public void mostrarDialogoInput(Context context) {
        builder.setTitle("Seleccionar Mercancía:");
        // Inflar el layout personalizado con el EditText
        View viewInflada = LayoutInflater.from(context).inflate(R.layout.fragment_mercancias_ventas, null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        final SearchView searchView = viewInflada.findViewById(R.id.txtbuscar);
        final ImageButton admp = viewInflada.findViewById(R.id.btnmerc);//botón agregar mercancia
        RecyclerView recyclerMercancia = viewInflada.findViewById(R.id.idRecycler);
        adapter1    = new MercanciasVentasAdapter(listaMercancia);
        recyclerMercancia.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerMercancia.setHasFixedSize(true);
        recyclerMercancia.setAdapter(adapter1);
        // Configurar los botones del diálogo
        admp.setOnClickListener(v->{
            //Asignar los items seleccionados
            List<Mercancia> selectedItems = adapter1.getSelectedDataOnly();
            //Validar items
            if (selectedItems.isEmpty()) {
                Toast.makeText((getContext()),"Ninguno seleccionado",Toast.LENGTH_SHORT).show();
            } else {
                n   = selectedItems.size();
                //Recorrer 'Items Seleccionados'
                for (int i = 0; i < n; i++) {
                    String idp = selectedItems.get(i).getIdp();
                    String desc = selectedItems.get(i).getDescripcion();
                    String precioStr = selectedItems.get(i).getPreciov();
                    String cantStr = selectedItems.get(i).getCantidad();
                    String subtStr = selectedItems.get(i).getSubtotal();
                    if (cantStr.isEmpty() || Double.parseDouble(cantStr) <= 0) {
                        Toast.makeText((getContext()), "Asigne una cantidad válida para " + desc + "!", Toast.LENGTH_SHORT).show();
                    } else {
                        //llenar el recycleview Factura_Compras
                        Ventas ventas = new Ventas(idp, desc, precioStr, cantStr, subtStr);
                        adapter.addDatos(ventas);
                        Toast.makeText((getContext()), "Mercancia:" + desc + "(" + cantStr + ")", Toast.LENGTH_SHORT).show();
                        //asignar elementos para el envío de la venta (si lo necesitas)
                        codpv.append(idp).append("-");
                        descv.append(desc).append("-");
                        preciov.append(precioStr).append("-");
                        cantv.append(cantStr).append("-");
                        subtv.append(subtStr).append("-");
                        obsv.append(desc).append("(").append(cantStr).append(")").append(";");
                        //Calcular el total
                        try {
                            subtotal += Double.parseDouble(subtStr); // Acumula el subtotal en la variable del Fragment.
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Error al calcular total del ítem: " + desc, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                txtvpagado.requestFocus();//activar el foco
                txttotal.setText(String.format(Locale.US,"%.0f",subtotal)); // Actualiza el EditText del total.
                // Reiniciar saldo y valor pagado si se añaden nuevos ítems y la venta es "nueva"
                txtvpagado.setText("");
                txtsaldo.setText("");
                saldo = subtotal; // Inicializa el saldo con el nuevo total
                txtsaldo.setText(String.format(Locale.US, "%.0f", saldo)); // Muestra el saldo actualizado
            }
            //Borrar el campo de búsqueda y actualizar el recylerview
            searchView.setQuery("",false);
            searchView.requestFocus();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.cancel();
            //Borrar el campo de búsqueda y actualizar el recylerview
            searchView.setQuery("",false);
            searchView.requestFocus();
        } );
        builder.setOnDismissListener(dialog -> {
            //Borrar el campo de búsqueda y actualizar el recylerview
            searchView.setQuery("",false);
            searchView.requestFocus();
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String Text) {
                adapter1.filtrado(Text);
                return true;
            }

        });

        //Inflar la vista del alert
        builder.setView(viewInflada);
        //builder.show();
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void intputboxNProveedor() {
        builder.setTitle("");
        // Inflar el layout personalizado con el EditText
        View viewInflada = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_input_proveedor, null);
        //Instanciar objetos
        final TextView idpr       = viewInflada.findViewById(R.id.lblidpr);
        final TextView nit        = viewInflada.findViewById(R.id.txtnit);
        final EditText rsocial    = viewInflada.findViewById(R.id.txtrsocial);
        final EditText tel        = viewInflada.findViewById(R.id.txttel);
        final EditText cel        = viewInflada.findViewById(R.id.txtcel);
        final EditText dir        = viewInflada.findViewById(R.id.txtdir);
        final EditText ciudad     = viewInflada.findViewById(R.id.txtciudad);
        final ImageButton btcerrar  = viewInflada.findViewById(R.id.btcerrar);
        final ImageButton btlimpiar = viewInflada.findViewById(R.id.btlimpiar);
        final ImageButton btguardar = viewInflada.findViewById(R.id.btguardar);
        //cerrar el alert
        btcerrar.setOnClickListener(v-> alertDialog.dismiss());
        //limpiar campos
        btlimpiar.setOnClickListener(v->{
            idpr.setText("");
            nit.setText("");
            rsocial.setText("");
            tel.setText("");
            cel.setText("");
            dir.setText("");
            ciudad.setText("");
            nit.requestFocus();
        });
        //Guardar el nuevo proveedor
        btguardar.setOnClickListener(v->{
            if (nit.getText().toString().isEmpty() || rsocial.getText().toString().isEmpty() ||
                    tel.getText().toString().isEmpty() || cel.getText().toString().isEmpty() || ciudad.getText().toString().isEmpty() ||
                    dir.getText().toString().isEmpty()){
                Toast.makeText(getContext(), "Debe completar todos los campos!", Toast.LENGTH_SHORT).show();
            }else{
                //Confirmar operación
                android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(requireContext());
                builder1.setMessage("¿Está seguro de registrar este proveedor?").setTitle("Softpymes");
                builder1.setPositiveButton("Si", (dialog1, which1) -> {
                    nit1        = nit.getText().toString().trim();
                    rsocial1    = rsocial.getText().toString().trim();
                    tel1        = tel.getText().toString().trim();
                    cel1        = cel.getText().toString().trim();
                    dir1        = dir.getText().toString().trim();
                    ciudad1     = ciudad.getText().toString().trim();
                    guardarRegistro();
                    alertDialog.dismiss();
                });
                builder1.setNegativeButton("No", (dialog1, which1) ->
                        Toast.makeText(getContext(),
                                "Operación cancelada",
                                Toast.LENGTH_SHORT).show());
                android.app.AlertDialog dialog1 = builder1.create();
                dialog1.show();//Mostrar
            }
        });

        //Inflar la vista del alert
        builder.setView(viewInflada);
        //builder.show();
        alertDialog = builder.create();
        alertDialog.show();
    }
    
    private void buscarProveedores() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaProveedores.php";
        // Mostrar la ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }

    private void buscarMercancia() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaMercancia.php";
        // Mostrar la ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }

    private void guardarRegistro() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/guardarProveedor.php";
        // Mostrar la ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    // Ocultar ProgressBar
                    progressBar.setVisibility(View.GONE);
                    // Manejar la respuesta del servidor
                    try {
                        jsonObject = new JSONObject(response);
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), jsonObject.optString("mensaje"), Toast.LENGTH_SHORT).show();
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                    }
                    Toast.makeText(getContext(), jsonObject.optString("mensaje"), Toast.LENGTH_SHORT).show();
                },
                error -> {
                    // Ocultar ProgressBar
                    progressBar.setVisibility(View.GONE);
                    // Manejar errores
                    Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("nit", nit1);
                params.put("rsocial",rsocial1);
                params.put("tel",tel1);
                params.put("cel", cel1);
                params.put("dir", dir1);
                params.put("ciudad", ciudad1);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

    private void loadData() {
        /*--------------------*/
        // Inicialización de StringBuilders para evitar NullPointerException.
        // Reiniciar antes de cada nueva compra.
        auxcodpv   = new StringBuilder();
        auxdescv   = new StringBuilder();
        auxpreciov = new StringBuilder();
        auxcantv   = new StringBuilder();
        auxsubtv   = new StringBuilder();
        auxobsv    = new StringBuilder();
        codpv   = new StringBuilder();
        descv   = new StringBuilder();
        preciov = new StringBuilder();
        cantv   = new StringBuilder();
        subtv   = new StringBuilder();
        obsv    = new StringBuilder();
        /*------------------*/
        // Inicialización de variables numéricas para el estado inicial de la venta.
        subtotal  = 0; // El total acumulado de los ítems en el RecyclerView.
        vpagado   = 0;
        saldo     = 0;
        total     = 0; // Esta variable 'total' puede ser redundante si 'subtotal' es el total de la venta.
        // Mantendré 'subtotal' como el total de ítems y 'txttotal' como el EditText que lo muestra.
        txtidfc.setText("");
        txtvpagado.setText("");
        txttotal.setText("0"); // Asegurar que el campo de texto del total se inicialice en 0.
        txtsaldo.setText("0"); // Asegurar que el campo de texto del saldo se inicialice en 0.
        txtidfc.requestFocus();
    }

    private void guardarCompra() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/guardarCompra.php";
        // Mostrar la ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    // Ocultar ProgressBar
                    progressBar.setVisibility(View.GONE);
                    // Manejar la respuesta del servidor
                    try {
                        jsonObject = new JSONObject(response);
                    } catch (JSONException e) {
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                    }
                    Toast.makeText(getContext(), jsonObject.optString("mensaje"), Toast.LENGTH_SHORT).show();
                    if (jsonObject.optBoolean("success")) {
                        limpiar(); // limpiar después de una operación exitosa
                    }

                },
                error -> {
                    // Ocultar ProgressBar
                    progressBar.setVisibility(View.GONE);
                    // Manejar errores
                    String errorMessage = "Error desconocido al guardar compra.";
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        String responseData = new String(error.networkResponse.data, StandardCharsets.UTF_8);

                        errorMessage = "Error del servidor (" + statusCode + "): ";

                        if (responseData.isEmpty()) {
                            errorMessage += "Respuesta vacía o error interno del servidor.";
                        } else {
                            // Intenta parsear la respuesta si no está vacía (podría ser HTML de error)
                            errorMessage += "Detalles: " + responseData;
                        }
                    } else if (error.getMessage() != null && error.getMessage().equals("End of input at character 0 of ")) {
                        errorMessage = "El servidor no envió una respuesta. Verifica tu script PHP.";
                    } else if (error instanceof NoConnectionError) {
                        errorMessage = "No hay conexión a Internet. Por favor, verifica tu conexión.";
                    } else if (error instanceof TimeoutError) {
                        errorMessage = "Tiempo de espera agotado. El servidor no respondió a tiempo.";
                    } else {
                        errorMessage += error.getMessage(); // Mensaje genérico de Volley
                    }

                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("GuardarCompra", "Error: " + errorMessage, error); // Para ver el stack trace completo

                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("idfc", idfc1);
                params.put("nit", idcl);//nit del proveedor
                params.put("idp", String.valueOf(codpv).trim());
                params.put("precio", String.valueOf(preciov).trim());
                params.put("cant", String.valueOf(cantv).trim());
                params.put("subtotal", String.valueOf(subtv).trim());
                params.put("total", total1); // total1 ya se actualiza desde 'subtotal' antes de guardar
                params.put("vpagado", vpagado1);
                params.put("saldo", saldo1);
                params.put("fpago",fpago);
                params.put("obs", String.valueOf(obsv).trim());
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

    private void limpiar() {
        txtidfc.setText("");
        txtidcl.setText("");
        txtcliente.setText("");
        txttotal.setText("0"); // Asegurar que el total visual se resetee a 0
        txtvpagado.setText("");
        txtsaldo.setText("0"); // Asegurar que el saldo visual se resetee a 0
        /*--------------------*/
        // Limpiar los StringBuilders para la próxima compra
        auxcodpv.setLength(0);
        auxdescv.setLength(0);
        auxpreciov.setLength(0);
        auxcantv.setLength(0);
        auxsubtv.setLength(0);
        auxobsv.setLength(0);
        codpv.setLength(0);
        descv.setLength(0);
        preciov.setLength(0);
        cantv.setLength(0);
        subtv.setLength(0);
        obsv.setLength(0);
        /*------------------*/
        // Resetear las variables numéricas
        subtotal  = 0; // Reiniciar la variable del total acumulado en el fragmento
        vpagado   = 0;
        saldo     = 0;
        total     = 0;
        /*------------------*/
        // Limpiar el RecyclerView de las ventas.
        // Esto también notificará a este fragmento a través de onSubtotalChanged
        // que el subtotal ha cambiado a 0 (si estaba lleno).
        adapter.clearData();
        txtidfc.requestFocus();
    }

    //Traer datos entre fragments: siguiente y a anterior
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("clientes", this, (clientes, bundle) -> {
            cliente  = bundle.getString("cliente");
            idcl     = bundle.getString("idcl");
            txtidcl.setText(idcl);
            txtcliente.setText(cliente);
        });
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // Manejar la respuesta
        progressBar.setVisibility(View.GONE); // Asegurar que la barra de progreso se oculte en caso de error
        Toast.makeText((getContext()),"No se pudo consultar: " + error.getMessage(),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResponse(JSONObject response) {
        /*-- Lista de mercancías --*/
        JSONArray jsonMercancia = response.optJSONArray("mercancia");
        if (jsonMercancia != null) {
            try {
                listaMercancia.clear(); // Limpiar lista antes de añadir nuevos datos
                for (int i = 0; i < jsonMercancia.length(); i++){
                    Mercancia mercancia = new Mercancia();
                    JSONObject jsonObjectMercancia = jsonMercancia.getJSONObject(i);
                    mercancia.setIdp(jsonObjectMercancia.optString("idp"));
                    mercancia.setDescripcion(jsonObjectMercancia.optString("detalle"));
                    mercancia.setPrecioc(jsonObjectMercancia.optString("precio_compra"));
                    mercancia.setPreciov(jsonObjectMercancia.optString("precio_venta"));
                    mercancia.setCantidad(jsonObjectMercancia.optString("cantidad_inicial"));
                    listaMercancia.add(mercancia);
                }
            } catch (JSONException e) {
                Toast.makeText((getContext()),"Error al procesar mercancía: " + e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
        /*-- Lista de proveedores --*/
        Proveedores proveedor;
        JSONArray json = response.optJSONArray("proveedor");
        if (json != null) {
            try {
                listaProveedores.clear(); // Limpiar lista antes de añadir nuevos datos
                for (int i = 0; i< json.length(); i++){
                    proveedor = new Proveedores();
                    JSONObject jsonObject;
                    jsonObject = json.getJSONObject(i);

                    proveedor.setIdpr(jsonObject.optString("idpr"));
                    proveedor.setNit(jsonObject.optString("nit"));
                    proveedor.setRsocial(jsonObject.optString("rsocial"));
                    proveedor.setTelefono(jsonObject.optString("telefono"));
                    proveedor.setCelular(jsonObject.optString("celular"));
                    proveedor.setDireccion(jsonObject.optString("direccion"));
                    proveedor.setCiudad(jsonObject.optString("ciudad"));
                    listaProveedores.add(proveedor);
                }
            } catch (JSONException e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText((getContext()),"Error al procesar proveedores: " + e.getMessage(),Toast.LENGTH_SHORT).show();
            }
            listaProveedores(getContext());
        }
        progressBar.setVisibility(View.GONE); // Ocultar ProgressBar una vez que ambas cargas han terminado
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        fpago  = spnrfpago.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        fpago  = "";
    }

    // MODIFICACIÓN CLAVE 3: Implementación del método de la interfaz OnSubtotalChangeListener
    @Override
    public void onSubtotalChanged(double subtotalChange,int position) {
        auxcodpv = new StringBuilder(codpv);
        auxdescv = new StringBuilder(descv);
        auxcantv = new StringBuilder(cantv);
        auxpreciov = new StringBuilder(preciov);
        auxsubtv   = new StringBuilder(subtv);
        auxobsv    = new StringBuilder(obsv);

        final String separador = "-";
        auxcodpv = removeElementAtPosition(codpv,position, separador);
        auxdescv = removeElementAtPosition(descv,position, separador);
        auxcantv = removeElementAtPosition(cantv,position, separador);
        auxpreciov = removeElementAtPosition(preciov,position, separador);
        auxsubtv = removeElementAtPosition(subtv,position, separador);
        auxobsv = removeElementAtPosition(obsv,position,";");
        // Este método es invocado por el VentasAdapter cada vez que el subtotal de la lista cambia
        // (específicamente, cuando un ítem es eliminado, el 'subtotalChange' será negativo).

        // Actualiza la variable 'subtotal' del fragmento con el cambio recibido.
        subtotal += subtotalChange;

        // Asegúrate de que el subtotal no sea negativo (si por alguna razón llega a serlo, lo fijamos en 0).
        if (subtotal < 0) {
            subtotal = 0;
        }

        // Actualiza el EditText que muestra el total de la venta.
        txttotal.setText(String.format(Locale.US, "%.0f", subtotal));

        // También, recalcula y actualiza el saldo, ya que el total de la venta ha cambiado.
        // Aquí asumimos que 'vpagado' tiene el valor actual ingresado por el usuario.
        // Si no se ha ingresado nada, o si txtvpagado está vacío, se asume 0 para el cálculo.
        double valorPagadoActual = 0;
        try {
            if (!txtvpagado.getText().toString().isEmpty()) {
                valorPagadoActual = Double.parseDouble(txtvpagado.getText().toString());
            }
        } catch (NumberFormatException e) {
            Log.e("ComprasFragment", "Error al parsear vpagado en onSubtotalChanged: " + e.getMessage());
            // No es necesario mostrar un Toast aquí, ya que el usuario no está interactuando activamente con el campo.
        }
        saldo = subtotal - valorPagadoActual;
        txtsaldo.setText(String.format(Locale.US, "%.0f", saldo));
        codpv = auxcodpv;
        descv = auxdescv;
        cantv = auxcantv;
        preciov = auxpreciov;
        subtv   = auxsubtv;
        obsv    = auxobsv;
    }
    //Remover un item de array cuando se elimina un elemento del recyclerview
    public static StringBuilder removeElementAtPosition(StringBuilder sb, int position, String separator) {
        // 1. Dividir el StringBuilder en sus elementos lógicos
        String fullString = sb.toString();
        String[] elementsArray = fullString.split(separator);
        // 2. Validar la posición
        if (position < 0 || position >= elementsArray.length) {
            System.out.println("Error: Posición " + position + " fuera de rango. No se realizó ninguna eliminación.");
            return sb;
        }
        // 3. Crear una lista mutable para eliminar el elemento fácilmente
        List<String> elementsList = new ArrayList<>(Arrays.asList(elementsArray));
        // 4. Eliminar el elemento en la posición especificada
        elementsList.remove(position);
        // 5. Reconstruir el StringBuilder a partir de la lista modificada
        sb.setLength(0); // Vaciar el StringBuilder existente
        for (int i = 0; i < elementsList.size(); i++) {
            sb.append(elementsList.get(i));
            sb.append(separator);
        }
        return sb;
    }
}
