<#include '../../../layout.ftl'>
<#import 'drawingSummary.ftl' as drawingSummary>

<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="summary" type="uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PipelineDrawingSummaryView" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PipelineDrawingUrlFactory" -->

<@defaultPage htmlTitle="Remove pipeline schematic" pageHeading="Are you sure you want to remove this pipeline schematic?" breadcrumbs=true>

    <@drawingSummary.drawingSummary summary=summary urlFactory=urlFactory validatorFactory={} showReferenceAsKey=true showActions=false />

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove pipeline schematic" primaryButtonClass="govuk-button govuk-button--warning" secondaryLinkText="Back to overview" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>