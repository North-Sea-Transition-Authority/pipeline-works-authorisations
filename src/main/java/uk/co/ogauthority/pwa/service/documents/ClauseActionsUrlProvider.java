package uk.co.ogauthority.pwa.service.documents;

/**
 * Allows a common declaration of actions to be made available to document instances and templates.
 */
public interface ClauseActionsUrlProvider {

  String getAddClauseAfterRoute(Integer clauseIdToAddAfter);

  String getAddClauseBeforeRoute(Integer clauseIdToAddBefore);

  String getAddSubClauseRoute(Integer clauseIdToAddSubFor);

  String getEditClauseRoute(Integer editingClauseId);

  String getRemoveClauseRoute(Integer clauseIdToRemove);

}
