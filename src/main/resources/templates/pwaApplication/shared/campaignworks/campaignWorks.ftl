<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="dependencySectionName" type="java.lang.String" -->
<#-- @ftlvariable name="dependencySectionUrl" type="java.lang.String" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksUrlFactory" -->


<@defaultPage htmlTitle="Campaign works" pageHeading="Campaign works" breadcrumbs=true>

    <@fdsInsetText.insetText>
        <p>Your application requires campaign works information due to the information provided in the ${dependencySectionName} section.</p>
        <p><a href="${springUrl(dependencySectionUrl)}" class="govuk-link">Click here to change your campaign works approach.</a></p>
        <p>You will lose any progress on this page by clicking this link.</p>
    </@fdsInsetText.insetText>
    <@fdsAction.link linkText="Add work schedule" linkUrl=springUrl(urlFactory.addWorkScheduleUrl()) linkClass="govuk-button govuk-button--blue"/>


</@defaultPage>