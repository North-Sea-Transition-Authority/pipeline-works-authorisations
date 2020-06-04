<#include '../../../layout.ftl'>
<#import '../pipelines/pipelineOverview.ftl' as pipelineOverviewMacro>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="dependencySectionName" type="java.lang.String" -->
<#-- @ftlvariable name="dependencySectionUrl" type="java.lang.String" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksUrlFactory" -->
<#-- @ftlvariable name="workScheduleViewList" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleView>" -->


<@defaultPage htmlTitle="Campaign works" pageHeading="Campaign works" breadcrumbs=true>

    <@fdsInsetText.insetText>
      <p>Your application requires campaign works information due to the information provided in the ${dependencySectionName} section.</p>
      <p><a href="${springUrl(dependencySectionUrl)}" class="govuk-link">Click here to change your campaign works approach.</a></p>
    </@fdsInsetText.insetText>
    <@fdsAction.link linkText="Add work schedule" linkUrl=springUrl(urlFactory.addWorkScheduleUrl()) linkClass="govuk-button govuk-button--blue"/>

    <#list workScheduleViewList as workSchedule>
        <@fdsCard.card>
            <@fdsCard.cardHeader cardHeadingText="Scheduled ${workSchedule.getFormattedWorkStartDate()} to ${workSchedule.getFormattedWorkEndDate()}">
                <@fdsCard.cardAction cardLinkText="Edit"
                  cardLinkScreenReaderText="Edit work schedule starting ${workSchedule.getFormattedWorkStartDate()} and ending ${workSchedule.getFormattedWorkEndDate()}"
                  cardLinkUrl=springUrl(urlFactory.editWorkScheduleUrl(workSchedule.getPadCampaignWorkScheduleId()))
                 />
            </@fdsCard.cardHeader>

            <#if workSchedule.getSchedulePipelines()?hasContent>
                <table id="work-schedule-${workSchedule?index}" class="govuk-table">
                    <thead class="govuk-table__head">
                    <tr class="govuk-table__row">
                        <th class="govuk-table__header" scope="col">Pipeline number</th>
                        <th class="govuk-table__header" scope="col">Pipeline type</th>
                        <th class="govuk-table__header" scope="col">From</th>
                        <th class="govuk-table__header" scope="col">To</th>
                        <th class="govuk-table__header" scope="col">Length</th>
                    </tr>
                    </thead>
                    <tbody class="govuk-table__body">
                    <#list workSchedule.getSchedulePipelines() as pipeline>
                        <tr class="govuk-table__row">
                            <td class="govuk-table__cell">${pipeline.getPipelineNumber()}</td>
                            <td class="govuk-table__cell">${pipeline.getPipelineType().displayName}</td>
                            <td class="govuk-table__cell">${pipeline.getFromLocation()}</td>
                            <td class="govuk-table__cell">${pipeline.getToLocation()}</td>
                            <td class="govuk-table__cell">${pipeline.getLength()}m</td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
            <#else>
                <p class="govuk-body">No pipelines have been added to this work schedule</p>
            </#if>
        </@fdsCard.card>
    </#list>

</@defaultPage>