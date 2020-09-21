<#include '../../../layout.ftl'>

<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="caseManagementUrl" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="${caseSummaryView.pwaApplicationRef} - Add case note" breadcrumbs=true fullWidthColumn=true>

    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
    </#if>

    <h2 class="govuk-heading-l">Add case note</h2>

    <@fdsForm.htmlForm>

        <@fdsTextarea.textarea path="form.noteText" labelText="Note text" />

        <@fdsAction.submitButtons primaryButtonText="Add case note" linkSecondaryAction=true secondaryLinkText="Back to case management" linkSecondaryActionUrl=springUrl(caseManagementUrl) />

    </@fdsForm.htmlForm>

</@defaultPage>