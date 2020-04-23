package uk.co.ogauthority.pwa.service.pwaapplications.generic.summary;

import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.users.UserAccountService;

@Service
public class ApplicationSummaryFactory {


  private final UserAccountService userAccountService;

  @Autowired
  public ApplicationSummaryFactory(UserAccountService userAccountService) {
    this.userAccountService = userAccountService;
  }

  public ApplicationSubmissionSummary createSubmissionSummary(PwaApplicationDetail detail) {
    var submittedBy = userAccountService.getWebUserAccount(detail.getSubmittedByWuaId()).getFullName();
    var submittedDateTime = detail.getSubmittedTimestamp().atZone(ZoneId.systemDefault()).toLocalDateTime();
    return new ApplicationSubmissionSummary(
        detail.getPwaApplication(),
        detail.isFirstVersion(),
        submittedDateTime,
        submittedBy);
  }
}
