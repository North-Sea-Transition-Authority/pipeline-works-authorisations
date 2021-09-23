<#include '../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="backUrl" type="String" -->
<#-- @ftlvariable name="pwaReference" type="String" -->
<#-- @ftlvariable name="userOrgGroupsCsv" type="String" -->
<#-- @ftlvariable name="applicantOrganisationOptions" type="java.util.Map<String, String>" -->

<@defaultPage htmlTitle="Applicant organisation" errorItems=errorList backLink=true backLinkUrl=springUrl(backUrl)>

    <@fdsForm.htmlForm>

        <@fdsRadio.radioGroup path="form.applicantOrganisationOuId"
            labelText="Which organisation is applying for this variation?"
            fieldsetHeadingSize="h1"
            fieldsetHeadingClass="govuk-fieldset__legend--l"
            caption=pwaReference
            captionClass="govuk-caption-xl">
            <#assign firstItem=true/>
            <#list applicantOrganisationOptions as ouId, ouName>
                <@fdsRadio.radioItem path="form.applicantOrganisationOuId" itemMap={ouId : ouName} isFirstItem=firstItem></@fdsRadio.radioItem>
                <#assign firstItem=false/>
            </#list>
        </@fdsRadio.radioGroup>

        <@fdsDetails.summaryDetails summaryTitle="The organisation is not in the list">
            <p>You can only access organisations within the following groups: ${userOrgGroupsCsv} </p>
            <p>The organisation must be the current holder of the PWA.</p>
        </@fdsDetails.summaryDetails>

        <@fdsAction.submitButtons primaryButtonText="Continue" linkSecondaryAction=true secondaryLinkText="Change PWA" linkSecondaryActionUrl=springUrl(backUrl) />

    </@fdsForm.htmlForm>

</@defaultPage>