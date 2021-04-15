<#include '../../../layout.ftl'>
<#import 'workScheduleView.ftl' as workScheduleView>

<#-- @ftlvariable name="workSchedule" type="uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleView" -->
<#-- @ftlvariable name="overviewUrl" type="java.lang.String" -->


<@defaultPage htmlTitle="Remove scheduled campaign work" pageHeading="Remove work scheduled ${workSchedule.getFormattedWorkStartDate()} to ${workSchedule.getFormattedWorkEndDate()}" breadcrumbs=true fullWidthColumn=true>

    <@workScheduleView.pipelineList workSchedule=workSchedule/>
    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons
        linkSecondaryAction=true
        primaryButtonText="Remove scheduled work"
        primaryButtonClass="govuk-button govuk-button--warning"
        secondaryLinkText="Back to overview"
        linkSecondaryActionUrl=springUrl(overviewUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>