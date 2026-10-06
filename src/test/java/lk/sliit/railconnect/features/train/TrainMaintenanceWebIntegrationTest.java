package lk.sliit.railconnect.features.train;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import lk.sliit.railconnect.features.route.domain.Route;
import lk.sliit.railconnect.features.route.repository.RouteRepository;
import lk.sliit.railconnect.features.schedule.dto.ScheduleForm;
import lk.sliit.railconnect.features.schedule.service.ScheduleService;
import lk.sliit.railconnect.features.train.domain.Maintenance;
import lk.sliit.railconnect.features.train.domain.MaintenanceStatus;
import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.features.train.domain.TrainStatus;
import lk.sliit.railconnect.features.train.dto.MaintenanceForm;
import lk.sliit.railconnect.features.train.dto.TrainForm;
import lk.sliit.railconnect.features.train.service.MaintenanceService;
import lk.sliit.railconnect.features.train.service.TrainService;
import lk.sliit.railconnect.features.train.repository.TrainRepository;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TrainMaintenanceWebIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired TrainService trainService;
    @Autowired TrainRepository trainRepository;
    @Autowired MaintenanceService maintenanceService;
    @Autowired ScheduleService scheduleService;
    @Autowired RouteRepository routeRepository;

    @Test
    @WithMockUser(roles = "RAILWAY_ADMIN")
    void adminTrainAndMaintenancePagesFitTheRestOfTheAdminPortal() throws Exception {
        Train train = trainService.create(train("1801", "Web Alignment"));
        maintenanceService.create(maintenance(train.getId(), "Engine service", MaintenanceStatus.SCHEDULED));

        mockMvc.perform(get("/admin/trains"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1801")))
                .andExpect(content().string(containsString("Maintenance")));
        mockMvc.perform(get("/admin/trains/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Train details")))
                .andExpect(content().string(containsString("Train name")))
                .andExpect(content().string(not(containsString("First class fare"))));
        mockMvc.perform(get("/admin/schedules/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("First class fare")))
                .andExpect(content().string(containsString("Second class fare")))
                .andExpect(content().string(containsString("Third class fare")));
        mockMvc.perform(get("/admin/trains/" + train.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Engine service")))
                .andExpect(content().string(containsString("seats")));
        mockMvc.perform(get("/admin/trains").param("q", "").param("status", ""))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/maintenance")
                        .param("trainId", "")
                        .param("status", "")
                        .param("type", "")
                        .param("date", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Engine service")));
        mockMvc.perform(get("/admin/maintenance")
                        .param("trainId", train.getId().toString())
                        .param("status", "SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1801")));
        mockMvc.perform(get("/admin/schedules/new")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/carriages/new")).andExpect(status().isOk());
        mockMvc.perform(get("/admin")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RAILWAY_ADMIN")
    void maintenanceFormKeepsTheTrainActive() throws Exception {
        Train train = trainService.create(train("1802", "Form Train"));

        mockMvc.perform(post("/admin/maintenance")
                        .with(csrf())
                        .param("trainId", train.getId().toString())
                        .param("maintenanceType", "Wheel lathe")
                        .param("maintenanceDate", LocalDate.now().toString())
                        .param("description", "Shop work")
                        .param("status", "SCHEDULED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/maintenance"));

        assertThat(maintenanceService.historyForTrain(train.getId()))
                .extracting(Maintenance::getMaintenanceType)
                .contains("Wheel lathe");
        assertThat(trainService.require(train.getId()).getStatus()).isEqualTo(TrainStatus.ACTIVE);
    }

    @Test
    @WithMockUser(roles = "RAILWAY_ADMIN")
    void trainUpdateRequiresStatusAndShowsFieldValidationMessage() throws Exception {
        Train train = trainService.create(train("1810", "Status Validation"));

        mockMvc.perform(post("/admin/trains/" + train.getId())
                        .with(csrf())
                        .param("trainNumber", "1810")
                        .param("trainName", "Status Validation")
                        .param("description", "Alignment check"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Train status is required.")));

        assertThat(trainService.require(train.getId()).getStatus()).isEqualTo(TrainStatus.ACTIVE);
    }

    @Test
    void onlyAnActiveTrainCanBeScheduled() {
        Train active = trainService.create(train("1803", "Active Service"));
        maintenanceService.create(maintenance(active.getId(), "Future service", MaintenanceStatus.SCHEDULED));
        Route route = routeRepository.save(new Route("R-ACT", "Colombo", "Galle", new BigDecimal("100.00")));
        scheduleService.create(schedule("S-ACT", active.getId(), route.getId()));

        Train held = trainService.create(train("1804", "Held Service"));
        TrainForm update = train("1804", "Held Service");
        update.setStatus(TrainStatus.MAINTENANCE);
        trainService.update(held.getId(), update);

        assertThatThrownBy(() -> scheduleService.create(schedule("S-HOLD", held.getId(), route.getId())))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Only active trains");
    }

    @Test
    @WithMockUser(roles = "RAILWAY_ADMIN")
    void inactiveTrainWithoutHistoryCanBeDeleted() throws Exception {
        Train train = trainService.create(train("1805", "Disposable Train"));
        trainService.deactivate(train.getId());

        mockMvc.perform(post("/admin/trains/" + train.getId() + "/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/trains"))
                .andExpect(flash().attribute("success", "Train deleted."));

        assertThat(trainRepository.existsById(train.getId())).isFalse();
    }

    @Test
    @WithMockUser(roles = "RAILWAY_ADMIN")
    void trainWithScheduleHistoryCannotBeDeleted() throws Exception {
        Train train = trainService.create(train("1806", "Historical Train"));
        Route route = routeRepository.save(new Route("R-HIST", "Colombo", "Galle", new BigDecimal("100.00")));
        var schedule = scheduleService.create(schedule("S-HIST", train.getId(), route.getId()));
        scheduleService.cancel(schedule.getId());
        trainService.deactivate(train.getId());

        mockMvc.perform(post("/admin/trains/" + train.getId() + "/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/trains"))
                .andExpect(flash().attribute("error", containsString("schedule history")))
                .andExpect(flash().attribute("error", containsString("cannot be permanently deleted")));

        assertThat(trainRepository.existsById(train.getId())).isTrue();
    }

    @Test
    @WithMockUser(roles = "BOOKING_OFFICER")
    void bookingOfficerCannotManageTrainsOrMaintenance() throws Exception {
        mockMvc.perform(get("/admin/trains")).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/maintenance")).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/bookings")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    void passengerCannotOpenMaintenance() throws Exception {
        mockMvc.perform(get("/admin/maintenance")).andExpect(status().isForbidden());
    }

    private TrainForm train(String number, String name) {
        TrainForm form = new TrainForm();
        form.setTrainNumber(number);
        form.setTrainName(name);
        form.setDescription("Alignment check");
        return form;
    }

    private MaintenanceForm maintenance(Long trainId, String type, MaintenanceStatus status) {
        MaintenanceForm form = new MaintenanceForm();
        form.setTrainId(trainId);
        form.setMaintenanceType(type);
        form.setMaintenanceDate(LocalDate.now());
        form.setDescription("Alignment record");
        form.setStatus(status);
        return form;
    }

    private ScheduleForm schedule(String code, Long trainId, Long routeId) {
        ScheduleForm form = new ScheduleForm();
        form.setScheduleCode(code);
        form.setTrainId(trainId);
        form.setRouteId(routeId);
        form.setTravelDate(LocalDate.now().plusDays(4));
        form.setDepartureTime(LocalTime.of(8, 0));
        form.setArrivalTime(LocalTime.of(10, 30));
        form.setFirstClassFare(new BigDecimal("1800.00"));
        form.setSecondClassFare(new BigDecimal("1200.00"));
        form.setThirdClassFare(new BigDecimal("900.00"));
        return form;
    }
}
