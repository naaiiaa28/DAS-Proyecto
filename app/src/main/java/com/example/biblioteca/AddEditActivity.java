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
//actividad para cuando se quiere añadir una peli (pulsanbdo al mas pekeñito abajo) o cuando se edita una peli/serie ya existente
    private miBD db;  //base de datos
    private MediaItem itemEditar = null;  //para cuando se edita aqui se guarda su info y no perderla
    private EditText etTitulo; //para formulario
    private EditText etGenero; //para formulario
    private EditText etResumen; //para formulario
    private EditText etComentario; //para formulario
    private EditText etTemporadas; //para formulario
    private EditText etTemporadaActual; //para formulario
    private EditText etCapituloActual; //para formulario
    private RatingBar ratingBar; //para formulario
    private Spinner spinnerTipo; //para formulario
    private Spinner spinnerEstado; //para formulario
    private LinearLayout layoutSerie; //el campo que aparece y desaparece depende de la opcion
    private String rutaImagenSeleccionada = null; //ruta de la imagen
    private static final int REQUEST_IMAGEN = 200; // para la eleccion de imagen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit); //coloca el layout

        db = new miBD(this); //abre conexion con bd

        //cpnecta cada variable del java con la del xml mediante su id
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

        // Rellena el spinner de tipo con las opciones traducidas al idioma activo (es el cuadrado de varias opciones: Peliculas o serie)
        spinnerTipo.setAdapter(new ArrayAdapter<>(this,R.layout.spinner_item, new String[]{getString(R.string.tipo_pelicula), getString(R.string.tipo_serie)}));

        //lo mismo de arriba pero para el progreso de visualizacion.
        spinnerEstado.setAdapter(new ArrayAdapter<>(this,R.layout.spinner_item,
        new String[]{getString(R.string.estado_visto), getString(R.string.estado_en_progreso), getString(R.string.estado_pendiente)}));

        // Comprueba si nos han pasado un id por intent
        // Si id != -1 significa que venimos a editar un item existente
        int id = getIntent().getIntExtra("item_id", -1);
        if (id != -1) {
            itemEditar = db.obtenerPorId(id);
            rellenarFormulario(itemEditar);
            setTitle("Editar"); //el boton de cuando entras en la info de una peli
        } else {
            setTitle("Añadir");
        }

        // Escucha cambios en el spinner de tipo Pelicula/serie
        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long i) {
                // Si pos==1 es Serie → muestra  campos de serie
                // Si pos==0 es Película →  oculta
                layoutSerie.setVisibility(pos == 1 ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> p) {}
        });

        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar()); //se pulsa guardar


        // Cuando se pulsa el botón de imagen abre la galería del móvil
        ImageView ivPreview = findViewById(R.id.ivPreviewImagen);
        findViewById(R.id.btnSeleccionarImagen).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK); // Intent : abre galería
            intent.setType("image/*"); // Filtra solo imágenes
            startActivityForResult(intent, REQUEST_IMAGEN); // Lanza la galería y espera resultado
        });
    }

    private void rellenarFormulario(MediaItem item) { //metodo para meter editar peli/serie
        // Pone el texto de cada campo con los datos del item que viene de la BD
        etTitulo.setText(item.getTitulo());
        etGenero.setText(item.getGenero());
        etResumen.setText(item.getResumen());
        etComentario.setText(item.getComentario());
        ratingBar.setRating(item.getPuntuacion());

        // Si el item tiene imagen guardada, la muestra en el preview
        if (item.getRutaImagen() != null && !item.getRutaImagen().isEmpty()) {
            ImageView ivPreview = findViewById(R.id.ivPreviewImagen);
            ivPreview.setImageURI(Uri.parse(item.getRutaImagen()));
            ivPreview.setVisibility(View.VISIBLE);
            rutaImagenSeleccionada = item.getRutaImagen();
        }

        // Busca en qué posición del array español está el tipo del item
        // y selecciona esa posición en el spinner (que puede mostrar otro idioma) para que aun sean diferentes idiomas los detecte igual
        String[] tiposES = {"Película", "Serie"};
        for (int i = 0; i < tiposES.length; i++) {
            if (tiposES[i].equals(item.getTipo())) spinnerTipo.setSelection(i);
        }

        // lo mismo que arriva para el estado
        String[] estadosES = {"Visto", "En progreso", "Pendiente"};
        for (int i = 0; i < estadosES.length; i++) {
            if (estadosES[i].equals(item.getEstado())) spinnerEstado.setSelection(i);
        }

        // Para serie muestra el bloque de campos y rellena los datos de progreso
        if ("Serie".equals(item.getTipo())) {
            layoutSerie.setVisibility(View.VISIBLE);
            etTemporadas.setText(String.valueOf(item.getTemporadasTotales()));
            etTemporadaActual.setText(String.valueOf(item.getTemporadaActual()));
            etCapituloActual.setText(String.valueOf(item.getCapituloActual()));
        }
    }

    private void guardar() {
        String titulo = etTitulo.getText().toString().trim(); // Lee el título y elimina espacios
        if (titulo.isEmpty()) { //titulo es obligatorio ( sera la clave de la base de datos)
            etTitulo.setError(getString(R.string.titulo_obligatorio)); // Muestra error en el campo
            return; // Para la ejecución, no guarda nada
        }

        // Si estamos editando reutiliza el item existente, si no crea uno nuevo
        MediaItem item = itemEditar != null ? itemEditar : new MediaItem();
        item.setTitulo(titulo);

        // Guarda el tipo SIEMPRE en español sin importar el idioma activo
        // porque la BD siempre trabaja con valores en español
        int posTipo = spinnerTipo.getSelectedItemPosition();
        item.setTipo(posTipo == 0 ? "Película" : "Serie");

        //mismo que tipo
        int posEstado = spinnerEstado.getSelectedItemPosition();
        String[] estadosES = {"Visto", "En progreso", "Pendiente"};
        item.setEstado(estadosES[posEstado]);

        item.setGenero(etGenero.getText().toString());
        item.setResumen(etResumen.getText().toString());
        item.setComentario(etComentario.getText().toString());
        item.setPuntuacion(ratingBar.getRating());

        //guarda la fecha para que la bd ordene pero no se muestra en la app (coso interno)
        item.setFechaAdicion(new SimpleDateFormat("yyyy-MM-dd HH:mm",
                Locale.getDefault()).format(new Date()));

        // Solo actualiza la imagen si se ha seleccionado una nueva
        if (rutaImagenSeleccionada != null) {
            item.setRutaImagen(rutaImagenSeleccionada);
        }

        // Solo guarda los campos de serie si el tipo es Serie
        if ("Serie".equals(item.getTipo())) {
            item.setTemporadasTotales(parseIntSafe(etTemporadas.getText().toString()));
            item.setTemporadaActual(parseIntSafe(etTemporadaActual.getText().toString()));
            item.setCapituloActual(parseIntSafe(etCapituloActual.getText().toString()));
        }

        if (itemEditar == null) {
            db.insertar(item); //si es nuevo se mete en la bd
            NotificationHelper.mostrarNotificacion(this, item.getTitulo(), item.getTipo()); //para que salte la noti
        } else { //sino lo actualiza
            db.actualizar(item);
        }

        setResult(RESULT_OK);
        finish(); //cierra actividad y vuelve atras
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) { //boton de atras del movil
        if (menuItem.getItemId() == android.R.id.home) {
            mostrarDialogoDescartar();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void mostrarDialogoDescartar() { //dialogo de aviso antes de salir por si quieres guardar
    new AlertDialog.Builder(this)
        .setTitle(getString(R.string.descartar_titulo))
        .setMessage(getString(R.string.descartar_mensaje))
        .setPositiveButton(getString(R.string.salir), (d, w) -> finish())
        .setNegativeButton(getString(R.string.seguir_editando), null)
        .show();
    }

    // Convierte un String a int de forma segura, devuelve 0 si falla
    // Necesario porque los EditText de números devuelven String y pueden estar vacíos
    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    // Intercepta el botón físico atrás del móvil para mostrar también el diálogo
    //sale en rojo porque esta deprecated pero funciona
    @Override
    public void onBackPressed() {
        mostrarDialogoDescartar();
    }


    // Se ejecuta cuando volvemos de la galería con el resultado de elegir imagen
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

    // Copia la imagen elegida de la galería al almacenamiento interno de la app
    // Esto es necesario porque la URI de la galería es temporal y caduca
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
    //primer metodo que se ejecuta para establecer el idioma a español por defecto
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageHelper.aplicarIdioma(newBase));
    }
    
}
