<#include '../layout.ftl'>

<@defaultPage htmlTitle="case reassignment" pageHeading="Select case officer to reassign to" topNavigation=true twoThirdsColumn=true wrapperWidth=true>
  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
      <@fdsAddToList.addToList
      pathForList="selectedCases.selectedApplicationIds"
      pathForSelector="selectedCases.selectedApplicationIds"
      alreadyAdded=selectedPwas
      itemName="The following applications will be affected:"
      selectorInputClass="govuk-visually-hidden"/>
      <@fdsSearchSelector.searchSelectorEnhanced
      path="form.assignedCaseOfficerPersonId"
      options=caseOfficerCandidates
      labelText="Select case officer to reassign cases to"/>
      <@fdsAction.button buttonText="Reassign cases"/>
  </@fdsForm.htmlForm>
</@defaultPage>
