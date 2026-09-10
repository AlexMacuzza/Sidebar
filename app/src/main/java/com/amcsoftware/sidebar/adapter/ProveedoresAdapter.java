package com.amcsoftware.sidebar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amcsoftware.sidebar.Entidades.Proveedores;
import com.amcsoftware.sidebar.R;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProveedoresAdapter extends RecyclerView.Adapter<ProveedoresAdapter.ProveedoresHolder> implements View.OnClickListener {
    List<Proveedores> listaProveedores;
    List<Proveedores> filteredData;
    public Context context;

    private View.OnClickListener listener;

    public ProveedoresAdapter(List<Proveedores> listaProveedores) {
        this.listaProveedores = listaProveedores;
        filteredData = new ArrayList<>();
        filteredData.addAll(listaProveedores);
    }


    @NonNull
    @Override
    public ProveedoresAdapter.ProveedoresHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.proveedores_list,parent,false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        vista .setLayoutParams(layoutParams);

        vista.setOnClickListener(this);//Evento click

        return new ProveedoresHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ProveedoresAdapter.ProveedoresHolder holder, int position) {
        holder.txtidpr.setText(listaProveedores.get(position).getIdpr());
        holder.txtnit.setText(listaProveedores.get(position).getNit());
        holder.txtrsocial.setText(listaProveedores.get(position).getRsocial());
        holder.txttel.setText(listaProveedores.get(position).getTelefono());
        holder.txtcelular.setText(listaProveedores.get(position).getCelular());
        holder.txtdir.setText(listaProveedores.get(position).getDireccion());
        holder.txtciudad.setText(listaProveedores.get(position).getCiudad());

    }

    @SuppressLint("NotifyDataSetChanged")
    public void filtrado(String txtbuscar){
        int longitud = txtbuscar.length();
        if (longitud==0){
            listaProveedores.clear();
            listaProveedores.addAll(filteredData);
        }else{
            List<Proveedores> collection = listaProveedores.stream().
                    filter(i -> i.getRsocial().toLowerCase().contains(txtbuscar.toLowerCase()))
                    .collect(Collectors.toList());
            listaProveedores.clear();
            listaProveedores.addAll(collection);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listaProveedores.size();
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }
    @Override
    public void onClick(View v) {
        if (listener!=null){
            listener.onClick(v);
        }

    }

    public static class ProveedoresHolder extends RecyclerView.ViewHolder {
        Context context;
        TextView txtidpr,txtnit,txtrsocial,txttel,txtcelular,txtdir,txtciudad;
        public ProveedoresHolder(@NonNull View itemView) {
            super(itemView);
            context =  itemView.getContext();
            txtidpr = itemView.findViewById(R.id.txtidpr1);
            txtnit = itemView.findViewById(R.id.txtnit1);
            txtrsocial = itemView.findViewById(R.id.txtrsocial1);
            txttel = itemView.findViewById(R.id.txttel1);
            txtcelular = itemView.findViewById(R.id.txtcel1);
            txtdir = itemView.findViewById(R.id.txtdir1);
            txtciudad = itemView.findViewById(R.id.txtciudad1);
           }
    }
}
