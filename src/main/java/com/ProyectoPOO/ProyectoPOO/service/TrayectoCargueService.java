package com.ProyectoPOO.ProyectoPOO.service;

import com.ProyectoPOO.ProyectoPOO.dto.CargueResumenResponse;
import com.ProyectoPOO.ProyectoPOO.dto.TrayectoTmpResponse;
import com.ProyectoPOO.ProyectoPOO.model.TrayectoTmp;
import com.ProyectoPOO.ProyectoPOO.repository.TrayectoTmpRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de cargue masivo de trayectos desde una plantilla Excel.
 * Orquesta el volcado a la tabla de staging trayecto_tmp y la invocación de los
 * procedimientos almacenados de cargue, validación y procesado.
 *
 * Plantilla Excel esperada (fila 1 = encabezado, se ignora):
 * 1. Identificación Conductor
 * 2. Placa Vehículo
 * 3. Código Ruta
 * 4. Ubicación
 * 5. Orden Parada
 * 6. Latitud (opcional)
 * 7. Longitud (opcional)
 */
@Service
@RequiredArgsConstructor
public class TrayectoCargueService {

    private static final DateTimeFormatter ID_CARGUE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final JdbcTemplate jdbcTemplate;
    private final TrayectoTmpRepository trayectoTmpRepository;

    @Transactional
    public CargueResumenResponse cargarDesdeExcel(MultipartFile file, String registeredByLogin) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo Excel es obligatorio");
        }

        Long idCargue = Long.parseLong(LocalDateTime.now().format(ID_CARGUE_FORMAT));
        DataFormatter dataFormatter = new DataFormatter();

        try (InputStream is = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row, dataFormatter)) {
                    continue;
                }

                String personaIdentification = getCellValue(row, 0, dataFormatter);
                String vehiclePlate = getCellValue(row, 1, dataFormatter);
                String routeCode = getCellValue(row, 2, dataFormatter);
                String location = getCellValue(row, 3, dataFormatter);
                String stopOrder = getCellValue(row, 4, dataFormatter);
                String latitude = getCellValue(row, 5, dataFormatter);
                String longitude = getCellValue(row, 6, dataFormatter);

                jdbcTemplate.update("CALL sp_cargue_trayecto(?,?,?,?,?,?,?,?,?)",
                        idCargue,
                        blankToNull(personaIdentification),
                        blankToNull(vehiclePlate),
                        blankToNull(routeCode),
                        blankToNull(location),
                        blankToNull(stopOrder),
                        blankToNull(latitude),
                        blankToNull(longitude),
                        registeredByLogin);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el archivo Excel: " + e.getMessage());
        }

        jdbcTemplate.update("CALL sp_validar_trayecto(?)", idCargue);
        jdbcTemplate.update("CALL sp_procesar_trayecto(?)", idCargue);

        return construirResumen(idCargue);
    }

    public CargueResumenResponse consultarCargue(Long idCargue) {
        List<TrayectoTmp> registros = trayectoTmpRepository.findByIdCargueOrderById(idCargue);
        if (registros.isEmpty()) {
            throw new IllegalArgumentException("No existe un cargue con el identificador: " + idCargue);
        }
        return construirResumen(idCargue);
    }

    private CargueResumenResponse construirResumen(Long idCargue) {
        List<TrayectoTmp> registros = trayectoTmpRepository.findByIdCargueOrderById(idCargue);

        int cargados = 0;
        int validados = 0;
        int procesados = 0;
        int conError = 0;

        for (TrayectoTmp registro : registros) {
            switch (registro.getEstado()) {
                case CARGADO -> cargados++;
                case VALIDADO -> validados++;
                case PROCESADO -> procesados++;
                case ERROR -> conError++;
            }
        }

        return CargueResumenResponse.builder()
                .idCargue(idCargue)
                .totalRegistros(registros.size())
                .cargados(cargados)
                .validados(validados)
                .procesados(procesados)
                .conError(conError)
                .registros(registros.stream().map(this::mapToResponse).collect(Collectors.toList()))
                .build();
    }

    private TrayectoTmpResponse mapToResponse(TrayectoTmp registro) {
        return TrayectoTmpResponse.builder()
                .id(registro.getId())
                .idCargue(registro.getIdCargue())
                .estado(registro.getEstado().name())
                .observacion(registro.getObservacion())
                .personaIdentification(registro.getPersonaIdentification())
                .vehiclePlate(registro.getVehiclePlate())
                .routeCode(registro.getRouteCode())
                .location(registro.getLocation())
                .stopOrder(registro.getStopOrder())
                .latitude(registro.getLatitude())
                .longitude(registro.getLongitude())
                .registeredByLogin(registro.getRegisteredByLogin())
                .createdAt(registro.getCreatedAt())
                .build();
    }

    private boolean isRowEmpty(Row row, DataFormatter dataFormatter) {
        for (int i = 0; i < 7; i++) {
            if (!getCellValue(row, i, dataFormatter).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(Row row, int columnIndex, DataFormatter dataFormatter) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
        }
        return dataFormatter.formatCellValue(cell).trim();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
