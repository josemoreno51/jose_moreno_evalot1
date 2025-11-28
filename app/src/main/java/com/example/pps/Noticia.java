package com.example.pps;

import java.io.Serializable;
import java.util.Date;

public class Noticia implements Serializable {  // ← Agrega Serializable aquí
    private String id;
    private String titulo;
    private String resumen;
    private String contenido;
    private String autor;
    private Date fecha;
    private String userId;

    // Constructor vacío (OBLIGATORIO para Firestore)
    public Noticia() {
        // Firestore necesita este constructor vacío
    }

    // Constructor completo (sin ID - Firestore lo genera automáticamente)
    public Noticia(String titulo, String resumen, String contenido, String autor, Date fecha, String userId) {
        this.titulo = titulo;
        this.resumen = resumen;
        this.contenido = contenido;
        this.autor = autor;
        this.fecha = fecha;
        this.userId = userId;
    }

    // Constructor con todos los campos incluyendo ID
    public Noticia(String id, String titulo, String resumen, String contenido, String autor, Date fecha, String userId) {
        this.id = id;
        this.titulo = titulo;
        this.resumen = resumen;
        this.contenido = contenido;
        this.autor = autor;
        this.fecha = fecha;
        this.userId = userId;
    }

    // GETTERS Y SETTERS (todos son necesarios para Firestore)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Método toString para debugging
    @Override
    public String toString() {
        return "Noticia{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", resumen='" + resumen + '\'' +
                ", autor='" + autor + '\'' +
                ", fecha=" + fecha +
                '}';
    }
}