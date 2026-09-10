package com.amcsoftware.sidebar.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.amcsoftware.sidebar.Entidades.Ventas;
import com.amcsoftware.sidebar.R;
import java.util.ArrayList;

// Importamos nuestra interfaz personalizada para la comunicación.
import com.amcsoftware.sidebar.listener.OnSubtotalChangeListener;

public class VentasAdapter extends RecyclerView.Adapter<VentasAdapter.VentasViewHolder> {

    ArrayList<Ventas> listaventas;
    // DECLARACIÓN DE LA INTERFAZ:
    // Esta variable mantendrá una referencia al objeto (generalmente el Fragment)
    // que ha implementado la interfaz OnSubtotalChangeListener.
    // A través de esta referencia, el adaptador podrá llamar a los métodos definidos en la interfaz.
    private final OnSubtotalChangeListener subtotalChangeListener;

    // CONSTRUCTOR ACTUALIZADO:
    // Ahora, el constructor del adaptador toma un segundo argumento: una instancia de nuestra interfaz.
    // Esto permite que el Fragment (o cualquier clase que implemente la interfaz) se "registre"
    // como el oyente de los cambios del subtotal.
    public VentasAdapter(ArrayList<Ventas> listaventas, OnSubtotalChangeListener listener) {
        this.listaventas = listaventas;
        this.subtotalChangeListener = listener; // Asignamos el listener proporcionado.
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
            if (currentPosition != RecyclerView.NO_POSITION) { // Siempre verifica que la posición sea válida
                // Alerta de confirmación antes de eliminar.
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("¿Está seguro de eliminar este registro?").setTitle("Softpymes");
                builder.setPositiveButton("Si", (dialog, which) -> removeItem(currentPosition));
                builder.setNegativeButton("No", (dialog, which) ->
                        Toast.makeText(v.getContext(),
                                "Operación cancelada",
                                Toast.LENGTH_SHORT).show());
                AlertDialog dialog = builder.create();
                dialog.show(); // Mostrar la Alerta
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaventas.size();
    }

    /*-- Método para eliminar un elemento --*/
    // Este método es crucial para la comunicación con el Fragment.
    public void removeItem(int position) {
        if (position >= 0 && position < listaventas.size()) {
            // PASO 1: Obtener el ítem ANTES de eliminarlo.
            // Necesitamos el subtotal de este ítem para notificar al listener.
            Ventas removedItem = listaventas.get(position);
            double subtotalValue = 0.0;
            try {
                // Intentamos parsear el subtotal de String a double.
                subtotalValue = Double.parseDouble(removedItem.getSubtotal());
            } catch (NumberFormatException e) {
                // Manejo de errores: Si el subtotal no es un número válido.
                // Es importante manejar esta excepción para evitar un crasheo.
                System.err.println("Error al parsear el subtotal para eliminar: " + e.getMessage());

            }

            // PASO 2: Eliminar el ítem de la fuente de datos.
            listaventas.remove(position);
            // PASO 3: Notificar al RecyclerView que un ítem ha sido eliminado.
            notifyItemRemoved(position);

            // PASO 4: Notificar al listener (el Fragment) sobre el cambio en el subtotal.
            // Verificamos que el listener no sea nulo para evitar NullPointerException.
            // Pasamos un valor NEGATIVO porque este subtotal se está RESTANDO del total general.
            if (subtotalChangeListener != null) {
                subtotalChangeListener.onSubtotalChanged(-subtotalValue,position);
            }
        }
    }

    public void clearData() {
        int size = listaventas.size();
        if (size > 0){
            // Cuando se borran todos los datos, calculamos la suma total de los subtotales
            // para enviar un único cambio al listener.
            double totalClearedSubtotal = 0.0;
            for (Ventas item : listaventas) {
                try {
                    totalClearedSubtotal += Double.parseDouble(item.getSubtotal());
                } catch (NumberFormatException e) {
                    System.err.println("Error al parsear el subtotal durante clearData: " + e.getMessage());
                }
            }

            listaventas.clear(); // Limpiamos la lista de ventas.
            notifyItemRangeRemoved(0, size); // Notificamos al adaptador sobre la eliminación de un rango de ítems.

            // Notificamos al listener, pasando la suma total como un valor negativo.
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
        // Si necesitas actualizar el total cuando se AÑADE un item,
        // deberías implementar una lógica similar aquí para llamar a subtotalChangeListener.onSubtotalChanged(addedSubtotal);
    }
}
