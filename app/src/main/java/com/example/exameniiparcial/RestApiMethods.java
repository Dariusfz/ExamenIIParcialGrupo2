package com.example.exameniiparcial;

public class RestApiMethods {

    // URL base del backend PHP
    public static final String BASE_URL = "http://172.30.7.80/examen/";

    // Endpoints del CRUD
    public static final String EndpointGetPersons   = BASE_URL + "GetPersons.php";
    public static final String EndpointCreatePerson = BASE_URL + "PostPersons.php";
    public static final String EndpointUpdatePerson = BASE_URL + "UpdatePersons.php";
    public static final String EndpointDeletePerson = BASE_URL + "DeletePersons.php";

}
