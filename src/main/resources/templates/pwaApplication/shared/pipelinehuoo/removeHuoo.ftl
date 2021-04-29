<#include '../../../layout.ftl'>


<@defaultPage htmlTitle="Remove huoo" pageHeading="Are you sure you want to remove '${huooName}'?" fullWidthColumn=true>
    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove" primaryButtonClass="govuk-button govuk-button--warning" secondaryLinkText="Back to HUOO summary" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>
