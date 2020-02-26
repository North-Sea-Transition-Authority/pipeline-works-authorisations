package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.pwa.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwa.huoo.ApplicationHolderOrganisation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaHolderForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.huoo.ApplicationHolderOrganisationRepository;

@Service
public class ApplicationHolderService {

  private final ApplicationHolderOrganisationRepository applicationHolderOrganisationRepository;

  @Autowired
  public ApplicationHolderService(ApplicationHolderOrganisationRepository applicationHolderOrganisationRepository) {
    this.applicationHolderOrganisationRepository = applicationHolderOrganisationRepository;
  }

  /**
   * Clear existing holder information for an application detail, then add new.
   */
  @Transactional
  public void updateHolderDetails(PwaApplicationDetail detail, PortalOrganisationUnit organisationUnit) {

    // clear out any pre-existing data (legacy apps could have multiple holders)
    applicationHolderOrganisationRepository
        .deleteAll(applicationHolderOrganisationRepository.findByPwaApplicationDetail(detail));

    var holder = new ApplicationHolderOrganisation(detail, organisationUnit);
    applicationHolderOrganisationRepository.save(holder);

  }

  public PwaHolderForm mapHolderDetailsToForm(PwaApplicationDetail detail) {
    var form = new PwaHolderForm();
    // clear out any pre-existing data (legacy apps could have multiple holders)
    form.setHolderOuId(
        applicationHolderOrganisationRepository.findByPwaApplicationDetail(detail)
            .stream()
            .findFirst()
            .map(appHolderOrganisation -> appHolderOrganisation.getOrganisationUnit().getOuId())
        .orElse(null)
    );

    return form;
  }

}
