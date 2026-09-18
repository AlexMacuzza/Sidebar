package com.amcsoftware.sidebar.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import androidx.core.content.FileProvider;
import cn.pedant.SweetAlert.SweetAlertDialog;
import java.io.File;

public class AppUtils {

    // Constructor privado: clase utilitaria, no se instancia
    private AppUtils() {}

    // ════════════════════════════════════════════════════════════════════════
    //  ARCHIVOS – Abrir PDF generado
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Abre un PDF recién generado con el visor que el usuario tenga instalado,
     * en vez de dejarlo solo guardado en Descargas para que lo busque a mano.
     * Usa FileProvider porque desde Android 7 (API 24) un content:// es
     * obligatorio para compartir un archivo del almacenamiento externo con
     * otra app.
     *
     * @param context Contexto de la Activity o Fragment
     * @param archivo Archivo PDF ya cerrado/escrito en disco
     */
    public static void abrirPdf(Context context, File archivo) {
        try {
            Uri uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", archivo);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            alertAdvertencia(context, "Sin visor de PDF",
                    "El reporte se guardó en Descargas, pero no hay una app instalada para abrirlo.");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CONECTIVIDAD
    // ════════════════════════════════════════════════════════════════════════
    /**
     * Comprueba si el dispositivo tiene acceso REAL a Internet.
     * Compatible con API 23+ (Android 6.0 en adelante).
     * Detecta WiFi, datos móviles y Ethernet.
     * <p>
     * IMPORTANTE: no basta con {@code hasTransport(...)}, ya que esa bandera
     * solo indica que la interfaz de red está activa (ej. datos móviles
     * encendidos), pero NO que tenga salida real a Internet. Un caso típico
     * donde falla: datos móviles activados sin señal/plan, o un Wi-Fi con
     * portal cautivo sin autenticar. En esos casos {@code hasTransport}
     * devuelve true igual, y la app termina enviando la petición para que
     * falle después con un error genérico.
     * <p>
     * Por eso se exige además {@code NET_CAPABILITY_VALIDATED}: bandera que
     * el sistema Android setea únicamente cuando ya comprobó, con su propio
     * mecanismo interno, que esa red efectivamente llega a Internet.
     *
     * @param context Contexto de la Activity o Fragment
     * @return true si hay conexión activa y validada, false en caso contrario
     */
    public static boolean hayConectividad(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        android.net.Network red = cm.getActiveNetwork();
        if (red == null) return false;

        NetworkCapabilities caps = cm.getNetworkCapabilities(red);
        if (caps == null) return false;

        boolean tieneTransporte =
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);

        boolean tieneInternetValidado =
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

        return tieneTransporte && tieneInternetValidado;
    }
    // ════════════════════════════════════════════════════════════════════════
    //  SWEETALERT – Carga (PROGRESS_TYPE)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Muestra un diálogo de progreso animado.
     * El llamador guarda la referencia para cerrarlo con cerrarCargando().
     *
     * @param context   Contexto
     * @param titulo    Título del diálogo  (ej. "Guardando...")
     * @param subtitulo Mensaje secundario  (ej. "Por favor espera.")
     * @return          Instancia activa del diálogo
     */
    public static SweetAlertDialog mostrarCargando(Context context,
                                                   String titulo,
                                                   String subtitulo) {
        SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        dialog.setTitleText(titulo);
        dialog.setContentText(subtitulo);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    /**
     * Cierra el diálogo de carga de forma segura con animación.
     * Verifica que no sea nulo ni ya esté cerrado antes de cerrarlo.
     *
     * @param dialog Instancia devuelta por mostrarCargando()
     */
    public static void cerrarCargando(SweetAlertDialog dialog) {
        if (dialog != null && dialog.isShowing())
            dialog.dismissWithAnimation();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SWEETALERT – Éxito (SUCCESS_TYPE)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Muestra un diálogo de éxito con ícono verde de verificación.
     *
     * @param context Contexto
     * @param titulo  Título      (ej. "¡Guardado!")
     * @param mensaje Descripción (ej. "Registro creado exitosamente.")
     */
    public static void alertExito(Context context, String titulo, String mensaje) {
        alertExito(context, titulo, mensaje, null);
    }

    public static void alertExito(Context context, String titulo, String mensaje, Runnable onAceptar) {
        new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(titulo)
                .setContentText(mensaje)
                .setConfirmText("ACEPTAR")
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    if (onAceptar != null) {
                        onAceptar.run();
                    }
                })
                .show();
    }
    // ════════════════════════════════════════════════════════════════════════
    //  SWEETALERT – Error (ERROR_TYPE)
    // ════════════════════════════════════════════════════════════════════════
    /**
     * Muestra un diálogo de error con ícono rojo.
     *
     * @param context Contexto
     * @param titulo  Título      (ej. "Error")
     * @param mensaje Descripción (ej. "No se pudo conectar al servidor.")
     */
    public static void alertError(Context context, String titulo, String mensaje) {
        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(titulo)
                .setContentText(mensaje)
                .setConfirmText("Cerrar")
                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SWEETALERT – Advertencia (WARNING_TYPE)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Muestra un diálogo de advertencia con ícono amarillo.
     *
     * @param context Contexto
     * @param titulo  Título      (ej. "Campos incompletos")
     * @param mensaje Descripción (ej. "Completa todos los campos.")
     */
    public static void alertAdvertencia(Context context, String titulo, String mensaje) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(titulo)
                .setContentText(mensaje)
                .setConfirmText("ACEPTAR")
                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SWEETALERT – Sin internet (WARNING_TYPE especializado)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Atajo para mostrar el aviso estándar de sin conexión.
     * Mensaje fijo, no requiere parámetros de texto.
     *
     * @param context Contexto
     */
    public static void alertSinInternet(Context context) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Sin conexión")
                .setContentText("Verifica tu conexión a Internet e intenta de nuevo.")
                .setConfirmText("Entendido")
                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SWEETALERT – Confirmación (WARNING_TYPE con dos botones)
    // ════════════════════════════════════════════════════════════════════════
    /**
     * Muestra un diálogo de confirmación con botón positivo y botón cancelar.
     * Ideal para operaciones destructivas: eliminar, sobrescribir, cerrar sesión, etc.
     *
     * @param context        Contexto
     * @param titulo         Título       (ej. "¿Eliminar tarea?")
     * @param mensaje        Descripción  (ej. "Esta acción no se puede deshacer.")
     * @param textoConfirmar Botón OK     (ej. "Sí, eliminar")
     * @param textoCancelar  Botón cancel (ej. "Cancelar")
     * @param onConfirmar    Lambda ejecutado solo si el usuario confirma
     */
    public static void alertConfirmar(Context context,
                                      String titulo,
                                      String mensaje,
                                      String textoConfirmar,
                                      String textoCancelar,
                                      Runnable onConfirmar) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(titulo)
                .setContentText(mensaje)
                .setConfirmText(textoConfirmar)
                .setCancelText(textoCancelar)
                .showCancelButton(true)
                .setConfirmClickListener(dialog -> {
                    dialog.dismissWithAnimation();
                    onConfirmar.run();
                })
                .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }
}
