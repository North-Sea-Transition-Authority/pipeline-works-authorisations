package uk.co.ogauthority.pwa.service.search.applicationsearch.restrictions;

import static java.util.stream.Collectors.toSet;

import java.time.Instant;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit_;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsulteeGroupId;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup_;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest_;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.PadVersionLookup;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.PadVersionLookup_;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole_;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent_;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
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
                                   CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                                   Root<ApplicationDetailView> searchCoreRoot) {
    var userType = applicationSearchContext.getUserType();

    switch (userType) {
      case OGA:
        LOGGER.debug("Using OGA restriction predicate.");
        return createRegulatorUserPredicate(searchCoreQuery, searchCoreRoot);
      case INDUSTRY:
        LOGGER.debug("Using INDUSTRY restriction predicate.");
        return createIndustryUserPredicate(applicationSearchContext, searchCoreQuery, searchCoreRoot);
      case CONSULTEE:
        LOGGER.debug("Using CONSULTEE restriction predicate.");
        return createConsulteeUserPredicate(applicationSearchContext, searchCoreQuery, searchCoreRoot);
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

  private Predicate getLastestSatisfactoryVersionPredicate(CriteriaQuery<ApplicationDetailView> searchCoreQuery,
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
  private Predicate createRegulatorUserPredicate(CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                                                 Root<ApplicationDetailView> searchCoreRoot) {
    return getLastSubmittedVersionPredicate(searchCoreQuery, searchCoreRoot);

  }

  /**
   * Return applications only where the user's consultee group has been consulted and only return the "last accepted" version.
   */
  private Predicate createConsulteeUserPredicate(ApplicationSearchContext applicationSearchContext,
                                                CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                                                Root<ApplicationDetailView> searchCoreRoot) {

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    var consulteeGroupIds = applicationSearchContext.getConsulteeGroupIds()
        .stream()
        .map(ConsulteeGroupId::asInt)
        .collect(toSet());

    var latestSatisfactoryVersionPredicate = getLastestSatisfactoryVersionPredicate(searchCoreQuery, searchCoreRoot);

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
                                                CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                                                Root<ApplicationDetailView> searchCoreRoot) {

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    var lastSubmittedVersionPredicate = getLastSubmittedVersionPredicate(searchCoreQuery, searchCoreRoot);

    // limit so we only ever see the last submitted version
    // and
    // A the last submitted version is for an INITIAL_PWA app where HOLDER set to users org
    // or
    // B the master pwa ID of the application matches one where a consented HOLDER is one of the user's orgs

    return cb.and(
        lastSubmittedVersionPredicate,
        cb.or(
            getSubmittedInitialPwaApplicationsPredicate(applicationSearchContext, searchCoreRoot),
            getSubmittedVariationApplicationsPredicate(applicationSearchContext, searchCoreQuery, searchCoreRoot)
        )
    );

  }

  /**
   * Predicate that makes sure latest version of InitialPwa applications are returned when user org unit
   * is in holder orggrp of HOLDER on latest submitted version of app.
   */
  private Predicate getSubmittedInitialPwaApplicationsPredicate(ApplicationSearchContext applicationSearchContext,
                                                                Root<ApplicationDetailView> searchCoreRoot) {

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    var holderOrgUnitIds = applicationSearchContext.getOrgUnitIdsAssociatedWithHolderTeamMembership()
        .stream()
        .map(OrganisationUnitId::asInt)
        .collect(toSet());

    // for initial app, do a separate independent query search to get the last submitted Initial Pwa apps where is a holder
    // return app id
    CriteriaQuery<Integer> initialPwaApplicationQuery = cb.createQuery(Integer.class);
    Root<PadOrganisationRole> padOrgRoleRoot = initialPwaApplicationQuery.from(PadOrganisationRole.class);
    Join<PadOrganisationRole, PortalOrganisationUnit> padOrgRoleToPortalOrgUnitJoin = padOrgRoleRoot
        .join(PadOrganisationRole_.ORGANISATION_UNIT);
    Join<PadOrganisationRole, PwaApplicationDetail> orgRoleToAppDetailJoin = padOrgRoleRoot
        .join(PadOrganisationRole_.PWA_APPLICATION_DETAIL);
    Join<PwaApplicationDetail, PwaApplication> appDetailToAppJoin = orgRoleToAppDetailJoin.join(PwaApplicationDetail_.PWA_APPLICATION);

    // have to do a separate "last submitted version" subquery here so that only those initial pwa app where you are a holder on the last
    // submitted version get returned and included in the results. Dont want a situation where if you were a holder on a previous
    // version the whole initial PWA app gets returned.
    Subquery<Instant> lastSubmittedVersionSubQuery = initialPwaApplicationQuery.subquery(Instant.class);
    Root<PadVersionLookup> padVersionLookupRoot = lastSubmittedVersionSubQuery.from(PadVersionLookup.class);
    lastSubmittedVersionSubQuery.select(padVersionLookupRoot.get(PadVersionLookup_.LATEST_SUBMITTED_TIMESTAMP));
    // correlate version lookup subquery by app id to avoid returning rows which happen to have the matching timestamps
    lastSubmittedVersionSubQuery.where(
        cb.equal(appDetailToAppJoin.get(PwaApplication_.ID), padVersionLookupRoot.get(PadVersionLookup_.PWA_APPLICATION_ID))
    );

    Path<Integer> applicationDetailIdPath = appDetailToAppJoin.getParent().get(PwaApplicationDetail_.ID);
    Path<Instant> applicationDetailSubmittedInstantPath = appDetailToAppJoin.getParent().get(PwaApplicationDetail_.SUBMITTED_TIMESTAMP);
    Path<PwaApplicationType> applicationTypePath = appDetailToAppJoin.get(PwaApplication_.APPLICATION_TYPE);
    initialPwaApplicationQuery.select(applicationDetailIdPath);
    initialPwaApplicationQuery.where(cb.and(
        cb.equal(applicationTypePath, PwaApplicationType.INITIAL),
        cb.equal(padOrgRoleRoot.get(PadOrganisationRole_.ROLE), HuooRole.HOLDER),
        cb.in(padOrgRoleToPortalOrgUnitJoin.get(PortalOrganisationUnit_.OU_ID)).value(holderOrgUnitIds),
        cb.in(applicationDetailSubmittedInstantPath).value(lastSubmittedVersionSubQuery)
    ));

    List<Integer> initialPwaAppDetailIdsWhereHolder = entityManager.createQuery(initialPwaApplicationQuery).getResultList();

    // when there are no initial pwa apps for user orgs - simply return an always false condition that will be optimised out by the db.
    if (initialPwaAppDetailIdsWhereHolder.isEmpty()) {
      return cb.isFalse(cb.literal(true));
    }

    return cb.in(searchCoreRoot.get(ApplicationDetailView_.PWA_APPLICATION_DETAIL_ID)).value(initialPwaAppDetailIdsWhereHolder);

  }

  //TODO PWA-1185 rename to remove "submitted" as not accurate
  private Predicate getSubmittedVariationApplicationsPredicate(ApplicationSearchContext applicationSearchContext,
                                                               CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                                                               Root<ApplicationDetailView> searchCoreRoot) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    var holderOrgUnitIds = applicationSearchContext.getOrgUnitIdsAssociatedWithHolderTeamMembership()
        .stream()
        .map(OrganisationUnitId::asInt)
        .collect(toSet());

    Subquery<Integer> variationApplicationSubQuery = searchCoreQuery.subquery(Integer.class);
    Root<PwaConsentOrganisationRole> pwaConsentOrgRoleRoot = variationApplicationSubQuery.from(PwaConsentOrganisationRole.class);

    Join<PwaConsentOrganisationRole, PwaConsent> orgRoleToConsentJoin = pwaConsentOrgRoleRoot
        .join(PwaConsentOrganisationRole_.ADDED_BY_PWA_CONSENT);

    // return master pwa ids from subquery where a holder is one of the org units within searchers org group(s)
    variationApplicationSubQuery.select(orgRoleToConsentJoin.get(PwaConsent_.MASTER_PWA));
    variationApplicationSubQuery.where(cb.and(
        cb.in(pwaConsentOrgRoleRoot.get(PwaConsentOrganisationRole_.ORGANISATION_UNIT_ID)).value(holderOrgUnitIds),
        cb.isNull(pwaConsentOrgRoleRoot.get(PwaConsentOrganisationRole_.ENDED_BY_PWA_CONSENT)),
        cb.equal(pwaConsentOrgRoleRoot.get(PwaConsentOrganisationRole_.ROLE), HuooRole.HOLDER)
    ));

    return cb.in(searchCoreRoot.get(ApplicationDetailView_.PWA_ID)).value(variationApplicationSubQuery);
  }
}
