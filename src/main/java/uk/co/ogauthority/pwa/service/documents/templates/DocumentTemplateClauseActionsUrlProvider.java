package uk.co.ogauthority.pwa.service.documents.templates;

import uk.co.ogauthority.pwa.service.documents.ClauseActionsUrlProvider;

public class DocumentTemplateClauseActionsUrlProvider implements ClauseActionsUrlProvider {

  @Override
  public String getAddClauseAfterRoute(Integer clauseIdToAddAfter) {
    return "";
  }

  @Override
  public String getAddClauseBeforeRoute(Integer clauseIdToAddBefore) {
    return "";
  }

  @Override
  public String getAddSubClauseRoute(Integer clauseIdToAddSubFor) {
    return "";
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
