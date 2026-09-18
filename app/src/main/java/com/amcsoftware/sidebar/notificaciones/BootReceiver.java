package com.amcsoftware.sidebar.notificaciones;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.amcsoftware.sidebar.Entidades.Agenda;
import com.amcsoftware.sidebar.utils.RecordatorioScheduler;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Android borra todas las alarmas de AlarmManager al reiniciar el equipo, así
 * que sin esto los recordatorios de Agenda dejarían de sonar después de un
 * reinicio. Al recibir BOOT_COMPLETED se vuelve a consultar la lista de
 * tareas al servidor y se reprograma la alarma de cada una.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String URL_LISTA = "https://www.wmcsoftware.net/apps/softpymes/listaAgenda.php";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        // goAsync() extiende la vida del receiver mientras la petición de red
        // (asíncrona) termina; sin esto el sistema podría matar el proceso
        // antes de que Volley reciba la respuesta.
        PendingResult pendingResult = goAsync();
        Context appContext = context.getApplicationContext();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, URL_LISTA, null,
                response -> {
                    try {
                        if (response.optBoolean("success")) {
                            JSONArray arr = response.optJSONArray("agenda");
                            for (int i = 0; i < (arr != null ? arr.length() : 0); i++) {
                                JSONObject o = arr.getJSONObject(i);
                                Agenda tarea = new Agenda(
                                        o.optString("id"),
                                        o.optString("descripcion"),
                                        o.optString("fecha"),
                                        o.optString("prioridad"),
                                        o.optString("hora"),
                                        o.optString("repetir", "Ninguna"),
                                        o.optString("intervalodias")
                                );
                                RecordatorioScheduler.programar(appContext, tarea);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("BootReceiver", "Error reprogramando recordatorios: " + e.getMessage());
                    } finally {
                        pendingResult.finish();
                    }
                },
                error -> {
                    Log.e("BootReceiver", "No se pudo consultar la agenda tras el reinicio: " + error.getMessage());
                    pendingResult.finish();
                }
        );

        Volley.newRequestQueue(appContext).add(request);
    }
}
