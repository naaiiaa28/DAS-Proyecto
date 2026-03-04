package com.example.biblioteca;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.net.Uri;
import android.widget.ImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.content.Context;

public class AddEditActivity extends AppCompatActivity {

    private miBD db;
    private MediaItem itemEditar = null;
    private EditText etTitulo, etGenero, etResumen, etComentario;
    private EditText etTemporadas, etTemporadaActual, etCapituloActual;
    private RatingBar ratingBar;
    private Spinner spinnerTipo, spinnerEstado;
    private LinearLayout layoutSerie;
    private String rutaImagenSeleccionada = null;
    private static final int REQUEST_IMAGEN = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        db = new miBD(this);

        etTitulo         = findViewById(R.id.etTitulo);
        etGenero         = findViewById(R.id.etGenero);
        etResumen        = findViewById(R.id.etResumen);
        etComentario     = findViewById(R.id.etComentario);
        etTemporadas     = findViewById(R.id.etTemporadas);
        etTemporadaActual= findViewById(R.id.etTemporadaActual);
        etCapituloActual = findViewById(R.id.etCapituloActual);
        ratingBar        = findViewById(R.id.ratingBar);
        spinnerTipo      = findViewById(R.id.spinnerTipo);
        spinnerEstado    = findViewById(R.id.spinnerEstado);
        layoutSerie      = findViewById(R.id.layoutSerie);

        // Configurar spinners
        spinnerTipo.setAdapter(new ArrayAdapter<>(this,R.layout.spinner_item,
        new String[]{getString(R.string.tipo_pelicula), getString(R.string.tipo_serie)}));

        spinnerEstado.setAdapter(new ArrayAdapter<>(this,R.layout.spinner_item,
        new String[]{getString(R.string.estado_visto), getString(R.string.estado_en_progreso), getString(R.string.estado_pendiente)}));

        int id = getIntent().getIntExtra("item_id", -1);
        if (id != -1) {
            itemEditar = db.obtenerPorId(id);
            rellenarFormulario(itemEditar);
            setTitle("Editar");
        } else {
            setTitle("Añadir");
        }

        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long i) {
                layoutSerie.setVisibility(pos == 1 ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView ivPreview = findViewById(R.id.ivPreviewImagen);
        findViewById(R.id.btnSeleccionarImagen).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGEN);
        });
    }

    private void rellenarFormulario(MediaItem item) {
        etTitulo.setText(item.getTitulo());
        etGenero.setText(item.getGenero());
        etResumen.setText(item.getResumen());
        etComentario.setText(item.getComentario());
        ratingBar.setRating(item.getPuntuacion());

        if (item.getRutaImagen() != null && !item.getRutaImagen().isEmpty()) {
            ImageView ivPreview = findViewById(R.id.ivPreviewImagen);
            ivPreview.setImageURI(Uri.parse(item.getRutaImagen()));
            ivPreview.setVisibility(View.VISIBLE);
            rutaImagenSeleccionada = item.getRutaImagen();
        }

        // Seleccionar tipo en spinner por posición
        String[] tiposES = {"Película", "Serie"};
        for (int i = 0; i < tiposES.length; i++) {
            if (tiposES[i].equals(item.getTipo())) spinnerTipo.setSelection(i);
        }

        // Seleccionar estado en spinner por posición
        String[] estadosES = {"Visto", "En progreso", "Pendiente"};
        for (int i = 0; i < estadosES.length; i++) {
            if (estadosES[i].equals(item.getEstado())) spinnerEstado.setSelection(i);
        }
        
        if ("Serie".equals(item.getTipo())) {
            layoutSerie.setVisibility(View.VISIBLE);
            etTemporadas.setText(String.valueOf(item.getTemporadasTotales()));
            etTemporadaActual.setText(String.valueOf(item.getTemporadaActual()));
            etCapituloActual.setText(String.valueOf(item.getCapituloActual()));
        }
    }

    private void guardar() {
        String titulo = etTitulo.getText().toString().trim();
        if (titulo.isEmpty()) {
            etTitulo.setError(getString(R.string.titulo_obligatorio));
            return;
        }

        MediaItem item = itemEditar != null ? itemEditar : new MediaItem();
        item.setTitulo(titulo);

        // Guardar siempre en español independientemente del idioma
        int posTipo = spinnerTipo.getSelectedItemPosition();
        item.setTipo(posTipo == 0 ? "Película" : "Serie");

        int posEstado = spinnerEstado.getSelectedItemPosition();
        String[] estadosES = {"Visto", "En progreso", "Pendiente"};
        item.setEstado(estadosES[posEstado]);

        item.setGenero(etGenero.getText().toString());
        item.setResumen(etResumen.getText().toString());
        item.setComentario(etComentario.getText().toString());
        item.setPuntuacion(ratingBar.getRating());
        item.setFechaAdicion(new SimpleDateFormat("yyyy-MM-dd HH:mm",
                Locale.getDefault()).format(new Date()));
        if (rutaImagenSeleccionada != null) {
            item.setRutaImagen(rutaImagenSeleccionada);
        }

        if ("Serie".equals(item.getTipo())) {
            item.setTemporadasTotales(parseIntSafe(etTemporadas.getText().toString()));
            item.setTemporadaActual(parseIntSafe(etTemporadaActual.getText().toString()));
            item.setCapituloActual(parseIntSafe(etCapituloActual.getText().toString()));
        }

        if (itemEditar == null) {
            db.insertar(item);
            NotificationHelper.mostrarNotificacion(this, item.getTitulo(), item.getTipo());
        } else {
            db.actualizar(item);
        }

        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            mostrarDialogoDescartar();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void mostrarDialogoDescartar() {
    new AlertDialog.Builder(this)
        .setTitle(getString(R.string.descartar_titulo))
        .setMessage(getString(R.string.descartar_mensaje))
        .setPositiveButton(getString(R.string.salir), (d, w) -> finish())
        .setNegativeButton(getString(R.string.seguir_editando), null)
        .show();
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    @Override
    public void onBackPressed() {
        mostrarDialogoDescartar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGEN && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String rutaCopia = copiarImagenAlStorage(uri);
            if (rutaCopia != null) {
                rutaImagenSeleccionada = rutaCopia;
                ImageView ivPreview = findViewById(R.id.ivPreviewImagen);
                ivPreview.setImageURI(Uri.fromFile(new File(rutaCopia)));
                ivPreview.setVisibility(View.VISIBLE);
            }
        }
    }

    private String copiarImagenAlStorage(Uri uri) {
    try {
        InputStream input = getContentResolver().openInputStream(uri);
        File dir = new File(getFilesDir(), "imagenes");
        if (!dir.exists()) dir.mkdirs();
        File archivo = new File(dir, UUID.randomUUID().toString() + ".jpg");
        OutputStream output = new FileOutputStream(archivo);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = input.read(buffer)) != -1) {
            output.write(buffer, 0, len);
        }
        output.close();
        input.close();
        return archivo.getAbsolutePath();
    } catch (Exception e) {
        e.printStackTrace();
        return null;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageHelper.aplicarIdioma(newBase));
    }
    
}
