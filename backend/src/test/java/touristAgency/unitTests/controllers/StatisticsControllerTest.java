package touristAgency.unitTests.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import touristAgency.controllers.StatisticsController;
import touristAgency.dto.StatisticsResponse;
import touristAgency.services.StatisticsService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsControllerTest {

    private static final LocalDate FROM = LocalDate.of(2026, 11, 1);
    private static final LocalDate TO = LocalDate.of(2026, 11, 30);

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private StatisticsController statisticsController;

    @Test
    void getStatistics_returnsResponseFromService() {
        StatisticsResponse response = new StatisticsResponse(
                FROM, TO, 42L, 3L, new BigDecimal("4890.00"), List.of(), List.of(), List.of());
        when(statisticsService.getStatistics(FROM, TO)).thenReturn(response);

        StatisticsResponse result = statisticsController.getStatistics(FROM, TO);

        assertEquals(response, result);
    }

    @Test
    void exportCsv_returnsCsvFileForDownload() {
        when(statisticsService.exportCsv(FROM, TO)).thenReturn("Broj rezervacija\n42\n");

        ResponseEntity<byte[]> result = statisticsController.exportCsv(FROM, TO);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("attachment; filename=\"statistika_2026-11-01_2026-11-30.csv\"",
                result.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));

        MediaType type = result.getHeaders().getContentType();
        assertEquals("text", type.getType());
        assertEquals("csv", type.getSubtype());
        assertEquals(StandardCharsets.UTF_8, type.getCharset());

        assertEquals("\uFEFFBroj rezervacija\n42\n", new String(result.getBody(), StandardCharsets.UTF_8));
    }
}