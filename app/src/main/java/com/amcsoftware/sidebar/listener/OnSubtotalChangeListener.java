package com.amcsoftware.sidebar.listener;
public interface OnSubtotalChangeListener {
    /**
     * Se llama cuando el subtotal en la lista cambia.
     *
     * @param subtotalChange La cantidad en la que el total necesita ser ajustado.
     * Use un valor negativo cuando un elemento es eliminado.
     */
    void onSubtotalChanged(double subtotalChange, int position);
}