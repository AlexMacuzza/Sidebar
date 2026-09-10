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
import com.amcsoftware.sidebar.Entidades.Mercancia;
import com.amcsoftware.sidebar.R;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MercanciaAdapter extends RecyclerView.Adapter<MercanciaAdapter.MercanciaHolder> implements View.OnClickListener {
    List<Mercancia> listaMercancia;
    List<Mercancia> filteredData;
    public Context context;

    private View.OnClickListener listener;

    public MercanciaAdapter(List<Mercancia> listaMercancia) {
        this.listaMercancia = listaMercancia;
        filteredData = new ArrayList<>();
        filteredData.addAll(listaMercancia);
    }

    @NonNull
    @Override
    public MercanciaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.mercancia_list,parent,false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        vista.setLayoutParams(layoutParams);

        vista.setOnClickListener(this);//Evento click

        return new MercanciaHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull MercanciaHolder holder, int position) {
        holder.txtidp.setText(listaMercancia.get(position).getIdp());
        holder.txtdesc.setText(listaMercancia.get(position).getReferencia());
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
                    filter(i -> i.getReferencia().toLowerCase().contains(txtbuscar.toLowerCase()))
                            .collect(Collectors.toList());
            listaMercancia.clear();
            listaMercancia.addAll(colletion);
        }
    notifyDataSetChanged();
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

    public static class  MercanciaHolder  extends RecyclerView.ViewHolder implements View.OnClickListener{
        Context context;
        ImageButton bteditar;
        TextView txtidp,txtdesc,txtprecioc,txtpreciov,txtcantidad;
        public MercanciaHolder(View itemView) {
            super(itemView);
            context =  itemView.getContext();
            txtidp = itemView.findViewById(R.id.txtidmerc1);
            txtdesc = itemView.findViewById(R.id.txtdesc1);
            txtprecioc = itemView.findViewById(R.id.txtprecioc1);
            txtpreciov = itemView.findViewById(R.id.txtpreciov1);
            txtcantidad = itemView.findViewById(R.id.txtcantidad1);
            bteditar    = itemView.findViewById(R.id.bteditar);
            bteditar.setOnClickListener(v->{
                Bundle bundle = new Bundle();
                bundle.putString("idp", txtidp.getText().toString());
                bundle.putString("desc", txtdesc.getText().toString());
                bundle.putString("precioc", txtprecioc.getText().toString());
                bundle.putString("preciov", txtpreciov.getText().toString());
                bundle.putString("cantidad", txtcantidad.getText().toString());
                Navigation.findNavController(v).navigate(R.id.actualizarMercancia,bundle);//Navegación dinámica
            });
        }
        @Override
        public void onClick(View v) {

        }
    }
}
