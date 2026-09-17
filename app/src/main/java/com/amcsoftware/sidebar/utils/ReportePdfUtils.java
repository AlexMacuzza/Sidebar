package com.amcsoftware.sidebar.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.amcsoftware.sidebar.R;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Estilo visual único para todos los reportes PDF de la app: mismo logo, tipografía,
 * paleta de color y estructura de tabla (encabezado + filas alternadas + totales
 * destacados), en vez de que cada Fragment repita su propio boilerplate de iText.
 */
public class ReportePdfUtils {

    private ReportePdfUtils() {}

    public static final BaseColor COLOR_MARCA         = new BaseColor(0, 121, 107);   // Teal oscuro: encabezados y totales
    public static final BaseColor COLOR_FILA_ALTERNA  = new BaseColor(237, 247, 245); // Zebra striping
    public static final BaseColor COLOR_BORDE         = new BaseColor(214, 214, 214);
    public static final BaseColor COLOR_TEXTO_TENUE   = new BaseColor(110, 110, 110);

    public static Font fuenteTitulo()     { return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, BaseColor.BLACK); }
    public static Font fuenteSubtitulo()  { return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_MARCA); }
    public static Font fuenteImpresion()  { return FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_TEXTO_TENUE); }
    public static Font fuenteEncabezado() { return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE); }
    public static Font fuenteCelda()      { return FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK); }
    public static Font fuenteTotal()      { return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_MARCA); }

    /**
     * Agrega logo, título, subtítulo opcional (ej. cobrador/rango de fechas) y la
     * fecha de impresión, seguidos de una línea separadora. Mismo bloque para
     * todos los reportes en vez de repetir logo+título+fecha en cada Fragment.
     */
    public static void agregarEncabezado(Document document, Context context, String titulo, String subtitulo)
            throws DocumentException, java.io.IOException {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_negocio1);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Image logo = Image.getInstance(stream.toByteArray());
        logo.scaleToFit(110, 110);
        logo.setAbsolutePosition(20, 760);
        document.add(logo);

        Paragraph tituloP = new Paragraph(titulo, fuenteTitulo());
        tituloP.setAlignment(Element.ALIGN_CENTER);
        tituloP.setSpacingBefore(6f);
        document.add(tituloP);

        if (subtitulo != null && !subtitulo.isEmpty()) {
            Paragraph subtituloP = new Paragraph(subtitulo, fuenteSubtitulo());
            subtituloP.setAlignment(Element.ALIGN_CENTER);
            subtituloP.setSpacingBefore(2f);
            document.add(subtituloP);
        }

        String fechahora = "Generado: " + new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
        Paragraph impresionP = new Paragraph(fechahora, fuenteImpresion());
        impresionP.setAlignment(Element.ALIGN_RIGHT);
        impresionP.setSpacingBefore(6f);
        impresionP.setSpacingAfter(6f);
        document.add(impresionP);

        PdfPTable linea = new PdfPTable(1);
        linea.setWidthPercentage(100);
        PdfPCell celdaLinea = new PdfPCell();
        celdaLinea.setFixedHeight(1.5f);
        celdaLinea.setBackgroundColor(COLOR_MARCA);
        celdaLinea.setBorder(Rectangle.NO_BORDER);
        linea.addCell(celdaLinea);
        document.add(linea);
        document.add(new Paragraph("\n"));
    }

    public static PdfPTable crearTabla(float[] anchosRelativos) {
        PdfPTable table = new PdfPTable(anchosRelativos);
        table.setWidthPercentage(100);
        table.setSpacingBefore(2f);
        return table;
    }

    public static void agregarEncabezadosTabla(PdfPTable table, String[] headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, fuenteEncabezado()));
            cell.setBackgroundColor(COLOR_MARCA);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(5f);
            table.addCell(cell);
        }
        table.setHeaderRows(1);
    }

    /** Celda de datos con alternancia de color por fila (zebra), más legible que fondo plano. */
    public static PdfPCell celda(String texto, int alineacion, int fila) {
        PdfPCell cell = new PdfPCell(new Phrase(texto == null ? "" : texto, fuenteCelda()));
        cell.setHorizontalAlignment(alineacion);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        cell.setBorderColor(COLOR_BORDE);
        cell.setBackgroundColor(fila % 2 == 0 ? BaseColor.WHITE : COLOR_FILA_ALTERNA);
        return cell;
    }

    /** Línea de total destacada (fondo suave + texto en negrita), alineada a la derecha. */
    public static PdfPTable filaTotal(String etiqueta, String valor) {
        PdfPTable total = new PdfPTable(new float[]{3f, 2f});
        total.setWidthPercentage(55);
        total.setHorizontalAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(8f);

        PdfPCell etiquetaCell = new PdfPCell(new Phrase(etiqueta, fuenteTotal()));
        etiquetaCell.setBackgroundColor(COLOR_FILA_ALTERNA);
        etiquetaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        etiquetaCell.setPadding(6f);
        etiquetaCell.setBorder(Rectangle.BOX);
        etiquetaCell.setBorderColor(COLOR_MARCA);
        total.addCell(etiquetaCell);

        PdfPCell valorCell = new PdfPCell(new Phrase(valor, fuenteTotal()));
        valorCell.setBackgroundColor(COLOR_FILA_ALTERNA);
        valorCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valorCell.setPadding(6f);
        valorCell.setBorder(Rectangle.BOX);
        valorCell.setBorderColor(COLOR_MARCA);
        total.addCell(valorCell);

        return total;
    }
}
