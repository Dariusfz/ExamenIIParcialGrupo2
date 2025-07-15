<?php

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once 'database.php';
include_once 'Personas.php';

$db = new DataBase();
$instant = $db->getConnection();

$pinst = new Personas($instant);

$data = json_decode(file_get_contents("php://input"));

if(isset($data))
{
    // Asignación de los nuevos campos
    $pinst->nombre_completo = $data->nombre_completo;
    $pinst->telefono = $data->telefono;
    $pinst->firma_digital = isset($data->firma_digital) ? $data->firma_digital : null;
    $pinst->latitud = isset($data->latitud) ? $data->latitud : null;
    $pinst->longitud = isset($data->longitud) ? $data->longitud : null;

    if($pinst->createPerson())
    {
        http_response_code(200);
        echo json_encode( 
            array( "issuccess" => true,
            "message" => "Creado con éxito",
            "data" => array(
                "nombre_completo" => $pinst->nombre_completo,
                "telefono" => $pinst->telefono,
                "firma_digital" => $pinst->firma_digital,
                "latitud" => $pinst->latitud,
                "longitud" => $pinst->longitud
            ))
        );
    }
    else
    {
        http_response_code(503); // Servicio no disponible
        echo json_encode( 
            array("issuccess" => false,
            "message" => "Error al crear",
            "error_details" => $pinst->getLastError()) // Asumiendo que tienes un método para obtener errores
        );
    }
}
else
{
    http_response_code(400);
    echo json_encode(array(
        "issuccess" => false,
        "message" => "Datos incompletos o inválidos"));
}