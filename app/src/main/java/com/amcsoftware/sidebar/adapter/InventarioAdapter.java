package com.amcsoftware.sidebar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amcsoftware.sidebar.Entidades.Mercancia;
import com.amcsoftware.sidebar.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class InventarioAdapter extends RecyclerView.Adapter<InventarioAdapter.InventarioAdapterHolder> implements View.OnClickListener {
    List<Mercancia> listaMercancia;
    List<Mercancia> filteredData;
    public Context context;

    private View.OnClickListener listener;

    public InventarioAdapter(List<Mercancia> listaMercancia) {
        this.listaMercancia = listaMercancia;
        filteredData = new ArrayList<>();
        filteredData.addAll(listaMercancia);
    }

    @NonNull
    @Override
    public InventarioAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventario_list,parent,false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        vista .setLayoutParams(layoutParams);

        vista.setOnClickListener(this);//Evento click

        return new InventarioAdapterHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull InventarioAdapterHolder holder, int position) {
        holder.txtidp.setText(listaMercancia.get(position).getIdp());
        holder.txtdesc.setText(listaMercancia.get(position).getDescripcion());
        holder.txtprecioc.setText(listaMercancia.get(position).getPrecioc());
        holder.txtpreciov.setText(listaMercancia.get(position).getPreciov());
        holder.txtcantidad.setText(listaMercancia.get(position).getCantidad());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filtrado(String txtbuscar){
        int longitud = txtbuscar.length();
        if (longitud==0){
            listaMercancia.clear();
            listaMercancia.addAll(filteredData);
        }else{
            List<Mercancia> colletion = listaMercancia.stream().
                    filter(i -> i.getDescripcion().toLowerCase(Locale.ROOT).contains(txtbuscar.toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
            listaMercancia.clear();
            listaMercancia.addAll(colletion);
        }
        notifyDataSetChanged();
    }

    public Mercancia getItemAtPosition(int position) {
        if (position >= 0 && position < listaMercancia.size()) {
            return listaMercancia.get(position);
        }
        return null;
    }


    @Override
    public int getItemCount() {
        return listaMercancia.size();
    }


    public void setOnClickListener(View.OnClickListener listener){

        this.listener = listener;

    }

    @Override
    public void onClick(View vista) {
        if (listener!=null){
            listener.onClick(vista);
        }

    }

    public static class  InventarioAdapterHolder  extends RecyclerView.ViewHolder implements View.OnClickListener{
        Context context;
        TextView txtidp,txtdesc,txtprecioc,txtpreciov,txtcantidad;
        public InventarioAdapterHolder(View itemView) {
            super(itemView);
            context =  itemView.getContext();
            txtidp = itemView.findViewById(R.id.txtidmerc1);
            txtdesc = itemView.findViewById(R.id.txtdesc1);
            txtprecioc = itemView.findViewById(R.id.txtprecioc1);
            txtpreciov = itemView.findViewById(R.id.txtpreciov1);
            txtcantidad = itemView.findViewById(R.id.txtcantidad1);
        }
        @Override
        public void onClick(View v) {

        }
    }
}
