<#include '../../../layout.ftl'>

<#-- @ftlvariable name="docView" type="uk.co.ogauthority.pwa.model.documents.view.DocumentView"-->

<div>

  <#list docView.sections as section>
    <#list section.clauses as clauseView>
      <ul class="no-bullet">
        <@pwaClauseList.clause clauseView=clauseView clauseActionsUrlProvider="" listClass="" isLastInList=false addAndRemoveClauseAllowed=false showClauseHeading=false/>
      </ul>
    </#list>
  </#list>

</div>