package com.amcsoftware.sidebar;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.amcsoftware.sidebar.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        //Icono del action Bar
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.mipmap.image_android);
        binding.appBarMain.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show());
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_agenda, R.id.nav_inicio1, R.id.nav_buscar_lista_clientes, R.id.nav_cobradores,
                R.id.nav_consulta_compras, R.id.nav_config, R.id.nav_mercancia, R.id.nav_pagos,
                R.id.nav_proveedores, R.id.nav_vendedores, R.id.nav_salir, R.id.nav_usuarios,
                R.id.nav_ventas, R.id.nav_inventario, R.id.nav_consulta_ventas,
                R.id.nav_consulta_lista_clientes_imagen)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navigationView.setItemIconTintList(null);//muestra los iconos del menú en sus colores originales
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Controlar botón atrás
        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                // Alerta de confirmación antes de cerrar la sesión.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("¿Quiere cerrar la sesión?").setTitle("Softpymes");
                builder.setPositiveButton("Si", (dialog, which) -> logout());//Cerrar todas las activitys
                builder.setNegativeButton("No", (dialog, which) ->
                        Toast.makeText(MainActivity.this,
                                "Operación cancelada",
                                Toast.LENGTH_SHORT).show());
                AlertDialog dialog = builder.create();
                dialog.show(); // Mostrar la Alerta
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    //MENU OVERFLOW
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.cerrarSesion){
            logout();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart(){
        super.onStart();
        loadData();
    }

    //Cargar datos de la sesión de usuarios
    private void loadData() {
        final View vistaHeader = binding.navView.getHeaderView(0);
        final TextView tvNombre = vistaHeader.findViewById(R.id.tvUsuario),
                tvPerfil = vistaHeader.findViewById(R.id.tvPerfil);
        final String perfil,roles;
        SharedPreferences sp = getSharedPreferences("sesion", MODE_PRIVATE);
        perfil = sp.getString("perfil", "General");
        roles  = sp.getString("perfil", "General") + "-" + sp.getString("permisos", "General");
        tvNombre.setText(sp.getString("usuario", "INVITADO"));
        tvPerfil.setText(roles);

        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();

        if (perfil.equals("Cobrador")) {
            menu.findItem(R.id.nav_agenda).setVisible(false);
            menu.findItem(R.id.nav_consulta_ventas).setVisible(false);
            menu.findItem(R.id.nav_consulta_compras).setVisible(false);
            menu.findItem(R.id.nav_cobradores).setVisible(false);
            menu.findItem(R.id.nav_inventario).setVisible(false);
            menu.findItem(R.id.nav_mercancia).setVisible(false);
            menu.findItem(R.id.nav_proveedores).setVisible(false);
            menu.findItem(R.id.nav_salir).setVisible(false);
            menu.findItem(R.id.nav_usuarios).setVisible(false);
            menu.findItem(R.id.nav_vendedores).setVisible(false);
            menu.findItem(R.id.nav_ventas).setVisible(false);
        }else if (perfil.equals("Vendedor")){
            menu.findItem(R.id.nav_agenda).setVisible(false);
            menu.findItem(R.id.nav_consulta_ventas).setVisible(false);
            menu.findItem(R.id.nav_cobradores).setVisible(false);
            menu.findItem(R.id.nav_consulta_compras).setVisible(false);
            menu.findItem(R.id.nav_inventario).setVisible(false);
            menu.findItem(R.id.nav_mercancia).setVisible(false);
            menu.findItem(R.id.nav_pagos).setVisible(false);
            menu.findItem(R.id.nav_proveedores).setVisible(false);
            menu.findItem(R.id.nav_salir).setVisible(false);
            menu.findItem(R.id.nav_usuarios).setVisible(false);
            menu.findItem(R.id.nav_vendedores).setVisible(false);
        }
    }

    //Cerrar Sesión
    private void logout() {
        SharedPreferences preferences = getSharedPreferences("sesion", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("sesion");
        editor.apply();
        this.finish();
        this.overridePendingTransition(R.anim.left_in, R.anim.left_out);
    }



}