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
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
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
        return createIndustryUserPredicate(applicationSearchContext, searchCoreQuery, searchCoreRoot);
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
   * NB. Holders defined the application should be ignored expect when its an INITIAL_PWA application.
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
            getSubmittedInitialPwaApplicationsPredicate(applicationSearchContext, searchCoreQuery, searchCoreRoot),
            getSubmittedVariationApplicationsPredicate(applicationSearchContext, searchCoreQuery, searchCoreRoot)
        )
    );

  }

  /**
   * Predicate that makes sure latest version of InitialPwa applications are returned when user org unit
   * is in holder orggrp of HOLDER on latest submitted version of app.
   */
  private Predicate getSubmittedInitialPwaApplicationsPredicate(ApplicationSearchContext applicationSearchContext,
                                                                CriteriaQuery<ApplicationDetailView> searchCoreQuery,
                                                                Root<ApplicationDetailView> searchCoreRoot) {

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    var holderOrgUnitIds = applicationSearchContext.getOrgUnitIdsAssociatedWithHolderTeamMembership()
        .stream()
        .map(OrganisationUnitId::asInt)
        .collect(toSet());

    // for initial app, do a seperate independant query search to get the last submitted Initial Pwa apps where is a holder
    // return app id
    CriteriaQuery<Integer> initialPwaApplicationQuery = cb.createQuery(Integer.class);
    Root<PadOrganisationRole> padOrgRoleRoot = initialPwaApplicationQuery.from(PadOrganisationRole.class);
    Join<PadOrganisationRole, PortalOrganisationUnit> padOrgRoleToPortalOrgUnitJoin = padOrgRoleRoot
        .join(PadOrganisationRole_.ORGANISATION_UNIT);
    Join<PadOrganisationRole, PwaApplicationDetail> orgRoleToAppDetailJoin = padOrgRoleRoot
        .join(PadOrganisationRole_.PWA_APPLICATION_DETAIL);
    Join<PwaApplicationDetail, PwaApplication> appDetailToAppJoin = orgRoleToAppDetailJoin.join(PwaApplicationDetail_.PWA_APPLICATION);


    // have to do a sepeate "last submitted version"  subquery here so that only those initial pwa app where you are a holder on the last
    // submitted version get returned and included in the results. Dont want a situation where if you were a holder on a previous
    // version the whole initial PWA app gets returned.
    Subquery<Instant> lastSubmittedVersionSubQuery = initialPwaApplicationQuery.subquery(Instant.class);
    Root<PadVersionLookup> padVersionLookupRoot = lastSubmittedVersionSubQuery.from(PadVersionLookup.class);
    lastSubmittedVersionSubQuery.select(padVersionLookupRoot.get(PadVersionLookup_.LATEST_SUBMITTED_TIMESTAMP));
    // correlate version lookup subquery by app id to avoid returning rows which happen to have the matching timestamps
    lastSubmittedVersionSubQuery.where(
        cb.equal(appDetailToAppJoin.get(PwaApplication_.ID), padVersionLookupRoot.get(PadVersionLookup_.PWA_APPLICATION_ID))
    );

    Path<Integer> applicationDetailId = appDetailToAppJoin.getParent().get(PwaApplicationDetail_.ID);
    Path<Instant> applicationDetailSubmittedInstant = appDetailToAppJoin.getParent().get(PwaApplicationDetail_.SUBMITTED_TIMESTAMP);
    initialPwaApplicationQuery.select(applicationDetailId);
    initialPwaApplicationQuery.where(cb.and(
        cb.equal(padOrgRoleRoot.get(PadOrganisationRole_.ROLE), HuooRole.HOLDER),
        cb.in(padOrgRoleToPortalOrgUnitJoin.get(PortalOrganisationUnit_.OU_ID)).value(holderOrgUnitIds),
        cb.in(applicationDetailSubmittedInstant).value(lastSubmittedVersionSubQuery)
    ));

    List<Integer> initialPwaAppDetailIdsWhereHolder = entityManager.createQuery(initialPwaApplicationQuery).getResultList();


    return cb.in(searchCoreRoot.get(ApplicationDetailView_.PWA_APPLICATION_DETAIL_ID)).value(initialPwaAppDetailIdsWhereHolder);

  }

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
        cb.isNull(pwaConsentOrgRoleRoot.get(PwaConsentOrganisationRole_.ENDED_BY_PWA_CONSENT)), // maybe move to WHERE
        cb.equal(pwaConsentOrgRoleRoot.get(PwaConsentOrganisationRole_.ROLE), HuooRole.HOLDER)
    ));

    return cb.in(searchCoreRoot.get(ApplicationDetailView_.PWA_ID)).value(variationApplicationSubQuery);
  }
}
