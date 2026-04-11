package com.ProyectoPOO.ProyectoPOO.repository;

import com.ProyectoPOO.ProyectoPOO.model.DocumentState;
import com.ProyectoPOO.ProyectoPOO.model.Vehicle;
import com.ProyectoPOO.ProyectoPOO.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByPlate(String plate);
    List<Vehicle> findByType(VehicleType type);

    @Query("select distinct v from Vehicle v join v.documents vd join vd.document d where d.code = :code")
    List<Vehicle> findByDocumentCode(@Param("code") String code);

    List<Vehicle> findDistinctByDocumentsState(DocumentState state);
}

