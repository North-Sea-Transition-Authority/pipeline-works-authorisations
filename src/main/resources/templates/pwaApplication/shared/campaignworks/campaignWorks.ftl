<#include '../../../layout.ftl'>
<#import 'workScheduleView.ftl' as workScheduleView>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="dependencySectionName" type="java.lang.String" -->
<#-- @ftlvariable name="dependencySectionUrl" type="java.lang.String" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksUrlFactory" -->
<#-- @ftlvariable name="workScheduleViewList" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleView>" -->
<#-- @ftlvariable name="sectionValidationResult" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksSummaryValidationResult" -->

<#macro campaignWorkScheduleCard workSchedule cardIndex>
    <#local hasErrors=sectionValidationResult.isWorkScheduleInvalid(workSchedule.getPadCampaignWorkScheduleId())/>

    <@fdsCard.card cardClass=hasErrors?then("fds-card--error", "")>
        <#local workScheduleFromTo="${workSchedule.getFormattedWorkStartDate()} to ${workSchedule.getFormattedWorkEndDate()}"/>

        <@fdsCard.cardHeader cardHeadingText="Scheduled ${workScheduleFromTo}">
            <@fdsCard.cardAction cardLinkText="Edit"
            cardLinkScreenReaderText="Edit work schedule starting ${workScheduleFromTo}"
            cardLinkUrl=springUrl(urlFactory.editWorkScheduleUrl(workSchedule.getPadCampaignWorkScheduleId()))
            />
            <@fdsCard.cardAction cardLinkText="Remove"
            cardLinkScreenReaderText="Remove work schedule starting ${workScheduleFromTo}"
            cardLinkUrl=springUrl(urlFactory.removeWorkScheduleUrl(workSchedule.getPadCampaignWorkScheduleId()))
            />
        </@fdsCard.cardHeader>

        <#if hasErrors>
          <span id="${workSchedule.getPadCampaignWorkScheduleId()}-error" class="govuk-error-message">
            Edit this work schedule to fix validation errors
          </span>

        </#if>

        <@workScheduleView.pipelineList workSchedule=workSchedule tableIdx=cardIndex/>

    </@fdsCard.card>
</#macro>

<@defaultPage htmlTitle="Campaign works" pageHeading="Campaign works" breadcrumbs=true fullWidthColumn=true>

    <#if sectionValidationResult.getCompleteSectionErrorMessage()?has_content>
        <@fdsError.singleErrorSummary errorMessage=sectionValidationResult.getCompleteSectionErrorMessage() />
    </#if>

    <@fdsInsetText.insetText>
      <p>Your application requires campaign works information due to the information provided in the ${dependencySectionName} section.</p>
      <p><a href="${springUrl(dependencySectionUrl)}" class="govuk-link">Click here to change your campaign works approach.</a></p>
      <p>Provide the schedule of campaign works for the pipelines on this application. You do not have to provide information on pipelines that are not part of this application. If a pipeline is installed over multiple periods, then add it to each relevant period.</p>
    </@fdsInsetText.insetText>

    <@fdsAction.link linkText="Add work schedule" linkUrl=springUrl(urlFactory.addWorkScheduleUrl()) linkClass="govuk-button govuk-button--blue"/>

    <#list workScheduleViewList as workSchedule>
        <@campaignWorkScheduleCard workSchedule=workSchedule cardIndex=workSchedule?index?string/>
    </#list>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons
        primaryButtonText="Complete"
        linkSecondaryAction=true
        secondaryLinkText="Back to task list"
        linkSecondaryActionUrl=springUrl(backUrl)
        />
    </@fdsForm.htmlForm>

</@defaultPage>