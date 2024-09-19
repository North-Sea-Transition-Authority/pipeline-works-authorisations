package uk.co.ogauthority.pwa.service.search.applicationsearch.restrictions;

import static java.util.stream.Collectors.toSet;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.Instant;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication_;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsulteeGroupId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup_;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.PadVersionLookup;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.PadVersionLookup_;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnit;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnit_;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContext;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParameters;

/**
 * Based on the user type, make sure that
 * 1. only 1 row per applications is returned
 * 2. only applications that are visible to specific searcher are returned
 * 3. only the latest version applicable per user type is returned.
 */
@Service
public class RestrictByUserTypePredicateProvider implements ApplicationSearchPredicateProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestrictByUserTypePredicateProvider.class);

  private final EntityManager entityManager;

  @Autowired
  public RestrictByUserTypePredicateProvider(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public boolean doesPredicateApply(ApplicationSearchContext applicationSearchContext,
                                    ApplicationSearchParameters applicationSearchParameters) {
    return true;
  }

  @Override
  public Predicate createPredicate(ApplicationSearchContext applicationSearchContext,
                                   ApplicationSearchParameters applicationSearchParameters,
                                   CriteriaQuery<?> searchCoreQuery,
                                   Root<ApplicationDetailView> searchCoreRoot) {
    Set<UserType> userTypes = applicationSearchContext.getUserTypes();
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    if (userTypes.contains(UserType.OGA) && userTypes.contains(UserType.INDUSTRY)) {
      LOGGER.debug("Using OGA AND INDUSTRY restriction predicate.");
      return cb.and(cb.or(
          createRegulatorUserPredicate(searchCoreQuery, searchCoreRoot),
          createIndustryUserPredicate(applicationSearchContext, searchCoreQuery, searchCoreRoot)
      ));

    } else if (userTypes.contains(UserType.OGA)) {
      LOGGER.debug("Using OGA restriction predicate.");
      return createRegulatorUserPredicate(searchCoreQuery, searchCoreRoot);

    } else if (userTypes.contains(UserType.INDUSTRY)) {
      LOGGER.debug("Using INDUSTRY restriction predicate.");
      return createIndustryUserPredicate(applicationSearchContext, searchCoreQuery, searchCoreRoot);

    } else if (userTypes.contains(UserType.CONSULTEE)) {
      LOGGER.debug("Using CONSULTEE restriction predicate.");
      return createConsulteeUserPredicate(applicationSearchContext, searchCoreQuery, searchCoreRoot);
    }

    throw new IllegalArgumentException(
          String.format("App search does not support user type of %s for wua_id: %s", userTypes, applicationSearchContext.getWuaIdAsInt())
      );

  }

  private Predicate getLastSubmittedVersionPredicate(CriteriaQuery<?> searchCoreQuery,
                                                     Root<ApplicationDetailView> searchCoreRoot) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    Subquery<Integer> subQuery = searchCoreQuery.subquery(Integer.class);
    Root<PadVersionLookup> subRoot = subQuery.from(PadVersionLookup.class);
    // basically a correlated subquery without setting up entity relationships
    subQuery.select(subRoot.get(PadVersionLookup_.LATEST_SUBMITTED_VERSION_NO));
    subQuery.where(
        cb.equal(searchCoreRoot.get(ApplicationDetailView_.PWA_APPLICATION_ID), subRoot.get(PadVersionLookup_.PWA_APPLICATION_ID))
    );

    return cb.equal(searchCoreRoot.get(ApplicationDetailView_.VERSION_NO), subQuery);

  }

  private Predicate getLastSubmittedVersionOrFirstDraftPredicate(CriteriaQuery<?> searchCoreQuery,
                                                                 Root<ApplicationDetailView> searchCoreRoot) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    Subquery<Integer> subQuery = searchCoreQuery.subquery(Integer.class);
    Root<PadVersionLookup> subRoot = subQuery.from(PadVersionLookup.class);
    // basically a correlated subquery without setting up entity relationships
    subQuery.select(cb.coalesce(
        subRoot.get(PadVersionLookup_.LATEST_SUBMITTED_VERSION_NO), subRoot.get(PadVersionLookup_.MAX_DRAFT_VERSION_NO))
    );
    subQuery.where(
        cb.equal(searchCoreRoot.get(ApplicationDetailView_.PWA_APPLICATION_ID), subRoot.get(PadVersionLookup_.PWA_APPLICATION_ID))
    );

    return cb.equal(searchCoreRoot.get(ApplicationDetailView_.VERSION_NO), subQuery);

  }

  private Predicate getLatestSatisfactoryVersionPredicate(CriteriaQuery<?> searchCoreQuery,
                                                          Root<ApplicationDetailView> searchCoreRoot) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    // use instant so we can filter on the last confirmed satisfactory timestamp
    Subquery<Instant> subQuery = searchCoreQuery.subquery(Instant.class);
    Root<PadVersionLookup> subRoot = subQuery.from(PadVersionLookup.class);
    // basically a correlated subquery without setting up entity relationships
    subQuery.select(subRoot.get(PadVersionLookup_.LATEST_CONFIRMED_SATISFACTORY_TIMESTAMP));
    subQuery.where(
        cb.equal(searchCoreRoot.get(ApplicationDetailView_.PWA_APPLICATION_ID), subRoot.get(PadVersionLookup_.PWA_APPLICATION_ID))
    );

    return cb.equal(searchCoreRoot.get(ApplicationDetailView_.PAD_CONFIRMED_SATISFACTORY_TIMESTAMP), subQuery);

  }

  /**
   * Restrict to the last submitted version of all apps only.
   */
  private Predicate createRegulatorUserPredicate(CriteriaQuery<?> searchCoreQuery,
                                                 Root<ApplicationDetailView> searchCoreRoot) {
    return getLastSubmittedVersionPredicate(searchCoreQuery, searchCoreRoot);

  }

  /**
   * Return applications only where the user's consultee group has been consulted and only return the "last accepted" version.
   */
  private Predicate createConsulteeUserPredicate(ApplicationSearchContext applicationSearchContext,
                                                CriteriaQuery<?> searchCoreQuery,
                                                Root<ApplicationDetailView> searchCoreRoot) {

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    var consulteeGroupIds = applicationSearchContext.getConsulteeGroupIds()
        .stream()
        .map(ConsulteeGroupId::asInt)
        .collect(toSet());

    var latestSatisfactoryVersionPredicate = getLatestSatisfactoryVersionPredicate(searchCoreQuery, searchCoreRoot);

    Subquery<Integer> consultedUponAppIdSubQuery = searchCoreQuery.subquery(Integer.class);
    Root<ConsultationRequest> consultationRequestRoot = consultedUponAppIdSubQuery.from(ConsultationRequest.class);
    Join<ConsultationRequest, ConsulteeGroup> requestToGroupJoin = consultationRequestRoot.join(ConsultationRequest_.CONSULTEE_GROUP);
    Join<ConsultationRequest, PwaApplication> requestToApplicationJoin = consultationRequestRoot.join(ConsultationRequest_.PWA_APPLICATION);
    Path<Integer> pwaApplicationId = requestToApplicationJoin.get(PwaApplication_.ID);

    consultedUponAppIdSubQuery.select(pwaApplicationId);
    consultedUponAppIdSubQuery.where(cb.in(requestToGroupJoin.get(ConsulteeGroup_.ID)).value(consulteeGroupIds));

    return cb.and(
        latestSatisfactoryVersionPredicate,
        cb.in(searchCoreRoot.get(ApplicationDetailView_.PWA_APPLICATION_ID)).value(consultedUponAppIdSubQuery)
    );

  }

  /**
   * Return the last submitted version of applications where the user's "holder" team is a Holder of the top level PWA.
   * NB. Holders defined the application should be ignored except when its an INITIAL_PWA application.
   */
  private Predicate createIndustryUserPredicate(ApplicationSearchContext applicationSearchContext,
                                                CriteriaQuery<?> searchCoreQuery,
                                                Root<ApplicationDetailView> searchCoreRoot) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    var lastSubmittedOrFirstDraftVersionPredicate = getLastSubmittedVersionOrFirstDraftPredicate(searchCoreQuery, searchCoreRoot);

    // limit so we only ever see the last submitted version or first draft version
    // and
    // the last submitted/first draft version is for an where HOLDER set to users org
    return cb.and(
        lastSubmittedOrFirstDraftVersionPredicate,
        getHolderOrgApplicationsPredicate(applicationSearchContext, searchCoreQuery, searchCoreRoot)
    );

  }

  private Predicate getHolderOrgApplicationsPredicate(ApplicationSearchContext applicationSearchContext,
                                                      CriteriaQuery<?> searchCoreQuery,
                                                      Root<ApplicationDetailView> searchCoreRoot) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    var holderOrgUnitIds = applicationSearchContext.getOrgUnitIdsAssociatedWithHolderTeamMembership()
        .stream()
        .map(OrganisationUnitId::asInt)
        .collect(toSet());

    if (holderOrgUnitIds.isEmpty()) {
      // always false
      return cb.isFalse(cb.literal(true));
    }

    Subquery<Integer> applicationSubQuery = searchCoreQuery.subquery(Integer.class);
    Root<PwaHolderOrgUnit> holderOrgUnitRoot = applicationSubQuery.from(PwaHolderOrgUnit.class);

    // return master pwa ids from subquery where a holder is one of the org units within searchers org group(s)
    applicationSubQuery.select(holderOrgUnitRoot.get(PwaHolderOrgUnit_.PWA_ID));
    applicationSubQuery.where(cb.in(holderOrgUnitRoot.get(PwaHolderOrgUnit_.OU_ID)).value(holderOrgUnitIds));

    return cb.in(searchCoreRoot.get(ApplicationDetailView_.PWA_ID)).value(applicationSubQuery);
  }

}
