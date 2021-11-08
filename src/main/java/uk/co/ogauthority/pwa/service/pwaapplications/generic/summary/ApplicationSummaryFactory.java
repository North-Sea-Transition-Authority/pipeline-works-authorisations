package uk.co.ogauthority.pwa.service.pwaapplications.generic.summary;

import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * Construct summary data objects for pwa applications.
 * */
@Service
public class ApplicationSummaryFactory {


  private final PersonService personService;

  @Autowired
  public ApplicationSummaryFactory(PersonService personService) {
    this.personService = personService;
  }

  public ApplicationSubmissionSummary createSubmissionSummary(PwaApplicationDetail detail) {
    var submittedBy = personService.getPersonById(detail.getSubmittedByPersonId()).getFullName();
    var submittedDateTime = detail.getSubmittedTimestamp().atZone(ZoneId.systemDefault()).toLocalDateTime();
    return new ApplicationSubmissionSummary(
        detail.getPwaApplication(),
        detail.isFirstVersion(),
        submittedDateTime,
        submittedBy);
  }
}
