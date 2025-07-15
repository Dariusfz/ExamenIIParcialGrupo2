package com.example.exameniiparcial;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Query;

import java.util.List;

public interface ApiService {
    @GET("examen/GetPersons.php")
    Call<List<PersonaLista>> getPersonas();


    @DELETE("examen/DeletePersons.php")
    Call<Void> eliminarPersona(@Query("id") int id);


    @HTTP(method = "DELETE", path = "examen/DeletePersons.php", hasBody = true)
    Call<Void> eliminarPersona(@Body IdRequest idRequest);


    class IdRequest {
        private int id;

        public IdRequest(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}