package lk.sliit.railconnect.features.train;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lk.sliit.railconnect.features.train.domain.Maintenance;
import lk.sliit.railconnect.features.train.domain.MaintenanceStatus;
import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.features.train.domain.TrainStatus;
import lk.sliit.railconnect.features.train.dto.MaintenanceForm;
import lk.sliit.railconnect.features.train.dto.TrainForm;
import lk.sliit.railconnect.features.train.service.MaintenanceService;
import lk.sliit.railconnect.features.train.service.TrainService;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@SpringBootTest
@Transactional
class MaintenanceServiceIntegrationTest {
    @Autowired MaintenanceService maintenanceService;
    @Autowired TrainService trainService;

    @Test
    void trainNumberMustBeFourDigitsAndCannotBeReused() {
        trainService.create(trainForm("1703", "First Number"));

        assertThatThrownBy(() -> trainService.create(trainForm("12", "Too Short")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("4 digits");
        assertThatThrownBy(() -> trainService.create(trainForm("1703", "Repeated Number")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cannot be repeated");
    }

    @Test
    void maintenanceCanBeRecordedWithoutChangingTrainStatus() {
        Train train = trainService.create(trainForm("1701", "Maintenance Test"));
        Maintenance saved = maintenanceService.create(form(train.getId(), "Engine service", MaintenanceStatus.IN_PROGRESS));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("Routine inspection");
        assertThat(trainService.require(train.getId()).getStatus()).isEqualTo(TrainStatus.ACTIVE);
        assertThat(maintenanceService.historyForTrain(train.getId())).hasSize(1);
    }

    @Test
    void completedMaintenanceCannotBeCancelledAndOnlyScheduledRecordsCanBeDeleted() {
        Train train = trainService.create(trainForm("1702", "Rule Test"));
        trainService.toggleActive(train.getId());
        Maintenance completed = maintenanceService.create(form(train.getId(), "Historical inspection", MaintenanceStatus.COMPLETED));
        Maintenance scheduled = maintenanceService.create(form(train.getId(), "Future service", MaintenanceStatus.SCHEDULED));

        assertThat(trainService.require(train.getId()).getStatus()).isEqualTo(TrainStatus.INACTIVE);
        assertThatThrownBy(() -> maintenanceService.cancel(completed.getId()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Completed maintenance cannot be cancelled.");
        assertThatThrownBy(() -> maintenanceService.deleteScheduled(completed.getId()))
                .isInstanceOf(BusinessRuleException.class);

        maintenanceService.deleteScheduled(scheduled.getId());
        assertThat(maintenanceService.historyForTrain(train.getId()))
                .extracting(Maintenance::getMaintenanceType)
                .containsExactly("Historical inspection");
    }

    private TrainForm trainForm(String number, String name) {
        TrainForm form = new TrainForm();
        form.setTrainNumber(number);
        form.setTrainName(name);
        form.setDescription("Maintenance integration record");
        return form;
    }

    private MaintenanceForm form(Long trainId, String type, MaintenanceStatus status) {
        MaintenanceForm form = new MaintenanceForm();
        form.setTrainId(trainId);
        form.setMaintenanceType(type);
        form.setMaintenanceDate(LocalDate.now());
        form.setDescription("Routine inspection");
        form.setStatus(status);
        return form;
    }
}
