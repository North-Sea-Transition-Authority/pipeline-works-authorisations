package uk.co.ogauthority.pwa.service.documents.templates;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.documents.DocumentTemplateController;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.documents.ClauseActionsUrlProvider;

public class DocumentTemplateClauseActionsUrlProvider implements ClauseActionsUrlProvider {

  private final DocumentSpec documentSpec;

  public DocumentTemplateClauseActionsUrlProvider(DocumentSpec documentSpec) {
    this.documentSpec = documentSpec;
  }

  @Override
  public String getAddClauseAfterRoute(Integer clauseIdToAddAfter) {
    return ReverseRouter.route(on(DocumentTemplateController.class)
        .renderAddClauseAfter(documentSpec, clauseIdToAddAfter, null, null));
  }

  @Override
  public String getAddClauseBeforeRoute(Integer clauseIdToAddBefore) {
    return ReverseRouter.route(on(DocumentTemplateController.class)
        .renderAddClauseBefore(documentSpec, clauseIdToAddBefore, null, null));
  }

  @Override
  public String getAddSubClauseRoute(Integer clauseIdToAddSubFor) {
    return ReverseRouter.route(on(DocumentTemplateController.class)
        .renderAddSubClauseFor(documentSpec, clauseIdToAddSubFor, null, null));
  }

  @Override
  public String getEditClauseRoute(Integer editingClauseId) {
    return "";
  }

  @Override
  public String getRemoveClauseRoute(Integer clauseIdToRemove) {
    return "";
  }

}
