package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.huoo.AddHuooController;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnitDetail;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.form.pwaapplications.huoo.HuooForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.HuooOrganisationUnitRoleView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.HuooTreatyAgreementView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.huoo.PadOrganisationRolesRepository;

@Service
public class PadOrganisationRoleService {

  private final PadOrganisationRolesRepository padOrganisationRolesRepository;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Autowired
  public PadOrganisationRoleService(
      PadOrganisationRolesRepository padOrganisationRolesRepository,
      PortalOrganisationsAccessor portalOrganisationsAccessor) {
    this.padOrganisationRolesRepository = padOrganisationRolesRepository;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
  }

  @Transactional
  public void save(PadOrganisationRole padOrganisationRole) {
    padOrganisationRolesRepository.save(padOrganisationRole);
  }

  public List<PadOrganisationRole> getOrgRolesForDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padOrganisationRolesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
  }

  public PadOrganisationRole getOrganisationRoleById(Integer id) {
    return padOrganisationRolesRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Unable to find PadOrganisationRole with ID: " + id));
  }

  public List<HuooOrganisationUnitRoleView> getHuooOrganisationUnitRoleViews(PwaApplicationDetail detail,
                                                                             List<PadOrganisationRole> padOrganisationRoleList) {

    // filter so we are only looking at portal organisation roles
    var orgRoles = padOrganisationRoleList.stream()
        .filter(orgRole -> orgRole.getType().equals(HuooType.PORTAL_ORG))
        .collect(Collectors.toList());

    // get the org units so that we can query the details for each
    var portalOrgUnits = orgRoles.stream()
        .map(PadOrganisationRole::getOrganisationUnit)
        .collect(Collectors.toList());

    Map<Integer, PortalOrganisationUnitDetail> portalOrgUnitDetails = portalOrganisationsAccessor
        .getOrganisationUnitDetails(portalOrgUnits).stream()
        .collect(Collectors.toMap(PortalOrganisationUnitDetail::getOuId, orgUnitDetail -> orgUnitDetail));

    var holderCount = orgRoles.stream()
        .filter(padOrganisationRole -> padOrganisationRole.getRoles().contains(HuooRole.HOLDER))
        .count();

    return orgRoles.stream()
        .map(orgUnitRole -> {

          PortalOrganisationUnitDetail orgUnitDetail = portalOrgUnitDetails.getOrDefault(orgUnitRole.getOrganisationUnit().getOuId(), null);

          boolean canRemoveOrg = !orgUnitRole.getRoles().contains(HuooRole.HOLDER) || holderCount > 1;

          return new HuooOrganisationUnitRoleView(
              orgUnitDetail,
              orgUnitRole.getRoles(),
              getEditHuooUrl(detail, orgUnitRole),
              canRemoveOrg ? getRemoveHuooUrl(detail, orgUnitRole) : null);

        })
        .sorted()
        .collect(Collectors.toList());

  }

  private String getEditHuooUrl(PwaApplicationDetail detail, PadOrganisationRole orgRole) {
    return ReverseRouter.route(on(AddHuooController.class)
            .renderEditHuoo(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), orgRole.getId(), null, null, null));
  }

  private String getRemoveHuooUrl(PwaApplicationDetail detail, PadOrganisationRole orgRole) {
    return ReverseRouter.route(on(AddHuooController.class)
        .postDeleteHuoo(detail.getPwaApplicationType(), detail.getPwaApplication().getId(), orgRole.getId(), null, null, null, null));
  }

  public List<HuooTreatyAgreementView> getTreatyAgreementViews(PwaApplicationDetail detail,
                                                               List<PadOrganisationRole> padOrganisationRoleList) {
    return padOrganisationRoleList.stream()
        .filter(padOrganisationRole -> padOrganisationRole.getType().equals(HuooType.TREATY_AGREEMENT))
        .map(treatyRole -> new HuooTreatyAgreementView(
            treatyRole,
            getEditHuooUrl(detail, treatyRole),
            getRemoveHuooUrl(detail, treatyRole)))
        .sorted(Comparator.comparing(HuooTreatyAgreementView::getCountry))
        .collect(Collectors.toList());
  }

  /**
   * If the organisation being removed is a holder, return true if there is > 1 holder on the application, false otherwise.
   * If the organisation being removed isn't a holder, return true.
   */
  public boolean canRemoveOrganisationRole(PwaApplicationDetail detail, PadOrganisationRole padOrganisationRole) {
    var padOrgs = getOrgRolesForDetail(detail);
    var holderCount = padOrgs.stream()
        .filter(padOrgRole -> padOrgRole.getRoles().contains(HuooRole.HOLDER))
        .count();
    if (padOrganisationRole.getRoles().contains(HuooRole.HOLDER)) {
      return holderCount != 1;
    }
    return true;
  }

  @Transactional
  public void removeRole(PadOrganisationRole padOrganisationRole) {
    padOrganisationRolesRepository.delete(padOrganisationRole);
  }

  public void createAndSaveEntityUsingForm(PwaApplicationDetail detail, HuooForm form) {
    var role = new PadOrganisationRole();
    role.setPwaApplicationDetail(detail);
    saveEntityUsingForm(role, form);
  }

  public void mapPadOrganisationRoleToForm(PadOrganisationRole padOrganisationRole, HuooForm form) {
    form.setHuooType(padOrganisationRole.getType());
    form.setHuooRoles(padOrganisationRole.getRoles());
    form.setOrganisationUnit(padOrganisationRole.getOrganisationUnit());
    form.setTreatyAgreement(padOrganisationRole.getAgreement());
  }

  @Transactional
  public void saveEntityUsingForm(PadOrganisationRole padOrganisationRole, HuooForm form) {
    padOrganisationRole.setType(form.getHuooType());
    padOrganisationRole.setRoles(form.getHuooRoles());
    if (form.getHuooType().equals(HuooType.PORTAL_ORG)) {
      padOrganisationRole.setAgreement(null);
      padOrganisationRole.setOrganisationUnit(form.getOrganisationUnit());
    } else if (form.getHuooType().equals(HuooType.TREATY_AGREEMENT)) {
      padOrganisationRole.setAgreement(form.getTreatyAgreement());
      padOrganisationRole.setOrganisationUnit(null);
    }
    save(padOrganisationRole);
  }

  @Transactional
  public void addHolder(PwaApplicationDetail detail, PortalOrganisationUnit organisationUnit) {

    var holderRole = new PadOrganisationRole();
    holderRole.setPwaApplicationDetail(detail);
    holderRole.setType(HuooType.PORTAL_ORG);
    holderRole.setRoles(Set.of(HuooRole.HOLDER));
    holderRole.setOrganisationUnit(organisationUnit);
    save(holderRole);

  }
}
