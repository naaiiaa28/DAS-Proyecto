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
//la primera pantalla que aparece al abrir la app. En un principio donde se muestran todas las pelis/series de la base de datos
    private RecyclerView recyclerView;
    private MediaAdapter adapter;
    private miBD db;
    private List<MediaItem> lista;
    private TextView tvVacio;

    //lo primero que se ejecuta para establecer idioma
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
        // Conecta el botón de idioma y actualiza su texto (ES/EN/EU)
        // según el idioma guardado en preferencias
        findViewById(R.id.btnIdioma).setOnClickListener(v -> mostrarDialogoIdioma());
        actualizarBotonIdioma();

        db = new miBD(this); //conexiooon bd
        NotificationHelper.createChannel(this);

        tvVacio = findViewById(R.id.tvVacio);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        lista = db.obtenerTodos(); // Carga todos los items de la BD ordenados por fecha
        adapter = new MediaAdapter(lista, new MediaAdapter.OnItemClickListener() {
            // Al pulsar la tarjeta de peli/serie abre DetailActivity pasándole el id del item
            @Override
            public void onClick(MediaItem item) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("id", item.getId());
                startActivity(intent);
            }
            // Al pulsar el botón editar abre AddEditActivity pa editar
            @Override
            public void onEdit(MediaItem item) {
                Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
                intent.putExtra("item_id", item.getId());
                startActivityForResult(intent, 100);
            }
            // Al pulsar el botón eliminar muestra el diálogo de confirmación
            @Override
            public void onDelete(MediaItem item) {
                mostrarDialogoEliminar(item);
            }
        });
        recyclerView.setAdapter(adapter); // Conecta el adapter al RecyclerView

        //boton del mas pa añadir una nueva peli/serie
        findViewById(R.id.fabAdd).setOnClickListener(v ->
            startActivityForResult(new Intent(this, AddEditActivity.class), 100));

        configurarFiltros(); //si quieres filtrar por un tipo de progreso
        actualizarVista(); //actualiza las pelis que se ven en base al filtro o si hay o no pelis
    }

    //uan vez aceptas borrar el item se borra de la bd y actualiza la lista y la vista
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
        // Las opciones se construyen con getString para que estén traducidas
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
                    lista = db.obtenerTodos(); //como en la bd se guarda en orden jugamos con las posiciones
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
        // Si la lista está vacía muestra el texto y oculta el RecyclerView
        // Si tiene items oculta el texto y muestra el RecyclerView
        tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(lista.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        // recargamos la lista para reflejar los cambios de añadir o editar
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
            // Determina cuál opción aparece marcada según el idioma actual
            case "en": seleccionado = 1; break;
            case "eu": seleccionado = 2; break;
            default:   seleccionado = 0; break;
        }
        // setSingleChoiceItems crea un diálogo con botones de radio solo se puede elegir uno no tiene sentido poder marcxar mas de un idioma
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
    // Obtiene la referencia al botón de idioma casteándolo a su tipo real
    com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton btnIdioma =
        findViewById(R.id.btnIdioma);
    String lang = LanguageHelper.getIdioma(this);
    switch (lang) {
        // Actualiza el texto del botón para que muestre el código del idioma activo
        case "en": btnIdioma.setText("EN"); break;
        case "eu": btnIdioma.setText("EU"); break;
        default:   btnIdioma.setText("ES"); break;
        }
    }
}