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
//actividad para mostrar los detalles de la pelicula/serie se abre cuando pulsas en una peli/serie en la pantalla principal
    private miBD db; //conexion bd
    private int itemId; //viene de main o addEdit para guaradr el id del item

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = new miBD(this); //
        // Recoge el id que le pasó la actividad anterior mediante el Intent
        // Si no viene ningún id pone -1 por defecto porque algo paso y eso es malo
        itemId = getIntent().getIntExtra("id", -1);

        cargarDatos(); // Llama al metodo que rellena todos los campos de la pantalla para que se vea la info

        // Cuando se pulsa el botón Editar abre AddEditActivity pasándole el id del item
        findViewById(R.id.btnDetalleEditar).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditActivity.class);
            intent.putExtra("item_id", itemId);
            startActivityForResult(intent, 100);
        });
    }

    private void cargarDatos() { //principal para poner la info en pantalla
        MediaItem item = db.obtenerPorId(itemId); //busca en bd
        if (item == null) { finish(); return; } //cierra siu no lo encuentra

        setTitle(item.getTitulo()); // Pone el título del item en la barra superior
        ((TextView) findViewById(R.id.tvDetalleTitulo)).setText(item.getTitulo());
        // El tipo se guarda en español en la BD, así que hay que traducirlo para mostrarlo
        String tipoTraducido;
        switch (item.getTipo() != null ? item.getTipo() : "") {
            case "Película": tipoTraducido = getString(R.string.tipo_pelicula); break;
            case "Serie":    tipoTraducido = getString(R.string.tipo_serie); break;
            default:         tipoTraducido = item.getTipo(); break;
        }
        // Muestra el tipo traducido junto al género separados por un punto
        ((TextView) findViewById(R.id.tvDetalleTipo)).setText(tipoTraducido + " • " + item.getGenero());
        ((TextView) findViewById(R.id.tvDetallePuntuacion)).setText("⭐ " + (int) item.getPuntuacion() + "/5");
        // El cast (int) elimina los decimales, por ejemplo de 4.0 a 4

        TextView tvEstado = findViewById(R.id.tvDetalleEstado);
        // asigna el color según el estado guardado en español
        switch (item.getEstado() != null ? item.getEstado() : "") {
            case "Visto":       tvEstado.setTextColor(0xFF4CAF50); break;
            case "En progreso": tvEstado.setTextColor(0xFFFF9800); break;
            case "Pendiente":   tvEstado.setTextColor(0xFF9E9E9E); break;
        }
        // muestra el texto del estado traducido al idioma activo
        switch (item.getEstado() != null ? item.getEstado() : "") {
            case "Visto":       tvEstado.setText(getString(R.string.estado_visto)); break;
            case "En progreso": tvEstado.setText(getString(R.string.estado_en_progreso)); break;
            case "Pendiente":   tvEstado.setText(getString(R.string.estado_pendiente)); break;
            default:            tvEstado.setText(item.getEstado());
        }

        // Muestra el resumen, y si está vacío o es null muestra el texto "Sin resumen" traducido
        ((TextView) findViewById(R.id.tvDetalleResumen)).setText(
        item.getResumen() != null && !item.getResumen().isEmpty()
        ? item.getResumen() : getString(R.string.sin_resumen));
        //lo mismo
        ((TextView) findViewById(R.id.tvDetalleComentario)).setText(
                item.getComentario() != null && !item.getComentario().isEmpty()
                ? item.getComentario() : getString(R.string.sin_comentario));

        // Y el progreso:
        ((TextView) findViewById(R.id.tvDetalleProgreso)).setText(
                getString(R.string.progreso_texto,
                    item.getTemporadaActual(),
                    item.getCapituloActual(),
                    item.getTemporadasTotales()));

        // Muestra la imagen si existe, si no el ImageView  oculto
        ImageView ivImagen = findViewById(R.id.ivDetalleImagen);
        if (item.getRutaImagen() != null && !item.getRutaImagen().isEmpty()) {
            ivImagen.setVisibility(View.VISIBLE);
            ivImagen.setImageURI(Uri.fromFile(new File(item.getRutaImagen())));
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        // Cuando volvemos de editar (AddEditActivity) con resultado OK
        // recargamos los datos para mostrar los cambios actualizados
        if (res == RESULT_OK) cargarDatos();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    //primer metodo que se ejecuta para establecer el idioma a español por defecto
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageHelper.aplicarIdioma(newBase));
    }
}