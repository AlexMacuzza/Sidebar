package com.amcsoftware.sidebar;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Phrase;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import com.amcsoftware.sidebar.Entidades.Mercancia;
import com.amcsoftware.sidebar.adapter.InventarioAdapter;
import com.amcsoftware.sidebar.utils.AppUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class InventarioFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener,SearchView.OnQueryTextListener {
    ArrayList<Mercancia> listaMercancia;
    JSONObject jsonObject = null;
    JsonObjectRequest jsonObjectRequest;
    ImageButton btpdf;
    InventarioAdapter adapter;
    SweetAlertDialog dialogCargando;
    RecyclerView recyclerMercancia;
    RequestQueue request, requestQueue;
    SearchView txtbuscar;//buscador

    public InventarioFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_inventario, container, false);
        txtbuscar   = vista.findViewById(R.id.txtbuscar_mercancia);
        btpdf      = vista.findViewById(R.id.btpdf);
        listaMercancia    = new ArrayList<>();
        recyclerMercancia =  vista.findViewById(R.id.idRecycler);
        recyclerMercancia.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerMercancia.setHasFixedSize(true);

        adapter         = new InventarioAdapter(listaMercancia);
        request         = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo GET
        requestQueue    = Volley.newRequestQueue(requireContext());//Respuesta de las peticiones metódo POST

        cargarWebService();

        txtbuscar.setOnQueryTextListener(this);
        btpdf.setOnClickListener(v->generarPdf());

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

    private void generarPdf() {
        //Fecha y hora para el nombre del archivo
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fechahora = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
        // Definir la ruta donde se guardará el archivo
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Inventario_"+timeStamp+".pdf";

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
            Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 20, BaseColor.BLACK);
            // Crear el párrafo del título y centrarlo
            Paragraph title = new Paragraph("INVENTARIO", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            // Agregar un título al documento
            document.add(title);
            document.add(new Paragraph("\n")); // Salto de línea
            // Definir una fuente para el subtítulo
            Font subtitleFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10, BaseColor.BLACK);
            // Crear el párrafo del título y centrarlo
            Paragraph subtitle = new Paragraph(fechahora, subtitleFont);
            subtitle.setAlignment(Element.ALIGN_LEFT);
            // Agregar un subtítulo al documento
            document.add(subtitle);
            document.add(new Paragraph("\n")); // Salto de línea
            // Definir anchos relativos para 3 columnas (por ejemplo, 10% para la primera, 45% para las otras dos)
            float[] columnWidths = {1f, 4.5f, 2f};
            PdfPTable table = new PdfPTable(columnWidths);
            //table.setWidthPercentage(100); // Ocupa el 100% del ancho de la página
            // Definir una fuente para los encabezados
            Font headerFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 10, BaseColor.WHITE);
            // Definir un color de fondo para las celdas de encabezado
            BaseColor headerColor = new BaseColor(0, 169, 143); // Un color azul oscuro
            // Definir los encabezados de la tabla
            String[] headers = {"ID", "DETALLE", "CANTIDAD"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(headerColor);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER); // Alinear el texto al centro
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE); // Alinear verticalmente al centro
                table.addCell(cell);
            }
            // Llenar la tabla con datos
            if (adapter != null && adapter.getItemCount() > 0) {
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    Mercancia mercancia1= adapter.getItemAtPosition(i);
                    if (mercancia1 != null) {
                        // Las celdas de datos también se pueden alinear
                        PdfPCell idCell = new PdfPCell(new Phrase(mercancia1.getIdp(),subtitleFont));
                        idCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(idCell); // ID
                        PdfPCell detalle = new PdfPCell(new Phrase(mercancia1.getDescripcion(),subtitleFont));
                        detalle.setHorizontalAlignment(Element.ALIGN_LEFT);
                        table.addCell(detalle); // Detalle
                        PdfPCell qtyCell = new PdfPCell(new Phrase(mercancia1.getCantidad(),subtitleFont));
                        qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(qtyCell); // Cantidad
                    }
                }
                // Agregar la tabla al documento
                document.add(table);
                AppUtils.alertExito(requireContext(), "Éxito", "Reporte generado exitosamente.");
            }else{
                AppUtils.alertAdvertencia(requireContext(), "Sin datos", "No hay registros que mostrar.");
            }

        } catch (Exception e) {
            AppUtils.alertError(requireContext(), "Error", "No se pudo generar el PDF.");
        } finally {
            document.close();
        }

    }

    private void cargarWebService() {
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Cargando...", "Por favor espera.");
        String url = "https://www.wmcsoftware.net/apps/softpymes/inventario.php";
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
        txtbuscar.requestFocus();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // Ocultar el diálogo de carga
        AppUtils.cerrarCargando(dialogCargando);
        // Manejar la respuesta
        AppUtils.alertError(requireContext(), "Error", "No se pudo consultar el inventario.");
    }

    @Override
    public void onResponse(JSONObject response) {
        Mercancia mercancia;
        JSONArray json = response.optJSONArray("inventario");
        try {
            for (int i = 0; i< (json != null ? json.length() : 0); i++){
                mercancia = new Mercancia();
                JSONObject jsonObject;
                jsonObject = json.getJSONObject(i);
                mercancia.setIdp(jsonObject.optString("idp"));
                mercancia.setDescripcion(jsonObject.optString("detalle"));
                mercancia.setPreciov(jsonObject.optString("precio_venta"));
                mercancia.setCantidad(jsonObject.optString("saldo"));
                listaMercancia.add(mercancia);
            }
            // Ocultar el diálogo de carga
            AppUtils.cerrarCargando(dialogCargando);
            adapter = new InventarioAdapter(listaMercancia);
            //Mostrar inputbox para ajustar inventario
            adapter.setOnClickListener(v-> {
                final  String   idp   =  listaMercancia.get(recyclerMercancia.getChildAdapterPosition(v)).getIdp();
                final  Integer  saldo =  Integer.parseInt(listaMercancia.get(recyclerMercancia.getChildAdapterPosition(v)).getCantidad());
                mostrarDialogoInput(getContext(), idp,saldo);
            });
            recyclerMercancia.setAdapter(adapter);
            txtbuscar.requestFocus();
        } catch (JSONException e) {
            // Ocultar el diálogo de carga
            AppUtils.cerrarCargando(dialogCargando);
            // Manejar la respuesta
            AppUtils.alertError(requireContext(), "Error", "No se pudo consultar el inventario.");
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


    @SuppressLint("SetTextI18n")
    public void mostrarDialogoInput(Context context, String idp, Integer saldo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Ajustar Inventario:");
        // Inflar el layout personalizado con el EditText
        View viewInflada = LayoutInflater.from(context).inflate(R.layout.dialog_input_bodega, null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        final TextView codmer   = viewInflada.findViewById(R.id.txtidp);
        final EditText saldoant = viewInflada.findViewById(R.id.txtsaldoant),
                saldoact = viewInflada.findViewById(R.id.txtsaldoact);

        codmer.setText(idp);
        saldoant.setText(saldo.toString());
        builder.setView(viewInflada);

        // Configurar los botones del diálogo
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            final String saldo1 = saldoact.getText().toString();
            actualizarSaldo(idp, saldo1);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void actualizarSaldo(String idp, String saldo) {
        String url = "https://www.wmcsoftware.net/apps/softpymes/actualizarBodega.php";
        // Validar que los campos no estén vacíos
        if (saldo.isEmpty()) {
            AppUtils.alertAdvertencia(requireContext(), "Dato requerido", "El saldo no puede estar vacío.");
            return;
        }
        // Mostrar el diálogo de carga
        dialogCargando = AppUtils.mostrarCargando(requireContext(), "Actualizando...", "Por favor espera.");
        // Crear la solicitud POST
        StringRequest stringRequest =  new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    // Manejar la respuesta del servidor
                    String msj = "Inventario actualizado.";
                    try {
                        jsonObject = new JSONObject(response);
                        msj = jsonObject.optString("mensaje", msj);
                    } catch (JSONException e) {
                        Log.e("VOLLEY", "Error: " + e.getMessage());
                    }
                    AppUtils.alertExito(requireContext(), "Éxito", msj);
                    listaMercancia.clear();
                    cargarWebService();
                },
                error -> {
                    AppUtils.cerrarCargando(dialogCargando);
                    AppUtils.alertError(requireContext(), "Error de red", "No se pudo actualizar el inventario.");
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Enviar los parámetros al servidor
                Map<String, String> params = new HashMap<>();
                params.put("idp", idp);
                params.put("saldo", saldo);
                return params;
            }
        };
        // Agregar la solicitud a la cola
        requestQueue.add(stringRequest);
    }

}