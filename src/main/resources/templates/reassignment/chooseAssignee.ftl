<#include '../layout.ftl'>

<@defaultPage htmlTitle="case reassignment" pageHeading="Select case officer to assign to" topNavigation=true twoThirdsColumn=true wrapperWidth=true>
  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
      <@fdsAddToList.addToList
      pathForList="form.selectedApplicationIds"
      pathForSelector="form.selectedApplicationIds"
      alreadyAdded=selectedPwas
      itemName="The following applications will be affected:"
      selectorInputClass="govuk-visually-hidden"/>
      <@fdsSearchSelector.searchSelectorEnhanced
      path="form.caseOfficerAssignee"
      options=caseOfficerCandidates
      labelText="Select case officer to assign cases to"/>
      <@fdsAction.button buttonText="Reassign cases"/>
  </@fdsForm.htmlForm>
</@defaultPage>
