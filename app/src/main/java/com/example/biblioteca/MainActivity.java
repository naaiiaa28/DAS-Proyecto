package com.example.biblioteca;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MediaAdapter adapter;
    private miBD db;
    private List<MediaItem> lista;
    private TextView tvVacio;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageHelper.aplicarIdioma(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Forzar título de toolbar con string traducido
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.toolbar_principal));
        findViewById(R.id.btnIdioma).setOnClickListener(v -> mostrarDialogoIdioma());
        actualizarBotonIdioma();

        db = new miBD(this);
        NotificationHelper.createChannel(this);

        tvVacio = findViewById(R.id.tvVacio);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        lista = db.obtenerTodos();
        adapter = new MediaAdapter(lista, new MediaAdapter.OnItemClickListener() {
            @Override
            public void onClick(MediaItem item) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("id", item.getId());
                startActivity(intent);
            }
            @Override
            public void onEdit(MediaItem item) {
                Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
                intent.putExtra("item_id", item.getId());
                startActivityForResult(intent, 100);
            }
            @Override
            public void onDelete(MediaItem item) {
                mostrarDialogoEliminar(item);
            }
        });
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnIdioma).setOnClickListener(v -> mostrarDialogoIdioma());
        findViewById(R.id.fabAdd).setOnClickListener(v ->
            startActivityForResult(new Intent(this, AddEditActivity.class), 100));

        configurarFiltros();
        actualizarVista();
    }

    private void mostrarDialogoEliminar(MediaItem item) {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.eliminar_titulo))
            .setMessage(String.format(getString(R.string.eliminar_mensaje), item.getTitulo()))
            .setPositiveButton(getString(R.string.eliminar), (d, w) -> {
                db.eliminar(item.getId());
                lista = db.obtenerTodos();
                adapter.actualizarLista(lista);
                actualizarVista();
            })
            .setNegativeButton(getString(R.string.cancelar), null)
            .show();
    }

    private void configurarFiltros() {
        Spinner spinner = findViewById(R.id.spinnerFiltro);
        String[] opciones = {
            getString(R.string.filtro_todos),
            getString(R.string.estado_visto),
            getString(R.string.estado_en_progreso),
            getString(R.string.estado_pendiente)
        };
        spinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, opciones));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                if (pos == 0) {
                    lista = db.obtenerTodos();
                } else {
                    // Siempre filtramos por los valores originales en español que están en BD
                    String[] estadosDB = {"Visto", "En progreso", "Pendiente"};
                    lista = db.filtrarPorEstado(estadosDB[pos - 1]);
                }
                adapter.actualizarLista(lista);
                actualizarVista();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void actualizarVista() {
        tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(lista.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res == RESULT_OK) {
            lista = db.obtenerTodos();
            adapter.actualizarLista(lista);
            actualizarVista();
        }
    }

    private void mostrarDialogoIdioma() {
        String idiomaActual = LanguageHelper.getIdioma(this);
        String[] opciones = {"Español", "English", "Euskera"};
        int seleccionado;
        switch (idiomaActual) {
            case "en": seleccionado = 1; break;
            case "eu": seleccionado = 2; break;
            default:   seleccionado = 0; break;
        }

        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.cambiar_idioma))
            .setSingleChoiceItems(opciones, seleccionado, (dialog, which) -> {
                String lang;
                switch (which) {
                    case 1:  lang = "en"; break;
                    case 2:  lang = "eu"; break;
                    default: lang = "es"; break;
                }
                LanguageHelper.cambiarIdioma(this, lang);
                dialog.dismiss();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            })
            .show();
        }

    // Añade este método para actualizar el texto del botón
private void actualizarBotonIdioma() {
    com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton btnIdioma =
        findViewById(R.id.btnIdioma);
    String lang = LanguageHelper.getIdioma(this);
    switch (lang) {
        case "en": btnIdioma.setText("EN"); break;
        case "eu": btnIdioma.setText("EU"); break;
        default:   btnIdioma.setText("ES"); break;
        }
    }
}