<#include '../../layout.ftl'>

<@defaultPage
htmlTitle="Select a consultee group"
pageHeading=""
twoThirdsColumn=true
>
    <@fdsForm.htmlForm>
        <@fdsSearchSelector.searchSelectorRest
        path="form.consulteeGroupId"
        restUrl=springUrl(consulteeGroupSearchUrl)
        labelText="Select a consultee group"
        pageHeading=true
        />

        <@fdsDetails.summaryDetails summaryTitle="The consultee group I want to create a team for is not listed">
          If the consultee group you want to create a team for is not shown in the list
          then you must contact the person responsible for managing consultee groups on the
          U.K. Energy Portal.
        </@fdsDetails.summaryDetails>

        <@fdsAction.button buttonText="Create team"/>
    </@fdsForm.htmlForm>

</@defaultPage>