<?php
class Personas
{
    private $conexion;
    private $table = "personas";

    public $id;
    public $nombre_completo;
    public $telefono;
    public $firma_digital;
    public $latitud;
    public $longitud;

    // Constructor de la clase personas
    public function __construct($db)
    {
        $this->conexion = $db;
    }

    // Create
    public function createPerson()
    {
        $consulta = "INSERT INTO 
                    " . $this->table . "
                    SET 
                    nombre_completo = :nombre_completo,
                    telefono = :telefono,
                    firma_digital = :firma_digital,
                    latitud = :latitud,
                    longitud = :longitud";

        $comando = $this->conexion->prepare($consulta);

        // Sanitización
        $this->nombre_completo = htmlspecialchars(strip_tags($this->nombre_completo));
        $this->telefono = htmlspecialchars(strip_tags($this->telefono));
        $this->firma_digital = htmlspecialchars(strip_tags($this->firma_digital));
        $this->latitud = htmlspecialchars(strip_tags($this->latitud));
        $this->longitud = htmlspecialchars(strip_tags($this->longitud));

        // bind data
        $comando->bindParam(":nombre_completo", $this->nombre_completo);
        $comando->bindParam(":telefono", $this->telefono);
        $comando->bindParam(":firma_digital", $this->firma_digital);
        $comando->bindParam(":latitud", $this->latitud);
        $comando->bindParam(":longitud", $this->longitud);

        if($comando->execute())
        {
            return true;
        }
        return false;
    }

    // Read
    public function GetListPersons()
    {
        $consulta = "SELECT * FROM " . $this->table . "";
        $comando = $this->conexion->prepare($consulta);
        $comando->execute();

        return $comando;
    }

    // Update
    public function updatePerson()
    {
        $consulta = "UPDATE " . $this->table . " SET 
                        nombre_completo = :nombre_completo,
                        telefono = :telefono,
                        firma_digital = :firma_digital,
                        latitud = :latitud,
                        longitud = :longitud
                    WHERE id = :id";

        $comando = $this->conexion->prepare($consulta);

        // Sanitización
        $this->nombre_completo = htmlspecialchars(strip_tags($this->nombre_completo));
        $this->telefono = htmlspecialchars(strip_tags($this->telefono));
        $this->firma_digital = htmlspecialchars(strip_tags($this->firma_digital));
        $this->latitud = htmlspecialchars(strip_tags($this->latitud));
        $this->longitud = htmlspecialchars(strip_tags($this->longitud));
        $this->id = htmlspecialchars(strip_tags($this->id));

        // Binding de datos
        $comando->bindParam(':nombre_completo', $this->nombre_completo);
        $comando->bindParam(':telefono', $this->telefono);
        $comando->bindParam(':firma_digital', $this->firma_digital);
        $comando->bindParam(':latitud', $this->latitud);
        $comando->bindParam(':longitud', $this->longitud);
        $comando->bindParam(':id', $this->id);

        return $comando->execute();
    }

    // Delete
    public function deletePerson()
    {
        $consulta = "DELETE FROM " . $this->table . " WHERE id = :id";

        $comando = $this->conexion->prepare($consulta);

        // Sanitización
        $this->id = htmlspecialchars(strip_tags($this->id));

        // Binding
        $comando->bindParam(':id', $this->id);

        return $comando->execute();
    }
}
?>