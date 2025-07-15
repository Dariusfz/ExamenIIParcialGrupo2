<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: DELETE, POST"); // Agregar POST
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once 'database.php';
include_once 'Personas.php';

$db = new DataBase();
$instant = $db->getConnection();

$pinst = new Personas($instant);

// Obtener datos de entrada (para DELETE via POST)
$input = json_decode(file_get_contents("php://input"));

// Alternativa para DELETE tradicional (vía URL)
if($_SERVER['REQUEST_METHOD'] == 'DELETE' && isset($_GET['id'])) {
    $pinst->id = $_GET['id'];
} elseif(isset($input->id)) {
    $pinst->id = $input->id;
} else {
    http_response_code(400);
    echo json_encode([
        "issuccess" => false,
        "message" => "ID no proporcionado"
    ]);
    exit;
}

if ($pinst->deletePerson()) {
    http_response_code(200);
    echo json_encode([
        "issuccess" => true,
        "message" => "Registro eliminado correctamente",
        "id" => $pinst->id // Opcional: devolver el ID eliminado
    ]);
} else {
    http_response_code(500); // Cambiado a 500 (error interno)
    echo json_encode([
        "issuccess" => false,
        "message" => "No se pudo eliminar el registro",
        "error" => $pinst->getLastError() // Si tienes método para obtener errores
    ]);
}
?>