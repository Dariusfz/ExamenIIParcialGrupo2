<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: PUT");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once 'database.php';
include_once 'Personas.php';

$db = new DataBase();
$instant = $db->getConnection();

$pinst = new Personas($instant);
$data = json_decode(file_get_contents("php://input"));

// Validación de datos requeridos
if (
    isset($data) &&
    !empty($data->id) &&
    !empty($data->nombre_completo) &&
    !empty($data->telefono)
) {
    $pinst->id = $data->id;
    $pinst->nombre_completo = $data->nombre_completo;
    $pinst->telefono = $data->telefono;
    
    // Campos opcionales (si no vienen en el request, se establecen valores por defecto)
    $pinst->firma_digital = $data->firma_digital ?? null;
    $pinst->latitud = $data->latitud ?? null;
    $pinst->longitud = $data->longitud ?? null;

    if ($pinst->updatePerson()) {
        http_response_code(200);
        echo json_encode([
            "issuccess" => true,
            "message" => "Registro actualizado correctamente",
            "data" => [
                "id" => $pinst->id,
                "nombre_completo" => $pinst->nombre_completo,
                "telefono" => $pinst->telefono,
                "firma_digital" => $pinst->firma_digital ? "actualizada" : "sin cambios",
                "ubicacion" => ($pinst->latitud && $pinst->longitud) ? 
                    ["lat" => $pinst->latitud, "lng" => $pinst->longitud] : 
                    "no actualizada"
            ]
        ]);
    } else {
        http_response_code(503);
        echo json_encode([
            "issuccess" => false,
            "message" => "No se pudo actualizar el registro",
            "error" => $pinst->getLastError() // Asumiendo que tienes este método
        ]);
    }
} else {
    http_response_code(400);
    echo json_encode([
        "issuccess" => false,
        "message" => "Datos incompletos",
        "required_fields" => ["id", "nombre_completo", "telefono"],
        "received_data" => $data // Para depuración
    ]);
}
?>