package com.amcsoftware.sidebar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.amcsoftware.sidebar.Entidades.Cobrador;
import com.amcsoftware.sidebar.Entidades.Factura_Ventas;
import com.amcsoftware.sidebar.Entidades.Vendedor;
import com.amcsoftware.sidebar.adapter.CobradoresAdapter;
import com.amcsoftware.sidebar.adapter.FacturaVentasAdapter;
import com.amcsoftware.sidebar.adapter.VendedoresAdapter;
import com.amcsoftware.sidebar.utils.AppUtils;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConsultaVentasFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener,
        SearchView.OnQueryTextListener  {

    AlertDialog alertDialog;
    AlertDialog.Builder builder;
    ArrayAdapter<String> adapterspn1, adapterspn2;
    ArrayList<Cobrador> listaCobradores;
    ArrayList<Factura_Ventas> listafacturaventas;
    ArrayList<Vendedor> listaVendedores;
    ArrayList<String> cobradorsList, vendedoresList;
    boolean isValid;
    double tmora, tsaldo;
    JSONObject jsonObject = null;
    JsonObjectRequest jsonObjectRequest;
    ImageButton btfecha, btpdf, btrefresh;
    int ncuotas;
    FacturaVentasAdapter adapter;
    CobradoresAdapter    adapter1;
    VendedoresAdapter    adapter2;
    RecyclerView recyclerFacturaVentas;
    RequestQueue request, requestQueue;
    SearchView txtbuscar;//buscador por vendedor
    SearchView txtbuscarcliente;//buscador por cliente
    String[]   parts, headers;
    String auxcb, auxvd, idfv, estadof, fechai, fechaf, idvd,
            bfecha, formattedDate, msj, ncobrador, nfile, titulo;
    float[] columnWidths;
    public ConsultaVentasFragment() {
        // Required empty public constructor
    }


    SweetAlertDialog pDialog;
    int peticionesPendientes = 0;

    private void mostrarCargando() {
        if (getContext() == null) return;
        peticionesPendientes++;
        if (pDialog == null || !pDialog.isShowing()) {
            pDialog = AppUtils.mostrarCargando(getContext(), "Cargando...", "Por favor espera.");
        }
    }

    private void cerrarCargando() {
        if (peticionesPendientes > 0) peticionesPendientes--;
        if (peticionesPendientes == 0) {
            AppUtils.cerrarCargando(pDialog);
        }
    }

    private void mostrarAlerta(boolean success, String mensaje) {
        if (getContext() == null) return;
        if (success) {
            AppUtils.alertExito(getContext(), "Éxito", mensaje);
        } else {
            AppUtils.alertError(getContext(), "Atención", mensaje);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_consulta_ventas, container, false);
        btfecha    = vista.findViewById(R.id.btfecha);
        txtbuscar  = vista.findViewById(R.id.txtbuscarv);
        txtbuscarcliente = vista.findViewById(R.id.txtbuscarcliente);
        btpdf      = vista.findViewById(R.id.btpdf);
        btrefresh  = vista.findViewById(R.id.btrefresh);

        listaCobradores       = new ArrayList<>();
        listafacturaventas    = new ArrayList<>();
        listaVendedores       = new ArrayList<>();

        recyclerFacturaVentas =  vista.findViewById(R.id.idRecycler);
        recyclerFacturaVentas.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerFacturaVentas.setHasFixedSize(true);

        adapter         = new FacturaVentasAdapter(listafacturaventas,getContext());
        adapter1        = new CobradoresAdapter(listaCobradores);
        adapter2        = new VendedoresAdapter(listaVendedores);

        request         = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET
        requestQueue    = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo POST
        bfecha          = "cws";
        estadof         = " ";
        builder = new AlertDialog.Builder(requireContext());

        cargarWebService();
        buscarCobradores();
        buscarVendedores();

        txtbuscar.setOnQueryTextListener(this);
        txtbuscar.requestFocus();
        txtbuscarcliente.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.filtrarPorCliente(s);
                return false;
            }
        });
        btfecha.setOnClickListener(v-> mostrarInputBox(getContext()));
        btpdf.setOnClickListener(v-> generarPdf(estadof));
        btrefresh.setOnClickListener(v-> {
            estadof = " ";
            listafacturaventas.clear();
            cargarWebService();
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



    private void obtenerLista() {
        cobradorsList = new ArrayList<>();
        cobradorsList.add(" ");
        for (int i=0; i < listaCobradores.size();i++){
            cobradorsList.add(listaCobradores.get(i).getIdcob()+"-"+listaCobradores.get(i).getNombre());
        }
    }

    private void obtenerLista1() {
        vendedoresList = new ArrayList<>();
        vendedoresList.add(" ");
        for (int i=0; i < listaVendedores.size();i++){
            vendedoresList.add(listaVendedores.get(i).getIdvd()+"-"+listaVendedores.get(i).getNombre());
        }
    }

    /**
     * Encola una petición Volley subiendo el timeout por intento.
     * El default de Volley es 2500 ms; el servidor PHP de wmcsoftware.net
     * a veces tarda más y lanzaba {@code com.android.volley.TimeoutError}.
     * 30 s por intento, 1 reintento, sin backoff exponencial.
     */
    private void encolar(Request<?> req, RequestQueue cola) {
        req.setRetryPolicy(new DefaultRetryPolicy(
                30000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        cola.add(req);
    }

    private void buscarCobradores() {
        // Mostrar el diálogo de carga
        mostrarCargando();
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaCobradores.php";
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        encolar(jsonObjectRequest, request);
    }

    private void buscarVendedores(){
        // Mostrar el diálogo de carga
        mostrarCargando();
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaVendedores.php";
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        encolar(jsonObjectRequest, request);
    }

    private void cargarWebService() {
        bfecha = "cws";
        // Mostrar el diálogo de carga
        mostrarCargando();
        String url = "https://www.wmcsoftware.net/apps/softpymes/consultaFacturaVentas.php";
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        encolar(jsonObjectRequest, request);
    }

    private void cargarWebService1(String fechai, String fechaf, String idvd, String estado) {
        bfecha = "cws1";
        mostrarCargando();

        String url = "https://www.wmcsoftware.net/apps/softpymes/consultaFVentaxfecha.php"
                + "?idvd=" + Uri.encode(idvd)
                + "&fechai=" + Uri.encode(fechai)
                + "&fechaf=" + Uri.encode(fechaf)
                + "&estado=" + Uri.encode(estado);
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, this, this);
        encolar(jsonObjectRequest, request);
    }
    //MOSTRAR COMBOBOX DE COBRADORES
    @SuppressLint("SetTextI18n")
    private void mostrarInputBoxCb(Context context, String idfv) {
        // Inflar el layout personalizado con el EditText
        View viewInflada = LayoutInflater.from(context).inflate(R.layout.dialog_input_cobradores, null);
        //Instanciar objetos
        final Spinner cob          = viewInflada.findViewById(R.id.spncob);
        final ImageButton btback   = viewInflada.findViewById(R.id.btnback);
        final ImageButton btaceptar = viewInflada.findViewById(R.id.btaceptar);
        adapterspn1 = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, cobradorsList);
        cob.setAdapter(adapterspn1);
        /* Programar botones de imagen */
        btback.setOnClickListener(v->{//Regresar a la consulta de pagos
            alertDialog.dismiss();
        });
        btaceptar.setOnClickListener(v->{
            idvd   = cob.getSelectedItem().toString();
            parts  =  idvd.split("-");//Convertir en vector el string
            idvd   = parts[0];
            auxcb  = parts[0];
            alertDialog.dismiss();
            listafacturaventas.clear();
            actualizarVenta(idfv,idvd);
        });
        //Inflar la vista del alert
        builder.setView(viewInflada);
        alertDialog = builder.create();
        alertDialog.show();
    }
    //MOSTRAR DIALOGO PARA BÚSQUEDA DE VENTAS X FECHA Y VENDEDOR
    @SuppressLint("SetTextI18n")
    private void mostrarInputBox(Context context) {
        // Inflar el layout personalizado con el EditText
        View viewInflada = LayoutInflater.from(context).inflate(R.layout.dialog_input_fechav, null);
        //Instanciar objetos
        final TextView lblfecha1    = viewInflada.findViewById(R.id.lblfechai);
        final TextView lblfecha2    = viewInflada.findViewById(R.id.lblfechaf);
        final TextView lblvd        = viewInflada.findViewById(R.id.lblvd);
        final TextView lblcb        = viewInflada.findViewById(R.id.lblcb);
        final EditText fecha1       = viewInflada.findViewById(R.id.txtfechai);
        final EditText fecha2       = viewInflada.findViewById(R.id.txtfechaf);
        final Spinner estado        = viewInflada.findViewById(R.id.spnest);
        final Spinner cob           = viewInflada.findViewById(R.id.spnvd);
        final Spinner cob1          = viewInflada.findViewById(R.id.spncob1);
        final ImageButton btback    = viewInflada.findViewById(R.id.btnback);
        final ImageButton btborrar  = viewInflada.findViewById(R.id.btnborrar);
        final ImageButton btaceptar = viewInflada.findViewById(R.id.btaceptar);

        estado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                estadof = estado.getSelectedItem().toString();
                switch (estadof) {
                    case "Cuotas":
                    case "Cartera":
                    case "Vencida":
                        // Ocultar: Fechas y Vendedor - Mostrar: Cobrador
                        lblfecha1.setVisibility(View.GONE);
                        fecha1.setVisibility(View.GONE);
                        lblfecha2.setVisibility(View.GONE);
                        fecha2.setVisibility(View.GONE);
                        lblvd.setVisibility(View.GONE);
                        cob.setVisibility(View.GONE); // Combobox vendedor
                        lblcb.setVisibility(View.VISIBLE);
                        cob1.setVisibility(View.VISIBLE); // Combobox cobrador
                        break;

                    case "Pagada":
                        // Ocultar: Vendedor - Mostrar: Fechas y cobrador
                        lblfecha1.setVisibility(View.VISIBLE);
                        fecha1.setVisibility(View.VISIBLE);
                        lblfecha2.setVisibility(View.VISIBLE);
                        fecha2.setVisibility(View.VISIBLE);
                        lblvd.setVisibility(View.GONE);
                        cob.setVisibility(View.GONE); // Combobox vendedor
                        lblcb.setVisibility(View.VISIBLE);
                        cob1.setVisibility(View.VISIBLE); // Combobox cobrador
                        break;

                    case "Pendiente":
                    default:
                        // Ocultar: Cobrador - Mostrar: Fechas y Vendedor
                        lblfecha1.setVisibility(View.VISIBLE);
                        fecha1.setVisibility(View.VISIBLE);
                        lblfecha2.setVisibility(View.VISIBLE);
                        fecha2.setVisibility(View.VISIBLE);
                        lblvd.setVisibility(View.VISIBLE);
                        cob.setVisibility(View.VISIBLE); // Combobox vendedor
                        lblcb.setVisibility(View.GONE);
                        cob1.setVisibility(View.GONE); // Combobox cobrador
                        break;

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                estadof = " ";
            }
        });

        //Asignar vendedores de la BD
        adapterspn1 = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, vendedoresList);
        cob.setAdapter(adapterspn1);
        //Asignar cobradores de la BD
        adapterspn2 = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, cobradorsList);
        cob1.setAdapter(adapterspn2);
        //Asignar el calendario al editext
        fecha1.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year  = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day   = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year1, monthOfYear, dayOfMonth) -> fecha1.setText(year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth), year, month, day);
            datePickerDialog.show();
        });
        fecha2.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year  = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day   = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year1, monthOfYear, dayOfMonth) -> fecha2.setText(year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth), year, month, day);
            datePickerDialog.show();
        });
        /* Programar botones de imagen */
        btback.setOnClickListener(v->{//Regresar a la consulta de pagos
            alertDialog.dismiss();
        });
        btborrar.setOnClickListener(v->{//Limpiar los editext
            fecha1.setText("");
            fecha2.setText("");
            fecha1.requestFocus();
        });
        btaceptar.setOnClickListener(v->{
            estadof = estado.getSelectedItem().toString();
            fechai  = " ";
            fechaf  = " ";
            idvd    = " ";
            msj     = "Seleccione los campos requeridos!";
            isValid = true;
            switch (estadof) {
                case "Pagada":
                    fechai = fecha1.getText().toString();
                    fechaf = fecha2.getText().toString();
                    idvd   = cob1.getSelectedItem().toString();//COMBOBOX COBRADOR
                    if (fechai.equals(" ") || fechaf.equals(" ") || idvd.equals(" ")) {
                        isValid = false;
                    }
                    break;
                case "Pendiente":
                    fechai = fecha1.getText().toString();
                    fechaf = fecha2.getText().toString();
                    idvd   = cob.getSelectedItem().toString();//COMBOBOX VENDEDOR

                    if (fechai.equals(" ") || fechaf.equals(" ") || idvd.equals(" ")) {
                        isValid = false;
                    }
                    break;
                case "Cuotas":
                default:
                    idvd = cob1.getSelectedItem().toString();//COMBOBOX COBRADOR
                    if (idvd.equals(" ")) {
                        isValid = false;
                    }
                    break;
            }

            if(isValid){
                parts = idvd.split("-");//Convertir en vector el string
                auxcb = parts[0];
                auxvd = parts[0];
                idvd  = parts[0];
                ncobrador = parts[1];
                alertDialog.dismiss();
                listafacturaventas.clear();
                cargarWebService1(fechai, fechaf, idvd, estadof);
            }else{
                mostrarAlerta(false, msj);
            }

        });
        //Inflar la vista del alert
        builder.setView(viewInflada);
        alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        adapter.filtrarPorVendedor(s);
        return false;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // Ocultar el diálogo de carga
        cerrarCargando();
        // Manejar la respuesta
        mostrarAlerta(false, "No se pudo consultar: " + error.toString());
    }

    @Override
    public void onResponse(JSONObject response) {
        //ADAPTER VENTAS
        Factura_Ventas factura_ventas;
        Cobrador cobrador;
        Vendedor vendedor;

        JSONArray json  = response.optJSONArray("fventas");
        JSONArray json1 = response.optJSONArray("cobrador");
        JSONArray json2 = response.optJSONArray("vendedor");

        final String  mensaje = response.optString("mensaje","");
        //CONSULTAR VENTAS
        try {
            for (int i = 0; i< (json != null ? json.length() : 0); i++){
                factura_ventas = new Factura_Ventas();
                JSONObject jsonObject;
                jsonObject = json.getJSONObject(i);
                factura_ventas.setIdfv(jsonObject.optString("idfv"));
                factura_ventas.setCliente(jsonObject.optString("cliente"));
                factura_ventas.setDircl(jsonObject.optString("direccion"));
                formattedDate = convertDateString(jsonObject.optString("fechav"));
                factura_ventas.setFecha(formattedDate);
                factura_ventas.setVendedor(jsonObject.optString("vendedor"));
                factura_ventas.setCobrador(jsonObject.optString("cobrador"));
                factura_ventas.setObservacion(jsonObject.optString("detalle"));
                factura_ventas.setMonto(jsonObject.optString("monto"));
                factura_ventas.setVmora(jsonObject.optString("vmora"));
                factura_ventas.setSaldo(jsonObject.optString("saldo"));
                factura_ventas.setScore(jsonObject.optString("score"));
                formattedDate = convertDateString(jsonObject.optString("plazo"));
                factura_ventas.setPlazo(formattedDate);
                factura_ventas.setCuotas(jsonObject.optString("cuotas"));
                factura_ventas.setVcuotas(jsonObject.optString("vcuota"));
                listafacturaventas.add(factura_ventas);
            }
            //Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
            adapter = new FacturaVentasAdapter(listafacturaventas,getContext());
            adapter.setOnClickListener(v->{
                //obtener el número de la factura
                idfv = listafacturaventas.get(recyclerFacturaVentas.getChildAdapterPosition(v)).getIdfv();
                mostrarInputBoxCb(getContext(),idfv);
            });
            recyclerFacturaVentas.setAdapter(adapter);

            // La clave "fventas" solo viene en la respuesta de la consulta de ventas
            // (cobradores/vendedores usan otras claves y comparten este mismo onResponse).
            // Si la consulta fue exitosa pero no trajo filas, avisar con el mismo SweetAlert
            // que usamos para éxito/error, en vez de dejar la lista vacía sin explicación.
            if (json != null && json.length() == 0 && getContext() != null) {
                AppUtils.alertAdvertencia(getContext(), "Sin resultados",
                        mensaje.isEmpty() ? "No se encontraron ventas." : mensaje);
            }
        } catch (JSONException e) {
            // Manejar la respuesta
            mostrarAlerta(false, mensaje);
        }
        //CONSULTAR COBRADORES
        try {
            for (int i = 0; i< (json1 != null ? json1.length() : 0); i++){
                cobrador = new Cobrador();
                JSONObject jsonObject1;
                jsonObject1 = json1.getJSONObject(i);
                cobrador.setIdcob(jsonObject1.optString("idcobrador"));
                cobrador.setNombre(jsonObject1.optString("nombre"));
                listaCobradores.add(cobrador);
            }
            obtenerLista();
        } catch (JSONException e) {
            // Manejar la respuesta
            mostrarAlerta(false, "No se pudo consultar!");
        }
        //CONSULTAR VENDEDORES
        try {
            for (int i = 0; i< (json2 != null ? json2.length() : 0); i++){
                vendedor = new Vendedor();
                JSONObject jsonObject;
                jsonObject = json2.getJSONObject(i);
                vendedor.setIdvd(jsonObject.optString("idvd"));
                vendedor.setNombre(jsonObject.optString("nombre"));
                listaVendedores.add(vendedor);
            }
            obtenerLista1();
        } catch (JSONException e) {
            // Manejar la respuesta
            mostrarAlerta(false, "No se pudo consultar!");
        }

        cerrarCargando();
    }

    private void actualizarVenta(String idfv, String idcb) {
        String url = "https://www.wmcsoftware.net/apps/softpymes/actualizarVenta.php";
        // Validar que los campos no estén vacíos
        if (idcb.equals(" ")) {
            mostrarAlerta(false, "No ha seleccionado el cobrador!");
            return;
        }
        // Mostrar el diálogo de carga
        mostrarCargando();
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    // Ocultar el diálogo de carga
                    cerrarCargando();
                    // Manejar la respuesta del servidor
                    try {
                        jsonObject = new JSONObject(response);
                        mostrarAlerta(jsonObject.optBoolean("success"), jsonObject.optString("mensaje"));
                        //refrescar le recyclerview
                        if (bfecha.equals("cws")){
                            cargarWebService();
                        }else{
                            if (estadof.equals("Cuotas") || estadof.equals("Pendiente")) {
                                cargarWebService1(fechai,fechaf,auxvd,estadof);
                            }else {
                                cargarWebService1(fechai,fechaf,auxcb,estadof);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                    }
                },
                error -> {
                    // Ocultar el diálogo de carga
                    cerrarCargando();
                    // Manejar errores
                    mostrarAlerta(false, error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("idfv", idfv);
                params.put("idcb", idcb);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        encolar(stringRequest, requestQueue);
    }
    //REPORTE PDF
    private void generarPdf(String tipo) {
        //Fecha y hora para el nombre del archivo
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fechahora = "Fecha y hora de impresión: " + new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
        switch (tipo) {
            case "Cartera":
                nfile = "/Cartera_Cobrador_" + ncobrador;
                titulo = "CARTERA X COBRADOR: " + ncobrador;
                headers = new String[]{"VENTA N°", "CLIENTE", "FECHA", "MORA", "SALDO"};
                break;
            case "Cuotas":
                nfile = "/Clientes_Ventas_" + ncobrador;
                titulo = "CLIENTES PROXIMOS A RENOVAR: " + ncobrador;
                headers = new String[]{"VENTA N°", "CLIENTE", "DIRECCION", "MONTO", "CUOTAS", "COBROS", "SCORE"};
                break;
            case "Pagada":
                nfile = "/Ventas_X_Cobrador_" + ncobrador;
                titulo = "VENTAS CANCELADAS- " + ncobrador;
                headers = new String[]{"VENTA N°", "CLIENTE", "FECHA", "PLAZO", "MONTO"};
                break;
            case "Pendiente":
                nfile = "/Resumen_X_Vendedor_" + ncobrador;
                titulo = "RESUMEN DE VENTAS - " + ncobrador;
                headers = new String[]{"VENTA N°", "CLIENTE", "FECHA", "MONTO", "SALDO"};
                break;
            case "Vencida":
                nfile = "/Resumen_Ventas_Vencidas_";
                titulo = "FACTURAS VENCIDAS X COBRADOR: " + ncobrador;
                headers = new String[]{"VENTA N°", "CLIENTE", "PLAZO", "DIAS", "MORA"};
                break;
            default:
                nfile = "/Resumen_General_";
                titulo = "RESUMEN DE VENTAS GENERAL";
                headers = new String[]{"VENTA N°", "CLIENTE", "FECHA", "MONTO", "SALDO"};
                break;
        }
        // Definir la ruta donde se guardará el archivo
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + nfile + timeStamp +".pdf";
        // Crear el documento
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();
            // Obtener la imagen y ajustar su tamaño
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_negocio1);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Image logo = Image.getInstance(stream.toByteArray());
            logo.scaleToFit(250, 250); // Redimensionar el logo a 50x50 puntos
            // Posicionar el logo en la esquina superior izquierda
            logo.setAbsolutePosition(20, 740);
            document.add(logo);
            // Definir una fuente para el título
            Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 16, BaseColor.BLACK);
            // Crear el párrafo del título y centrarlo
            Paragraph title = new Paragraph(titulo, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            // Agregar un título al documento
            document.add(title);
            document.add(new Paragraph("\n")); // Salto de línea
            // Definir una fuente para el subtítulo2
            Font subtitleFont2 = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10, BaseColor.BLACK);
            // Crear el párrafo del título y alinearlo
            Paragraph subtitle = new Paragraph(fechahora, subtitleFont2);
            subtitle.setAlignment(Element.ALIGN_LEFT);
            // Agregar un subtítulo2 al documento
            document.add(subtitle);
            document.add(new Paragraph("\n")); // Salto de línea
            // Definir anchos relativos para 3 columnas (por ejemplo, 10% para la primera, 45% para las otras dos)
            if (tipo.equals("Cuotas")) {
                columnWidths = new float[]{1.2f, 1.9f, 2.3f, 1.1f, 1.2f, 1.2f, 1.1f};
            } else {
                columnWidths = new float[]{1.5f, 3.5f, 2f, 1.5f, 1.5f};
            }
            PdfPTable table = new PdfPTable(columnWidths);
            //table.setWidthPercentage(100); // Ocupa el 100% del ancho de la página
            // Definir una fuente para los encabezados
            Font headerFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 10, BaseColor.WHITE);
            // Definir un color de fondo para las celdas de encabezado
            BaseColor headerColor = new BaseColor(0, 169, 143); // Un color verde
            // Definir los encabezados de la tabla
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(headerColor);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER); // Alinear el texto al centro
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE); // Alinear verticalmente al centro
                table.addCell(cell);
            }
            // Llenar la tabla con datos
            tsaldo = 0;
            tmora  = 0;
            if (adapter != null && adapter.getItemCount() > 0) {
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    Factura_Ventas facturaVentas1= adapter.getItemAtPosition(i);
                    if (facturaVentas1 != null) {
                        // Las celdas alineadas y communes
                        PdfPCell idCell = new PdfPCell(new Phrase(facturaVentas1.getIdfv(), subtitleFont2));
                        idCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(idCell); // ID venta

                        PdfPCell clienteCell = new PdfPCell(new Phrase(facturaVentas1.getCliente(), subtitleFont2));
                        clienteCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        table.addCell(clienteCell); // CLIENTE
                        switch (tipo) {
                            case "Cartera": {
                                //calcular el monto y saldo de la cartera
                                tsaldo += Double.parseDouble(facturaVentas1.getSaldo());
                                tmora += Double.parseDouble(facturaVentas1.getVmora());

                                PdfPCell fechaCell = new PdfPCell(new Phrase(facturaVentas1.getFecha(), subtitleFont2));
                                fechaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(fechaCell); // FECHA

                                PdfPCell cuotaCell = new PdfPCell(new Phrase(facturaVentas1.getVmora(), subtitleFont2));
                                cuotaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(cuotaCell); // MORA

                                PdfPCell saldoCell = new PdfPCell(new Phrase(facturaVentas1.getSaldo(), subtitleFont2));
                                saldoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(saldoCell); // SALDO

                                break;
                            }
                            case "Cuotas": {
                                // Las celdas alineadas
                                PdfPCell dirCell = new PdfPCell(new Phrase(facturaVentas1.getDircl(), subtitleFont2));
                                dirCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(dirCell); // DIRECCION

                                PdfPCell montoCell = new PdfPCell(new Phrase(facturaVentas1.getMonto(), subtitleFont2));
                                montoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(montoCell); // MONTO

                                ncuotas = Integer.parseInt(facturaVentas1.getMonto()) / Integer.parseInt(facturaVentas1.getVcuotas());
                                PdfPCell cuotaCell = new PdfPCell(new Phrase(String.valueOf(ncuotas), subtitleFont2));
                                cuotaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(cuotaCell); // CUOTAS

                                PdfPCell cobroCell = new PdfPCell(new Phrase(facturaVentas1.getCuotas(), subtitleFont2));
                                cobroCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(cobroCell); // COBROS

                                PdfPCell scoreCell = new PdfPCell(new Phrase(facturaVentas1.getScore(), subtitleFont2));
                                scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(scoreCell); // SCORE

                                break;
                            }
                            case "Pagada": {
                                //calcular el monto y saldo de la cartera
                                tmora += Double.parseDouble(facturaVentas1.getMonto());
                                // Las celdas alineadas
                                PdfPCell fechaCell = new PdfPCell(new Phrase(facturaVentas1.getFecha(), subtitleFont2));
                                fechaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(fechaCell); // FECHA

                                PdfPCell plazoCell = new PdfPCell(new Phrase(facturaVentas1.getPlazo(), subtitleFont2));
                                plazoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(plazoCell); // PLAZO

                                PdfPCell cuotaCell = new PdfPCell(new Phrase(facturaVentas1.getMonto(), subtitleFont2));
                                cuotaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(cuotaCell); // MONTO

                                break;
                            }
                            case "Vencida": {
                                //calcular el monto y saldo de la cartera
                                tmora += Double.parseDouble(facturaVentas1.getVmora());
                                // Las celdas alineadas
                                PdfPCell plazoCell = new PdfPCell(new Phrase(facturaVentas1.getPlazo(), subtitleFont2));
                                plazoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(plazoCell); // PLAZO

                                // Extraer los días de diferencia
                                long daysDifference = getDaysDifference(facturaVentas1.getPlazo());
                                PdfPCell diasCell = new PdfPCell(new Phrase(String.valueOf(daysDifference), subtitleFont2));
                                diasCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(diasCell); // DIAST

                                PdfPCell cuotaCell = new PdfPCell(new Phrase(facturaVentas1.getVmora(), subtitleFont2));
                                cuotaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(cuotaCell); // MORA

                                break;
                            }
                            case "Pendiente":
                            default: {
                                //calcular el monto y saldo de la cartera
                                tsaldo += Double.parseDouble(facturaVentas1.getSaldo());
                                tmora += Double.parseDouble(facturaVentas1.getMonto());
                                // Las celdas alineadas
                                PdfPCell fechaCell = new PdfPCell(new Phrase(facturaVentas1.getFecha(), subtitleFont2));
                                fechaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(fechaCell); // FECHA

                                PdfPCell cuotaCell = new PdfPCell(new Phrase(facturaVentas1.getMonto(), subtitleFont2));
                                cuotaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(cuotaCell); // MONTO

                                PdfPCell saldoCell = new PdfPCell(new Phrase(facturaVentas1.getSaldo(), subtitleFont2));
                                saldoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(saldoCell); // SALDO

                                break;
                            }
                        }
                    }
                }
                // Agregar la tabla al documento
                document.add(table);
                //Resumen Cartera
                switch (tipo) {
                    case "Cartera": {
                        Font vcuotaFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 12, BaseColor.BLACK);
                        Paragraph subtmora = new Paragraph("TOTAL MORA:" + String.format(Locale.US, "%.0f", tmora), vcuotaFont);//Subtítulo

                        title.setAlignment(Element.ALIGN_LEFT);
                        document.add(subtmora);
                        Font saldoFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 12, BaseColor.BLACK);
                        Paragraph subtsaldo = new Paragraph("TOTAL SALDO:" + String.format(Locale.US, "%.0f", tsaldo), saldoFont);//Subtítulo

                        title.setAlignment(Element.ALIGN_LEFT);
                        document.add(subtsaldo);
                        break;
                    }
                    case "Pagada": {
                        Font vcuotaFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 12, BaseColor.BLACK);
                        Paragraph subtmora = new Paragraph("TOTAL MONTO:" + String.format(Locale.US, "%.0f", tmora), vcuotaFont);//Subtítulo

                        title.setAlignment(Element.ALIGN_LEFT);
                        document.add(subtmora);
                        break;
                    }
                    case " ":
                    case "Pendiente": {
                        Font vcuotaFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 12, BaseColor.BLACK);
                        Paragraph subtmora = new Paragraph("TOTAL MONTO:" + String.format(Locale.US, "%.0f", tmora), vcuotaFont);//Subtítulo

                        title.setAlignment(Element.ALIGN_LEFT);
                        document.add(subtmora);
                        Font saldoFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 12, BaseColor.BLACK);
                        Paragraph subtsaldo = new Paragraph("TOTAL SALDO:" + String.format(Locale.US, "%.0f", tsaldo), saldoFont);//Subtítulo

                        title.setAlignment(Element.ALIGN_LEFT);
                        document.add(subtsaldo);
                        break;
                    }
                    case "Vencida": {
                        Font vcuotaFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 12, BaseColor.BLACK);
                        Paragraph subtmora = new Paragraph("TOTAL MORA:" + String.format(Locale.US, "%.0f", tmora), vcuotaFont);//Subtítulo

                        title.setAlignment(Element.ALIGN_LEFT);
                        document.add(subtmora);
                        break;
                    }
                }
                mostrarAlerta(true, "Reporte generado exitosamente!");
            }else{
                mostrarAlerta(false, "No hay registros que mostrar!");
            }

        } catch (Exception e) {
            mostrarAlerta(false, "No se pudo generar el pdf!");
        } finally {
            document.close();
        }
    }
    //COVERTIR FORMATO DE FECHAS
    public String convertDateString(String inputDateString) {
        // 1. Definir el formato de la fecha de entrada
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        // 2. Definir el formato de la fecha de salida
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            // 3. Analizar la cadena de entrada para obtener un objeto Date
            Date date = inputFormat.parse(inputDateString);
            // 4. Formatear el objeto Date al formato de salida deseado
            assert date != null;
            return outputFormat.format(date);
        } catch (ParseException e) {
            // Manejar la excepción si la cadena no tiene el formato esperado
            return "Error al convertir la fecha";
        }
    }
    //DIFERENCIA ENTRE DIAS
    public long getDaysDifference(String invoiceDate) {
        // Definir el formato de fecha de entrada (yyyy-MM-dd HH:mm:ss)
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        // Obtener la fecha actual (solo fecha, sin hora)
        LocalDate today = LocalDate.now();
        try {
            // Analizar las cadenas de fecha para obtener objetos LocalDate
            // Se trunca el tiempo para que solo se considere la fecha
            LocalDate date1 = LocalDate.parse(invoiceDate, formatter1);
            // Calcular la diferencia en días
            return ChronoUnit.DAYS.between(date1, today);
        } catch (Exception e) {
            return 0; // Devolver 0 o manejar el error según sea necesario
        }
    }

}