package touristAgency.unitTests.repositories;

import org.junit.jupiter.api.Test;
import touristAgency.models.ReservationAdditionalService;
import touristAgency.repositories.projection.TopItem;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReservationAdditionalServiceRepositoryTest extends RepositoryTestBase {
    @Test
    void findByReservationId_returnsSelectedServicesWithQuantity() {
        List<ReservationAdditionalService> items = reservationAdditionalServices.findByReservationId(upcomingRes.getId());

        assertEquals(1, items.size());
        assertEquals("Prijevoz", items.get(0).getAdditionalService().getName());
        assertEquals(7, items.get(0).getQuantity());
        assertEquals(0, new BigDecimal("15.00").compareTo(items.get(0).getUnitPrice()));
    }

    @Test
    void unitPrice_isSnapshotAndDoesNotChangeWithServicePrice() {
        transport.setPrice(new BigDecimal("99.00"));
        additionalServices.saveAndFlush(transport);

        ReservationAdditionalService item = reservationAdditionalServices.findByReservationId(upcomingRes.getId()).get(0);
        assertEquals(0, new BigDecimal("15.00").compareTo(item.getUnitPrice()));
    }

    @Test
    void findByReservationId_returnsEmptyWhenNoServices() {
        assertTrue(reservationAdditionalServices.findByReservationId(pastRes.getId()).isEmpty());
    }

    @Test
    void findTop5AdditionalServices_sumsQuantityOfNonCancelledReservations() {
        cancelledRes.addAdditionalService(transport, 2);
        reservations.saveAndFlush(cancelledRes);

        List<TopItem> top = reservationAdditionalServices.findTop5AdditionalServices(now.minusDays(10), now.plusDays(10));

        assertEquals(1, top.size());
        assertEquals("Prijevoz", top.get(0).getName());
        assertEquals(7L, top.get(0).getTotal());
    }

}
