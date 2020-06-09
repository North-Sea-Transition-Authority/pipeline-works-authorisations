<#include '../../../layout.ftl'>
<#import 'drawingSummary.ftl' as drawingSummary>

<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="summary" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.techdrawings.PipelineDrawingSummaryView" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PipelineDrawingUrlFactory" -->

<@defaultPage htmlTitle="Remove pipeline drawing" pageHeading="Are you sure you want to remove this pipeline drawing?" breadcrumbs=true>

    <@drawingSummary.drawingSummary summary=summary urlFactory=urlFactory showReferenceAsKey=true showActions=false />

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove pipeline drawing" secondaryLinkText="Back to overview" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>