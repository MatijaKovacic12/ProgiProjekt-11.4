package touristAgency.unitTests.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import touristAgency.models.AppUser;
import touristAgency.models.Role;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AppUserRepositoryTest extends  RepositoryTestBase {
    @Test
    void findByEmailIgnoreCase_findsUserRegardlessOfCase() {
        assertEquals(customer.getId(), users.findByEmailIgnoreCase("ana@test.hr").orElseThrow().getId());
        assertEquals(customer.getId(), users.findByEmailIgnoreCase("ANA@TEST.HR").orElseThrow().getId());
    }

    @Test
    void findByEmailIgnoreCase_returnsEmptyForUnknownEmail() {
        assertTrue(users.findByEmailIgnoreCase("nepostoji@test.hr").isEmpty());
    }

    @Test
    void save_rejectsDuplicateEmailWithDifferentCase() {
        AppUser duplicate = new AppUser();
        duplicate.setFirstName("Druga");
        duplicate.setLastName("Ana");
        duplicate.setEmail("ANA@test.hr");
        assertThrows(DataIntegrityViolationException.class, () -> users.saveAndFlush(duplicate));
    }

    @Test
    void save_setsDefaultRoleUser() {
        AppUser u = new AppUser();
        u.setFirstName("Novi");
        u.setLastName("Korisnik");
        u.setEmail("novi@test.hr");
        assertEquals(Role.USER, users.saveAndFlush(u).getRole());
    }

    @Test
    void findByRoleIn_returnsOnlyEmployeesSortedByLastName() {
        List<AppUser> employees = users.findByRoleInOrderByLastNameAscFirstNameAsc(
                List.of(Role.ADMIN, Role.ORGANIZER, Role.GUIDE));

        assertEquals(List.of(admin.getId(), guide1.getId(), guide2.getId()),
                employees.stream().map(AppUser::getId).toList());
    }

    @Test
    void findByRole_returnsAllGuides() {
        List<AppUser> guides = users.findByRoleOrderByLastNameAscFirstNameAsc(Role.GUIDE);
        assertEquals(List.of(guide1.getId(), guide2.getId()), guides.stream().map(AppUser::getId).toList());
    }

    @Test
    void findAvailableGuides_excludesGuideAssignedToOverlappingSession() {
        List<AppUser> available = users.findAvailableGuides(
                upcoming.getStartTime().plusHours(1), upcoming.getEndTime().plusHours(1));

        assertEquals(List.of(guide2.getId()), available.stream().map(AppUser::getId).toList());
    }

    @Test
    void findAvailableGuides_includesGuideWhenSessionsOnlyTouch() {
        List<AppUser> available = users.findAvailableGuides(
                upcoming.getEndTime(), upcoming.getEndTime().plusHours(2));

        assertEquals(2, available.size());
    }

    @Test
    void findAvailableGuides_neverReturnsNonGuides() {
        List<AppUser> available = users.findAvailableGuides(now.plusYears(1), now.plusYears(1).plusHours(2));
        assertTrue(available.stream().allMatch(u -> u.getRole() == Role.GUIDE));
    }
}
