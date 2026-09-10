package com.amcsoftware.sidebar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amcsoftware.sidebar.Entidades.Mercancia;
import com.amcsoftware.sidebar.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MercanciasVentasAdapter extends RecyclerView.Adapter<MercanciasVentasAdapter.MercanciasVentasAdapterHolder> implements View.OnClickListener {
    double precio,subt;
    int cant;
    List<Mercancia> listaMercancia;
    List<Mercancia> filteredData;
    public Context context;

    private View.OnClickListener listener;

    public MercanciasVentasAdapter(List<Mercancia> listaMercancia) {
        this.listaMercancia = listaMercancia;
        filteredData = new ArrayList<>();
        filteredData.addAll(listaMercancia);
    }

    @NonNull
    @Override
    public MercanciasVentasAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.buscar_mercancia_list,parent,false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        vista .setLayoutParams(layoutParams);

        vista.setOnClickListener(this);//Evento click

        return new MercanciasVentasAdapterHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull MercanciasVentasAdapterHolder holder, int position) {
        Mercancia item = listaMercancia.get(position);
        holder.txtidp.setText(listaMercancia.get(position).getIdp());
        holder.txtdesc.setText(listaMercancia.get(position).getDescripcion());
        holder.txtprecioc.setText(listaMercancia.get(position).getPrecioc());
        holder.txtpreciov.setText(listaMercancia.get(position).getPreciov());
        holder.txtcantidad.setText(listaMercancia.get(position).getCantidad());
        holder.txtsubtotal.setText(listaMercancia.get(position).getSubtotal());
        holder.checkBoxItem.setChecked(listaMercancia.get(position).isSelected());

        // Luego, establecer el listener
        holder.checkBoxItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Actualiza el estado 'isSelected' en el objeto de datos cuando el checkbox cambia
            item.setSelected(isChecked);
        });

        //Calcula el subtotal al escribir la cantidad
        holder.txtcantidad.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    precio = Double.parseDouble(holder.txtpreciov.getText().toString());
                    cant   = Integer.parseInt(holder.txtcantidad.getText().toString());
                    subt   = cant * precio;
                    //Asignar los valores que se han digitado dinámicamente
                    item.setSubtotal(String.format(Locale.US,"%.0f",subt));
                    item.setCantidad(String.valueOf(cant));
                } catch (NumberFormatException e) {
                    subt = 0;
                }
            }
            return false;
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filtrado(String txtbuscar){
        int longitud = txtbuscar.length();
        if (longitud==0){
            listaMercancia.clear();
            listaMercancia.addAll(filteredData);
        }else{
            List<Mercancia> colletion = listaMercancia.stream().
                    filter(i -> i.getDescripcion().toLowerCase().contains(txtbuscar.toLowerCase()))
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
    //Obtener filas seleccionadas del checkbox
    public List<Mercancia> getSelectedDataOnly() {
        List<Mercancia> selectedItems = new ArrayList<>();
        for (Mercancia item : listaMercancia) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }

    public static class  MercanciasVentasAdapterHolder  extends RecyclerView.ViewHolder implements View.OnClickListener{
        Context context;
        CheckBox checkBoxItem;
        TextView txtidp,txtdesc,txtprecioc,txtpreciov;
        EditText txtcantidad,txtsubtotal;
        public MercanciasVentasAdapterHolder(View itemView) {
            super(itemView);
            context =  itemView.getContext();
            txtidp = itemView.findViewById(R.id.txtidmerc1);
            txtdesc = itemView.findViewById(R.id.txtdesc1);
            txtprecioc = itemView.findViewById(R.id.txtprecioc1);
            txtpreciov = itemView.findViewById(R.id.txtpreciov1);
            txtcantidad = itemView.findViewById(R.id.txtcantidad1);
            txtsubtotal = itemView.findViewById(R.id.txtsubt1);
            checkBoxItem = itemView.findViewById(R.id.checkBoxItem);
           }
        @Override
        public void onClick(View v) {

        }
    }
}
