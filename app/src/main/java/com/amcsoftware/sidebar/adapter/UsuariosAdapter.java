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
import com.amcsoftware.sidebar.Entidades.Usuario;
import com.amcsoftware.sidebar.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.UsuariosHolder> implements View.OnClickListener {
    List<Usuario> listaUsuarios;
    List<Usuario> filteredData;
    public Context context;
    private View.OnClickListener listener;

    public UsuariosAdapter(List<Usuario> listaUsuarios){
        this.listaUsuarios = listaUsuarios;
        filteredData = new ArrayList<>();
        filteredData.addAll(listaUsuarios);
    }

    @NonNull
    @Override
    public UsuariosAdapter.UsuariosHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.usuarios_list,parent,false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        vista .setLayoutParams(layoutParams);

        vista.setOnClickListener(this);//Evento click

        return new UsuariosHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuariosAdapter.UsuariosHolder holder, int position) {
        holder.txtiduser.setText(listaUsuarios.get(position).getIdu());
        holder.txtuser.setText(listaUsuarios.get(position).getNombre());
        holder.txtcodperfil.setText(listaUsuarios.get(position).getCodperfil());
        holder.txtperfil.setText(listaUsuarios.get(position).getPerfil());
        holder.txtpermisos.setText(listaUsuarios.get(position).getPermisos());
        holder.txtsucursal.setText(listaUsuarios.get(position).getSucursal());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filtrado(String txtbuscar){
        int longitud = txtbuscar.length();
        if (longitud==0){
            listaUsuarios.clear();
            listaUsuarios.addAll(filteredData);
        }else{
            List<Usuario> collection = listaUsuarios.stream().
                    filter(i -> i.getNombre().toLowerCase(Locale.ROOT).contains(txtbuscar.toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
            listaUsuarios.clear();
            listaUsuarios.addAll(collection);
        }
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    @Override
    public void onClick(View v) {
        if (listener!=null){
            listener.onClick(v);
        }

    }

    public static class UsuariosHolder extends RecyclerView.ViewHolder {
        Context context;
        TextView txtiduser,txtuser,txtcodperfil,txtperfil,txtpermisos,txtsucursal;
        ImageButton bteditar;
        public UsuariosHolder(@NonNull View itemView) {
            super(itemView);
            context      =  itemView.getContext();
            txtiduser    = itemView.findViewById(R.id.txtiduser1);
            txtuser      = itemView.findViewById(R.id.txtuser1);
            txtcodperfil = itemView.findViewById(R.id.txtcodperfil1);
            txtperfil    = itemView.findViewById(R.id.txtperfil1);
            txtpermisos  = itemView.findViewById(R.id.txtpermisos1);
            txtsucursal  = itemView.findViewById(R.id.txtsuc1);
            bteditar     = itemView.findViewById(R.id.bteditaruser);
            bteditar.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("idu",txtiduser.getText().toString());
                bundle.putString("usuario",txtuser.getText().toString());
                bundle.putString("codperfil",txtcodperfil.getText().toString());
                bundle.putString("perfil",txtperfil.getText().toString());
                bundle.putString("permisos",txtpermisos.getText().toString());
                bundle.putString("sucursal",txtsucursal.getText().toString());
                Navigation.findNavController(v).navigate(R.id.actualizarUsuario,bundle);//Navegación dinámica
            });
        }
    }
}
