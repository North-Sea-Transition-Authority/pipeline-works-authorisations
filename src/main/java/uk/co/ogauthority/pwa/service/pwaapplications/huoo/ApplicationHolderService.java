package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.ApplicationHolderOrganisation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaHolderForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.huoo.ApplicationHolderOrganisationRepository;

@Service
public class ApplicationHolderService {

  private final ApplicationHolderOrganisationRepository applicationHolderOrganisationRepository;
  private final PadOrganisationRoleService padOrganisationRoleService;

  @Autowired
  public ApplicationHolderService(ApplicationHolderOrganisationRepository applicationHolderOrganisationRepository,
                                  PadOrganisationRoleService padOrganisationRoleService) {
    this.applicationHolderOrganisationRepository = applicationHolderOrganisationRepository;
    this.padOrganisationRoleService = padOrganisationRoleService;
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

    padOrganisationRoleService.addHolder(detail, organisationUnit);


  }

  // TODO integrate with controller PWA-332
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

  public List<ApplicationHolderOrganisation> getHoldersFromApplicationDetail(PwaApplicationDetail detail) {
    return applicationHolderOrganisationRepository.findByPwaApplicationDetail(detail);
  }

}
