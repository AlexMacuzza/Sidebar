package com.amcsoftware.sidebar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amcsoftware.sidebar.Entidades.Cliente;
import com.amcsoftware.sidebar.R;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ClientesVentasAdapter extends RecyclerView.Adapter<ClientesVentasAdapter.ClientesVentasHolder>
        implements View.OnClickListener
{
    List<Cliente> listaClientes;
    List<Cliente> filteredData;//Buscar datos
    public Context context;

    private View.OnClickListener listener;

    public ClientesVentasAdapter(List<Cliente> listaClientes){
        this.listaClientes = listaClientes;
        filteredData = new ArrayList<>();
        filteredData.addAll(listaClientes);
    }

    @NonNull
    @Override
    public ClientesVentasHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.buscar_clientes_list,parent,false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        vista .setLayoutParams(layoutParams);

        vista.setOnClickListener(this);//Evento click

        return new ClientesVentasHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientesVentasHolder holder, int position) {
        holder.txtidcl.setText(listaClientes.get(position).getIdcl());
        holder.txtid.setText(listaClientes.get(position).getCedula());
        holder.txtcliente.setText(listaClientes.get(position).getNombre());
        holder.txtcelular.setText(listaClientes.get(position).getCelular());
        holder.txtdir.setText(listaClientes.get(position).getDireccion());
        holder.txtcodeudor.setText(listaClientes.get(position).getCodeudor());
        holder.txtcelcodeudor.setText(listaClientes.get(position).getCelularcode());
        holder.txtobs.setText(listaClientes.get(position).getObservacion());
        //IMAGEN BLOB
        if (listaClientes.get(position).getFoto()!=null){
            holder.imagen.setImageBitmap(listaClientes.get(position).getFoto());
        }else{
            holder.imagen.setImageResource(R.drawable.clientes);
        }
        //IMAGEN A TRAVES DE URL
        //URL BASE DE TU SERVIDOR
        String BASE_URL = "https://www.wmcsoftware.net/";
        String rutaImagen = listaClientes.get(position).getRfoto();
        if (rutaImagen != null && !rutaImagen.isEmpty()) {
            String urlCompleta = BASE_URL + rutaImagen;
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(urlCompleta)
                    .placeholder(R.drawable.clientes) // mientras carga
                    .error(R.drawable.clientes)       // si falla
                    .into(holder.imagen);
        } else {
            holder.imagen.setImageResource(R.drawable.clientes);
        }


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



    public static class ClientesVentasHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Context context;
        TextView txtid,txtidcl,txtcliente,txtcelular,txtcodeudor,txtcelcodeudor,txtdir,txtobs;
        ImageView imagen;
            public ClientesVentasHolder(View itemView) {
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
            imagen = itemView.findViewById(R.id.idfoto);
        }
        @Override
        public void onClick(View v) {

        }
    }
}
