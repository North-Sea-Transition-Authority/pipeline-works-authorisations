package uk.co.ogauthority.pwa.service.search.applicationsearch.restrictions;

import static java.util.stream.Collectors.toSet;

import java.time.Instant;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.PadVersionLookup;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.PadVersionLookup_;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole_;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent_;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
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

  private static final int FIRST_VERSION_NUMBER = 1;

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
                                   CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                                   Root<ApplicationDetailView> searchCoreRoot) {
    var userType = applicationSearchContext.getUserType();

    switch (userType) {
      case OGA:
        return createRegulatorUserPredicate(applicationSearchContext, applicationSearchParameters, searchCoreQuery, searchCoreRoot);
      case INDUSTRY:
        return createIndustryUserPredicate(applicationSearchContext, applicationSearchParameters, searchCoreQuery, searchCoreRoot);
      case CONSULTEE:
        return createConsulteeUserPredicate(applicationSearchContext, applicationSearchParameters, searchCoreQuery, searchCoreRoot);
      default: throw new IllegalArgumentException(
          String.format("App search does not support user type of %s for wua_id: %s", userType, applicationSearchContext.getWuaIdAsInt())
      );
    }

  }


  private Predicate getLastSubmittedVersionPredicate(CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                                                     Root<ApplicationDetailView> searchCoreRoot) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    // use instant so we can filter on the last submitted timestamp
    Subquery<Instant> subQuery = searchCoreQuery.subquery(Instant.class);
    Root<PadVersionLookup> subRoot = subQuery.from(PadVersionLookup.class);
    // basically a correlated subquery without setting up entity relationships
    subQuery.select(subRoot.get(PadVersionLookup_.LATEST_SUBMITTED_TIMESTAMP));
    subQuery.where(
        cb.equal(searchCoreRoot.get(ApplicationDetailView_.PWA_APPLICATION_ID), subRoot.get(PadVersionLookup_.PWA_APPLICATION_ID))
    );

    return cb.equal(searchCoreRoot.get(ApplicationDetailView_.PAD_SUBMITTED_TIMESTAMP), subQuery);

  }



  /**
   * Restrict to the last submitted version of all apps only.
   */
  private Predicate createRegulatorUserPredicate(ApplicationSearchContext applicationSearchContext,
                                                 ApplicationSearchParameters applicationSearchParameters,
                                                 CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                                                 Root<ApplicationDetailView> searchCoreRoot) {
    return getLastSubmittedVersionPredicate(searchCoreQuery, searchCoreRoot);

  }

  /**
   * Return applications only where the user's consultee group has been consulted and only return the "last accepted" version.
   */
  private Predicate createConsulteeUserPredicate(ApplicationSearchContext applicationSearchContext,
                                                ApplicationSearchParameters applicationSearchParameters,
                                                CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                                                Root<ApplicationDetailView> searchCoreRoot) {

    // TODO PWA-1051 do consultee restrictions and remove commented code.
    LOGGER.error("PWA-1051 - Consultee user type restrictions not implemented. return nothing.");
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    return cb.equal(cb.literal(false), cb.literal(true));

  }

  /**
   * Return the last submitted version of applications where the user's "holder" team is a Holder of the top level PWA.
   * NB. Holders defined the application should be ignored.
   */
  private Predicate createIndustryUserPredicate(ApplicationSearchContext applicationSearchContext,
                                                ApplicationSearchParameters applicationSearchParameters,
                                                CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                                                Root<ApplicationDetailView> searchCoreRoot) {

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    var lastSubmittedVersionPredicate = getLastSubmittedVersionPredicate(searchCoreQuery, searchCoreRoot);
    var lastSubmittedOrFirstDraftPredicate = cb.or(
        lastSubmittedVersionPredicate,
        cb.and(
            cb.equal(searchCoreRoot.get(ApplicationDetailView_.VERSION_NO), FIRST_VERSION_NUMBER),
            cb.equal(searchCoreRoot.get(ApplicationDetailView_.PAD_STATUS), PwaApplicationStatus.DRAFT)
        )
    );

    var holderOrgUnitIds = applicationSearchContext.getOrgUnitIdsAssociatedWithHolderTeamMembership()
        .stream()
        .map(OrganisationUnitId::asInt)
        .collect(toSet());

    Subquery<Integer> subQuery = searchCoreQuery.subquery(Integer.class);
    Root<PwaConsentOrganisationRole> subRoot = subQuery.from(PwaConsentOrganisationRole.class);

    Join<PwaConsentOrganisationRole, PwaConsent> orgRoleToConsentJoin = subRoot.join(PwaConsentOrganisationRole_.ADDED_BY_PWA_CONSENT);

    // return master pwa ids from subquery where a holder is one of the org units within searchers org group(s)
    subQuery.select(orgRoleToConsentJoin.get(PwaConsent_.MASTER_PWA));
    subQuery.where(cb.and(
        cb.in(subRoot.get(PwaConsentOrganisationRole_.ORGANISATION_UNIT_ID)).value(holderOrgUnitIds),
        cb.isNull(subRoot.get(PwaConsentOrganisationRole_.ENDED_BY_PWA_CONSENT)), // maybe move to WHERE
        cb.equal(subRoot.get(PwaConsentOrganisationRole_.ROLE), HuooRole.HOLDER)
    ));

    var holderApplicationsPredicate = cb.in(searchCoreRoot.get(ApplicationDetailView_.PWA_ID)).value(subQuery);

    return cb.and(
        lastSubmittedOrFirstDraftPredicate,
        holderApplicationsPredicate
    );

  }
}
