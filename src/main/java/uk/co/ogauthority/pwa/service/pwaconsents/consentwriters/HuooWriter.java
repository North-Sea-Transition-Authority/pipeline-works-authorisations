package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.ConsentWriterDto;

@Service
public class HuooWriter implements ConsentWriter {

  private final PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;
  private final PadOrganisationRoleService padOrganisationRoleService;
  private final PwaConsentService pwaConsentService;

  @Autowired
  public HuooWriter(PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService,
                    PadOrganisationRoleService padOrganisationRoleService,
                    PwaConsentService pwaConsentService) {
    this.pwaConsentOrganisationRoleService = pwaConsentOrganisationRoleService;
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.pwaConsentService = pwaConsentService;
  }

  @Override
  public int getExecutionOrder() {
    return 10;
  }

  @Override
  public boolean writerIsApplicable(Collection<ApplicationTask> applicationTaskSet, PwaConsent pwaConsent) {
    return applicationTaskSet.contains(ApplicationTask.HUOO);
  }

  @Override
  public ConsentWriterDto write(PwaApplicationDetail pwaApplicationDetail,
                                PwaConsent pwaConsent,
                                ConsentWriterDto consentWriterDto) {

    // get the app roles and separate by type, we always need these
    var appRoles = padOrganisationRoleService.getOrgRolesForDetail(pwaApplicationDetail);

    var appOrgUnitRoles = appRoles.stream()
        .filter(role -> role.getType() == HuooType.PORTAL_ORG)
        .collect(Collectors.groupingBy(r -> new OrganisationUnitId(r.getOrganisationUnit().getOuId()),
            Collectors.mapping(PadOrganisationRole::getRole, Collectors.toSet())));

    var appTreatyRoles = appRoles.stream()
        .filter(role -> role.getType() == HuooType.TREATY_AGREEMENT)
        .collect(Collectors.groupingBy(PadOrganisationRole::getAgreement,
            Collectors.mapping(PadOrganisationRole::getRole, Collectors.toSet())));

    // setup the storage maps to hold the roles that we will want to create later
    Multimap<OrganisationUnitId, HuooRole> orgUnitConsentRolesToAdd = HashMultimap.create();
    Multimap<TreatyAgreement, HuooRole> treatyConsentRolesToAdd = HashMultimap.create();

    if (pwaConsent.getVariationNumber() > 0) {

      // if we're a variation, we need to check the currently consented roles to see what changes need making
      var consents = pwaConsentService.getConsentsByMasterPwa(pwaApplicationDetail.getMasterPwa());
      var consentRoles = pwaConsentOrganisationRoleService.getActiveOrgRolesAddedByConsents(consents);

      var consentRolesToEnd = new ArrayList<PwaConsentOrganisationRole>();

      // any remaining consented org roles that are not associated with an actual org unit should be ended.
      // If needed the app should have added replacements using actual org units.
      var consentedMigratedOrgRoles = consentRoles.stream()
          .filter(role -> role.getType() == HuooType.PORTAL_ORG)
          .filter(role -> role.getMigratedOrganisationName() != null)
          .collect(Collectors.toList());

      consentRolesToEnd.addAll(consentedMigratedOrgRoles);

      // split into org unit and treaty maps
      var consentOrgUnitRoles = consentRoles.stream()
          .filter(role -> role.getType() == HuooType.PORTAL_ORG)
          .filter(role -> role.getOrganisationUnitId() != null)
          .collect(Collectors.groupingBy(r -> new OrganisationUnitId(r.getOrganisationUnitId())));

      var consentTreatyRoles = consentRoles.stream()
          .filter(role -> role.getType() == HuooType.TREATY_AGREEMENT)
          .collect(Collectors.groupingBy(PwaConsentOrganisationRole::getAgreement));

      // process org unit roles
      processRoles(consentOrgUnitRoles, appOrgUnitRoles, orgUnitConsentRolesToAdd, consentRolesToEnd);

      // process treaty roles
      processRoles(consentTreatyRoles, appTreatyRoles, treatyConsentRolesToAdd, consentRolesToEnd);

      // end any roles we no longer need
      pwaConsentOrganisationRoleService.endConsentOrgRoles(pwaConsent, consentRolesToEnd);

      // store ended roles and currently active roles for access by later writers
      consentWriterDto.getActiveConsentRoles().addAll(consentRoles);
      consentWriterDto.getActiveConsentRoles().removeAll(consentRolesToEnd);
      consentWriterDto.setConsentRolesEnded(consentRolesToEnd);

    } else {

      // if we're a new PWA, set up new roles for everything based on the application
      appOrgUnitRoles.forEach(orgUnitConsentRolesToAdd::putAll);
      appTreatyRoles.forEach(treatyConsentRolesToAdd::putAll);

    }

    // create new org and treaty roles
    var orgUnitRoles = pwaConsentOrganisationRoleService
        .createNewConsentOrgUnitRoles(pwaConsent, orgUnitConsentRolesToAdd);

    var treatyRoles = pwaConsentOrganisationRoleService
        .createNewConsentTreatyRoles(pwaConsent, treatyConsentRolesToAdd);

    // store newly added roles for use in later writers
    consentWriterDto.getActiveConsentRoles().addAll(orgUnitRoles);
    consentWriterDto.getActiveConsentRoles().addAll(treatyRoles);
    consentWriterDto.getConsentRolesAdded().addAll(orgUnitRoles);
    consentWriterDto.getConsentRolesAdded().addAll(treatyRoles);

    return consentWriterDto;

  }

  private <T> void processRoles(Map<T, List<PwaConsentOrganisationRole>> consentRoleMap,
                                Map<T, Set<HuooRole>> padRoleMap,
                                Multimap<T, HuooRole> rolesToAddMap,
                                List<PwaConsentOrganisationRole> consentRolesToEnd) {

    // for each consented org role key (either org unit id or treaty)
    consentRoleMap.forEach((keyObject, consentRoles) ->

        // see if they are still available on the app, if they aren't, end all their consented roles
        Optional.ofNullable(padRoleMap.get(keyObject))
            .ifPresentOrElse(padRoles -> {

              // otherwise go through the app roles and see if we need to add/end any specific roles
              var rolesToAdd = processConsentedRolesForUpdate(consentRoles, padRoles, consentRolesToEnd);
              rolesToAdd.forEach(role -> rolesToAddMap.put(keyObject, role));

            }, () -> consentRolesToEnd.addAll(consentRoles))

    );

    // for every new org role key on the app (not already in consented model), add all of their roles
    padRoleMap.entrySet().stream()
        .filter(entry -> consentRoleMap.get(entry.getKey()) == null)
        .forEach(entry -> rolesToAddMap.putAll(entry.getKey(), entry.getValue()));

  }

  private Set<HuooRole> processConsentedRolesForUpdate(List<PwaConsentOrganisationRole> consentOrgRoles,
                                                       Set<HuooRole> padRoles,
                                                       List<PwaConsentOrganisationRole> consentRolesToEnd) {

    // end any consent org roles that aren't in new padroles
    var consentRolesRemovedOnApp = consentOrgRoles.stream()
        .filter(r -> !padRoles.contains(r.getRole()))
        .collect(Collectors.toList());

    consentRolesToEnd.addAll(consentRolesRemovedOnApp);

    // create new consent org roles for roles that are in new padroles but not in consent org roles
    var consentedRolesStillActive = consentOrgRoles.stream()
        .filter(r -> !consentRolesToEnd.contains(r))
        .collect(Collectors.toList());

    // filter out padroles that are already consented and active, return the list of roles to add
    return padRoles.stream()
        .filter(padRole -> consentedRolesStillActive.stream().noneMatch(r -> r.getRole() == padRole))
        .collect(Collectors.toSet());

  }

}
