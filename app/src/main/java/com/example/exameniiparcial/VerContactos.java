package com.example.exameniiparcial;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VerContactos extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PersonaAdapter adapter;
    private List<PersonaLista> personaList;
    private SearchView searchView;
    private Button btnAtras, btnEditar, btnEliminar;
    private PersonaLista selectedPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_contactos);


        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        btnAtras = findViewById(R.id.btnAtras);
        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        personaList = new ArrayList<>();
        adapter = new PersonaAdapter(personaList, this);
        recyclerView.setAdapter(adapter);
        selectedPerson = null;
        // Inicialmente deshabilitar botones de edición/eliminación
        btnEditar.setEnabled(false);
        btnEliminar.setEnabled(false);

        // Configurar listeners
        configurarListeners();


        obtenerPersonas();

        adapter.setOnLongItemClickListener(persona -> {
            mostrarDialogoNavegacion(persona);
        });


    }

    private void mostrarDialogoNavegacion(PersonaLista persona) {
        if (persona.getLatitud() == 0 || persona.getLongitud() == 0) {
            Toast.makeText(this, "Esta persona no tiene ubicación registrada", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Navegar a ubicación")
                .setMessage("¿Deseas navegar a la ubicación de " + persona.getNombre_completo() + "?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    abrirGoogleMaps(persona.getLatitud(), persona.getLongitud());
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void abrirGoogleMaps(double latitud, double longitud) {
        try {

            String uri = "google.navigation:q=" + latitud + "," + longitud + "&mode=d";

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");

            // Verificar si Google Maps está instalado
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Si Google Maps no está instalado, abrir en navegador web
                String webUri = "https://www.google.com/maps/dir/?api=1&destination=" +
                        latitud + "," + longitud + "&travelmode=driving";
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
                startActivity(webIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir Google Maps", Toast.LENGTH_SHORT).show();
            Log.e("MAPS", "Error al abrir Maps", e);
        }
    }

    private void configurarListeners() {

        btnAtras.setOnClickListener(v -> finish());


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });

        // Selección de items
        adapter.setOnItemClickListener(position -> {
            // Obtiene la persona usando la posición
            selectedPerson = adapter.getPersonaAtPosition(position); // Necesitarás implementar este método
            btnEditar.setEnabled(true);
            btnEliminar.setEnabled(true);
        });

        // Botón Editar
        btnEditar.setOnClickListener(v -> {
            if (selectedPerson != null) {
                Intent intent = new Intent(VerContactos.this, MainActivity.class);
                intent.putExtra("MODO_EDICION", true);
                intent.putExtra("ID_PERSONA", selectedPerson.getId());
                intent.putExtra("NOMBRE_COMPLETO", selectedPerson.getNombre_completo());
                intent.putExtra("TELEFONO", selectedPerson.getTelefono());
                intent.putExtra("FIRMA_DIGITAL", selectedPerson.getFirma_digital());
                intent.putExtra("LATITUD", selectedPerson.getLatitud());
                intent.putExtra("LONGITUD", selectedPerson.getLongitud());

                // Depuración: verifica los valores antes de enviar
                Log.d("EDICION", "Enviando datos - Nombre: " + selectedPerson.getNombre_completo());
                //Log.d("EDICION", "Firma (primeros 10 chars): " + (selectedPerson.getFirma_digital() != null ? selectedPerson.getFirma_digital().substring(0, Math.min(10, selectedPerson.getFirma_digital().length())) :"null");
                startActivity(intent);
            }
        });


        // Botón Eliminar
        btnEliminar.setOnClickListener(v -> {
            PersonaLista seleccionada = adapter.getSelectedPersona();
            if (seleccionada != null) {
                confirmarEliminacion(seleccionada.getId(), seleccionada.getNombre_completo());
            }
        });
    }

    private void confirmarEliminacion(int id, String nombre) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Eliminar a " + nombre + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    ejecutarEliminacion(id);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarEliminacion(int personaId) {
        ApiService apiService = ApiClient.getApiService();
        Call<Void> call = apiService.eliminarPersona(personaId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(VerContactos.this, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                    obtenerPersonas();
                } else {
                    try {

                        String errorBody = response.errorBody().string();
                        Log.e("API_ERROR", errorBody);
                        Toast.makeText(VerContactos.this, "Error: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(VerContactos.this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(VerContactos.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_FAILURE", "Error eliminando", t);
            }
        });
    }

    private void obtenerPersonas() {
        ApiService apiService = ApiClient.getApiService();
        Call<List<PersonaLista>> call = apiService.getPersonas();

        call.enqueue(new Callback<List<PersonaLista>>() {
            @Override
            public void onResponse(Call<List<PersonaLista>> call, Response<List<PersonaLista>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    personaList = response.body();
                    adapter.updateData(personaList); // Usamos el nuevo método updateData
                } else {
                    Toast.makeText(VerContactos.this, "Error al obtener datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PersonaLista>> call, Throwable t) {
                Toast.makeText(VerContactos.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        obtenerPersonas();
    }





}