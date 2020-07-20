<#include '../../../layout.ftl'>
<#import 'admiraltyChartManagement.ftl' as admiraltyChartManagement>
<#import 'pipelineDrawingManagement.ftl' as pipelineDrawingManagement>

<#-- @ftlvariable name="errorMessage" type="String" -->
<#-- @ftlvariable name="admiraltyChartUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.AdmiraltyChartUrlFactory" -->
<#-- @ftlvariable name="admiraltyChartFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="admiraltyOptional" type="java.lang.Boolean" -->
<#-- @ftlvariable name="validatorFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PipelineDrawingValidationFactory" -->

<@defaultPage htmlTitle="Admiralty chart and pipeline drawings" pageHeading="Admiralty chart and pipeline drawings" breadcrumbs=true fullWidthColumn=true>

    <#if errorMessage?has_content>
        <@fdsError.singleErrorSummary errorMessage=errorMessage />
    </#if>

    <@admiraltyChartManagement.admiraltyChartManagement
    urlFactory=admiraltyChartUrlFactory
    optionalSection=admiraltyOptional
    admiraltyChartFileViews=admiraltyChartFileViews />

    <hr class="govuk-section-break govuk-section-break--m"/>

    <@pipelineDrawingManagement.pipelineDrawingManagement
    urlFactory=pipelineDrawingUrlFactory
    pipelineDrawingSummaryViews=pipelineDrawingSummaryViews
    validatorFactory=validatorFactory!/>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>