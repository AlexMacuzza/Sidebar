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

import com.amcsoftware.sidebar.Entidades.Vendedor;
import com.amcsoftware.sidebar.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VendedoresAdapter extends RecyclerView.Adapter<VendedoresAdapter.VendedoresHolder> implements View.OnClickListener {
    List<Vendedor> listaVendedores;
    List<Vendedor> filteredData;
    public Context context;

    private View.OnClickListener listener;

    public VendedoresAdapter(List<Vendedor> listaVendedores) {
        this.listaVendedores = listaVendedores;
        filteredData = new ArrayList<>();
        filteredData.addAll(listaVendedores);
    }


    @NonNull
    @Override
    public VendedoresAdapter.VendedoresHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.vendedores_list,parent,false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        vista.setLayoutParams(layoutParams);

        vista.setOnClickListener(this);//Evento click

        return new VendedoresHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull VendedoresAdapter.VendedoresHolder holder, int position) {
        holder.txtidvd.setText(listaVendedores.get(position).getIdvd());
        holder.txtcedula.setText(listaVendedores.get(position).getCedula());
        holder.txtnombre.setText(listaVendedores.get(position).getNombre());
        holder.txtcelular.setText(listaVendedores.get(position).getCelular());
        holder.txtdir.setText(listaVendedores.get(position).getDireccion());

    }

    @SuppressLint("NotifyDataSetChanged")
    public void filtrado(String txtbuscar){
        int longitud = txtbuscar.length();
        if (longitud==0){
            listaVendedores.clear();
            listaVendedores.addAll(filteredData);
        }else{
            List<Vendedor> collection = listaVendedores.stream().
                    filter(i -> i.getNombre().toLowerCase().contains(txtbuscar.toLowerCase()))
                    .collect(Collectors.toList());
            listaVendedores.clear();
            listaVendedores.addAll(collection);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listaVendedores.size();
    }

    @Override
    public void onClick(View v) {
        if (listener!=null){
            listener.onClick(v);
        }

    }

    public static class VendedoresHolder extends RecyclerView.ViewHolder {
        Context context;
        TextView txtidvd,txtcedula,txtnombre,txtcelular,txtdir;
        ImageButton bteditar;
        public VendedoresHolder(@NonNull View itemView) {
            super(itemView);
            context =  itemView.getContext();
            txtidvd = itemView.findViewById(R.id.txtidvd1);
            txtcedula = itemView.findViewById(R.id.txtcedvd1);
            txtnombre = itemView.findViewById(R.id.txtvendedor1);
            txtcelular = itemView.findViewById(R.id.txtcelvd1);
            txtdir = itemView.findViewById(R.id.txtdirvd1);
            bteditar = itemView.findViewById(R.id.bteditarvd);
            bteditar.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("idvd", txtidvd.getText().toString());
                bundle.putString("cedula", txtcedula.getText().toString());
                bundle.putString("nombre", txtnombre.getText().toString());
                bundle.putString("celular", txtcelular.getText().toString());
                bundle.putString("dir", txtdir.getText().toString());
                Navigation.findNavController(v).navigate(R.id.actualizarVendedor,bundle);//Navegación dinámica
            });
        }
    }
}
