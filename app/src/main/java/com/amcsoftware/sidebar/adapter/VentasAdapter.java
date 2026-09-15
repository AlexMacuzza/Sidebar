package com.amcsoftware.sidebar.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amcsoftware.sidebar.Entidades.Ventas;
import com.amcsoftware.sidebar.R;
import com.amcsoftware.sidebar.utils.AppUtils;
import java.util.ArrayList;

import com.amcsoftware.sidebar.listener.OnSubtotalChangeListener;

public class VentasAdapter extends RecyclerView.Adapter<VentasAdapter.VentasViewHolder> {

    ArrayList<Ventas> listaventas;
    // Listener (el Fragment) que recibe los cambios de subtotal
    private final OnSubtotalChangeListener subtotalChangeListener;

    public VentasAdapter(ArrayList<Ventas> listaventas, OnSubtotalChangeListener listener) {
        this.listaventas = listaventas;
        this.subtotalChangeListener = listener;
    }

    @NonNull
    @Override
    public VentasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.mercancia_ventas_list, parent, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        vista.setLayoutParams(layoutParams);
        return new VentasViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull VentasViewHolder holder, int position) {
        Ventas datos = listaventas.get(position);
        holder.lblidp.setText(datos.getIdp());
        holder.lbldesc.setText(datos.getDesc());
        holder.lblprecio.setText(datos.getPreciov());
        holder.lblcant.setText(datos.getCantidad());
        holder.lblsubt.setText(datos.getSubtotal());
        holder.btborrar.setOnClickListener(v->{
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                AppUtils.alertConfirmar(v.getContext(), "Softpymes",
                        "¿Está seguro de eliminar este registro?", "Sí", "No",
                        () -> removeItem(currentPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaventas.size();
    }

    // Elimina un ítem y notifica al listener el subtotal restado (valor negativo)
    public void removeItem(int position) {
        if (position >= 0 && position < listaventas.size()) {
            Ventas removedItem = listaventas.get(position);
            double subtotalValue = 0.0;
            try {
                subtotalValue = Double.parseDouble(removedItem.getSubtotal());
            } catch (NumberFormatException e) {
                System.err.println("Error al parsear el subtotal para eliminar: " + e.getMessage());
            }

            listaventas.remove(position);
            notifyItemRemoved(position);

            if (subtotalChangeListener != null) {
                subtotalChangeListener.onSubtotalChanged(-subtotalValue,position);
            }
        }
    }

    public void clearData() {
        int size = listaventas.size();
        if (size > 0){
            // Suma todos los subtotales para notificar un único cambio al listener
            double totalClearedSubtotal = 0.0;
            for (Ventas item : listaventas) {
                try {
                    totalClearedSubtotal += Double.parseDouble(item.getSubtotal());
                } catch (NumberFormatException e) {
                    System.err.println("Error al parsear el subtotal durante clearData: " + e.getMessage());
                }
            }

            listaventas.clear();
            notifyItemRangeRemoved(0, size);

            if (subtotalChangeListener != null) {
                subtotalChangeListener.onSubtotalChanged(-totalClearedSubtotal,0);
            }
        }
    }

    public static class VentasViewHolder extends RecyclerView.ViewHolder {
        TextView lblidp,lbldesc,lblprecio,lblcant,lblsubt;
        ImageButton btborrar;

        public VentasViewHolder(@NonNull View itemView) {
            super(itemView);
            lblidp     = itemView.findViewById(R.id.lblidp);
            lbldesc    = itemView.findViewById(R.id.lbldesc);
            lblprecio  = itemView.findViewById(R.id.lblprecio);
            lblcant    = itemView.findViewById(R.id.lblcant);
            lblsubt    = itemView.findViewById(R.id.lblsubt);
            btborrar   = itemView.findViewById(R.id.bteliminar);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addDatos(Ventas datos) {
        listaventas.add(datos);
        notifyDataSetChanged();
    }
}
