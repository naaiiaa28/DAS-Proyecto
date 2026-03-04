package com.example.biblioteca;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.net.Uri;
import android.widget.ImageView;
import java.io.File;
import android.content.Context;

public class DetailActivity extends AppCompatActivity {

    private miBD db;
    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = new miBD(this);
        itemId = getIntent().getIntExtra("id", -1);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cargarDatos();

        findViewById(R.id.btnDetalleEditar).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditActivity.class);
            intent.putExtra("item_id", itemId);
            startActivityForResult(intent, 100);
        });
    }

    private void cargarDatos() {
        MediaItem item = db.obtenerPorId(itemId);
        if (item == null) { finish(); return; }

        setTitle(item.getTitulo());
        ((TextView) findViewById(R.id.tvDetalleTitulo)).setText(item.getTitulo());
        ((TextView) findViewById(R.id.tvDetalleTipo)).setText(item.getTipo() + " • " + item.getGenero());
        ((TextView) findViewById(R.id.tvDetallePuntuacion)).setText("⭐ " + (int) item.getPuntuacion() + "/5");

        TextView tvEstado = findViewById(R.id.tvDetalleEstado);
        // Color basado en valor español
        switch (item.getEstado() != null ? item.getEstado() : "") {
            case "Visto":       tvEstado.setTextColor(0xFF4CAF50); break;
            case "En progreso": tvEstado.setTextColor(0xFFFF9800); break;
            case "Pendiente":   tvEstado.setTextColor(0xFF9E9E9E); break;
        }
        // Mostrar traducido
        switch (item.getEstado() != null ? item.getEstado() : "") {
            case "Visto":       tvEstado.setText(getString(R.string.estado_visto)); break;
            case "En progreso": tvEstado.setText(getString(R.string.estado_en_progreso)); break;
            case "Pendiente":   tvEstado.setText(getString(R.string.estado_pendiente)); break;
            default:            tvEstado.setText(item.getEstado());
        }

        ((TextView) findViewById(R.id.tvDetalleResumen)).setText(
        item.getResumen() != null && !item.getResumen().isEmpty()
        ? item.getResumen() : getString(R.string.sin_resumen));

        ((TextView) findViewById(R.id.tvDetalleComentario)).setText(
                item.getComentario() != null && !item.getComentario().isEmpty()
                ? item.getComentario() : getString(R.string.sin_comentario));

        // Y el progreso:
        ((TextView) findViewById(R.id.tvDetalleProgreso)).setText(
                getString(R.string.progreso_texto,
                    item.getTemporadaActual(),
                    item.getCapituloActual(),
                    item.getTemporadasTotales()));

        ImageView ivImagen = findViewById(R.id.ivDetalleImagen);
        if (item.getRutaImagen() != null && !item.getRutaImagen().isEmpty()) {
            ivImagen.setVisibility(View.VISIBLE);
            ivImagen.setImageURI(Uri.fromFile(new File(item.getRutaImagen())));
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res == RESULT_OK) cargarDatos();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageHelper.aplicarIdioma(newBase));
    }
}