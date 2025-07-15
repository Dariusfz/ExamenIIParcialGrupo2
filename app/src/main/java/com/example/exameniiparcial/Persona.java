package com.example.exameniiparcial;


public class Persona {
    private String  id;
    private String nombre_completo;
    private String telefono;
    private String firma_digital;
    private String latitud;
    private String longitud;

    public Persona() {}

    public Persona(String nombre_completo, String telefono,String firma_digital, String latitud, String longitud) {
        this.nombre_completo = nombre_completo;
        this.telefono = telefono;
        this.firma_digital = firma_digital;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre_completo() {
        return nombre_completo;
    }

    public void setNombre_completo(String nombre_completo) {
        this.nombre_completo = nombre_completo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }


    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String   longitud) {
        this.longitud = longitud;
    }

    public String getFirma_digital() {
        return firma_digital;
    }

    public void setFirma_digital(String firma_digital) {
        this.firma_digital = firma_digital;
    }
}
