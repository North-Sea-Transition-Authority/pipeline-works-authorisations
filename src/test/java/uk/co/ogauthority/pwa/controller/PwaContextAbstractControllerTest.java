package uk.co.ogauthority.pwa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsTaskListService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContextService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionService;
import uk.co.ogauthority.pwa.service.search.consents.ConsentSearchService;
import uk.co.ogauthority.pwa.service.search.consents.SearchPwaBreadcrumbService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;

public abstract class PwaContextAbstractControllerTest extends AbstractControllerTest {

  @Autowired
  protected PwaContextService pwaContextService;

  @Autowired
  protected PwaPermissionService pwaPermissionService;

  @MockBean
  protected PipelineService pipelineService;

  @MockBean
  protected ConsentSearchService consentSearchService;

  @MockBean
  protected MasterPwaService masterPwaService;

  @SpyBean
  private SearchPwaBreadcrumbService searchPwaBreadcrumbService;

  @SpyBean
  protected ApplicationBreadcrumbService breadcrumbService;

  @MockBean
  private CrossingAgreementsTaskListService crossingAgreementsTaskListService;

  @SpyBean
  protected UserTypeService userTypeService;

  @MockBean
  protected PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  protected PwaAppProcessingContextService pwaAppProcessingContextService;

}