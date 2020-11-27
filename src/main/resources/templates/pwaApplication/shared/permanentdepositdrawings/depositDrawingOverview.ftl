<#include '../../../layout.ftl'>
<#include 'depositsDrawingsViewSummary.ftl'>

<#-- @ftlvariable name="errorMessage" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="Permanent deposit drawings" pageHeading="Permanent deposit drawings" breadcrumbs=true>

    <#if errorMessage?has_content>
        <@fdsError.singleErrorSummary errorMessage=errorMessage />
    </#if>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>

        <@fdsAction.link linkText="Add drawing" linkUrl=springUrl(depositDrawingUrlFactory.getAddPipelineDrawingUrl()) linkClass="govuk-button govuk-button--blue"/>

        <hr class="govuk-section-break govuk-section-break--m">

        <#list depositDrawingSummaryViews as depositDrawingView>
            <h2 class="govuk-heading-m">${depositDrawingView.reference}</h2>
            <@fdsAction.link linkText="Edit" linkUrl=springUrl(depositDrawingUrlFactory.getEditDrawingUrl(depositDrawingView.depositDrawingId)) linkScreenReaderText="Edit ${depositDrawingView.reference}" linkClass="govuk-link govuk-!-font-size-19"/>&nbsp;
            <@fdsAction.link linkText="Remove" linkUrl=springUrl(depositDrawingUrlFactory.getRemoveDrawingUrl(depositDrawingView.depositDrawingId)) linkScreenReaderText="Remove ${depositDrawingView.reference}" linkClass="govuk-link govuk-!-font-size-19"/>
            <@depositDrawingViewSummary depositDrawingView depositDrawingUrlFactory/>
        </#list>

        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(backUrl)/>

    </@fdsForm.htmlForm>

</@defaultPage>