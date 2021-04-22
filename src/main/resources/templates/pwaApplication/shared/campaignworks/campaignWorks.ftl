<#include '../../../layout.ftl'>
<#import 'workScheduleView.ftl' as workScheduleView>

<#-- @ftlvariable name="dependencySectionName" type="java.lang.String" -->
<#-- @ftlvariable name="dependencySectionUrl" type="java.lang.String" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksUrlFactory" -->
<#-- @ftlvariable name="workScheduleViewList" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleView>" -->
<#-- @ftlvariable name="summaryValidationResult" type="uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult" -->


<#macro campaignWorkScheduleCard workSchedule cardIndex>

    <#assign cardId = validationResult.constructObjectId(summaryValidationResult!, workSchedule.getPadCampaignWorkScheduleId()) />
    <#assign hasErrors = validationResult.hasErrors(summaryValidationResult!, cardId) />
    <#assign cardErrorMessage = validationResult.errorMessageOrEmptyString(summaryValidationResult!, cardId) />

    <@fdsCard.card cardId=cardId cardClass=hasErrors?then("fds-card--error", "")>
        <#local workScheduleFromTo="${workSchedule.getFormattedWorkStartDate()} to ${workSchedule.getFormattedWorkEndDate()}"/>

        <@fdsCard.cardHeader cardHeadingText="Scheduled ${workScheduleFromTo}" cardErrorMessage=cardErrorMessage>
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

<@defaultPage htmlTitle="Campaign works" breadcrumbs=true fullWidthColumn=true>

    <@validationResult.singleErrorSummary summaryValidationResult=summaryValidationResult! />
    <@validationResult.errorSummary summaryValidationResult=summaryValidationResult! />

    <h1 class="govuk-heading-xl">Campaign works</h1>

    <@fdsInsetText.insetText>
      Your application requires campaign works information due to the information provided in the ${dependencySectionName} section. </br>
      <@fdsAction.link linkText="Click here to change your campaign works approach." linkUrl=springUrl(dependencySectionUrl) linkClass="govuk-link"/>
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