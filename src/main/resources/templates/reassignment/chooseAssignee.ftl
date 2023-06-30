<#include '../layout.ftl'>

<@defaultPage htmlTitle="case reassignment" pageHeading="Select case officer to assign to" topNavigation=true twoThirdsColumn=true wrapperWidth=true>
  <@fdsForm.htmlForm>
      <div class="govuk-visually-hidden">
        <@fdsAddAField.addAField
        path="form.selectedApplicationIds"
        fieldListSize=form.selectedApplicationIds?size
        fieldLabelText=""
        actionLinkText=""/>
      </div>
      <@fdsSearchSelector.searchSelectorEnhanced
      path="form.caseOfficerAssignee"
      options=caseOfficerCandidates
      labelText="Select case officer to assign cases to"/>
      <@fdsAction.button buttonText="Reassign cases"/>
  </@fdsForm.htmlForm>
</@defaultPage>
