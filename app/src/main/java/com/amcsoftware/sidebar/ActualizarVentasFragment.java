package com.amcsoftware.sidebar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amcsoftware.sidebar.Entidades.Mercancia;
import com.amcsoftware.sidebar.Entidades.Ventas;
import com.amcsoftware.sidebar.adapter.MercanciasVentasAdapter;
import com.amcsoftware.sidebar.listener.OnSubtotalChangeListener;
import com.amcsoftware.sidebar.adapter.VentasAdapter;
import com.amcsoftware.sidebar.utils.AppUtils;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
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

/**
 * ActualizarVentasFragment
 * Permite buscar una factura existente por su id_fv, cargar el cliente
 * asociado y agregar nuevos productos / registrar un nuevo pago.
 */
public class ActualizarVentasFragment extends Fragment
        implements AdapterView.OnItemSelectedListener, OnSubtotalChangeListener {

    EditText      txtNumVenta, txtctnorm, txtfechacobro,
            txttotal, txtsaldo, txtvpagado, txtncuotas, txtfechav, txtobs;
    ImageButton   btnBuscarVenta, btmerc, btguardar, btborrar;
    SweetAlertDialog dialogCargando;
    RecyclerView  recyclerMercancia, recyclerVentasM;
    Spinner       spnrfpago;
    TextView      lblobs, txtidcl, txtcliente, lblvd;

    ArrayList<Mercancia> listaMercancia = new ArrayList<>();
    ArrayList<Ventas>    listaventas    = new ArrayList<>();
    MercanciasVentasAdapter adapter1;
    VentasAdapter           adapter;

    RequestQueue     requestQueue, request;
    JsonObjectRequest jsonObjectRequest;
    android.app.AlertDialog alertDialog;
    android.app.AlertDialog.Builder builder;

    double   cantidad, cuota, ncuotas, subtotal, total, precio,
            res, saldo, vpagado, auxtl;
    int      n, plazod;
    String   codvd, vendedor, perfil, fpago, msj,
            saldo1, total1, vpagado1, cuota2, ncuotas2,
            fechacobro, fechav, obs, idcl, idFvActual, cedula,
            nombre,idfv, auxtotal;
    String   separador = "-";
    StringBuilder codpv, descv, preciov, cantv, subtv, obsv,
            auxcodpv, auxdescv, auxpreciov, auxcantv, auxsubtv, auxobsv;

    private static final String BASE_URL =
            "https://www.wmcsoftware.net/apps/softpymes/";

    public ActualizarVentasFragment() { }

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "DefaultLocale"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_actualizarventas, container, false);

        lblvd         = vista.findViewById(R.id.lblvd);
        txtNumVenta   = vista.findViewById(R.id.txtNumVenta);
        btnBuscarVenta= vista.findViewById(R.id.btnBuscarVenta);
        txtidcl       = vista.findViewById(R.id.txtidcl);
        txtcliente    = vista.findViewById(R.id.txtcliente);
        btmerc        = vista.findViewById(R.id.btnmercancia);
        btguardar     = vista.findViewById(R.id.btregistrarv);
        btborrar      = vista.findViewById(R.id.btnborrar);
        lblobs        = vista.findViewById(R.id.lblobs);
        spnrfpago     = vista.findViewById(R.id.spnrfpago);
        txttotal      = vista.findViewById(R.id.txtventat);
        txtvpagado    = vista.findViewById(R.id.txtvpagado);
        txtsaldo      = vista.findViewById(R.id.txtsaldo);
        txtctnorm     = vista.findViewById(R.id.txtctanormal);
        txtncuotas    = vista.findViewById(R.id.txtnctas);
        txtfechav     = vista.findViewById(R.id.txtfechav);
        txtfechacobro = vista.findViewById(R.id.txtfechacobro);
        txtobs        = vista.findViewById(R.id.txtobs);
        recyclerVentasM = vista.findViewById(R.id.idRecycler);
        builder      = new android.app.AlertDialog.Builder(requireContext());
        requestQueue = Volley.newRequestQueue(requireContext());
        request      = Volley.newRequestQueue(requireContext());
        recyclerVentasM.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerVentasM.setHasFixedSize(true);
        adapter = new VentasAdapter(listaventas, this);
        recyclerVentasM.setAdapter(adapter);
        spnrfpago.setOnItemSelectedListener(this);

        loadData();
        // Aplicar argumentos recibidos desde VentasFragment
        if (idfv != null && !idfv.isEmpty()) {
            idFvActual = idfv;          // variable usada por el resto de la lógica
            idcl       = cedula;
            txtNumVenta.setText(idfv);
            txtidcl.setText(cedula);
            txtcliente.setText(nombre);
            resetAcumulados();
        }
        // Cargar mercancías al abrir el fragment
        buscarMercancia();
        // Buscar factura por número
        btnBuscarVenta.setOnClickListener(v -> {
            String numVenta = txtNumVenta.getText().toString().trim();
            if (numVenta.isEmpty()) {
                AppUtils.alertAdvertencia(requireContext(), "Dato requerido", "Ingrese el número de venta.");
                return;
            }
            if (!AppUtils.hayConectividad(requireContext())) {
                AppUtils.alertSinInternet(requireContext());
                return;
            }
            buscarFactura(numVenta);
        });

        // También buscar al pulsar "enter" en el teclado
        txtNumVenta.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                btnBuscarVenta.performClick();
                return true;
            }
            return false;
        });

        // Botón agregar mercancías
        btmerc.setOnClickListener(v -> {
            if (idFvActual == null || idFvActual.isEmpty()) {
                AppUtils.alertAdvertencia(requireContext(), "Atención", "Primero busque una venta válida.");
                return;
            }
            mostrarDialogoInput(getContext());
        });

        // Botón guardar actualización
        btguardar.setOnClickListener(v -> {
            if (idFvActual == null || idFvActual.isEmpty()) {
                AppUtils.alertAdvertencia(requireContext(), "Atención", "No hay venta cargada para actualizar.");
                return;
            }

            // Capturar valores
            total1     = String.format(Locale.US, "%.0f", subtotal);
            obs        = String.valueOf(obsv).trim();
            if (perfil.equals("Administrador")) {
                total1 = String.format(Locale.US, "%.0f", total);
                obs    = txtobs.getText().toString();
            }
            saldo1     = txtsaldo.getText().toString().trim();
            vpagado1   = txtvpagado.getText().toString().trim();
            cuota2     = txtctnorm.getText().toString().trim();
            ncuotas2   = txtncuotas.getText().toString().trim();
            fechacobro = txtfechacobro.getText().toString().trim();
            fechav     = txtfechav.getText().toString().trim();

            // Validar campos obligatorios
            if (fechacobro.isEmpty() || total1.isEmpty() ||
                    cuota2.isEmpty() || fpago == null || fpago.isEmpty()) {
                AppUtils.alertAdvertencia(requireContext(), "Campos incompletos",
                        "Por favor, complete todos los campos.");
                return;
            }

            // Confirmación
            AppUtils.alertConfirmar(requireContext(), "Softpymes",
                    "¿Confirma actualizar la venta N° " + idFvActual + "?", "Sí", "No", () -> {
                if (!AppUtils.hayConectividad(requireContext())) {
                    AppUtils.alertSinInternet(requireContext());
                    return;
                }
                actualizarRegistro();
            });
        });

        // Botón borrar (limpiar pagos / saldos)
        btborrar.setOnClickListener(v -> {
            txtvpagado.setText("");
            txtsaldo.setText("");
            txtctnorm.setText("");
            txtncuotas.setText("");
            txtfechacobro.setText("");
            txtfechav.setText("");
            txtobs.setText("");
            cuota = saldo = vpagado = ncuotas = 0;
            plazod = 0;
            txtvpagado.requestFocus();
        });

        // Calendario fecha de cobro
        txtfechacobro.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (view, yr, mo, day) -> txtfechacobro.setText(
                            String.format("%04d-%02d-%02d", yr, mo + 1, day)),
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Total (solo Administrador)
        txttotal.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    total = Double.parseDouble(txttotal.getText().toString());
                    saldo = total;
                    txtsaldo.setText(String.format(Locale.US, "%.0f", saldo));
                    txtvpagado.requestFocus();
                } catch (NumberFormatException e) {
                    txttotal.setText("");
                    txtsaldo.setText("");
                    AppUtils.alertAdvertencia(requireContext(), "Valor inválido", "Ingrese un valor numérico.");
                }
            }
            return false;
        });

        // Calcular saldo
        txtvpagado.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    vpagado = Double.parseDouble(txtvpagado.getText().toString());
                    total   = Double.parseDouble(txttotal.getText().toString());
                    saldo   = total - vpagado;
                    txtsaldo.setText(String.format(Locale.US, "%.0f", saldo));
                    txtctnorm.requestFocus();
                } catch (NumberFormatException e) {
                    txtvpagado.setText("");
                    txtsaldo.setText("");
                    AppUtils.alertAdvertencia(requireContext(), "Valor inválido", "Ingrese un valor numérico.");
                }
            }
            return false;
        });

        // Calcular cuotas
        txtctnorm.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    cuota = Double.parseDouble(txtctnorm.getText().toString());
                    if (saldo > 0) {
                        res = saldo % cuota;
                        if (res == 0) {
                            plazod  = plazodias();
                            ncuotas = saldo / cuota;
                            plazod  = (int) (plazod * ncuotas);
                            txtncuotas.setText(String.format(Locale.US, "%.0f", ncuotas));
                            msj = "Distribución de cuotas:\n" +
                                    "Cuotas normales: " + txtncuotas.getText().toString() +
                                    " de $" + String.format(Locale.US, "%.0f", cuota);
                            msgBox(msj);
                        } else {
                            ncuotas = Math.ceil(saldo / cuota);
                            double totalCuotasNormales = cuota * (ncuotas - 1);
                            double ultimaCuota         = saldo - totalCuotasNormales;
                            plazod  = plazodias();
                            plazod  = (int) (plazod * ncuotas);
                            txtncuotas.setText(String.format(Locale.US, "%.0f", ncuotas));
                            msj = "Distribución de cuotas:\n" +
                                    "Cuotas normales: " + (int) (ncuotas - 1) +
                                    " de $" + String.format(Locale.US, "%.0f", cuota) + "\n" +
                                    "Cuota  final    : $" +
                                    String.format(Locale.US, "%.0f", ultimaCuota);
                            msgBox(msj);
                        }
                        try {
                            LocalDate fechaVencimiento = LocalDate.now().plusDays(plazod);
                            txtfechav.setText(fechaVencimiento.toString());
                        } catch (DateTimeParseException e) {
                            AppUtils.alertError(requireContext(), "Error", "No se pudo procesar la fecha.");
                        }
                    } else {
                        txtncuotas.setText("");
                    }
                    btguardar.requestFocus();
                } catch (NumberFormatException e) {
                    txtctnorm.setText("");
                    AppUtils.alertAdvertencia(requireContext(), "Valor inválido",
                            "Ingrese un valor numérico para la cuota.");
                }
            }
            return false;
        });

        // Bloquear botón atrás (sin acción)
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { }
        };
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), callback);

        return vista;
    }

    // Buscar factura en el servidor
    private void buscarFactura(String numVenta) {
        String url = BASE_URL + "listaClientesImagen.php?id_fv=" + numVenta;
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Consultando...", "Por favor espera.");
        jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    boolean success = response.optBoolean("success", false);
                    if (success) {
                        JSONArray clientes = response.optJSONArray("data");
                        if (clientes != null && clientes.length() > 0) {
                            try {
                                JSONObject c = clientes.getJSONObject(0);
                                idcl = c.optString("cedula");
                                String nombre = c.optString("nombre");
                                // Leer id_fv si el PHP lo devuelve; de lo contrario usar el buscado
                                idFvActual = c.optString("idfv", numVenta);
                                txtidcl.setText(idcl);
                                txtcliente.setText(nombre);
                                // Resetear acumulados para la actualización
                                resetAcumulados();
                                txttotal.setText(auxtotal);
                                txtsaldo.setText("0");
                                AppUtils.alertExito(requireContext(), "Éxito",
                                        "Venta N° " + idFvActual + " cargada. Cliente: " + nombre);
                            } catch (JSONException e) {
                                AppUtils.alertError(requireContext(), "Error", "Error al leer datos de la venta.");
                            }
                        } else {
                            limpiarCliente();
                            AppUtils.alertError(requireContext(), "Atención",
                                    "No se encontró la venta N° " + numVenta + ".");
                        }
                    } else {
                        limpiarCliente();
                        AppUtils.alertError(requireContext(), "Atención",
                                response.optString("mensaje", "Error al buscar la venta."));
                    }
                },
                error -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    AppUtils.alertError(requireContext(), "Error de red", "No se pudo buscar la venta.");
                }
        );
        request.add(jsonObjectRequest);
    }

    // Cargar lista de mercancías disponibles
    private void buscarMercancia() {
        String url = "https://www.wmcsoftware.net/apps/softpymes/listaMercancia.php";
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Cargando...", "Por favor espera.");
        jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    JSONArray jsonMercancia = response.optJSONArray("mercancia");
                    if (jsonMercancia != null) {
                        try {
                            listaMercancia.clear();
                            for (int i = 0; i < jsonMercancia.length(); i++) {
                                Mercancia m = new Mercancia();
                                JSONObject obj = jsonMercancia.getJSONObject(i);
                                m.setIdp(obj.optString("idp"));
                                m.setDescripcion(obj.optString("detalle"));
                                m.setPrecioc(obj.optString("precio_compra"));
                                m.setPreciov(obj.optString("precio_venta"));
                                m.setCantidad(obj.optString("cantidad_inicial"));
                                listaMercancia.add(m);
                            }
                        } catch (JSONException e) {
                            AppUtils.alertError(requireContext(), "Error", "No se pudo procesar la mercancía.");
                        }
                    }
                },
                error -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    AppUtils.alertError(requireContext(), "Error", "No se pudo cargar la mercancía.");
                }
        );
        request.add(jsonObjectRequest);
    }

    // Diálogo para seleccionar mercancías
    public void mostrarDialogoInput(Context context) {
        builder.setTitle("Agregar Producto:");
        View viewInflada = LayoutInflater.from(context)
                .inflate(R.layout.fragment_mercancias_ventas, null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        android.widget.SearchView searchView =
                viewInflada.findViewById(R.id.txtbuscar);
        final ImageButton admp = viewInflada.findViewById(R.id.btnmerc);
        recyclerMercancia = viewInflada.findViewById(R.id.idRecycler);
        adapter1 = new MercanciasVentasAdapter(listaMercancia);
        recyclerMercancia.setLayoutManager(new LinearLayoutManager(context));
        recyclerMercancia.setHasFixedSize(true);
        recyclerMercancia.setAdapter(adapter1);

        admp.setOnClickListener(v -> {
            List<Mercancia> selected = adapter1.getSelectedDataOnly();
            if (selected.isEmpty()) {
                AppUtils.alertAdvertencia(context, "Sin selección", "No seleccionó ninguna mercancía.");
            } else {
                n = selected.size();
                for (int i = 0; i < n; i++) {
                    String idp      = selected.get(i).getIdp();
                    String desc     = selected.get(i).getDescripcion();
                    String precioStr= selected.get(i).getPreciov();
                    String cantStr  = selected.get(i).getCantidad();
                    String subtStr  = selected.get(i).getSubtotal();
                    if (cantStr.isEmpty() || Double.parseDouble(cantStr) <= 0) {
                        Toast.makeText(context,
                                "Asigne una cantidad válida para " + desc + "!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        adapter.addDatos(new Ventas(idp, desc, precioStr, cantStr, subtStr));
                        codpv.append(idp).append("-");
                        descv.append(desc).append("-");
                        preciov.append(precioStr).append("-");
                        cantv.append(cantStr).append("-");
                        subtv.append(subtStr).append("-");
                        obsv.append(desc).append("(").append(cantStr).append(")").append(";");
                        try {
                            subtotal += Double.parseDouble(subtStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(context,
                                    "Error de cálculo en: " + desc, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                auxtl = subtotal + Double.parseDouble(auxtotal);
                txttotal.setText(String.format(Locale.US, "%.0f", auxtl));
                txtvpagado.setText("");
                saldo = auxtl;
                txtsaldo.setText(String.format(Locale.US, "%.0f", saldo));
                txtvpagado.requestFocus();
            }
            searchView.setQuery("", false);
            searchView.requestFocus();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.cancel();
            searchView.setQuery("", false);
        });
        builder.setOnDismissListener(dialog -> searchView.setQuery("", false));
        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { return false; }
            @Override public boolean onQueryTextChange(String t) {
                adapter1.filtrado(t); return true;
            }
        });
        builder.setView(viewInflada);
        alertDialog = builder.create();
        alertDialog.show();
    }

    // Enviar actualización al servidor
    private void actualizarRegistro() {
        String url = BASE_URL + "actualizarVentaXCliente.php";
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Actualizando...", "Por favor espera.");

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, url,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    try {
                        JSONObject json = new JSONObject(response);
                        boolean ok = json.optBoolean("success", false);
                        String msj = json.optString("mensaje", "Operación realizada.");
                        if (ok) {
                            AppUtils.alertExito(requireContext(), "Éxito", msj);
                            limpiar();
                        } else {
                            AppUtils.alertError(requireContext(), "Atención", msj);
                        }
                    } catch (JSONException e) {
                        Log.e("ActualizarVentas", "JSON parse error: " + e.getMessage());
                        AppUtils.alertError(requireContext(), "Error", "No se pudo procesar la respuesta del servidor.");
                    }
                },
                error -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    String errorMessage = resolverErrorVolley(error);
                    AppUtils.alertError(requireContext(), "Error", errorMessage);
                    Log.e("ActualizarVentas", "Error: " + errorMessage, error);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_fv",    idFvActual);
                params.put("idcl",     idcl);
                params.put("idp",      String.valueOf(codpv).trim());
                params.put("precio",   String.valueOf(preciov).trim());
                params.put("cant",     String.valueOf(cantv).trim());
                params.put("subtotal", String.valueOf(subtv).trim());
                params.put("total",    total1);
                params.put("vpagado1", vpagado1);
                params.put("cuota2",   cuota2);
                params.put("ncuotas",  ncuotas2);
                params.put("saldo",    saldo1);
                params.put("fpago",    fpago != null ? fpago : "");
                params.put("fechacobro", fechacobro);
                params.put("fechav",   fechav);
                params.put("perfil",   perfil);
                params.put("obs",      obs);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void loadData() {
        SharedPreferences sp = requireContext()
                .getSharedPreferences("sesion", Context.MODE_PRIVATE);
        codvd   = sp.getString("codperfil", "Ivd0");
        perfil  = sp.getString("perfil", "General");
        vendedor= "Vendedor: " + sp.getString("usuario", "General");

        if (perfil.equals("Administrador")) {
            btmerc.setVisibility(View.GONE);
            txttotal.setEnabled(true);
            lblobs.setVisibility(View.VISIBLE);
            txtobs.setVisibility(View.VISIBLE);
        }


        resetAcumulados();
        subtotal = vpagado = saldo = auxtl =  total = precio = cantidad = 0;
        plazod = 0;
        txttotal.setText(auxtotal);
        txtsaldo.setText("0");
    }

    private void resetAcumulados() {
        codpv   = new StringBuilder(); descv    = new StringBuilder();
        preciov = new StringBuilder(); cantv    = new StringBuilder();
        subtv   = new StringBuilder(); obsv     = new StringBuilder();
        auxcodpv= new StringBuilder(); auxdescv = new StringBuilder();
        auxpreciov = new StringBuilder(); auxcantv = new StringBuilder();
        auxsubtv   = new StringBuilder(); auxobsv  = new StringBuilder();
        subtotal = 0;
        adapter.clearData();
        txttotal.setText(auxtotal);
        txtsaldo.setText("0");
    }

    private void limpiarCliente() {
        idFvActual = null;
        idcl       = null;
        txtidcl.setText("");
        txtcliente.setText("");
        resetAcumulados();
    }

    private void limpiar() {
        txtNumVenta.setText("");
        limpiarCliente();
        txtvpagado.setText("");
        txtsaldo.setText("0");
        txtctnorm.setText("");
        txtncuotas.setText("");
        txtfechacobro.setText("");
        txtfechav.setText("");
        txtobs.setText("");
        spnrfpago.setSelection(0);
        cuota = saldo = vpagado = ncuotas = total = 0;
        plazod = 0;
    }

    private int plazodias() {
        if (fpago == null) return 30;
        if (fpago.equals("Mensual"))    return 30;
        if (fpago.equals("Quincenal"))  return 15;
        return 7;
    }

    private void msgBox(String mensaje) {
        AppUtils.alertExito(requireContext(), "Softpymes", mensaje);
    }

    private String resolverErrorVolley(VolleyError error) {
        if (error instanceof NoConnectionError)
            return "No hay conexión a Internet.";
        if (error instanceof TimeoutError)
            return "Tiempo de espera agotado.";
        if (error.networkResponse != null) {
            return "Error del servidor (" + error.networkResponse.statusCode + "): "
                    + new String(error.networkResponse.data, StandardCharsets.UTF_8);
        }
        return "Error desconocido: " + error.getMessage();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        fpago = spnrfpago.getItemAtPosition(pos).toString();
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) { fpago = ""; plazod = 0; }

    // Eliminación de ítems en el RecyclerView de mercancías
    @Override
    public void onSubtotalChanged(double subtotalChange, int position) {
        auxcodpv   = removeElementAtPosition(codpv,   position, separador);
        auxdescv   = removeElementAtPosition(descv,   position, separador);
        auxcantv   = removeElementAtPosition(cantv,   position, separador);
        auxpreciov = removeElementAtPosition(preciov, position, separador);
        auxsubtv   = removeElementAtPosition(subtv,   position, separador);
        auxobsv    = removeElementAtPosition(obsv,    position, ";");

        auxtl    += subtotalChange;
        subtotal += subtotalChange;
        if (subtotal < 0) subtotal = 0;
        txttotal.setText(String.format(Locale.US, "%.0f", auxtl));

        double vp = 0;
        try {
            if (!txtvpagado.getText().toString().isEmpty())
                vp = Double.parseDouble(txtvpagado.getText().toString());
        } catch (NumberFormatException e) {
            Log.e("ActualizarVentas", "Error parsear vpagado: " + e.getMessage());
        }
        saldo = subtotal - vp;
        txtsaldo.setText(String.format(Locale.US, "%.0f", saldo));

        codpv = auxcodpv; descv = auxdescv; cantv = auxcantv;
        preciov = auxpreciov; subtv = auxsubtv; obsv = auxobsv;
    }
    // Eliminar elemento de un StringBuilder acumulado por posición
    public static StringBuilder removeElementAtPosition(
            StringBuilder sb, int pos, String sep) {
        String[] arr = sb.toString().split(sep);
        if (pos < 0 || pos >= arr.length) return sb;
        List<String> list = new ArrayList<>(Arrays.asList(arr));
        list.remove(pos);
        sb.setLength(0);
        for (String s : list) sb.append(s).append(sep);
        return sb;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener(
                "clientes", this, (key, bundle) -> {
                    // No aplica en este módulo: el cliente se resuelve desde la factura
                });
        // Recibir datos del fragment Ventas
        Bundle bundleArgs = getArguments();
        if (bundleArgs != null) {
            idfv     = bundleArgs.getString("id_fv", "");
            cedula   = bundleArgs.getString("cedula", "");
            nombre   = bundleArgs.getString("nombre", "");
            auxtotal = bundleArgs.getString("saldov", "0");
        }
    }
}
