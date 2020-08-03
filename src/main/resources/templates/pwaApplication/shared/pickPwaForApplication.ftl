<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Start PWA application">
    <@fdsError.errorSummary errorItems=errorList />

    <@fdsForm.htmlForm>

        <#assign pageHeader="What PWA is this variation for?"/>
        <#if pwaApplicationType == "DEPOSIT_CONSENT">
            <#assign pageHeader="What PWA is the deposit consent linked to?"/>
        </#if>

        <@fdsSearchSelector.searchSelectorEnhanced path="form.pickablePwaString" labelText=pageHeader options=selectablePwaMap pageHeading=true labelHeadingClass="govuk-label--l" />

        <#assign groups>
            <#list ogList as group> ${group} <#sep>, </#list>
        </#assign>
        <@fdsDetails.details detailsTitle="The PWA is not in the list" 
            detailsText="You can only access PWAs for organisations within the following groups: ${groups}.
            The organisation must be the current holder of the PWA.            
            If you do not have access to the PWA then you must contact the holder to create the application on your behalf. Once created they can provide you with access to the application. Alternatively, they can provide you with access to their organisation account."
            />
            
        <@fdsAction.submitButtons primaryButtonText="Continue" linkSecondaryAction=true secondaryLinkText="Back to work area" linkSecondaryActionUrl=springUrl(workareaUrl) />
    </@fdsForm.htmlForm>

</@defaultPage>