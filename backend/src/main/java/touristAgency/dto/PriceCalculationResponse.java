package touristAgency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationResponse {
    private Long tripId;
    private String tripName;
    private int numberOfParticipants;
    private BigDecimal pricePerPerson;
    private BigDecimal baseTotal;
    private BigDecimal addonsTotalPerPerson;
    private BigDecimal addonsTotal;
    private BigDecimal grandTotal;
    private List<AdditionalServiceResponse> selectedServices;
}