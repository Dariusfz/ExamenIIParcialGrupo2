package com.example.exameniiparcial;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText etNombre, etTelefono, etLatitud, etLongitud;
    private SignaturePad signaturePad;
    private Button btnGuardar, btnLimpiarFirma, btnVerContactos;
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etLatitud = findViewById(R.id.etLatitud);
        etLongitud = findViewById(R.id.etLongitud);
        signaturePad = findViewById(R.id.signaturePad);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnLimpiarFirma = findViewById(R.id.btnLimpiarFirma);
        btnVerContactos = findViewById(R.id.btnVerContactos);

        if (getIntent().hasExtra("MODO_EDICION")) {
            cargarDatosParaEdicion();
        }

        signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {}

            @Override
            public void onSigned() {
                validarCampos();
            }

            @Override
            public void onClear() {
                btnGuardar.setEnabled(false);
            }
        });

        // Configurar ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        obtenerUbicacion();

        // Botón limpiar firma
        btnLimpiarFirma.setOnClickListener(v -> {
            signaturePad.clear();
            btnGuardar.setEnabled(false);
        });

        // Botón guardar
        btnGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                SendData();
            }
        });

        btnVerContactos.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),VerContactos.class);
            startActivity(intent);
        });


        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validarCampos();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etNombre.addTextChangedListener(textWatcher);
        etTelefono.addTextChangedListener(textWatcher);

    }

    private void cargarDatosParaEdicion() {
        try {

            String nombre = getIntent().getStringExtra("NOMBRE_COMPLETO");
            String telefono = getIntent().getStringExtra("TELEFONO");
            String firmaBase64 = getIntent().getStringExtra("FIRMA_DIGITAL");
            double latitud = getIntent().getDoubleExtra("LATITUD", 0);
            double longitud = getIntent().getDoubleExtra("LONGITUD", 0);


            etNombre.setText(nombre);
            etTelefono.setText(telefono);
            etLatitud.setText(String.valueOf(latitud));
            etLongitud.setText(String.valueOf(longitud));


            if (firmaBase64 != null && !firmaBase64.isEmpty()) {
                byte[] decodedBytes = Base64.decode(firmaBase64, Base64.DEFAULT);
                Bitmap firmaBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                signaturePad.setSignatureBitmap(firmaBitmap);
            }


            btnGuardar.setText("Actualizar Contacto");

        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("EDICION", "Error al cargar datos", e);
        }

    }

    private boolean validarCampos() {
        boolean nombreValido = !etNombre.getText().toString().isEmpty();
        boolean telefonoValido = !etTelefono.getText().toString().isEmpty();
        boolean firmaValida = !signaturePad.isEmpty();

        btnGuardar.setEnabled(nombreValido && telefonoValido && firmaValida);


        if (!nombreValido) {
            etNombre.setError(nombreValido ? null : "Campo requerido");
        }

        if (!telefonoValido) {
            etTelefono.setError(telefonoValido ? null : "Campo requerido");
        }

        return nombreValido && telefonoValido && firmaValida;
    }

    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        etLatitud.setText(String.valueOf(location.getLatitude()));
                        etLongitud.setText(String.valueOf(location.getLongitude()));
                        validarCampos();
                    } else {
                        Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion();
        } else {
            Toast.makeText(this, "Se necesitan permisos de ubicación", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Aceptar", null)
                .show();
    }



    private void SendData() {
        if (!validarCampos()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getIntent().hasExtra("MODO_EDICION")) {
            actualizarContacto();
        } else {
            crearNuevoContacto();
        }
    }

    private void actualizarContacto() {

        int idPersona = getIntent().getIntExtra("ID_PERSONA", -1);
        if (idPersona == -1) {
            Toast.makeText(this, "Error: ID no válido", Toast.LENGTH_SHORT).show();
            return;
        }


        Bitmap firmaBitmap = signaturePad.getSignatureBitmap();
        String firmaBase64 = convertirBitmapABase64(firmaBitmap);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", idPersona);
            jsonObject.put("nombre_completo", etNombre.getText().toString());
            jsonObject.put("telefono", etTelefono.getText().toString());
            jsonObject.put("latitud", etLatitud.getText().toString());
            jsonObject.put("longitud", etLongitud.getText().toString());
            jsonObject.put("firma_digital", firmaBase64);


            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    RestApiMethods.EndpointUpdatePerson,
                    jsonObject,
                    response -> {
                        try {
                            // Manejar respuesta exitosa
                            String mensaje = response.getString("message");
                            Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_SHORT).show();


                            if (firmaBitmap != null) {
                                String filePath = guardarFirmaEnDispositivo(firmaBitmap, String.valueOf(idPersona));
                                Log.d("ACTUALIZACION", "Firma guardada en: " + filePath);
                            }


                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {

                        String errorMsg = "Error al actualizar: " + error.getMessage();
                        Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e("ACTUALIZACION", errorMsg);


                        if (firmaBitmap != null) {
                            String tempPath = guardarFirmaEnDispositivo(firmaBitmap, "temp_update_" + System.currentTimeMillis());
                            Log.e("ACTUALIZACION", "Firma temporal guardada en: " + tempPath);
                        }
                    });


            request.setRetryPolicy(new DefaultRetryPolicy(
                    10000, // timeout en ms
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(this);
            }
            requestQueue.add(request);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al preparar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void crearNuevoContacto() {
        requestQueue = Volley.newRequestQueue(this);
        Persona persona = new Persona();


        Bitmap firmaBitmap = signaturePad.getSignatureBitmap();
        String firmaBase64 = convertirBitmapABase64(firmaBitmap);


        persona.setNombre_completo(etNombre.getText().toString());
        persona.setTelefono(etTelefono.getText().toString());
        persona.setLatitud(etLatitud.getText().toString());//
        persona.setLongitud(etLongitud.getText().toString());//
        persona.setFirma_digital(firmaBase64);

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("nombre_completo", persona.getNombre_completo());
            jsonObject.put("telefono", persona.getTelefono());
            jsonObject.put("latitud", persona.getLatitud());
            jsonObject.put("longitud", persona.getLongitud());
            jsonObject.put("firma_digital", persona.getFirma_digital());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    RestApiMethods.EndpointCreatePerson,
                    jsonObject,
                    response -> {
                        try {
                            String mensaje = response.getString("message");


                            String idPersona = "temp_" + System.currentTimeMillis(); // Valor por defecto

                            if (response.has("id")) {
                                idPersona = response.getString("id");
                            } else if (response.has("personaId")) {
                                idPersona = response.getString("personaId");
                            } else if (response.has("ID")) {
                                idPersona = response.getString("ID");
                            }


                            if (firmaBitmap != null) {
                              guardarFirmaEnDispositivo(firmaBitmap, idPersona);

                            }

                            limpiarFormulario();

                        } catch (Exception ex) {
                            ex.printStackTrace();

                            if (firmaBitmap != null) {
                                String tempPath = guardarFirmaEnDispositivo(firmaBitmap, "temp_" + System.currentTimeMillis());
                                Toast.makeText(getApplicationContext(),
                                        "Error al procesar respuesta. Firma guardada temporalmente en: " + tempPath,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Error al procesar respuesta del servidor",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    },
                    error -> {

                        if (firmaBitmap != null) {
                            String tempPath = guardarFirmaEnDispositivo(firmaBitmap, "temp_" + System.currentTimeMillis());
                            Toast.makeText(getApplicationContext(),
                                    "Error de conexión: " + error.getMessage() + "\nFirma guardada temporalmente en: " + tempPath,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Error de conexión: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

            requestQueue.add(request);

        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Error al preparar los datos: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String guardarFirmaEnDispositivo(Bitmap bitmap, String idPersona) {
        if (bitmap == null) {
            return null;
        }

        try {
            // Crear directorio si no existe
            File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Firmas");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }


            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "FIRMA_" + idPersona + "_" + timeStamp + ".jpg";
            File imageFile = new File(storageDir, imageFileName);


            FileOutputStream out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();

            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private String convertirBitmapABase64(Bitmap bitmap) {
        if (bitmap == null) {
            return "";
        }

        try {

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            return Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void limpiarFormulario() {
        etNombre.setText("");
        etTelefono.setText("");
        signaturePad.clear();

        btnGuardar.setEnabled(false);
    }





}

