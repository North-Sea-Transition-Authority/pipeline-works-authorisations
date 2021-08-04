package uk.co.ogauthority.pwa.service.asbuilt;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltDeadlineReminderType;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupDetailRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;

/**
 * Perform database interactions for as--built notification group deadline changes.
 */
@Component
class AsBuiltGroupDeadlineService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsBuiltGroupDeadlineService.class);

  private final AsBuiltNotificationGroupDetailRepository asBuiltNotificationGroupDetailRepository;
  private final AsBuiltNotificationGroupStatusService asBuiltNotificationGroupStatusService;
  private final PwaHolderTeamService pwaHolderTeamService;
  private final PwaHolderService pwaHolderService;
  private final AsBuiltNotificationEmailService asBuiltNotificationEmailService;
  private final Clock clock;

  @Autowired
  AsBuiltGroupDeadlineService(
      AsBuiltNotificationGroupDetailRepository asBuiltNotificationGroupDetailRepository,
      AsBuiltNotificationGroupStatusService asBuiltNotificationGroupStatusService,
      PwaHolderTeamService pwaHolderTeamService,
      PwaHolderService pwaHolderService,
      AsBuiltNotificationEmailService asBuiltNotificationEmailService,
      @Qualifier("utcClock") Clock clock) {
    this.asBuiltNotificationGroupDetailRepository = asBuiltNotificationGroupDetailRepository;
    this.asBuiltNotificationGroupStatusService = asBuiltNotificationGroupStatusService;
    this.pwaHolderTeamService = pwaHolderTeamService;
    this.pwaHolderService = pwaHolderService;
    this.asBuiltNotificationEmailService = asBuiltNotificationEmailService;
    this.clock = clock;
  }

  void setNewDeadline(AsBuiltNotificationGroup group, LocalDate deadline, Person person) {

    var personId = person.getId();
    var instant = clock.instant();
    asBuiltNotificationGroupDetailRepository.findByAsBuiltNotificationGroupAndEndedTimestampIsNull(group)
        .ifPresent(asBuiltNotificationGroupDetail -> {
          asBuiltNotificationGroupDetail.setEndedByPersonId(personId);
          asBuiltNotificationGroupDetail.setEndedTimestamp(instant);

          asBuiltNotificationGroupDetailRepository.save(asBuiltNotificationGroupDetail);
        });

    var newGroupDetail = new AsBuiltNotificationGroupDetail(group, deadline, personId, instant);

    asBuiltNotificationGroupDetailRepository.save(newGroupDetail);
  }

  /**
   * Send deadline reminder emails for all as-built notification groups that have an upcoming or overdue deadline
   * to people in holder teams that are as-built submitters.
   */
  void notifyHoldersOfAsBuiltGroupDeadlines() {
    var nonCompleteGroups = asBuiltNotificationGroupStatusService
        .getAllNonCompleteAsBuiltNotificationGroups();
    notifyHoldersOfUpcomingAsBuiltGroupDeadlines(nonCompleteGroups);
    notifyHoldersOfPastAsBuiltGroupDeadlines(nonCompleteGroups);
  }

  private void notifyHoldersOfUpcomingAsBuiltGroupDeadlines(List<AsBuiltNotificationGroup> asBuiltNotificationGroups) {
    var groupsWithUpcomingDeadline = getAsBuiltNotificationGroupsFromDeadline(asBuiltNotificationGroups, LocalDate.now().plusWeeks(1));
    if (!groupsWithUpcomingDeadline.isEmpty()) {
      getHoldersAndSendDeadlineReminderEmails(groupsWithUpcomingDeadline, AsBuiltDeadlineReminderType.DEADLINE_UPCOMING);
    }
  }

  private void notifyHoldersOfPastAsBuiltGroupDeadlines(List<AsBuiltNotificationGroup> asBuiltNotificationGroups) {
    var groupsWithPastDeadline = getAsBuiltNotificationGroupsFromDeadline(asBuiltNotificationGroups,
        LocalDate.now().minusDays(1));
    if (!groupsWithPastDeadline.isEmpty()) {
      getHoldersAndSendDeadlineReminderEmails(groupsWithPastDeadline, AsBuiltDeadlineReminderType.DEADLINE_PASSED);
    }
  }

  //linking holder organisations to respective as-built notification groups through holder-master pwa relationship using guava MultiMap
  private void getHoldersAndSendDeadlineReminderEmails(List<AsBuiltNotificationGroup> asBuiltNotificationGroups,
                                                                   AsBuiltDeadlineReminderType asBuiltDeadlineReminderType) {
    var masterPwaIdToAsBuiltNotificationGroupMultiMap = getMasterPwaIdToAsBuiltNotificationGroupMultiMap(asBuiltNotificationGroups);

    var holderOrgGroupToMasterPwaIdMultiMap = pwaHolderService.getHolderOrgGroupsForMasterPwaIds(
        masterPwaIdToAsBuiltNotificationGroupMultiMap.keySet());

    var holderToAsBuiltNotificationGroupMultiMap = getHolderToAsBuiltNotificationGroupMultiMap(
        holderOrgGroupToMasterPwaIdMultiMap, masterPwaIdToAsBuiltNotificationGroupMultiMap);

    sendDeadlineEmailToEveryHolderOrganisation(holderToAsBuiltNotificationGroupMultiMap, asBuiltDeadlineReminderType);
  }

  private void sendDeadlineEmailToEveryHolderOrganisation(Multimap<PortalOrganisationGroup, AsBuiltNotificationGroup>
                                                              holderToAsBuiltNotificationGroupMultiMap,
                                                          AsBuiltDeadlineReminderType deadlineReminderType) {
    holderToAsBuiltNotificationGroupMultiMap.asMap().forEach((orgGroup, asBuiltNotificationGroupList) -> {

      var allAsBuiltSubmitters = pwaHolderTeamService.getPeopleWithHolderTeamRoleForOrgGroup(orgGroup,
          PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER);

      sendDeadlineEmailToHolderOrgPeople(allAsBuiltSubmitters, new ArrayList<>(asBuiltNotificationGroupList), deadlineReminderType);
      LOGGER.info("Sent {} emails to {}", deadlineReminderType.getDeadlineTypeText(), allAsBuiltSubmitters.stream()
              .map(Person::getEmailAddress)
              .collect(Collectors.joining(", ")));

    });
  }

  private void sendDeadlineEmailToHolderOrgPeople(Set<Person> holderOrgPeople, List<AsBuiltNotificationGroup> asBuiltNotificationGroups,
                                                  AsBuiltDeadlineReminderType asBuiltDeadlineReminderType) {
    holderOrgPeople.forEach(person -> {

      var asBuiltGroupReferencesString = asBuiltNotificationGroups.stream()
          .map(AsBuiltNotificationGroup::getReference)
          .sorted()
          .collect(Collectors.joining(", "));
      if (asBuiltDeadlineReminderType == AsBuiltDeadlineReminderType.DEADLINE_UPCOMING) {
        asBuiltNotificationEmailService.sendUpcomingDeadlineEmail(person.getEmailAddress(), person.getFullName(),
            asBuiltGroupReferencesString);
      } else {
        asBuiltNotificationEmailService.sendDeadlinePassedEmail(person.getEmailAddress(), person.getFullName(),
            asBuiltGroupReferencesString);
      }

    });
  }

  private Multimap<Integer, AsBuiltNotificationGroup> getMasterPwaIdToAsBuiltNotificationGroupMultiMap(List<AsBuiltNotificationGroup>
                                                                                                           asBuiltNotificationGroups) {
    var consents = asBuiltNotificationGroups.stream()
        .map(AsBuiltNotificationGroup::getPwaConsent)
        .collect(Collectors.toList());
    Multimap<Integer, AsBuiltNotificationGroup> masterPwaToAsBuiltNotificationGroupMultiMap = ArrayListMultimap.create();
    consents.forEach(pwaConsent -> masterPwaToAsBuiltNotificationGroupMultiMap.put(
        pwaConsent.getMasterPwa().getId(), asBuiltNotificationGroups.stream()
            .filter(group -> group.getPwaConsent().equals(pwaConsent))
            .findFirst().get()));
    return masterPwaToAsBuiltNotificationGroupMultiMap;
  }

  private Multimap<PortalOrganisationGroup, AsBuiltNotificationGroup> getHolderToAsBuiltNotificationGroupMultiMap(
      Multimap<PortalOrganisationGroup, Integer> holderOrgGroupToMasterPwaIdMultiMap,
      Multimap<Integer, AsBuiltNotificationGroup> masterPwaIdToAsBuiltNotificationGroupMultiMap) {
    Multimap<PortalOrganisationGroup, AsBuiltNotificationGroup> holderToAsBuiltNotificationGroupMultiMap = ArrayListMultimap.create();
    holderOrgGroupToMasterPwaIdMultiMap.forEach((group, pwaId) ->
        masterPwaIdToAsBuiltNotificationGroupMultiMap.get(pwaId)
            .forEach(pwaConsent -> holderToAsBuiltNotificationGroupMultiMap.put(group, pwaConsent))
    );
    return holderToAsBuiltNotificationGroupMultiMap;
  }

  private List<AsBuiltNotificationGroup> getAsBuiltNotificationGroupsFromDeadline(List<AsBuiltNotificationGroup> asBuiltNotificationGroups,
                                                                                  LocalDate deadline) {
    return asBuiltNotificationGroupDetailRepository
        .findAllByAsBuiltNotificationGroupInAndDeadlineDate(asBuiltNotificationGroups, deadline).stream()
        .map(AsBuiltNotificationGroupDetail::getAsBuiltNotificationGroup)
        .collect(Collectors.toList());
  }

}
