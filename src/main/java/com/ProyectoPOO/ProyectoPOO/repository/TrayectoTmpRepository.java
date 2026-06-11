package com.ProyectoPOO.ProyectoPOO.repository;

import com.ProyectoPOO.ProyectoPOO.model.TrayectoTmp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrayectoTmpRepository extends JpaRepository<TrayectoTmp, Long> {

    List<TrayectoTmp> findByIdCargueOrderById(Long idCargue);
}
