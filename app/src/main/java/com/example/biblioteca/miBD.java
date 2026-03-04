package com.example.biblioteca;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class miBD extends SQLiteOpenHelper {

    private static final String DB_NAME = "biblioteca.db";
    private static final int DB_VERSION = 2;
    public static final String TABLE = "objetos";

    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "titulo TEXT NOT NULL, "
            + "tipo TEXT, genero TEXT, puntuacion REAL, "
            + "comentario TEXT, resumen TEXT, estado TEXT, "
            + "temporadas_totales INTEGER DEFAULT 0, "
            + "temporada_actual INTEGER DEFAULT 0, "
            + "capitulo_actual INTEGER DEFAULT 0, "
            + "fecha_adicion TEXT)";

    public miBD(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        //se mete todo el texto de la creacion en una variable 
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE + " ADD COLUMN ruta_imagen TEXT");
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE);
            onCreate(db);
        }
    }

    public long insertar(MediaItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = toContentValues(item);
        return db.insert(TABLE, null, cv);
    }

    public int actualizar(MediaItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = toContentValues(item);
        return db.update(TABLE, cv, "id=?", new String[]{String.valueOf(item.getId())}); 
        //lo ultimo no es null porque necesito buscar el id
    }

    public boolean eliminar(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE, "id=?", new String[]{String.valueOf(id)}) > 0;
        //lo ultimo no es null porque necesito buscar el id
    }

    public List<MediaItem> obtenerTodos() {
        List<MediaItem> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE, null, null, null, null, null, "fecha_adicion DESC");
        if (c.moveToFirst()) {
            do { lista.add(cursorToItem(c)); } while (c.moveToNext());
        }
        c.close();
        return lista;
    }

    public List<MediaItem> filtrarPorEstado(String estado) {
        List<MediaItem> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE, null, "estado=?", new String[]{estado}, null, null, null);
        if (c.moveToFirst()) {
            do { lista.add(cursorToItem(c)); } while (c.moveToNext());
        }
        c.close();
        return lista;
    }

    public MediaItem obtenerPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE, null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        if (c.moveToFirst()) {
            MediaItem item = cursorToItem(c);
            c.close();
            return item;
        }
        c.close();
        return null;
    }

    private ContentValues toContentValues(MediaItem item) {
        ContentValues cv = new ContentValues();
        cv.put("titulo", item.getTitulo());
        cv.put("tipo", item.getTipo());
        cv.put("genero", item.getGenero());
        cv.put("puntuacion", item.getPuntuacion());
        cv.put("comentario", item.getComentario());
        cv.put("resumen", item.getResumen());
        cv.put("estado", item.getEstado());
        cv.put("temporadas_totales", item.getTemporadasTotales());
        cv.put("temporada_actual", item.getTemporadaActual());
        cv.put("capitulo_actual", item.getCapituloActual());
        cv.put("fecha_adicion", item.getFechaAdicion());
        cv.put("ruta_imagen", item.getRutaImagen());
        return cv;
    }

    private MediaItem cursorToItem(Cursor c) {
        MediaItem item = new MediaItem();
        item.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        item.setTitulo(c.getString(c.getColumnIndexOrThrow("titulo")));
        item.setTipo(c.getString(c.getColumnIndexOrThrow("tipo")));
        item.setGenero(c.getString(c.getColumnIndexOrThrow("genero")));
        item.setPuntuacion(c.getFloat(c.getColumnIndexOrThrow("puntuacion")));
        item.setComentario(c.getString(c.getColumnIndexOrThrow("comentario")));
        item.setResumen(c.getString(c.getColumnIndexOrThrow("resumen")));
        item.setEstado(c.getString(c.getColumnIndexOrThrow("estado")));
        item.setTemporadasTotales(c.getInt(c.getColumnIndexOrThrow("temporadas_totales")));
        item.setTemporadaActual(c.getInt(c.getColumnIndexOrThrow("temporada_actual")));
        item.setCapituloActual(c.getInt(c.getColumnIndexOrThrow("capitulo_actual")));
        item.setFechaAdicion(c.getString(c.getColumnIndexOrThrow("fecha_adicion")));
        item.setRutaImagen(c.getString(c.getColumnIndexOrThrow("ruta_imagen")));
        return item;
    }
}