<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, PUT, DELETE");
header("Access-Control-Allow-Headers: Content-Type");

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "dbexamen2p";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Modifica el código de CREATE y UPDATE para manejar BLOB
if ($_SERVER['REQUEST_METHOD'] === 'POST' || $_SERVER['REQUEST_METHOD'] === 'PUT') {
    $data = json_decode(file_get_contents("php://input"), true);
    
    $nombre = $data['nombre_completo'];
    $telefono = $data['telefono'];
    $latitud = $data['latitud'];
    $longitud = $data['longitud'];
    
    // Decodificar el Base64 a binario
    $firmaBinaria = base64_decode($data['firma_digital']);
    
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $stmt = $conn->prepare("INSERT INTO personas (nombre_completo, telefono, firma_digital, latitud, longitud) VALUES (?, ?, ?, ?, ?)");
        $stmt->bind_param("ssbdd", $nombre, $telefono, $firmaBinaria, $latitud, $longitud);
    } else {
        $id = $data['id'];
        $stmt = $conn->prepare("UPDATE personas SET nombre_completo=?, telefono=?, firma_digital=?, latitud=?, longitud=? WHERE id=?");
        $stmt->bind_param("ssbdds", $nombre, $telefono, $firmaBinaria, $latitud, $longitud, $id);
    }
    
    // Necesario para enviar datos BLOB
    $stmt->send_long_data(2, $firmaBinaria);
    
    if ($stmt->execute()) {
        echo json_encode(["success" => true, "message" => "Operación exitosa"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error en la operación"]);
    }
    $stmt->close();
}

//metodo get sin foto
if ($_SERVER['REQUEST_METHOD'] === 'GET' && !isset($_GET['id'])) {
    $result = $conn->query("SELECT id, nombre_completo, telefono, latitud, longitud FROM personas");
    
    if (!$result) {
        echo json_encode(["success" => false, "message" => "Error en la consulta"]);
        exit;
    }
    
    $personas = [];
    while($row = $result->fetch_assoc()) {
        $personas[] = [
            'id' => $row['id'],
            'nombre_completo' => $row['nombre_completo'],
            'telefono' => $row['telefono'],
            'latitud' => (float)$row['latitud'],
            'longitud' => (float)$row['longitud']
        ];
    }
    
    echo json_encode($personas);
    exit;
}



// DELETE (Eliminar persona)
if ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    $data = json_decode(file_get_contents("php://input"), true);
    $id = $data['id'];
    
    $stmt = $conn->prepare("DELETE FROM personas WHERE id=?");
    $stmt->bind_param("i", $id);
    
    if ($stmt->execute()) {
        echo json_encode(["success" => true, "message" => "Persona eliminada correctamente"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al eliminar persona"]);
    }
    $stmt->close();
}

$conn->close();
?>