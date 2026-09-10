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
import androidx.recyclerview.widget.RecyclerView;
import com.amcsoftware.sidebar.Entidades.Cliente;
import com.amcsoftware.sidebar.R;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ClientesAdapter extends RecyclerView.Adapter<ClientesAdapter.ClientesHolder> implements View.OnClickListener
         {
    List<Cliente> listaClientes;
    List<Cliente> filteredData;//Buscar datos
    public Context context;

    private View.OnClickListener listener;

    public ClientesAdapter(List<Cliente> listaClientes){
        this.listaClientes = listaClientes;
        filteredData = new ArrayList<>();
        filteredData.addAll(listaClientes);
    }

    @NonNull
    @Override
    public ClientesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.clientes_list,parent,false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        vista .setLayoutParams(layoutParams);

        vista.setOnClickListener(this);//Evento click

        return new ClientesHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientesHolder holder, int position) {
        holder.txtidcl.setText(listaClientes.get(position).getIdcl());
        holder.txtid.setText(listaClientes.get(position).getCedula());
        holder.txtcliente.setText(listaClientes.get(position).getNombre());
        holder.txtcelular.setText(listaClientes.get(position).getCelular());
        holder.txtdir.setText(listaClientes.get(position).getDireccion());
        holder.txtcodeudor.setText(listaClientes.get(position).getCodeudor());
        holder.txtcelcodeudor.setText(listaClientes.get(position).getCelularcode());
        holder.txtobs.setText(listaClientes.get(position).getObservacion());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filtrado(String txtbuscar){
        int longitud = txtbuscar.length();
        if (longitud==0){
            listaClientes.clear();
            listaClientes.addAll(filteredData);
        }else{
            List<Cliente> colletion = listaClientes.stream().
                    filter(i -> i.getNombre().toLowerCase().contains(txtbuscar.toLowerCase()))
                    .collect(Collectors.toList());
            listaClientes.clear();
            listaClientes.addAll(colletion);
        }
        notifyDataSetChanged();
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

    @Override
    public int getItemCount() {
        return listaClientes.size();
    }



    public static class ClientesHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Context context;
        TextView txtid,txtidcl,txtcliente,txtcelular,txtcodeudor,txtcelcodeudor,txtdir,txtobs;
        ImageButton bteditar1, bteliminar1;

        public ClientesHolder(View itemView) {
            super(itemView);
            context =  itemView.getContext();
            txtidcl = itemView.findViewById(R.id.txtidcl1);
            txtid = itemView.findViewById(R.id.txtid1);
            txtcliente = itemView.findViewById(R.id.txtcliente1);
            txtcelular = itemView.findViewById(R.id.txtcelular1);
            txtcodeudor = itemView.findViewById(R.id.txtcodeudor1);
            txtcelcodeudor = itemView.findViewById(R.id.txtcelcodeudor1);
            txtdir = itemView.findViewById(R.id.txtdir1);
            txtobs = itemView.findViewById(R.id.txtobservacion1);
            bteditar1 = itemView.findViewById(R.id.bteditar);
            bteliminar1 = itemView.findViewById(R.id.bteliminar);
            bteditar1.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("idcl", txtidcl.getText().toString());
                bundle.putString("idc", txtid.getText().toString());
                bundle.putString("nombre", txtcliente.getText().toString());
                bundle.putString("celular", txtcelular.getText().toString());
                bundle.putString("dir", txtdir.getText().toString());
                bundle.putString("codeudor", txtcodeudor.getText().toString());
                bundle.putString("celcode", txtcelcodeudor.getText().toString());
                bundle.putString("obs", txtobs.getText().toString());
                //Navigation.findNavController(v).navigate(R.id.actualizarCliente,bundle);//Navegación dinámica
            });
        }
        @Override
        public void onClick(View v) {

        }
    }
}
