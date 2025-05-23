<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="actionType" type="uk.co.ogauthority.pwa.model.form.enums.ScreenActionType" -->
<#-- @ftlvariable name="mergeFieldNames" type="java.util.List<String>" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle="${actionType.actionText} clause" pageHeading="${actionType.actionText} clause" topNavigation=true twoThirdsColumn=true breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>

        <@fdsTextInput.textInput path="form.name" labelText="Clause name" hintText="This will be shown in the document sidebar" />

        <@pwaMarkdownInsetText.text "clause text area"/>

        <@fdsTextarea.textarea path="form.text" labelText="Clause text" rows = "12" />

        <#if mergeFieldNames?has_content>
          <@fdsDetails.summaryDetails summaryTitle="How can I include application data in my clause?">

            <h2 class="govuk-heading-m">Mail merge fields</h2>
            <p class="govuk-body">Any of the fields listed below can be included in clause text to pull in information from the application.
              This will be visible after saving the clause.</p>
            <@pwaMailMerge.mailMergeFieldList mergeFields=mergeFieldNames />

            <@pwaMailMerge.manualMergeGuidance />

          </@fdsDetails.summaryDetails>
        </#if>

        <@fdsAction.submitButtons primaryButtonText="${actionType.submitButtonText} clause"  linkSecondaryAction=true secondaryLinkText="Go back" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>

</@defaultPage>