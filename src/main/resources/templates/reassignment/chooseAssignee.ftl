<#include '../layout.ftl'>

<@defaultPage htmlTitle="case reassignment" pageHeading="Choose case officer to assign to" topNavigation=true twoThirdsColumn=true wrapperWidth=true>
  <@fdsForm.htmlForm>
      <div class="govuk-visually-hidden">
        <@fdsAddAField.addAField
        path="form.selectedCases"
        fieldListSize=form.selectedCases?size
        fieldLabelText=""
        actionLinkText=""/>
      </div>
      <@fdsSearchSelector.searchSelectorEnhanced
      path="form.caseOfficerAssignee"
      options=caseOfficerCandidates
      labelText="Select Case officer to assign cases to"
      hintText="The selected user will be responsible for reviewing the selected cases."/>
      <@fdsAction.button buttonText="Reassign Cases"/>
  </@fdsForm.htmlForm>
</@defaultPage>
