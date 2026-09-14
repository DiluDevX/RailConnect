package lk.sliit.railconnect.features;

import static org.assertj.core.api.Assertions.assertThat;

import lk.sliit.railconnect.auth.domain.User;
import lk.sliit.railconnect.auth.domain.UserRole;
import lk.sliit.railconnect.auth.repository.UserRepository;
import lk.sliit.railconnect.features.carriage.domain.Carriage;
import lk.sliit.railconnect.features.carriage.domain.CarriageClass;
import lk.sliit.railconnect.features.carriage.dto.CarriageForm;
import lk.sliit.railconnect.features.carriage.service.CarriageService;
import lk.sliit.railconnect.features.complaint.domain.Complaint;
import lk.sliit.railconnect.features.complaint.domain.ComplaintStatus;
import lk.sliit.railconnect.features.complaint.domain.ComplaintType;
import lk.sliit.railconnect.features.complaint.dto.ComplaintForm;
import lk.sliit.railconnect.features.complaint.service.ComplaintService;
import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.features.train.domain.TrainStatus;
import lk.sliit.railconnect.features.train.dto.TrainForm;
import lk.sliit.railconnect.features.train.service.TrainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ModuleCrudIntegrationTest {
    @Autowired TrainService trainService;
    @Autowired CarriageService carriageService;
    @Autowired ComplaintService complaintService;
    @Autowired UserRepository userRepository;

    @Test
    void trainCanBeCreatedUpdatedReadAndDeactivated() {
        TrainForm createForm = trainForm("T-CRUD", "Original Name");
        Train created = trainService.create(createForm);

        TrainForm updateForm = trainForm("T-CRUD", "Updated Name");
        updateForm.setStatus(TrainStatus.ACTIVE);
        trainService.update(created.getId(), updateForm);
        trainService.toggleActive(created.getId());

        Train result = trainService.require(created.getId());
        assertThat(result.getTrainName()).isEqualTo("Updated Name");
        assertThat(result.getStatus()).isEqualTo(TrainStatus.INACTIVE);
    }

    @Test
    void carriageCreationGeneratesItsPhysicalSeats() {
        Train train = trainService.create(trainForm("T-SEATS", "Seat Test"));
        CarriageForm form = new CarriageForm();
        form.setTrainId(train.getId());
        form.setCarriageNumber("A01");
        form.setClassType(CarriageClass.SECOND);
        form.setCapacity(8);

        Carriage carriage = carriageService.create(form);

        assertThat(carriageService.seats(carriage.getId())).hasSize(8);
        assertThat(trainService.activeCapacity(train.getId())).isEqualTo(8);
    }

    @Test
    void complaintCanBeCreatedUpdatedAndResolved() {
        User passenger = userRepository.save(new User(
                "Complaint Tester", "complaint-test@example.com", "encoded", "0770000000", UserRole.PASSENGER));
        ComplaintForm createForm = complaintForm("Original subject", "Original description");
        Complaint complaint = complaintService.create(passenger, createForm);

        ComplaintForm updateForm = complaintForm("Updated subject", "Updated description");
        complaintService.updateByPassenger(complaint.getId(), passenger, updateForm);
        complaintService.respond(complaint.getId(), ComplaintStatus.RESOLVED, "Issue checked and resolved.");

        Complaint result = complaintService.require(complaint.getId());
        assertThat(result.getSubject()).isEqualTo("Updated subject");
        assertThat(result.getStatus()).isEqualTo(ComplaintStatus.RESOLVED);
        assertThat(result.getAdminResponse()).isEqualTo("Issue checked and resolved.");
    }

    private TrainForm trainForm(String number, String name) {
        TrainForm form = new TrainForm();
        form.setTrainNumber(number);
        form.setTrainName(name);
        form.setDescription("Integration test record");
        return form;
    }

    private ComplaintForm complaintForm(String subject, String description) {
        ComplaintForm form = new ComplaintForm();
        form.setComplaintType(ComplaintType.SERVICE);
        form.setSubject(subject);
        form.setDescription(description);
        return form;
    }
}
