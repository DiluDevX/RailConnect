package lk.sliit.railconnect.features.route;

import lk.sliit.railconnect.features.route.dto.RouteForm;
import lk.sliit.railconnect.features.route.service.RouteService;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class RouteServiceIntegrationTest {
    @Autowired RouteService routeService;

    @Test
    void rejectsIdenticalStartAndEndStation() {
        RouteForm form = new RouteForm();
        form.setRouteCode("R-SAME");
        form.setStartStation("Colombo Fort");
        form.setEndStation("colombo fort");
        form.setDistanceKm(new BigDecimal("1"));

        assertThatThrownBy(() -> routeService.create(form))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("different");
    }
}
