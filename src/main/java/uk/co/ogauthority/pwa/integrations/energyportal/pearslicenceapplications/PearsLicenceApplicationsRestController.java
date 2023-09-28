package uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchItem;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchResult;

@RestController()
@RequestMapping("/pwa-application/pears")
public class PearsLicenceApplicationsRestController {

  private final PearsLicenceApplicationService pearsLicenceService;

  @Autowired
  public PearsLicenceApplicationsRestController(PearsLicenceApplicationService pearsLicenceService) {
    this.pearsLicenceService = pearsLicenceService;
  }

  @GetMapping("/license-applications")
  public RestSearchResult getApplications(
      @RequestParam(value = "term", required = false) String searchTerm) {
    var applications = pearsLicenceService.getApplicationsByName(searchTerm);

    var restSearchItems = applications.stream()
        .map(application -> new RestSearchItem(
            application.getId(),
            application.getName())
        ).collect(Collectors.toList());

    return new RestSearchResult(restSearchItems);
  }
}
