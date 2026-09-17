package com.amcsoftware.sidebar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.amcsoftware.sidebar.Entidades.Cobrador;
import com.amcsoftware.sidebar.Entidades.Pagos_Ventas;
import com.amcsoftware.sidebar.adapter.PagosVentasAdapter;
import com.amcsoftware.sidebar.utils.AppUtils;
import com.amcsoftware.sidebar.utils.ReportePdfUtils;
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
import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class PagosFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener, SearchView.OnQueryTextListener {
    AlertDialog alertDialog;
    AlertDialog.Builder builder;
    ArrayList<Pagos_Ventas> listaPagos;
    ArrayList<String> cobradorsList;
    ArrayList<Cobrador> listaCobradores;
    ArrayAdapter<String> adapterspn, adapterspn1;
    boolean admin = false, buscarf = false ;
    double  nsaldo, vpagado, tsaldo, tcuota;
    int position;
    JSONObject jsonObject = null;
    JsonObjectRequest jsonObjectRequest;
    ImageButton btwhatsapp,btpdf, btfecha, btcfecha;
    PagosVentasAdapter adapter;
    SweetAlertDialog pDialog;
    // Cuenta las peticiones de red en curso que dependen del diálogo de carga.
    // Evita que una segunda petición concurrente (ej. cargarWebService() +
    // buscarCobradores() disparadas juntas) sobrescriba la referencia del
    // primer diálogo y lo deje huérfano en pantalla sin poder cerrarse.
    private int peticionesPendientes = 0;
    RecyclerView recyclerPagos;
    RequestQueue request, requestQueue;
    SearchView txtbuscar;//buscador
    String idcb,fechai, fechaf, formattedDate, idcbr, perfil,
            mensaje, celular, cliente, nfile, titulo,
            subtitulo,vcuota;
    String [] parts;
    StringBuilder auxcodpago, auxidfv, auxsaldo, auxvcuota;

    public PagosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = requireContext().getSharedPreferences("sesion",0);
        idcb   = sp.getString("codperfil", "General");
        perfil = sp.getString("perfil", "General");
        if (perfil.equals("Administrador")){
            idcb   = "";
            admin  = true;
            fechai = "";
            fechaf = "";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_pagos, container, false);

        btfecha = vista.findViewById(R.id.btfecha);
        btcfecha = vista.findViewById(R.id.btcfecha);
        btwhatsapp = vista.findViewById(R.id.fabWhatsapp);
        btpdf      = vista.findViewById(R.id.btpdf);
        txtbuscar  = vista.findViewById(R.id.txtbuscar);
        listaPagos         = new ArrayList<>();
        listaCobradores    = new ArrayList<>();
        recyclerPagos = vista.findViewById(R.id.idRecycler);
        recyclerPagos.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerPagos.setHasFixedSize(true);
        adapter     = new PagosVentasAdapter(listaPagos);
        request = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones método GET
        requestQueue = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones método POST
        mensaje = " ";
        auxcodpago = new StringBuilder();
        auxidfv    = new StringBuilder();
        auxsaldo   = new StringBuilder();
        auxvcuota  = new StringBuilder();

        //Desactivar botones en la vista del cobrador
        if(!admin){
            btfecha.setVisibility(View.GONE);
            btpdf.setVisibility(View.GONE);
        }
        btfecha.setOnClickListener(v-> {
            buscarf = true;
            mostrarInputBox(getContext());
        });
        btcfecha.setOnClickListener(v-> {
            buscarf = false;
            mostrarInputBox(getContext());
        });
        btwhatsapp.setOnClickListener(v->{
            if (!mensaje.equals(" ")){
                enviarMensaje();
            }else{
                msgBox();
            }
        });
        btpdf.setOnClickListener(v->generarPdf());

        cargarWebService();
        buscarCobradores();

        txtbuscar.setOnQueryTextListener(this);

        builder = new AlertDialog.Builder(requireContext());

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
        // Verificar conectividad antes de consultar
        if (!AppUtils.hayConectividad(requireContext())) {
            AppUtils.alertSinInternet(requireContext());
            return;
        }
        // Mostrar diálogo de carga
        mostrarCargando("Consultando...");
        String url = "https://www.wmcsoftware.net/apps/softpymes/consultaPagosVentas.php?idcb="+idcb;
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, this, this);
        request.add(jsonObjectRequest);
        txtbuscar.requestFocus();
    }

    private void buscarCobradores() {
        // Verificar conectividad antes de consultar
        if (!AppUtils.hayConectividad(requireContext())) {
            AppUtils.alertSinInternet(requireContext());
            return;
        }
        // Mostrar diálogo de carga
        mostrarCargando("Consultando...");
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaCobradores.php";
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }

    /**
     * Muestra el diálogo de carga y suma una petición pendiente. Si ya hay
     * un diálogo visible (otra petición concurrente en curso), NO crea uno
     * nuevo ni sobrescribe la referencia — solo incrementa el contador, para
     * que el diálogo original siga siendo el que se cierra al final.
     *
     * @param titulo Título del diálogo (ej. "Consultando...", "Guardando...")
     */
    private void mostrarCargando(String titulo) {
        peticionesPendientes++;
        if (pDialog == null || !pDialog.isShowing()) {
            pDialog = AppUtils.mostrarCargando(requireContext(), titulo, "Por favor espera.");
        }
    }

    /**
     * Resta una petición pendiente y solo cierra el diálogo de carga cuando
     * ya no queda ninguna petición en curso (contador en cero). Así, si dos
     * peticiones se dispararon juntas, el diálogo permanece visible hasta
     * que ambas respondan.
     */
    private void cerrarCargando() {
        if (peticionesPendientes > 0) {
            peticionesPendientes--;
        }
        if (peticionesPendientes == 0) {
            AppUtils.cerrarCargando(pDialog);
        }
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
        // Ocultar diálogo de carga
        cerrarCargando();
        // Limpiar la vista: si la petición terminó en error, no debe
        // quedar en pantalla el resultado de una consulta anterior.
        listaPagos.clear();
        adapter = new PagosVentasAdapter(listaPagos);
        recyclerPagos.setAdapter(adapter);
        // Manejar la respuesta
        Toast.makeText((getContext()),"No hay registros! ",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(JSONObject response) {
        JSONArray json  = response.optJSONArray("pagosVentas");
        JSONArray json1 = response.optJSONArray("cobrador");
        final String mensajeResp = response.optString("mensaje","");
        try {
            // Repoblar solo si la respuesta trae la clave "pagosVentas" (aunque venga vacía)
            if (json != null) {
                listaPagos.clear();
                for (int i = 0; i < json.length(); i++) {
                    Pagos_Ventas pagosVentas = new Pagos_Ventas();
                    JSONObject item = json.getJSONObject(i);
                    pagosVentas.setCodpago(item.optString("codpago"));
                    pagosVentas.setIdfv(item.optString("idfv"));
                    pagosVentas.setCliente(item.optString("cliente"));
                    pagosVentas.setCelular(item.optString("celular"));
                    formattedDate = convertDateString(item.optString("fecha"));
                    pagosVentas.setFecha(formattedDate);
                    formattedDate = convertDateString(item.optString("fechac"));
                    pagosVentas.setFechacobro(formattedDate);
                    pagosVentas.setRef(item.optString("ref"));
                    pagosVentas.setVcuota(item.optString("vcuota"));
                    pagosVentas.setValor(item.optString("valor"));
                    pagosVentas.setSaldo(item.optString("saldo"));
                    listaPagos.add(pagosVentas);
                }
                adapter = new PagosVentasAdapter(listaPagos);
                // Evento al tocar una fila del recyclerView
                adapter.setOnClickListener(v -> {
                    // Lista vacía (sin resultados en el filtro): no hay fila real que leer
                    if (listaPagos.isEmpty()) {
                        return;
                    }
                    int posicion = recyclerPagos.getChildAdapterPosition(v);
                    if (posicion == RecyclerView.NO_POSITION || posicion >= listaPagos.size()) {
                        return;
                    }
                    final  String   codpago =  listaPagos.get(posicion).getCodpago();
                    final  String   idfv    =  listaPagos.get(posicion).getIdfv();
                    final  Double   saldo   =  Double.parseDouble(listaPagos.get(posicion).getSaldo());
                    final  String   fecha   =  listaPagos.get(posicion).getFechacobro();
                    final  String   refCuota =  listaPagos.get(posicion).getRef();
                    vcuota  =  listaPagos.get(posicion).getVcuota();
                    cliente =  listaPagos.get(posicion).getCliente();
                    celular =  "57"+listaPagos.get(posicion).getCelular();
                    mostrarDialogoInput(getContext(), codpago,idfv,saldo,fecha,refCuota);
                });
                recyclerPagos.setAdapter(adapter);
                txtbuscar.requestFocus();

                // Consulta exitosa pero sin filas: avisar en vez de dejar la lista vacía sin explicación
                if (json.length() == 0 && getContext() != null) {
                    AppUtils.alertAdvertencia(getContext(), "Sin resultados",
                            mensajeResp.isEmpty() ? "No se encontraron pagos." : mensajeResp);
                }
            }
        } catch (JSONException e) {
            // Manejar la respuesta
            Toast.makeText((getContext()), "No se pudo consultar!", Toast.LENGTH_SHORT).show();
        }

        try {
            for (int i = 0; i< (json1 != null ? json1.length() : 0); i++){
                Cobrador cobrador = new Cobrador();
                JSONObject item = json1.getJSONObject(i);
                cobrador.setIdcob(item.optString("idcobrador"));
                cobrador.setNombre(item.optString("nombre"));
                listaCobradores.add(cobrador);
            }
            obtenerLista();
        } catch (JSONException e) {
            // Manejar la respuesta
            Toast.makeText((getContext()),"No se pudo consultar!",Toast.LENGTH_SHORT).show();
        }

        // Ocultar diálogo de carga: se llama una sola vez por respuesta HTTP
        // recibida (independientemente de si trajo "pagosVentas", "cobrador"
        // o ambos), para que el contador de peticiones pendientes sea exacto.
        cerrarCargando();
    }

    private void obtenerLista() {
        cobradorsList = new ArrayList<>();
        cobradorsList.add("Seleccione:");
        for (int i=0; i < listaCobradores.size();i++){
            cobradorsList.add(listaCobradores.get(i).getIdcob()+"-"+listaCobradores.get(i).getNombre());
        }
    }
    //INPUT PARA BUSCAR POR FECHA
    @SuppressLint("SetTextI18n")
    private void mostrarInputBox(Context context){
        // Inflar el layout personalizado con el EditText
        View viewInflada = LayoutInflater.from(context).inflate(R.layout.dialog_input_fecha, null);
        //Instanciar objetos
        final TextView t1         = viewInflada.findViewById(R.id.lbltitulo);
        final TextView f1         = viewInflada.findViewById(R.id.lblf1);
        final TextView f2         = viewInflada.findViewById(R.id.lblf2);
        final TextView cbr        = viewInflada.findViewById(R.id.lblcbr);
        final EditText fecha1      = viewInflada.findViewById(R.id.txtfechai);
        final EditText fecha2      = viewInflada.findViewById(R.id.txtfechaf);
        final Spinner cob          = viewInflada.findViewById(R.id.spncob);
        final ImageButton btback   = viewInflada.findViewById(R.id.btnback);
        final ImageButton btborrar = viewInflada.findViewById(R.id.btnborrar);
        final ImageButton btaceptar = viewInflada.findViewById(R.id.btaceptar);
        adapterspn1 = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, cobradorsList);
        cob.setAdapter(adapterspn1);
        //MOSTRAR OBJETOS DEPENDIENDO DEL TIPO DE BOTON
        if (buscarf){
            t1.setText("BUSCAR X FECHA");
            f1.setText("Fecha inicial:");
            f2.setVisibility(View.VISIBLE);
            fecha2.setVisibility(View.VISIBLE);
            cbr.setVisibility(View.VISIBLE);
            cob.setVisibility(View.VISIBLE);
        }else {
            t1.setText("FECHAJE X LOTE");
            f1.setText("Fecha de Cobro:");
            f2.setVisibility(View.GONE);
            fecha2.setVisibility(View.GONE);
            cbr.setVisibility(View.GONE);
            cob.setVisibility(View.GONE);
        }

        //Asignar el calendario al EditText
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
        btborrar.setOnClickListener(v->{//Limpiar los EditText
            fecha1.setText("");
            fecha2.setText("");

        });
        btaceptar.setOnClickListener(v->{
            fechai = fecha1.getText().toString();
            fechaf = fecha2.getText().toString();
            idcbr  = cob.getSelectedItem().toString();
            parts  =  idcbr.split("-");//Convertir en vector el string

            if (buscarf) {
                cargarWebService1(fechai, fechaf, parts[0]);
            }else{
                showPagosAndPositions();
            }

            alertDialog.dismiss();
            listaPagos.clear();
        });
        //Inflar la vista del alert
        builder.setView(viewInflada);
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void cargarWebService1(String fechai, String fechaf, String idcbr) {
        // Verificar conectividad antes de consultar
        if (!AppUtils.hayConectividad(requireContext())) {
            AppUtils.alertSinInternet(requireContext());
            return;
        }
        mostrarCargando("Consultando...");
        String url = "https://www.wmcsoftware.net/apps/softpymes/consultaPagoxfecha.php?idcb=" + idcbr + "&fechai=" + fechai + "&fechaf=" + fechaf;
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, this, this);
        request.add(jsonObjectRequest);
    }
    //INPUT PARA REALIZAR EL PAGO
    @SuppressLint("SetTextI18n")//Quitar advertencia de traducción de la fecha
    private void mostrarDialogoInput(Context context, String codpago, String idfv, Double saldo, String fecha, String refCuota) {
        vpagado=0;
        builder.setTitle("");
        // Inflar el layout personalizado con el EditText
        View viewInflada = LayoutInflater.from(context).inflate(R.layout.dialog_input_pagos, null);
        //Instanciar objetos
        final TextView codpago1    = viewInflada.findViewById(R.id.txtcodpago);
        final TextView idfv1       = viewInflada.findViewById(R.id.txtidfv);
        final EditText saldoant    = viewInflada.findViewById(R.id.txtsaldoant);
        final EditText valor       = viewInflada.findViewById(R.id.txtvalor);
        final EditText saldoact    = viewInflada.findViewById(R.id.txtsaldoact);
        final EditText fechac      = viewInflada.findViewById(R.id.txtfechacobro);
        final ImageButton btback   = viewInflada.findViewById(R.id.btnback);
        final ImageButton btborrar = viewInflada.findViewById(R.id.btnborrar);
        final ImageButton btpago   = viewInflada.findViewById(R.id.btnpago);
        final ImageButton btnFechaDialog = viewInflada.findViewById(R.id.btnfecha);
        final Spinner ref          = viewInflada.findViewById(R.id.spnref);
        //Obtener el adaptador del Spinner
        adapterspn = (ArrayAdapter<String>) ref.getAdapter();
        // Buscar el índice del valor que quieres asignar
        position = adapterspn.getPosition(refCuota);
        // Asignar el valor al Spinner
        ref.setSelection(position);
        //Asignar el calendario al EditText
        fechac.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year  = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day   = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year1, monthOfYear, dayOfMonth) -> fechac.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1), year, month, day);
            datePickerDialog.show();
        });
        //Asignar datos de la lista de pagos
        codpago1.setText(codpago);
        idfv1.setText(idfv);
        saldoant.setText(String.format(Locale.US,"%.0f",saldo));
        valor.setText("0");
        saldoact.setText(String.format(Locale.US,"%.0f",saldo));
        fechac.setText(fecha);
        /* Calcular el saldo al terminar de escribir el valor */
        valor.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    vpagado = Double.parseDouble(valor.getText().toString());
                    nsaldo = saldo - vpagado;
                    saldoact.setText(String.format(Locale.US,"%.0f",nsaldo));
                } catch (NumberFormatException e) {
                    saldoact.setText(String.format(Locale.US,"%.0f",saldo));
                    vpagado=0;
                }
            }
            return false;
        });
        /* Programar botones de imagen */
        btback.setOnClickListener(v->{//Regresar a la consulta de pagos
            alertDialog.dismiss();
        });
        btborrar.setOnClickListener(v->{//Borrar el EditText valor pagado y reasignar el saldo
            valor.setText("0");
            saldoact.setText(String.format(Locale.US,"%.0f",saldo));
            valor.requestFocus();
        });
        btnFechaDialog.setOnClickListener(v->{//Reprogramar la fecha de cobro
            if (fechac.getText().toString().equals(fecha)){
                Toast.makeText(getContext(), "Debe actualizar la fecha de cobro !", Toast.LENGTH_SHORT).show();
            }else if (!esFechaPosteriorHoy(fechac.getText().toString())){
                Toast.makeText(getContext(), "La fecha de cobro no puede ser menor o igual a la fecha actual !", Toast.LENGTH_SHORT).show();
            }else{
                final String fechacobro = convertDateString1(fechac.getText().toString());
                final String referencia = ref.getSelectedItem().toString();
                //Confirmar operación
                AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                builder1.setMessage("¿Está seguro de cambiar la fecha de cobro?").setTitle("Softpymes");
                builder1.setPositiveButton("Si", (dialog1, which1) -> {
                    fechaje(codpago,idfv,saldo,fechacobro,referencia);
                    alertDialog.dismiss();
                });

                builder1.setNegativeButton("No", (dialog1, which1) ->
                        Toast.makeText(getContext(),
                                "Cambio de fecha cancelado!",
                                Toast.LENGTH_SHORT).show());

                AlertDialog dialog1 = builder1.create();
                dialog1.show();//Mostrar

            }
        });
        btpago.setOnClickListener(v->{//Validar y guardar el pago actual
            mensaje = " ";
            if (valor.getText().toString().isEmpty() || valor.getText().toString().equals("0") ){
                Toast.makeText(getContext(), "El pago no puede ser 0 !", Toast.LENGTH_SHORT).show();
            }else if(vpagado > saldo){
                Toast.makeText(getContext(), "El pago no puede ser mayor que el saldo !", Toast.LENGTH_SHORT).show();
            }else if (fechac.getText().toString().equals(fecha)){
                Toast.makeText(getContext(), "Debe actualizar la fecha de cobro !", Toast.LENGTH_SHORT).show();
            }else if (!esFechaPosteriorHoy(fechac.getText().toString())){
                Toast.makeText(getContext(), "La fecha de cobro no puede ser menor o igual a la fecha actual !", Toast.LENGTH_SHORT).show();
            }else {
                final String fechacobro = convertDateString1(fechac.getText().toString());
                final String referencia = ref.getSelectedItem().toString();
                final Double valorp     = Double.parseDouble(valor.getText().toString());
                final Double nuevosaldo = Double.parseDouble(saldoact.getText().toString());
                //Confirmar operación
                AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                builder1.setMessage("¿Está seguro de registrar este pago?").setTitle("Softpymes");
                builder1.setPositiveButton("Si", (dialog1, which1) -> {
                    registrarPago(codpago, idfv, valorp, nuevosaldo, fechacobro, referencia);
                    mensaje = "Cliente: "+cliente+"\n"+"Valor Pagado: " + String.format(Locale.US,"%.0f",valorp) +
                            "\n"+"Próximo cobro:" + fechacobro + "\n"+"Saldo: "+String.format(Locale.US,"%.0f",nuevosaldo)+
                            "\n"+"Referencia: "+referencia ;
                    alertDialog.dismiss();
                });

                builder1.setNegativeButton("No", (dialog1, which1) ->
                        Toast.makeText(getContext(),
                                "Pago cancelado",
                                Toast.LENGTH_SHORT).show());

                AlertDialog dialog1 = builder1.create();
                dialog1.show();//Mostrar
            }
        });
        //Inflar la vista del alert
        builder.setView(viewInflada);
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void enviarMensaje() {
        try {
            Uri uri = Uri.parse("whatsapp://send?phone=" + celular + "&text=" + Uri.encode(mensaje));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (Exception e) {
            // WhatsApp no está instalado
            Toast.makeText(getContext(), "WhatsApp no está instalado." + e, Toast.LENGTH_SHORT).show();
        }
    }

    public void generarPdf() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        if (fechai.isEmpty()){
            nfile     = "/Recaudo_General_";
            titulo    = "RECAUDO GENERAL";
            subtitulo = null;
        }else if (fechaf.isEmpty() || fechai.equals(fechaf)){
            nfile     = "/RecaudoxCobrador_";
            titulo    = "RECAUDO POR COBRADOR: " + parts[1] ;
            subtitulo = "FECHA DE COBRO: " + fechai;
        }else{
            nfile     = "/RecaudoxCobrador_";
            titulo    = "RECAUDO POR COBRADOR: " + parts[1] ;
            subtitulo = "FECHA DE COBRO: " + fechai + " HASTA " + fechaf;
        }
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + nfile + timeStamp +".pdf";
        Document document = new Document();
        boolean exito = false;
        try {
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();
            ReportePdfUtils.agregarEncabezado(document, requireContext(), titulo, subtitulo);

            // Tabla con encabezados VENTA N°/CLIENTE/VALOR PAGADO/SALDO
            float[] columnWidths = {1f, 4.5f, 2f, 2f};
            PdfPTable table = ReportePdfUtils.crearTabla(columnWidths);
            ReportePdfUtils.agregarEncabezadosTabla(table, new String[]{"VENTA N°", "CLIENTE", "VALOR PAGADO", "SALDO"});
            // Llenar la tabla y acumular los totales de cuota/saldo
            tsaldo = 0;
            tcuota = 0;
            if (adapter != null && adapter.getItemCount() > 0) {
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    Pagos_Ventas pagosVentas1= adapter.getItemAtPosition(i);
                    if (pagosVentas1 != null) {
                        tsaldo += Double.parseDouble(pagosVentas1.getSaldo());
                        tcuota += Double.parseDouble(pagosVentas1.getValor());
                        table.addCell(ReportePdfUtils.celda(pagosVentas1.getIdfv(), Element.ALIGN_CENTER, i));
                        table.addCell(ReportePdfUtils.celda(pagosVentas1.getCliente(), Element.ALIGN_LEFT, i));
                        table.addCell(ReportePdfUtils.celda(pagosVentas1.getValor(), Element.ALIGN_CENTER, i));
                        table.addCell(ReportePdfUtils.celda(pagosVentas1.getSaldo(), Element.ALIGN_CENTER, i));
                    }
                }
                document.add(table);
                // Resumen del recaudo (totales)
                document.add(ReportePdfUtils.filaTotal("TOTAL COBRO", String.format(Locale.US, "%.0f", tcuota)));
                document.add(ReportePdfUtils.filaTotal("TOTAL RECAUDO", String.format(Locale.US, "%.0f", tsaldo)));
                exito = true;
            }else{
                Toast.makeText((getContext()), "No hay registros que mostrar!", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText((getContext()), "No se pudo generar el pdf!", Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
            if (exito && getContext() != null) {
                Toast.makeText(getContext(), "Reporte generado exitosamente!", Toast.LENGTH_SHORT).show();
                AppUtils.abrirPdf(getContext(), new File(path));
            }
        }
    }
    private void fechaje(String codpago, String idfv, Double nuevosaldo, String fechacobro,String ref) {
        // Verificar conectividad antes de guardar
        if (!AppUtils.hayConectividad(requireContext())) {
            AppUtils.alertSinInternet(requireContext());
            return;
        }
        String url = "https://www.wmcsoftware.net/apps/softpymes/actualizarFechacobro.php";
        // Mostrar diálogo de carga
        mostrarCargando("Guardando...");
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    // Ocultar diálogo de carga
                    cerrarCargando();
                    // Manejar la respuesta del servidor
                    try {
                        jsonObject = new JSONObject(response);
                    } catch (JSONException e) {
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                        jsonObject = null;
                    }
                    // Mostrar el mensaje del servidor en un SweetAlert
                    // (éxito o error) en vez de un Toast.
                    if (jsonObject == null) {
                        AppUtils.alertError(getContext(), "Error", "No se pudo interpretar la respuesta del servidor.");
                    } else if (jsonObject.optBoolean("success")) {
                        AppUtils.alertExito(getContext(), "¡Listo!", jsonObject.optString("mensaje"));
                    } else {
                        AppUtils.alertError(getContext(), "Error", jsonObject.optString("mensaje"));
                    }
                    //Refrescar Recyclerview
                    listaPagos.clear();
                    adapter = new PagosVentasAdapter(listaPagos);
                    recyclerPagos.setAdapter(adapter);
                    cargarWebService();
                },
                error -> {
                    // Ocultar diálogo de carga
                    cerrarCargando();
                    // Manejar errores
                    Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("codpago", codpago);
                params.put("cuota", vcuota);
                params.put("fechac", fechacobro);
                params.put("idfv", idfv);
                params.put("saldo", String.format(Locale.US,"%.0f",nuevosaldo));
                params.put("ref", ref);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

    private void registrarPago(String codpago, String idfv, Double valorp, Double nuevosaldo, String fechacobro,String ref) {
        // Verificar conectividad antes de guardar
        if (!AppUtils.hayConectividad(requireContext())) {
            AppUtils.alertSinInternet(requireContext());
            return;
        }
        String url = "https://www.wmcsoftware.net/apps/softpymes/guardarPago.php";
        // Mostrar diálogo de carga
        mostrarCargando("Guardando...");
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    // Ocultar diálogo de carga
                    cerrarCargando();
                    // Manejar la respuesta del servidor
                    try {
                        jsonObject = new JSONObject(response);
                    } catch (JSONException e) {
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                        jsonObject = null;
                    }
                    // Mostrar el mensaje del servidor en un SweetAlert
                    // (éxito o error) en vez de un Toast.
                    if (jsonObject == null) {
                        AppUtils.alertError(getContext(), "Error", "No se pudo interpretar la respuesta del servidor.");
                    } else if (jsonObject.optBoolean("success")) {
                        AppUtils.alertExito(getContext(), "¡Listo!", jsonObject.optString("mensaje"));
                    } else {
                        AppUtils.alertError(getContext(), "Error", jsonObject.optString("mensaje"));
                    }
                    //enviarMensaje(); //enviar soporte de pago por Whatsapp
                    listaPagos.clear();
                    adapter = new PagosVentasAdapter(listaPagos);
                    recyclerPagos.setAdapter(adapter);
                    cargarWebService();
                },
                error -> {
                    // Ocultar diálogo de carga
                    cerrarCargando();
                    // Manejar errores
                    Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("codpago", codpago);
                params.put("cuota", vcuota);
                params.put("idfv", idfv);
                params.put("valor", String.format(Locale.US,"%.0f",valorp));
                params.put("saldo", String.format(Locale.US,"%.0f",nuevosaldo));
                params.put("fechac", fechacobro);
                params.put("ref", ref);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }
    /**
     * Valida que la fecha de cobro digitada (formato dd-MM-yyyy) sea
     * estrictamente posterior a la fecha actual (no menor ni igual).
     *
     * @param fechaTexto Fecha en formato dd-MM-yyyy
     * @return true si la fecha es posterior a hoy, false si es menor,
     *         igual o si no se pudo interpretar
     */
    private boolean esFechaPosteriorHoy(String fechaTexto) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            sdf.setLenient(false);
            Date fechaSeleccionada = sdf.parse(fechaTexto);
            if (fechaSeleccionada == null) return false;

            Calendar hoy = Calendar.getInstance();
            hoy.set(Calendar.HOUR_OF_DAY, 0);
            hoy.set(Calendar.MINUTE, 0);
            hoy.set(Calendar.SECOND, 0);
            hoy.set(Calendar.MILLISECOND, 0);

            return fechaSeleccionada.after(hoy.getTime());
        } catch (ParseException e) {
            return false;
        }
    }

    public String convertDateString(String inputDateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date date = inputFormat.parse(inputDateString);
            assert date != null;
            return outputFormat.format(date);
        } catch (ParseException e) {
            return "Error al convertir la fecha";
        }
    }

    public String convertDateString1(String inputDateString) {
        SimpleDateFormat inputFormat  = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = inputFormat.parse(inputDateString);
            assert date != null;
            return outputFormat.format(date);
        } catch (ParseException e) {
            return "Error al convertir la fecha";
        }
    }

    private void msgBox() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setMessage("No hay datos para enviar!").setTitle("Softpymes");
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();//Mostrar el Alert
    }

    //ALMACENAR LOS IDPAGO DEL RECYCLER VIEW ACTIVO
    private void showPagosAndPositions() {
        auxcodpago.setLength(0);
        auxidfv.setLength(0);
        auxsaldo.setLength(0);
        auxvcuota.setLength(0);
        if (adapter != null && adapter.getItemCount() > 0) {
            for (int i = 0; i < adapter.getItemCount(); i++) {
                Pagos_Ventas pagosVentas = adapter.getItemAtPosition(i);
                if (pagosVentas != null) {
                    // Agrega la información del cliente y la posición al StringBuilder
                    auxcodpago.append(pagosVentas.getCodpago()).append("-");
                    auxidfv.append(pagosVentas.getIdfv()).append("-");
                    auxsaldo.append(pagosVentas.getSaldo()).append("-");
                    auxvcuota.append(pagosVentas.getVcuota()).append("-");
                }
            }
            //Alert de confirmación
            AppUtils.alertConfirmar(
                    requireContext(),
                    "Softpymes",
                    "¿Está seguro de realizar esta operación?",
                    "Si",
                    "No",
                    this::nfechaje
            );

        } else {
            // Mensaje si la lista está vacía
            AppUtils.alertAdvertencia(requireContext(), "Softpymes", "El RecyclerView está vacío.");
        }
    }

    private void nfechaje() {
        // Verificar conectividad antes de guardar
        if (!AppUtils.hayConectividad(requireContext())) {
            AppUtils.alertSinInternet(requireContext());
            return;
        }
        String url = "https://www.wmcsoftware.net/apps/softpymes/actualizarFechacobro1.php";
        // Mostrar diálogo de carga
        mostrarCargando("Guardando...");
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    // Ocultar diálogo de carga
                    cerrarCargando();
                    // Manejar la respuesta del servidor
                    try {
                        jsonObject = new JSONObject(response);
                    } catch (JSONException e) {
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                        jsonObject = null;
                    }
                    // Mostrar el resultado en un SweetAlert (éxito o error)
                    // en vez de un Toast, que pasaba desapercibido y no
                    // dejaba claro si el fechaje masivo se ejecutó bien o
                    // falló.
                    if (jsonObject == null) {
                        AppUtils.alertError(getContext(), "Error", "No se pudo interpretar la respuesta del servidor.");
                    } else if (jsonObject.optBoolean("success")) {
                        AppUtils.alertExito(getContext(), "¡Listo!", jsonObject.optString("mensaje"));
                    } else {
                        AppUtils.alertError(getContext(), "Error", jsonObject.optString("mensaje"));
                    }
                    //Refrescar Recyclerview
                    listaPagos.clear();
                    adapter = new PagosVentasAdapter(listaPagos);
                    recyclerPagos.setAdapter(adapter);
                    cargarWebService();
                },
                error -> {
                    // Ocultar diálogo de carga
                    cerrarCargando();
                    // Manejar errores
                    Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("codpago",   String.valueOf(auxcodpago).trim());
                params.put("idfv",   String.valueOf(auxidfv).trim());
                params.put("saldo",   String.valueOf(auxsaldo).trim());
                params.put("vcuota",   String.valueOf(auxvcuota).trim());
                params.put("fechac", fechai);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Evitar fuga de ventana (WindowLeaked) si el fragment se destruye
        // mientras el diálogo de carga sigue visible (ej. el usuario navega
        // hacia atrás antes de que la petición responda).
        AppUtils.cerrarCargando(pDialog);
        peticionesPendientes = 0;
    }


}