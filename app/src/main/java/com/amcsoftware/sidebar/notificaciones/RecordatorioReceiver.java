package com.amcsoftware.sidebar.notificaciones;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.amcsoftware.sidebar.Entidades.Agenda;
import com.amcsoftware.sidebar.PrincipalActivity;
import com.amcsoftware.sidebar.R;
import com.amcsoftware.sidebar.utils.RecordatorioScheduler;

import java.util.Date;

/**
 * Recibe el disparo de una alarma de Agenda (llega del sistema operativo, no
 * de la app) y muestra la notificación. Si la tarea es recurrente, calcula la
 * siguiente ocurrencia y vuelve a programarla antes de terminar.
 */
public class RecordatorioReceiver extends BroadcastReceiver {

    public static final String CANAL_ID = "recordatorios_agenda";

    @Override
    public void onReceive(Context context, Intent intent) {
        String idt          = intent.getStringExtra(RecordatorioScheduler.EXTRA_ID);
        String descripcion  = intent.getStringExtra(RecordatorioScheduler.EXTRA_DESCRIPCION);
        String repetir      = intent.getStringExtra(RecordatorioScheduler.EXTRA_REPETIR);
        String intervalodias = intent.getStringExtra(RecordatorioScheduler.EXTRA_INTERVALO);

        mostrarNotificacion(context, idt, descripcion);

        // Recordatorio recurrente: se reprograma la siguiente ocurrencia
        // porque una alarma exacta del sistema es de un solo disparo.
        if (repetir != null && !"Ninguna".equals(repetir)) {
            Date siguiente = RecordatorioScheduler.siguienteOcurrencia(new Date(), repetir, intervalodias);
            if (siguiente != null) {
                Agenda tareaSiguiente = new Agenda(idt, descripcion, "", "", "", repetir, intervalodias);
                // calcularDisparo necesita fecha+hora ya resueltas: se arma directo con la fecha calculada
                RecordatorioScheduler.programarEnFecha(context, tareaSiguiente, siguiente);
            }
        }
    }

    private void mostrarNotificacion(Context context, String idt, String descripcion) {
        crearCanalSiNoExiste(context);

        Intent abrirApp = new Intent(context, PrincipalActivity.class);
        abrirApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, idt == null ? 0 : idt.hashCode(), abrirApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CANAL_ID)
                .setSmallIcon(R.drawable.ventas)
                .setContentTitle("Recordatorio de agenda")
                .setContentText(descripcion)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(descripcion))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        int notificationId;
        try {
            notificationId = Integer.parseInt(idt);
        } catch (Exception e) {
            notificationId = idt == null ? 0 : idt.hashCode();
        }
        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    private void crearCanalSiNoExiste(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null || manager.getNotificationChannel(CANAL_ID) != null) return;

        NotificationChannel canal = new NotificationChannel(
                CANAL_ID, "Recordatorios de agenda", NotificationManager.IMPORTANCE_HIGH);
        canal.setDescription("Avisos de tareas y pagos programados en la Agenda.");
        manager.createNotificationChannel(canal);
    }
}
