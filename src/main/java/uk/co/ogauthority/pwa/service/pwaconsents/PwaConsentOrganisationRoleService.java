package uk.co.ogauthority.pwa.service.pwaconsents;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Multimap;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates.OrganisationRolePipelineGroupDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates.OrganisationRolesSummaryDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrgRoleInstanceType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.aggregates.AllOrgRolePipelineGroupsView;
import uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.aggregates.OrganisationRolePipelineGroupView;
import uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.model.PipelineNumbersAndSplits;
import uk.co.ogauthority.pwa.features.generalcase.pipelinehuooview.PipelineNumberAndSplitsService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentOrganisationRoleRepository;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentPipelineOrganisationRoleLinkRepository;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentRepository;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

@Service
public class PwaConsentOrganisationRoleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PwaConsentOrganisationRoleService.class);

  private final PwaConsentOrganisationRoleRepository pwaConsentOrganisationRoleRepository;
  private final PwaConsentPipelineOrganisationRoleLinkRepository pwaConsentPipelineOrganisationRoleLinkRepository;
  private final PipelineNumberAndSplitsService pipelineNumberAndSplitsService;
  private final PipelineDetailService pipelineDetailService;
  private final PwaConsentRepository pwaConsentRepository;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final Clock clock;

  @Autowired
  public PwaConsentOrganisationRoleService(
      PwaConsentOrganisationRoleRepository pwaConsentOrganisationRoleRepository,
      PwaConsentPipelineOrganisationRoleLinkRepository pwaConsentPipelineOrganisationRoleLinkRepository,
      PipelineNumberAndSplitsService pipelineNumberAndSplitsService,
      PipelineDetailService pipelineDetailService,
      PwaConsentRepository pwaConsentRepository,
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      @Qualifier("utcClock") Clock clock) {
    this.pwaConsentOrganisationRoleRepository = pwaConsentOrganisationRoleRepository;
    this.pwaConsentPipelineOrganisationRoleLinkRepository = pwaConsentPipelineOrganisationRoleLinkRepository;
    this.pipelineNumberAndSplitsService = pipelineNumberAndSplitsService;
    this.pipelineDetailService = pipelineDetailService;
    this.pwaConsentRepository = pwaConsentRepository;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.clock = clock;
  }


  /**
   * This will return consents where one of the given organisation units was added as a Holder.
   * This is not necessarily the same as the latest consent of a PWA as the Holder is still the holder
   * if later consents add/replace other holders.
   */
  public Set<PwaConsent> getPwaConsentsWhereCurrentHolderWasAdded(
      Collection<PortalOrganisationUnit> organisationUnits) {
    var orgUnitIds = organisationUnits.stream()
        .map(PortalOrganisationUnit::getOuId).collect(Collectors.toSet());
    return pwaConsentOrganisationRoleRepository.findByOrganisationUnitIdInAndRoleInAndEndTimestampIsNull(
        orgUnitIds,
        EnumSet.of(HuooRole.HOLDER))
        .stream()
        .map(PwaConsentOrganisationRole::getAddedByPwaConsent)
        .collect(Collectors.toSet());
  }


  public Set<MasterPwaHolderDto> getCurrentConsentedHoldersOrgRolesForMasterPwa(MasterPwa masterPwa) {
    var pwaConsents = pwaConsentRepository.findByMasterPwa(masterPwa);
    var activeHolders = pwaConsentOrganisationRoleRepository.findByAddedByPwaConsentInAndRoleInAndEndTimestampIsNull(
        pwaConsents,
        Set.of(HuooRole.HOLDER));

    var distinctHolderOrgUnitIds = activeHolders.stream()
        .map(PwaConsentOrganisationRole::getOrganisationUnitId)
        .collect(Collectors.toSet());

    var holderOrganisationUnitsLookup = portalOrganisationsAccessor.getOrganisationUnitsByIdIn(distinctHolderOrgUnitIds)
        .stream()
        .collect(Collectors.toMap(PortalOrganisationUnit::getOuId, ou -> ou));

    var masterPwaHolders = new HashSet<MasterPwaHolderDto>();
    for (PwaConsentOrganisationRole holderRole : activeHolders) {
      if (holderOrganisationUnitsLookup.containsKey(holderRole.getOrganisationUnitId())) {
        masterPwaHolders.add(new MasterPwaHolderDto(
            holderOrganisationUnitsLookup.get(holderRole.getOrganisationUnitId()),
            holderRole.getAddedByPwaConsent()
        ));
      } else {
        LOGGER.debug(
            String.format(
                "Could not reconcile all holders with current org unit. MasterPwaId: %s pwaConsentOrgRoleId: %s",
                masterPwa.getId(),
                holderRole.getId()
            )
        );
      }
    }

    return masterPwaHolders;

  }


  public OrganisationRolesSummaryDto getActiveOrganisationRoleSummaryForSeabedPipelines(MasterPwa masterPwa) {

    var allOrganisationPipelineRoles = pwaConsentPipelineOrganisationRoleLinkRepository
        .findActiveOrganisationPipelineRolesByMasterPwa(masterPwa);

    return OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(allOrganisationPipelineRoles);

  }

  public OrganisationRolesSummaryDto getOrganisationRoleSummaryForConsentsAndPipeline(Collection<PwaConsent> pwaConsents,
                                                                                      Pipeline pipeline) {

    var organisationPipelineRole = pwaConsentPipelineOrganisationRoleLinkRepository
        .findActiveOrganisationPipelineRolesByPwaConsent(pwaConsents, pipeline);

    return OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(organisationPipelineRole);

  }

  public Long getNumberOfHolders(MasterPwa masterPwa) {
    var pwaConsents = pwaConsentRepository.findByMasterPwa(masterPwa);
    return pwaConsentOrganisationRoleRepository.countByAddedByPwaConsentInAndRoleInAndEndTimestampIsNull(
        pwaConsents,
        Set.of(HuooRole.HOLDER));
  }



  private Set<PipelineIdentifier> getPipelineSplitsForRole(Collection<PwaConsent> consents,
                                                           HuooRole huooRole) {

    return pwaConsentPipelineOrganisationRoleLinkRepository
        .findActiveLinksAtTimeOfPwaConsents(consents)
        .stream()
        .filter(orgRoleLink -> orgRoleLink.getRole().equals(huooRole))
        .filter(orgRoleLink -> orgRoleLink.getOrgRoleInstanceType().equals(OrgRoleInstanceType.SPLIT_PIPELINE))
        .map(PwaConsentPipelineOrganisationRoleLink::getPipelineIdentifier)
        .collect(toSet());
  }



  private List<OrganisationRolePipelineGroupView> getOrgRolePipelineGroupView(
      MasterPwa masterPwa,
      Collection<PwaConsent> consents,
      Map<OrganisationUnitId, OrganisationUnitDetailDto> orgUnitDetailsAndIdsMap,
      Set<OrganisationRolePipelineGroupDto> preComputedOrgRolePipelineGroups,
      HuooRole huooRole) {

    var allPipelineSplitInfoForRole = pipelineNumberAndSplitsService.getAllPipelineNumbersAndSplitsRole(
        () -> pipelineDetailService.getAllPipelineOverviewsForMasterPwaMap(masterPwa),
        () -> getPipelineSplitsForRole(consents, huooRole));

    var views = new ArrayList<OrganisationRolePipelineGroupView>();

    preComputedOrgRolePipelineGroups.forEach(orgRolePipelineGroup -> {

      try {
        LOGGER.debug(
            "getOrgRolePipelineGroupView({}) for master pwa id [{}] and orgRolePipelineGroup [{}] for pipeline identifiers [{}]",
            huooRole.name(),
            masterPwa.getId(),
            orgRolePipelineGroup.getManualOrganisationName().orElse(
                String.valueOf(orgRolePipelineGroup.getOrganisationUnitId().asInt())),
            orgRolePipelineGroup.getPipelineIdentifiers().stream().map(PipelineIdentifier::toString).collect(
                Collectors.joining(",")));
      } catch (Exception e) {
        LOGGER.warn("Error logging org role pipeline group view for role {} and master pwa id {}", huooRole.name(), masterPwa.getId());
      }

      var numbersAndSplits = orgRolePipelineGroup.getPipelineIdentifiers().stream()
          .map(allPipelineSplitInfoForRole::get)
          .collect(toList());

      try {
        LOGGER.debug("numbersAndSplits size = {} identifiers = {}",
            numbersAndSplits.size(),
            numbersAndSplits.stream()
                .filter(Objects::nonNull)
                .map(PipelineNumbersAndSplits::toString)
                .collect(Collectors.joining(","))
        );
      } catch (Exception e) {
        LOGGER.warn("Error logging numbersAndSplits object for role {} and master pwa id {}", huooRole.name(), masterPwa.getId());
      }

      var orgRolePipelineGroupView = new OrganisationRolePipelineGroupView(
          orgRolePipelineGroup.getHuooType(),
          orgUnitDetailsAndIdsMap.get(orgRolePipelineGroup.getOrganisationUnitId()),
          orgRolePipelineGroup.getHuooType().equals(HuooType.PORTAL_ORG)
              && orgUnitDetailsAndIdsMap.get(orgRolePipelineGroup.getOrganisationUnitId()) == null,
          orgRolePipelineGroup.getOrganisationRoleInstanceDto().getManualOrganisationName().orElse(null),
          orgRolePipelineGroup.getOrganisationRoleInstanceDto().getOrganisationRoleOwnerDto().getTreatyAgreement(),
          orgRolePipelineGroup.getOrganisationRoleInstanceDto().getOrganisationRoleOwnerDto(),
          numbersAndSplits);

      views.add(orgRolePipelineGroupView);

    });

    return views;

  }

  private Map<OrganisationUnitId, OrganisationUnitDetailDto> getOrgUnitDetailsAndIdsMap(
      OrganisationRolesSummaryDto organisationRolesSummaryDto) {
    Set<OrganisationRolePipelineGroupDto> allOrgUnitGroups = new HashSet<>();
    allOrgUnitGroups.addAll(organisationRolesSummaryDto.getHolderOrganisationUnitGroups());
    allOrgUnitGroups.addAll(organisationRolesSummaryDto.getUserOrganisationUnitGroups());
    allOrgUnitGroups.addAll(organisationRolesSummaryDto.getOperatorOrganisationUnitGroups());
    allOrgUnitGroups.addAll(organisationRolesSummaryDto.getOwnerOrganisationUnitGroups());

    Set<OrganisationUnitId> orgUnitIds = allOrgUnitGroups.stream()
        .map(OrganisationRolePipelineGroupDto::getOrganisationUnitId)
        .collect(Collectors.toSet());
    return portalOrganisationsAccessor.getOrganisationUnitDetailDtosByOrganisationUnitId(orgUnitIds).stream()
        .collect(toMap(OrganisationUnitDetailDto::getOrganisationUnitId, Function.identity()));
  }

  public AllOrgRolePipelineGroupsView getAllOrganisationRolePipelineGroupView(MasterPwa masterPwa) {

    var orgRolesSummaryDto = getActiveOrganisationRoleSummaryForSeabedPipelines(masterPwa);
    var pwaConsents = pwaConsentRepository.findByMasterPwa(masterPwa);
    return getAllOrganisationRolePipelineGroupView(masterPwa, pwaConsents, orgRolesSummaryDto);

  }

  public AllOrgRolePipelineGroupsView getAllOrganisationRolePipelineGroupView(MasterPwa masterPwa,
                                                                              OrganisationRolesSummaryDto orgRolesSummaryDto) {

    var pwaConsents = pwaConsentRepository.findByMasterPwa(masterPwa);
    return getAllOrganisationRolePipelineGroupView(masterPwa, pwaConsents, orgRolesSummaryDto);

  }

  public AllOrgRolePipelineGroupsView getAllOrganisationRolePipelineGroupView(MasterPwa masterPwa,
                                                                              Collection<PwaConsent> consents,
                                                                              OrganisationRolesSummaryDto orgRolesSummaryDto) {

    Comparator<OrganisationRolePipelineGroupView> viewComparator =
        Comparator.comparing(OrganisationRolePipelineGroupView::getCompanyName,  Comparator.nullsLast(Comparator.naturalOrder()));

    Map<OrganisationUnitId, OrganisationUnitDetailDto> orgUnitDetailsAndIdsMap = getOrgUnitDetailsAndIdsMap(orgRolesSummaryDto);

    Set<OrganisationRolePipelineGroupDto> holderOrgUnitGroups = new HashSet<>();
    holderOrgUnitGroups.addAll(orgRolesSummaryDto.getHolderOrganisationUnitGroups());
    holderOrgUnitGroups.addAll(orgRolesSummaryDto.getHolderNonPortalOrgRoleGroups());
    List<OrganisationRolePipelineGroupView> holderOrgRolePipelineGroups = getOrgRolePipelineGroupView(
        masterPwa,
        consents,
        orgUnitDetailsAndIdsMap,
        holderOrgUnitGroups,
        HuooRole.HOLDER
    );
    holderOrgRolePipelineGroups.sort(viewComparator);

    Set<OrganisationRolePipelineGroupDto> userOrgUnitGroups = new HashSet<>();
    userOrgUnitGroups.addAll(orgRolesSummaryDto.getUserOrganisationUnitGroups());
    userOrgUnitGroups.addAll(orgRolesSummaryDto.getUserNonPortalOrgRoleGroups());
    List<OrganisationRolePipelineGroupView> userOrgRolePipelineGroups = getOrgRolePipelineGroupView(
        masterPwa,
        consents,
        orgUnitDetailsAndIdsMap,
        userOrgUnitGroups,
        HuooRole.USER
    );
    userOrgRolePipelineGroups.sort(viewComparator);

    Set<OrganisationRolePipelineGroupDto> operatorOrgUnitGroups = new HashSet<>();
    operatorOrgUnitGroups.addAll(orgRolesSummaryDto.getOperatorOrganisationUnitGroups());
    operatorOrgUnitGroups.addAll(orgRolesSummaryDto.getOperatorNonPortalOrgRoleGroups());
    List<OrganisationRolePipelineGroupView> operatorOrgRolePipelineGroups = getOrgRolePipelineGroupView(
        masterPwa,
        consents,
        orgUnitDetailsAndIdsMap,
        operatorOrgUnitGroups,
        HuooRole.OPERATOR
    );
    operatorOrgRolePipelineGroups.sort(viewComparator);

    Set<OrganisationRolePipelineGroupDto> ownerOrgUnitGroups = new HashSet<>();
    ownerOrgUnitGroups.addAll(orgRolesSummaryDto.getOwnerOrganisationUnitGroups());
    ownerOrgUnitGroups.addAll(orgRolesSummaryDto.getOwnerNonPortalOrgRoleGroups());
    List<OrganisationRolePipelineGroupView> ownerOrgRolePipelineGroups = getOrgRolePipelineGroupView(
        masterPwa,
        consents,
        orgUnitDetailsAndIdsMap,
        ownerOrgUnitGroups,
        HuooRole.OWNER
    );
    ownerOrgRolePipelineGroups.sort(viewComparator);

    return new AllOrgRolePipelineGroupsView(
        holderOrgRolePipelineGroups,
        userOrgRolePipelineGroups,
        operatorOrgRolePipelineGroups,
        ownerOrgRolePipelineGroups
    );
  }

  public List<PwaConsentOrganisationRole> getActiveOrgRolesAddedByConsents(Collection<PwaConsent> consents) {
    return pwaConsentOrganisationRoleRepository.findByAddedByPwaConsentInAndEndTimestampIsNull(consents);
  }

  public void endConsentOrgRoles(PwaConsent endingConsent,
                                 Collection<PwaConsentOrganisationRole> consentRolesToEnd) {

    consentRolesToEnd.forEach(role -> {
      role.setEndedByPwaConsent(endingConsent);
      role.setEndTimestamp(clock.instant());
    });

    pwaConsentOrganisationRoleRepository.saveAll(consentRolesToEnd);

  }

  public List<PwaConsentOrganisationRole> createNewConsentOrgUnitRoles(PwaConsent pwaConsent,
                                           Multimap<OrganisationUnitId, HuooRole> consentRolesToAdd) {

    var newOrgRoles = consentRolesToAdd.entries().stream()
        .map(entry -> PwaConsentOrganisationRole.createOrgUnitRole(pwaConsent, entry.getValue(), entry.getKey().asInt(), clock.instant()))
        .collect(Collectors.toList());

    pwaConsentOrganisationRoleRepository.saveAll(newOrgRoles);

    return newOrgRoles;

  }

  public List<PwaConsentOrganisationRole> createNewConsentTreatyRoles(PwaConsent pwaConsent,
                                          Multimap<TreatyAgreement, HuooRole> consentRolesToAdd) {

    var newOrgRoles = consentRolesToAdd.entries().stream()
        .map(entry -> PwaConsentOrganisationRole.createTreatyAgreementRole(pwaConsent, entry.getValue(), entry.getKey(), clock.instant()))
        .collect(Collectors.toList());

    pwaConsentOrganisationRoleRepository.saveAll(newOrgRoles);

    return newOrgRoles;

  }

}
