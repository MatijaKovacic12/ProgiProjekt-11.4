package touristAgency.unitTests.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import touristAgency.controllers.TripController;
import touristAgency.dto.CreateTripRequest;
import touristAgency.dto.PriceCalculationResponse;
import touristAgency.dto.TripDetailsResponse;
import touristAgency.dto.TripSummaryResponse;
import touristAgency.exceptions.ResourceNotFoundException;
import touristAgency.services.TripService;
import touristAgency.controllers.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TripControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TripService tripService;

    @InjectMocks
    private TripController tripController;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders.standaloneSetup(tripController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/trips vraća status 200 OK i JSON listu")
    void getAllTrips_returns200AndJsonArray() throws Exception {
        TripSummaryResponse trip = TripSummaryResponse.builder()
                .id(1L)
                .title("Zagreb Tour")
                .price(new BigDecimal("25.00"))
                .build();

        when(tripService.getAllTrips()).thenReturn(List.of(trip));

        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Zagreb Tour"));
    }

    @Test
    @DisplayName("GET /api/trips/{id} vraća 200 OK kada izlet postoji")
    void getTripById_whenFound_returns200() throws Exception {
        TripDetailsResponse details = TripDetailsResponse.builder()
                .id(100L)
                .title("Zagreb Walking Tour")
                .price(new BigDecimal("25.00"))
                .build();

        when(tripService.getTripById(100L)).thenReturn(details);

        mockMvc.perform(get("/api/trips/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.title").value("Zagreb Walking Tour"));
    }

    @Test
    @DisplayName("GET /api/trips/{id} vraća 404 Not Found kada izlet ne postoji")
    void getTripById_whenNotFound_returns404() throws Exception {
        when(tripService.getTripById(999L))
                .thenThrow(new ResourceNotFoundException("Trip with id 999 not found"));

        mockMvc.perform(get("/api/trips/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Trip with id 999 not found"));
    }

    @Test
    @DisplayName("POST /api/trips vraća 201 Created sa Location zaglavljem")
    void createTrip_whenValid_returns201AndLocationHeader() throws Exception {
        // Čisti JSON tekstualni blok - bez potrebe za ObjectMapperom
        String jsonPayload = """
            {
                "title": "Dubrovnik Tour",
                "description": "Historic tour",
                "categoryId": 1,
                "durationMinutes": 120,
                "price": 30.00,
                "maxParticipants": 15
            }
            """;

        TripDetailsResponse createdResponse = TripDetailsResponse.builder()
                .id(50L)
                .title("Dubrovnik Tour")
                .build();

        when(tripService.createTrip(any(CreateTripRequest.class))).thenReturn(createdResponse);

        mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/trips/50"))
                .andExpect(jsonPath("$.id").value(50L));
    }

    @Test
    @DisplayName("DELETE /api/trips/{id} vraća 204 No Content")
    void deleteTrip_returns204() throws Exception {
        doNothing().when(tripService).deleteTrip(100L);

        mockMvc.perform(delete("/api/trips/100"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/trips/{id}/calculate-price vraća 200 OK s točnim iznosom")
    void calculatePrice_returns200() throws Exception {
        PriceCalculationResponse calc = PriceCalculationResponse.builder()
                .tripId(100L)
                .numberOfParticipants(2)
                .grandTotal(new BigDecimal("50.00"))
                .build();

        when(tripService.calculatePrice(100L, 2, null)).thenReturn(calc);

        mockMvc.perform(get("/api/trips/100/calculate-price?participants=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grandTotal").value(50.00));
    }
}