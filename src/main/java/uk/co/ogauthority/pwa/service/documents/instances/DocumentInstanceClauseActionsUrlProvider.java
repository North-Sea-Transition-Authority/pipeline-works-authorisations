package uk.co.ogauthority.pwa.service.documents.instances;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.documents.DocumentInstanceController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.documents.ClauseActionsUrlProvider;

public class DocumentInstanceClauseActionsUrlProvider implements ClauseActionsUrlProvider {

  private final PwaApplication pwaApplication;
  private final DocumentView documentView;

  public DocumentInstanceClauseActionsUrlProvider(PwaApplication pwaApplication, DocumentView docView) {
    this.pwaApplication = pwaApplication;
    documentView = docView;
  }

  @Override
  public String getAddClauseAfterRoute(Integer clauseIdToAddAfter) {

    return ReverseRouter.route(on(DocumentInstanceController.class)
        .renderAddClauseAfter(
            pwaApplication.getId(),
            pwaApplication.getApplicationType(),
            null,
            documentView.getDocumentTemplateMnem(),
            clauseIdToAddAfter,
            null,
            null));

  }

  @Override
  public String getAddClauseBeforeRoute(Integer clauseIdToAddBefore) {

    return ReverseRouter.route(on(DocumentInstanceController.class)
        .renderAddClauseBefore(
            pwaApplication.getId(),
            pwaApplication.getApplicationType(),
            null,
            documentView.getDocumentTemplateMnem(),
            clauseIdToAddBefore,
            null,
            null));

  }

  @Override
  public String getAddSubClauseRoute(Integer clauseIdToAddSubFor) {

    return ReverseRouter.route(on(DocumentInstanceController.class)
        .renderAddSubClauseFor(
            pwaApplication.getId(),
            pwaApplication.getApplicationType(),
            null,
            documentView.getDocumentTemplateMnem(),
            clauseIdToAddSubFor,
            null,
            null));

  }

  @Override
  public String getEditClauseRoute(Integer editingClauseId) {

    return ReverseRouter.route(on(DocumentInstanceController.class)
        .renderEditClause(
            pwaApplication.getId(),
            pwaApplication.getApplicationType(),
            null,
            documentView.getDocumentTemplateMnem(),
            editingClauseId,
            null,
            null));

  }

  @Override
  public String getRemoveClauseRoute(Integer clauseIdToRemove) {

    return ReverseRouter.route(on(DocumentInstanceController.class)
        .renderRemoveClause(
            pwaApplication.getId(),
            pwaApplication.getApplicationType(),
            null,
            documentView.getDocumentTemplateMnem(),
            clauseIdToRemove,
            null));

  }

}
