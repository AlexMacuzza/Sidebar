package com.amcsoftware.sidebar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.amcsoftware.sidebar.Entidades.Cobrador;
import com.amcsoftware.sidebar.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CobradoresAdapter extends RecyclerView.Adapter<CobradoresAdapter.CobradoresHolder> implements View.OnClickListener {
    List<Cobrador> listaCobradores;
    List<Cobrador> filteredData;
    public Context context;

    private View.OnClickListener listener;

    public CobradoresAdapter(List<Cobrador> listaCobradores) {
        this.listaCobradores = listaCobradores;
        filteredData = new ArrayList<>();
        filteredData.addAll(listaCobradores);
    }


    @NonNull
    @Override
    public CobradoresAdapter.CobradoresHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.cobradores_list,parent,false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        vista .setLayoutParams(layoutParams);

        vista.setOnClickListener(this);//Evento click

        return new CobradoresHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull CobradoresAdapter.CobradoresHolder holder, int position) {
        holder.txtidcob.setText(listaCobradores.get(position).getIdcob());
        holder.txtcedula.setText(listaCobradores.get(position).getCedula());
        holder.txtnombre.setText(listaCobradores.get(position).getNombre());
        holder.txtcelular.setText(listaCobradores.get(position).getCelular());
        holder.txtdir.setText(listaCobradores.get(position).getDireccion());

    }

    @SuppressLint("NotifyDataSetChanged")
    public void filtrado(String txtbuscar){
        int longitud = txtbuscar.length();
        if (longitud==0){
            listaCobradores.clear();
            listaCobradores.addAll(filteredData);
        }else{
            List<Cobrador> collection = listaCobradores.stream().
                    filter(i -> i.getNombre().toLowerCase(Locale.ROOT).contains(txtbuscar.toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
            listaCobradores.clear();
            listaCobradores.addAll(collection);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listaCobradores.size();
    }

    @Override
    public void onClick(View v) {
        if (listener!=null){
            listener.onClick(v);
        }

    }

    public static class CobradoresHolder extends RecyclerView.ViewHolder {
        Context context;
        TextView txtidcob,txtcedula,txtnombre,txtcelular,txtdir;
        ImageButton bteditar;
        public CobradoresHolder(@NonNull View itemView) {
            super(itemView);
            context =  itemView.getContext();
            txtidcob = itemView.findViewById(R.id.txtidcob1);
            txtcedula = itemView.findViewById(R.id.txtcedcob1);
            txtnombre = itemView.findViewById(R.id.txtcobrador1);
            txtcelular = itemView.findViewById(R.id.txtcelcob1);
            txtdir = itemView.findViewById(R.id.txtdircob1);
            bteditar = itemView.findViewById(R.id.bteditarcob);
            bteditar.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("idcob", txtidcob.getText().toString());
                bundle.putString("idcedcob", txtcedula.getText().toString());
                bundle.putString("nombre", txtnombre.getText().toString());
                bundle.putString("celular", txtcelular.getText().toString());
                bundle.putString("dir", txtdir.getText().toString());
                Navigation.findNavController(v).navigate(R.id.actualizarCobrador,bundle);//Navegación dinámica
            });
        }
    }
}
