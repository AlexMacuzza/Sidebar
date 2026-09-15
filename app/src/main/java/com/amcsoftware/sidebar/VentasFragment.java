package com.amcsoftware.sidebar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amcsoftware.sidebar.Entidades.Cliente;
import com.amcsoftware.sidebar.Entidades.Mercancia;
import com.amcsoftware.sidebar.Entidades.Ventas;
import com.amcsoftware.sidebar.adapter.ClientesImagenAdapter;
import com.amcsoftware.sidebar.adapter.MercanciasVentasAdapter;
// Interface para el subtotal al eliminar mercancía
import com.amcsoftware.sidebar.listener.OnSubtotalChangeListener;
import com.amcsoftware.sidebar.adapter.VentasAdapter;
import com.amcsoftware.sidebar.utils.AppUtils;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
// Implementa OnSubtotalChangeListener para reaccionar a cambios del subtotal desde VentasAdapter
public class VentasFragment extends Fragment implements Response.Listener<JSONObject>,
        Response.ErrorListener, AdapterView.OnItemSelectedListener, OnSubtotalChangeListener {
    //Variables locales para instanciar objetos
    android.app.AlertDialog alertDialog;
    android.app.AlertDialog.Builder builder;
    ArrayList<Cliente>   listaClientes;
    ArrayList<Mercancia> listaMercancia;
    ArrayList<Ventas>    listaventas;
    ClientesImagenAdapter adapter2;
    // 'subtotal' mantiene el total actual de los ítems en el RecyclerView
    double cantidad, cuota, ncuotas, subtotal, total, precio,
            res, saldo, vpagado;
    EditText txtdesc,txtcant,txtctnorm,txtfechacobro,txttotal,
            txtsaldo,txtvpagado,txtncuotas,txtfechav,txtobs;
    ImageButton btborrar,btcliente,btncliente,btmerc,btguardar;
    int n, plazod;
    JSONObject jsonObject = null;
    JsonObjectRequest jsonObjectRequest;
    MercanciasVentasAdapter adapter1;
    RecyclerView recyclerClientes, recyclerMercancia, recyclerVentasM;
    SweetAlertDialog dialogCargando;
    RequestQueue requestQueue,request;
    Spinner spnrfpago;
    String codvd,vendedor, cliente,idcl, fechacobro, fechav, 
            fpago, msj,saldo1,total1,vpagado1, 
            cuota2, ncuotas2, separador="-",perfil,obs;
    StringBuilder codpv, descv, preciov, cantv, subtv,
            obsv, auxcodpv, auxdescv, auxpreciov, auxcantv,
            auxsubtv,auxobsv;
    VentasAdapter adapter;
    TextView lblvd,txtcliente,txtidcl,lblobs;

    public VentasFragment() {
        // Required empty public constructor
    }

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "DefaultLocale"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_ventas, container, false);
        //Instanciar objetos
        lblvd      = vista.findViewById(R.id.lblvd);
        btborrar   = vista.findViewById(R.id.btnborrar);
        btmerc     = vista.findViewById(R.id.btnmercancia);
        btguardar  = vista.findViewById(R.id.btregistrarv);
        lblobs     = vista.findViewById(R.id.lblobs);
        spnrfpago  = vista.findViewById(R.id.spnrfpago);
        btcliente  = vista.findViewById(R.id.btbcliente);
        btncliente  = vista.findViewById(R.id.btncliente);
        txtidcl    = vista.findViewById(R.id.txtidcl);
        txtcliente = vista.findViewById(R.id.txtcliente);
        txtdesc    = vista.findViewById(R.id.txtdesc);
        txtcant    = vista.findViewById(R.id.txtcantidad);
        txttotal   = vista.findViewById(R.id.txtventat); // Asegúrate que este es el EditText para el total de la venta
        txtfechacobro = vista.findViewById(R.id.txtfechacobro);
        txtctnorm     = vista.findViewById(R.id.txtctanormal);
        txtsaldo      = vista.findViewById(R.id.txtsaldo);
        txtvpagado    = vista.findViewById(R.id.txtvpagado);
        txtncuotas    = vista.findViewById(R.id.txtnctas);
        txtfechav     = vista.findViewById(R.id.txtfechav);
        txtobs        = vista.findViewById(R.id.txtobs);
        recyclerVentasM =  vista.findViewById(R.id.idRecycler);
        //Inicializar objetos
        builder = new android.app.AlertDialog.Builder(requireContext());
        listaClientes     = new ArrayList<>();
        listaMercancia    = new ArrayList<>();
        listaventas       = new ArrayList<>();
        recyclerVentasM.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerVentasM.setHasFixedSize(true);
        // 'this' se pasa como listener de VentasAdapter (implementa OnSubtotalChangeListener)
        adapter     = new VentasAdapter(listaventas, this);
        recyclerVentasM.setAdapter(adapter); // Establece el adaptador al RecyclerView
        requestQueue    = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo POST
        request         = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET
        spnrfpago.setOnItemSelectedListener(this);//Selección de un item del spinner
        //Buscar datos en el servidor
        buscarMercancia();
        //Cargar datos iniciales
        loadData();
        //Buscar Cliente
        btcliente.setOnClickListener(v-> buscarCliente());
        //Crear Cliente
        btncliente.setOnClickListener(v-> Navigation.findNavController(v).navigate(R.id.nuevoClienteFotoV));
        btmerc.setOnClickListener(v-> mostrarDialogoInput(getContext()));
        btguardar.setOnClickListener(v ->{
            // Obtener los valores ingresados
            idcl        = txtidcl.getText().toString().trim();
            // Usamos la variable 'subtotal' del fragmento que se mantiene actualizada.
            total1  = String.format(Locale.US, "%.0f", subtotal); // Asegurarse de que total1 tome el valor de 'subtotal'
            obs     = String.valueOf(obsv).trim();// Cadena con las observaciones de los productos vendidos
            if (perfil.equals("Administrador")) {
                total1 = String.format(Locale.US, "%.0f", total); // Asegurarse de que total1 tome el valor de 'total'
                obs    = txtobs.getText().toString();
            }
            saldo1      = txtsaldo.getText().toString().trim();
            vpagado1    = txtvpagado.getText().toString().trim();
            cuota2      = txtctnorm.getText().toString().trim();
            ncuotas2    = txtncuotas.getText().toString().trim();
            fechacobro  = txtfechacobro.getText().toString().trim();
            fechav      = txtfechav.getText().toString().trim();
            // Validar que los campos no estén vacíos
            if (idcl.isEmpty() || fechacobro.isEmpty() || total1.isEmpty() || cuota2.isEmpty() || fpago.isEmpty()) {
                AppUtils.alertAdvertencia(requireContext(), "Campos incompletos",
                        "Por favor, complete todos los campos.");
                return;
            }
            //Alert de confirmación
            AppUtils.alertConfirmar(requireContext(), "Softpymes",
                    "¿Está seguro de realizar esta operación?", "Sí", "No",
                    this::guardarRegistro);
        });
        //limpiar objetos de los EditText de pagos/saldos (manteniendo el total de la venta)
        btborrar.setOnClickListener(v->{
            txtvpagado.setText("");
            txtsaldo.setText("");
            txtctnorm.setText("");
            txtncuotas.setText("");
            txtfechacobro.setText("");
            txtfechav.setText("");
            txtobs.setText("");
            cuota    = 0;
            saldo    = 0;
            vpagado  = 0;
            ncuotas  = 0;
            plazod   = 0;
            txtvpagado.requestFocus();
        });
        //mostrar calendario
        txtfechacobro.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year  = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day   = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String fechaSeleccionada = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        txtfechacobro.setText(fechaSeleccionada);
                    },
                    year, month, day);

            datePickerDialog.show();
        });
        //total para ventas crédito sin alterar inventario
        txttotal.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    total   = Double.parseDouble(txttotal.getText().toString());
                    saldo   = total;
                    txtsaldo.setText(String.format(Locale.US, "%.0f", saldo)); // Usar Locale.US para evitar problemas de coma/punto decimal
                    txtvpagado.requestFocus();
                }catch (NumberFormatException e) {
                txttotal.setText(""); // Limpiar si no es un número
                txtsaldo.setText("");
                AppUtils.alertAdvertencia(requireContext(), "Valor inválido",
                        "Ingrese un valor numérico para el valor total.");
            }
            }
            return false;
        });
        //Calcular saldo
        txtvpagado.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    vpagado = Double.parseDouble(txtvpagado.getText().toString());
                    total   = Double.parseDouble(txttotal.getText().toString()); // txttotal se actualiza con 'subtotal'
                    saldo   = total - vpagado;
                    txtsaldo.setText(String.format(Locale.US, "%.0f", saldo)); // Usar Locale.US para evitar problemas de coma/punto decimal
                    txtctnorm.requestFocus();
                } catch (NumberFormatException e) {
                    txtvpagado.setText(""); // Limpiar si no es un número
                    txtsaldo.setText("");
                    AppUtils.alertAdvertencia(requireContext(), "Valor inválido",
                            "Ingrese un valor numérico para el valor pagado.");
                }
            }
            return false;
        });
        //Asignar cuotas
        txtctnorm.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    cuota = Double.parseDouble(txtctnorm.getText().toString());
                    if (saldo > 0){
                        res = saldo % cuota;//Validar si las cuotas son un valor entero
                        if (res == 0) {
                            plazod  = plazodias();
                            ncuotas = saldo / cuota;
                            plazod  = (int) (plazod *  (ncuotas));
                            txtncuotas.setText(String.format(Locale.US, "%.0f", ncuotas));
                            //Mensaje explicando distribución
                            msj = "Distribución de cuotas:\n" +
                                    "Cuotas normales: "+ txtncuotas.getText().toString() + " de $" +
                                    String.format(Locale.US, "%.0f",cuota);
                            msgBox(msj);
                        }else{
                            ncuotas = Math.ceil(saldo / cuota); //Número de cuotas redondeado hacia arriba
                            double totalCuotasNormales = cuota * (ncuotas - 1);
                            double ultimaCuota = saldo - totalCuotasNormales;
                            plazod  = plazodias();
                            plazod  = (int) (plazod * ncuotas);
                            txtncuotas.setText(String.format(Locale.US, "%.0f", ncuotas));
                            //Mensaje explicando distribución
                            msj = "Distribución de cuotas:\n" +
                                    "Cuotas normales: " + (int)(ncuotas - 1) + " de $" +
                                    String.format(Locale.US, "%.0f",cuota) + "\n" +
                                    "Cuota  final    : $" + String.format(Locale.US, "%.0f", ultimaCuota);
                            msgBox(msj);
                        }
                        //Se procede a calcular la fecha de vencimiento
                        try {
                            LocalDate fechaVenta       = LocalDate.now();
                            LocalDate fechaVencimiento = fechaVenta.plusDays(plazod);
                            txtfechav.setText(fechaVencimiento.toString());
                        } catch (DateTimeParseException e) {
                            // Manejo de errores
                            AppUtils.alertError(requireContext(), "Error", "No se pudo procesar la fecha.");
                        }

                    }else{
                        txtncuotas.setText("");
                    }
                    btguardar.requestFocus();
                } catch (NumberFormatException e) {
                    txtctnorm.setText("");
                    AppUtils.alertAdvertencia(requireContext(), "Valor inválido",
                            "Ingrese un valor numérico para la cuota normal.");
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

    private int plazodias(){
        int plazo;
        if (fpago.equals("Mensual")){
            plazo = 30;
        }else if (fpago.equals("Quincenal")){
            plazo = 15;
        }else{
            plazo = 7;
        }
        return plazo;
    }

    private void msgBox(String mensaje) {
        AppUtils.alertExito(requireContext(), "Softpymes", mensaje);
    }

    private void listaClientes(Context context) {
        builder.setTitle("Seleccionar Cliente:");
        // Inflar el layout personalizado con los objetos
        View viewInflada = LayoutInflater.from(context).inflate(R.layout.fragment_clientes_ventas, null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        final SearchView searchView = viewInflada.findViewById(R.id.txtbuscarcl);
        final ImageButton adcl = viewInflada.findViewById(R.id.btncliente);//botón agregar Cliente
        adcl.setVisibility(View.GONE);
        recyclerClientes =  viewInflada.findViewById(R.id.idRecycler);
        adapter2         = new ClientesImagenAdapter(listaClientes);
        recyclerClientes.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerClientes.setHasFixedSize(true);
        recyclerClientes.setAdapter(adapter2);
        //Asignar cliente seleccionado
        adapter2.setOnClickListener(v -> {
            int pos = recyclerClientes.getChildAdapterPosition(v);
            String cedulaSel = listaClientes.get(pos).getCedula();
            String nombreSel = listaClientes.get(pos).getNombre();

            // Verificar si el cliente ya tiene una venta activa (Pendiente)
            verificarVentaExistente(cedulaSel, nombreSel);
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
        recyclerMercancia =  viewInflada.findViewById(R.id.idRecycler);
        adapter1          = new MercanciasVentasAdapter(listaMercancia);
        recyclerMercancia.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerMercancia.setHasFixedSize(true);
        recyclerMercancia.setAdapter(adapter1);
        // Configurar los botones del diálogo
        admp.setOnClickListener(v->{
            //Asignar los items seleccionados
            List<Mercancia> selectedItems = adapter1.getSelectedDataOnly();
            //Validar items
            if (selectedItems.isEmpty()) {
                AppUtils.alertAdvertencia(requireContext(), "Sin selección", "No seleccionó ninguna mercancía.");
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
                        //llenar el recycleview Ventas
                        Ventas ventas = new Ventas(idp, desc, precioStr, cantStr, subtStr);
                        adapter.addDatos(ventas);
                        Toast.makeText((getContext()), "Mercancia:" + desc + "(" + cantStr + ")", Toast.LENGTH_SHORT).show();
                        //asignar elementos para el envío de la venta
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
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void buscarCliente() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaClientesImagen.php";
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Cargando...", "Por favor espera.");
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }

    private void buscarMercancia() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaMercancia.php";
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Cargando...", "Por favor espera.");
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }

    private void loadData() {
        Context context = requireContext();
        SharedPreferences sp = context.getSharedPreferences("sesion", Context.MODE_PRIVATE);
        codvd    = sp.getString("codperfil", "Ivd0");
        perfil   = sp.getString("perfil", "General");
        vendedor = "Vendedor: "+sp.getString("usuario", "General");
        if (perfil.equals("Administrador")){
            btmerc.setVisibility(View.GONE);
            txttotal.setEnabled(true);
            lblobs.setVisibility(View.VISIBLE);
            txtobs.setVisibility(View.VISIBLE);
        }
        lblvd.setText(vendedor);
        // Reinicia los StringBuilders antes de cada nueva venta
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
        // Estado numérico inicial de la venta
        precio    = 0;
        cantidad  = 0;
        subtotal  = 0;
        vpagado   = 0;
        saldo     = 0;
        total     = 0;
        txttotal.setText("0");
        txtsaldo.setText("0");
    }

    // Verifica si el cliente ya tiene una venta pendiente
    private void verificarVentaExistente(String cedula, String nombre) {

        String url = "https://www.wmcsoftware.net/apps/softpymes/verificarVentaCliente.php?idc=" + cedula;

        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Verificando...", "Por favor espera.");

        JsonObjectRequest req = new JsonObjectRequest(
                com.android.volley.Request.Method.GET, url, null,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    boolean tieneVenta = response.optBoolean("tiene_venta", false);

                    if (tieneVenta) {
                        // El cliente ya tiene venta pendiente → advertencia
                        String idFv = response.optString("id_fv", "");
                        String saldov = response.optString("saldo", "");
                        AlertDialog.Builder warn = new AlertDialog.Builder(requireContext());
                        warn.setTitle("⚠ Venta existente");
                        warn.setMessage(
                                "El cliente " + nombre + " ya tiene una venta pendiente" +
                                        (idFv.isEmpty() ? "." : " (N° " + idFv + ").") +
                                        "\n\n¿Desea ir al módulo Actualizar Venta?");
                        warn.setPositiveButton("Sí, actualizar", (dialog, which) -> {
                            alertDialog.dismiss();
                            // Empaquetar datos para ActualizarVentasFragment
                            Bundle args = new Bundle();
                            args.putString("id_fv",   idFv);
                            args.putString("cedula",  cedula);
                            args.putString("nombre",  nombre);
                            args.putString("saldov",   saldov);
                            // Navegar al fragmento ActualizarVentasFragment
                            androidx.navigation.Navigation
                                    .findNavController(requireView())
                                    .navigate(R.id.actualizarVentas, args);
                        });
                        warn.setNegativeButton("No, nueva venta", (dialog, which) -> {
                            // Permite continuar igualmente si el usuario insiste
                            txtidcl.setText(cedula);
                            txtcliente.setText(nombre);
                            alertDialog.dismiss();
                        });
                        warn.setNeutralButton("Cancelar", (dialog, which) -> dialog.dismiss());
                        warn.create().show();

                    } else {
                        txtidcl.setText(cedula);
                        txtcliente.setText(nombre);
                        alertDialog.dismiss();
                    }
                },
                error -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    // Si el endpoint no existe aún, asigna el cliente sin validación para no bloquear el flujo
                    txtidcl.setText(cedula);
                    txtcliente.setText(nombre);
                    alertDialog.dismiss();
                    Log.w("VentasFragment", "verificarVentaExistente: " + error.getMessage());
                }
        );
        request.add(req);
    }


    private void guardarRegistro() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/guardarVenta.php";
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Guardando...", "Por favor espera.");
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    boolean ok = false;
                    String msj = "No se pudo procesar la respuesta del servidor.";
                    try {
                        jsonObject = new JSONObject(response);
                        msj = jsonObject.optString("mensaje", msj);
                        ok = jsonObject.optBoolean("success");
                    } catch (JSONException e) {
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                    }
                    if (ok) {
                        AppUtils.alertExito(requireContext(), "Éxito", msj);
                        limpiar(); // Llamar a limpiar después de una operación exitosa
                    } else {
                        AppUtils.alertError(requireContext(), "Atención", msj);
                    }

                },
                error -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    String errorMessage = "Error desconocido al guardar la venta.";
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

                    AppUtils.alertError(requireContext(), "Error", errorMessage);
                    Log.e("GuardarVenta", "Error: " + errorMessage, error); // Para ver el stack trace completo

                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("idcl", idcl);//cedula del cliente
                params.put("codvd", codvd);//id del vendedor
                params.put("idp", String.valueOf(codpv).trim());
                params.put("precio", String.valueOf(preciov).trim());
                params.put("cant", String.valueOf(cantv).trim());
                params.put("subtotal", String.valueOf(subtv).trim());
                params.put("total", total1); // total1 ya se actualiza desde 'subtotal' antes de guardar
                params.put("vpagado1", vpagado1);
                params.put("cuota2", cuota2);
                params.put("ncuotas", ncuotas2);
                params.put("saldo", saldo1);
                params.put("fpago",fpago);
                params.put("fechacobro", fechacobro);
                params.put("fechav", fechav);
                params.put("perfil", perfil);
                params.put("obs", obs);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

    private void limpiar() {
        txtidcl.setText("");
        txtcliente.setText("");
        txtfechacobro.setText("");
        txtfechav.setText("");
        txttotal.setText("0"); // Asegurar que el total visual se resetee a 0
        txtvpagado.setText("");
        txtctnorm.setText("");
        txtsaldo.setText("0"); // Asegurar que el saldo visual se resetee a 0
        txtncuotas.setText("");
        txtctnorm.setText("");
        txtobs.setText("");
        spnrfpago.setSelection(0);
        // Limpiar los StringBuilders para la próxima venta
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
        // Resetear las variables numéricas
        precio    = 0;
        cantidad  = 0;
        subtotal  = 0;
        vpagado   = 0;
        saldo     = 0;
        total     = 0;
        ncuotas   = 0;
        plazod    = 0;
        // Limpiar el RecyclerView; dispara onSubtotalChanged con el subtotal en 0
        adapter.clearData();
        txttotal.requestFocus();
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
        // Ocultar el diálogo de carga
        AppUtils.cerrarCargando(dialogCargando);
        AppUtils.alertError(requireContext(), "Error", "No se pudo consultar el servidor.");
    }

    @Override
    public void onResponse(JSONObject response) {
        // Lista de mercancías
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
                AppUtils.alertError(requireContext(), "Error", "No se pudo procesar la mercancía.");
            }
        }
        // Lista de clientes
        JSONArray jsonClientes = response.optJSONArray("cliente");
        if (jsonClientes != null) {
            try {
                listaClientes.clear(); // Limpiar lista antes de añadir nuevos datos
                for (int i = 0; i < jsonClientes.length(); i++){
                    Cliente clienteObj = new Cliente(); // Renombrar para evitar conflicto con la variable 'cliente' global
                    JSONObject jsonObjectCliente = jsonClientes.getJSONObject(i);
                    clienteObj.setCedula(jsonObjectCliente.optString("id"));
                    clienteObj.setNombre(jsonObjectCliente.optString("nombre"));
                    clienteObj.setObservacion(jsonObjectCliente.optString("observacion"));
                    clienteObj.setRfoto(jsonObjectCliente.optString("rfoto"));
                    clienteObj.setDato(jsonObjectCliente.optString("foto"));
                    listaClientes.add(clienteObj);
                }
            } catch (JSONException e) {
                // Error en la carga de clientes (último paso de la carga inicial)
                AppUtils.cerrarCargando(dialogCargando);
                AppUtils.alertError(requireContext(), "Error", "No se pudo procesar los clientes.");
            }
            listaClientes(getContext());
        }
        // Ocultar el diálogo de carga una vez que ambas cargas han terminado
        AppUtils.cerrarCargando(dialogCargando);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        fpago  = spnrfpago.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        fpago  = "";
        plazod = 0;
    }

    // Implementación de OnSubtotalChangeListener
    @Override
    public void onSubtotalChanged(double subtotalChange,int position) {
        auxcodpv = new StringBuilder(codpv);
        auxdescv = new StringBuilder(descv);
        auxcantv = new StringBuilder(cantv);
        auxpreciov = new StringBuilder(preciov);
        auxsubtv   = new StringBuilder(subtv);
        auxobsv    = new StringBuilder(obsv);

        auxcodpv = removeElementAtPosition(codpv,position,separador);
        auxdescv = removeElementAtPosition(descv,position,separador);
        auxcantv = removeElementAtPosition(cantv,position,separador);
        auxpreciov = removeElementAtPosition(preciov,position,separador);
        auxsubtv = removeElementAtPosition(subtv,position,separador);
        auxobsv = removeElementAtPosition(obsv,position,";");
        // Invocado por VentasAdapter cuando cambia el subtotal (negativo si se elimina un ítem)
        subtotal += subtotalChange;
        if (subtotal < 0) {
            subtotal = 0;
        }
        txttotal.setText(String.format(Locale.US, "%.0f", subtotal));
        // Recalcula el saldo con el valor pagado actual (0 si el campo está vacío)
        double valorPagadoActual = 0;
        try {
            if (!txtvpagado.getText().toString().isEmpty()) {
                valorPagadoActual = Double.parseDouble(txtvpagado.getText().toString());
            }
        } catch (NumberFormatException e) {
            Log.e("VentasFragment", "Error al parsear vpagado en onSubtotalChanged: " + e.getMessage());
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


    // Elimina el elemento en 'positionToRemove' de un StringBuilder separado por 'separator'
    public static StringBuilder removeElementAtPosition(StringBuilder sb, int positionToRemove, String separator) {
        String fullString = sb.toString();
        String[] elementsArray = fullString.split(separator);
        if (positionToRemove < 0 || positionToRemove >= elementsArray.length) {
            System.out.println("Error: Posición " + positionToRemove + " fuera de rango. No se realizó ninguna eliminación.");
            return sb;
        }
        List<String> elementsList = new ArrayList<>(Arrays.asList(elementsArray));
        elementsList.remove(positionToRemove);
        sb.setLength(0);
        for (int i = 0; i < elementsList.size(); i++) {
            sb.append(elementsList.get(i));
            sb.append(separator);
        }
        return sb;
    }


}
