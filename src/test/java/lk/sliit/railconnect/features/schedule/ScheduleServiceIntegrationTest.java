package lk.sliit.railconnect.features.schedule;

import lk.sliit.railconnect.features.route.domain.Route;
import lk.sliit.railconnect.features.route.repository.RouteRepository;
import lk.sliit.railconnect.features.schedule.dto.ScheduleForm;
import lk.sliit.railconnect.features.schedule.service.ScheduleService;
import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.features.train.repository.TrainRepository;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ScheduleServiceIntegrationTest {
    @Autowired ScheduleService scheduleService;
    @Autowired TrainRepository trainRepository;
    @Autowired RouteRepository routeRepository;

    @Test
    void rejectsOverlappingScheduleForTheSameTrain() {
        Train train = trainRepository.save(new Train("T-900", "Conflict Test", null));
        Route route = routeRepository.save(new Route("R-900", "Colombo", "Kandy", new BigDecimal("120")));
        LocalDate date = LocalDate.now().plusDays(10);
        scheduleService.create(form("S-900", train.getId(), route.getId(), date, LocalTime.of(8, 0), LocalTime.of(10, 0)));

        assertThatThrownBy(() -> scheduleService.create(
                form("S-901", train.getId(), route.getId(), date, LocalTime.of(9, 0), LocalTime.of(11, 0))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("overlapping");
    }

    private ScheduleForm form(String code, Long trainId, Long routeId, LocalDate date, LocalTime departure, LocalTime arrival) {
        ScheduleForm form = new ScheduleForm();
        form.setScheduleCode(code);
        form.setTrainId(trainId);
        form.setRouteId(routeId);
        form.setTravelDate(date);
        form.setDepartureTime(departure);
        form.setArrivalTime(arrival);
        form.setBaseFare(new BigDecimal("1000"));
        return form;
    }
}
