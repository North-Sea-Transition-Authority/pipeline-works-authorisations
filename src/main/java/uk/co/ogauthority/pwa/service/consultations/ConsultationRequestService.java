package uk.co.ogauthority.pwa.service.consultations;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Service
public class ConsultationRequestService {


  private final ConsulteeGroupTeamService consulteeGroupTeamService;

  @Autowired
  public ConsultationRequestService(ConsulteeGroupTeamService consulteeGroupTeamService) {
    this.consulteeGroupTeamService = consulteeGroupTeamService;
  }


  public List<ConsulteeGroupDetail> getConsulteeGroups(AuthenticatedUserAccount user) {
    var consulteeGroupDetails = consulteeGroupTeamService.getManageableGroupDetailsForUser(user);
    //to do: ensure only groups that don’t have an open consultation request are shown

    return consulteeGroupDetails;
  }


  public BindingResult validate(ConsultationRequestForm form, BindingResult bindingResult, ValidationType validationType, PwaApplicationDetail applicationDetail) {
    return bindingResult;
  }
}
