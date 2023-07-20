package uk.co.ogauthority.pwa.features.application.tasks.projectinfo.controller;

import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsLicenceService;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchItem;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchResult;

@RestController()
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/project-information")
public class ProjectInfoLicenseInfoRestController {

  private final PearsLicenceService pearsLicenceService;

  public ProjectInfoLicenseInfoRestController(PearsLicenceService pearsLicenceService) {
    this.pearsLicenceService = pearsLicenceService;
  }

  @GetMapping("/licenses")
  public RestSearchResult getLicenses(
      @RequestParam(value = "term", required = false) String searchTerm) {
    var licenses = pearsLicenceService.getLicencesByName(searchTerm);

    var restSearchItems = licenses.stream()
        .map(licence -> new RestSearchItem(
            String.valueOf(licence.getMasterId()),
            licence.getLicenceName())
        ).collect(Collectors.toList());

    return new RestSearchResult(restSearchItems);
  }
}
