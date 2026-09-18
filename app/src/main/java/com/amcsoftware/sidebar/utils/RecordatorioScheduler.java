package com.amcsoftware.sidebar.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.amcsoftware.sidebar.Entidades.Agenda;
import com.amcsoftware.sidebar.notificaciones.RecordatorioReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Programa/cancela los recordatorios de Agenda como alarmas exactas del sistema
 * (AlarmManager). La notificación suena aunque el módulo Agenda o la app no
 * estén abiertos, siempre que llegue la fecha y hora programadas: el disparo
 * lo entrega el sistema operativo a {@link RecordatorioReceiver}, no la app.
 */
public class RecordatorioScheduler {

    private RecordatorioScheduler() {}

    public static final String EXTRA_ID          = "idt";
    public static final String EXTRA_DESCRIPCION = "descripcion";
    public static final String EXTRA_REPETIR     = "repetir";
    public static final String EXTRA_INTERVALO   = "intervalodias";

    /** true si la app puede programar alarmas exactas (siempre true antes de Android 12). */
    public static boolean tienePermisoAlarmaExacta(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        return alarmManager != null && alarmManager.canScheduleExactAlarms();
    }

    /** Lleva al usuario a la pantalla del sistema para conceder "Alarmas y recordatorios". */
    public static void solicitarPermisoAlarmaExacta(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Programa (o reprograma) la alarma de una tarea de Agenda para su próxima
     * ocurrencia futura. Si la fecha/hora ya pasó y la tarea no se repite, no
     * programa nada (tarea puntual vencida).
     */
    public static void programar(Context context, Agenda tarea) {
        if (tarea.getIdt() == null || tarea.getIdt().isEmpty()) return;
        Date disparo = calcularDisparo(tarea.getFechat(), tarea.getHora());
        if (disparo == null) return;

        String repetir = (tarea.getRepetir() == null || tarea.getRepetir().isEmpty())
                ? "Ninguna" : tarea.getRepetir();

        if ("Ninguna".equals(repetir) && disparo.getTime() <= System.currentTimeMillis()) {
            return; // tarea puntual ya vencida: no se reprograma
        }
        // Si la ocurrencia calculada ya pasó pero la tarea es recurrente, se
        // adelanta hasta la próxima ocurrencia futura (ej. tras reinstalar o
        // reprogramar luego de un reinicio del dispositivo).
        while (!"Ninguna".equals(repetir) && disparo.getTime() <= System.currentTimeMillis()) {
            disparo = siguienteOcurrencia(disparo, repetir, tarea.getIntervalodias());
            if (disparo == null) return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, RecordatorioReceiver.class);
        intent.putExtra(EXTRA_ID, tarea.getIdt());
        intent.putExtra(EXTRA_DESCRIPCION, tarea.getDescripcion());
        intent.putExtra(EXTRA_REPETIR, repetir);
        intent.putExtra(EXTRA_INTERVALO, tarea.getIntervalodias());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, idAlarma(tarea.getIdt()), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        try {
            if (tienePermisoAlarmaExacta(context)) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparo.getTime(), pendingIntent);
            } else {
                // Sin el permiso especial de Android 12+, se degrada a una alarma
                // inexacta (el sistema puede retrasarla) en vez de fallar en silencio.
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparo.getTime(), pendingIntent);
            }
        } catch (SecurityException e) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparo.getTime(), pendingIntent);
        }
    }

    /**
     * Igual que {@link #programar}, pero recibe ya calculada la fecha/hora del
     * disparo en vez de derivarla de fechat+hora. La usa {@code RecordatorioReceiver}
     * para reprogramar la siguiente ocurrencia de una tarea recurrente.
     */
    public static void programarEnFecha(Context context, Agenda tarea, Date disparo) {
        if (tarea.getIdt() == null || tarea.getIdt().isEmpty() || disparo == null) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, RecordatorioReceiver.class);
        intent.putExtra(EXTRA_ID, tarea.getIdt());
        intent.putExtra(EXTRA_DESCRIPCION, tarea.getDescripcion());
        intent.putExtra(EXTRA_REPETIR, tarea.getRepetir());
        intent.putExtra(EXTRA_INTERVALO, tarea.getIntervalodias());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, idAlarma(tarea.getIdt()), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        try {
            if (tienePermisoAlarmaExacta(context)) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparo.getTime(), pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparo.getTime(), pendingIntent);
            }
        } catch (SecurityException e) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparo.getTime(), pendingIntent);
        }
    }

    /** Cancela la alarma programada de una tarea (ej. al eliminarla). */
    public static void cancelar(Context context, String idt) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null || idt == null) return;
        Intent intent = new Intent(context, RecordatorioReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, idAlarma(idt), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    /** Calcula la siguiente fecha/hora tras disparar una alarma recurrente. */
    public static Date siguienteOcurrencia(Date anterior, String repetir, String intervaloDiasStr) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(anterior);
        switch (repetir) {
            case "Diario":
                cal.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case "Semanal":
                cal.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case "Mensual":
                cal.add(Calendar.MONTH, 1);
                break;
            case "Personalizado": {
                int dias;
                try {
                    dias = Integer.parseInt(intervaloDiasStr);
                } catch (Exception e) {
                    dias = 1;
                }
                if (dias <= 0) dias = 1;
                cal.add(Calendar.DAY_OF_YEAR, dias);
                break;
            }
            default:
                return null; // "Ninguna": no hay siguiente ocurrencia
        }
        return cal.getTime();
    }

    private static Date calcularDisparo(String fecha, String hora) {
        if (fecha == null || fecha.isEmpty()) return null;
        String horaFinal = (hora == null || hora.isEmpty()) ? "08:00" : hora;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return sdf.parse(fecha + " " + horaFinal);
        } catch (ParseException e) {
            return null;
        }
    }

    private static int idAlarma(String idt) {
        try {
            return Integer.parseInt(idt);
        } catch (Exception e) {
            return idt.hashCode();
        }
    }
}
