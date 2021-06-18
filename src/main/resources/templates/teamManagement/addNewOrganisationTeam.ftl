<#include '../layout.ftl'>

<@defaultPage htmlTitle="Add new ${teamTypeDisplayName} team" backLink=true topNavigation=true twoThirdsColumn=true errorItems=errorList>


  <@fdsForm.htmlForm>
    <@fdsSearchSelector.searchSelectorRest
      path="form.organisationGroup"
      labelText="Which organisation would you like to create a team for?"
      pageHeading=true
      labelHeadingClass="govuk-label--l"
      selectorMinInputLength=1
      restUrl=springUrl(organisationRestUrl)
    />

    <@fdsDetails.summaryDetails summaryTitle="The organisation I want to create a team for is not listed">
      <p class="govuk-body">
        If the organisation you need to create a team for is not shown in the list
        then you must contact the person responsible for managing organisations on the U.K Energy Portal
      </p>
    </@fdsDetails.summaryDetails>

    <@fdsAction.submitButtons
      primaryButtonText=submitPrimaryButtonText
      linkSecondaryAction=true
      secondaryLinkText="Cancel"
      linkSecondaryActionUrl=springUrl(linkSecondaryActionUrl)
    />
  </@fdsForm.htmlForm>
</@defaultPage>