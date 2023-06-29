<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->
<#-- @ftlvariable name="coverLetter" type="java.lang.String" -->
<#-- @ftlvariable name="requestReason" type="java.lang.String" -->
<#-- @ftlvariable name="approvalResultOptions" type="java.util.List<java.lang.String>" -->


<#include '../layout.ftl'>

<@defaultPage htmlTitle="${appRef} review public notice request" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

    <@fdsForm.htmlForm>
        <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

        <h2 class="govuk-heading-l">Review public notice request</h2>

        <p class="govuk-body"><@fdsAction.link linkUrl=springUrl(downloadPublicNoticeUrl) linkText="Download public notice document" linkClass="govuk-link govuk-!-font-size-19"/></p>

        <h3 class="govuk-heading-m"> Cover letter </h3>
        <@grid.gridRow>
            <@grid.twoThirdsColumn>
                <@multiLineText.multiLineText blockClass="public-notice__text">
                    <p class="govuk-body"> ${coverLetter} </p>
                </@multiLineText.multiLineText>
            </@grid.twoThirdsColumn>
        </@grid.gridRow>

        <h3 class="govuk-heading-m"> Public notice request reason </h3>
        <p class="govuk-body">${requestReason}</p>

        <#if requestDescription?has_content>
            <@multiLineText.multiLineText blockClass="public-notice__text">
                <p class="govuk-body"> ${requestDescription} </p>
            </@multiLineText.multiLineText>
        </#if>

        <@fdsRadio.radioGroup path="form.requestApproved" labelText="Respond to the public notice approval request" hiddenContent=true>
            <#assign firstItem=true/>
            <#list approvalResultOptions as approvalResultOption>
                <@fdsRadio.radioItem path="form.requestApproved" itemMap={approvalResultOption : approvalResultOption.getDisplayValue()} isFirstItem=firstItem>
                    <#if approvalResultOption == "REQUEST_REJECTED">
                        <@fdsTextarea.textarea path="form.requestRejectedReason" nestingPath="form.requestApproved" labelText="Rejection reason" characterCount=true maxCharacterLength=maxCharacterLength?c inputClass="govuk-!-width-two-thirds"/>
                    </#if>
                </@fdsRadio.radioItem>
            </#list>
        </@fdsRadio.radioGroup>

        <@fdsAction.submitButtons primaryButtonText="Submit" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>


</@defaultPage>
