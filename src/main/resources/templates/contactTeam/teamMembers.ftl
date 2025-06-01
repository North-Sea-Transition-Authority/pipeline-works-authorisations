<#-- @ftlvariable name="teamName" type="java.lang.String" -->
<#-- @ftlvariable name="contactTeamMemberViews" type="java.util.List<uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.ContactTeamMemberView>" -->
<#-- @ftlvariable name="addUserUrl" type="java.lang.String" -->
<#-- @ftlvariable name="completeSectionUrl" type="java.lang.String" -->
<#-- @ftlvariable name="caseManagementUrl" type="java.lang.String" -->
<#-- @ftlvariable name="showBreadcrumbs" type="java.lang.Boolean" -->
<#-- @ftlvariable name="userCanManageAccess" type="java.lang.Boolean" -->
<#-- @ftlvariable name="userCanAccessTaskList" type="java.lang.Boolean" -->
<#-- @ftlvariable name="showCaseManagementLink" type="java.lang.Boolean" -->
<#-- @ftlvariable name="showTopNav" type="java.lang.Boolean" -->
<#-- @ftlvariable name="allRoles" type="java.util.Map<String,String>" -->
<#-- @ftlvariable name="orgGroupHolders" type="java.util.List<String>" -->
<#-- @ftlvariable name="userType" type="java.util.List<uk.co.ogauthority.pwa.service.enums.users.UserType>" -->

<#include "../layout.ftl">

<@defaultPage htmlTitle=teamName backLink=!showBreadcrumbs pageHeading=teamName topNavigation=showTopNav twoThirdsColumn=false breadcrumbs=showBreadcrumbs>

    <#if allRoles??>
      <#if userType == "INDUSTRY">
        <#if appUser == true>
          <#assign groups>
            <#list orgGroupHolders! as group> ${group} <#sep>, </#list>
          </#assign>
          <@fdsInsetText.insetText>
              <p>Every user in ${groups} has access to view this application. Depending on their role in the organisation they can update the application users listed below, submit the application and will receive notification of consent.</p>
              <p>The users listed below will have access to this application while it is being prepared or processed by the NSTA. They will see this application in their work area. The roles a user has determines the actions they can carry out on this application.</p>
          </@fdsInsetText.insetText>

        <#else>
          <@fdsInsetText.insetText>
            <p>The users listed below have access to all PWAs for your organisation. The roles a user has determines the actions they can carry out on behalf of your organisation.</p>
          </@fdsInsetText.insetText>
        </#if>
        
      </#if>

      <@fdsDetails.summaryDetails summaryTitle="What does each role allow a user to do?" >
          <@fdsCheckAnswers.checkAnswers summaryListClass="">
              <#list allRoles as propName, propValue>
                  <#assign description = propValue?keep_before("(") >
                  <@fdsCheckAnswers.checkAnswersRow keyText="${propName}" actionText="" actionUrl="" screenReaderActionText="">
                      ${description}
                  </@fdsCheckAnswers.checkAnswersRow>
              </#list>
          </@fdsCheckAnswers.checkAnswers>
      </@fdsDetails.summaryDetails>

    </#if>

    <#if userCanManageAccess>
        <@fdsAction.link linkText="Add user" linkUrl=springUrl(addUserUrl) linkClass="govuk-button govuk-button--blue" role=true/>
    </#if>

    <#if showCaseManagementLink?has_content && showCaseManagementLink>
        <@fdsAction.link linkText="View application management" linkClass="govuk-button govuk-button--secondary" role=true linkUrl=springUrl(caseManagementUrl)/>
    </#if>

    <#list contactTeamMemberViews>
      <table class="govuk-table">
        <thead class="govuk-table__head">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="col">Name</th>
          <th class="govuk-table__header" scope="col">Contact details</th>
          <th class="govuk-table__header" scope="col">Roles</th>
          <#if userCanManageAccess>
            <th class="govuk-table__header" scope="col">Actions</th>
          </#if>
        </tr>
        <tbody class="govuk-table__body">
        <#items as teamMemberView>
          <tr class="govuk-table__row">
            <td class="govuk-table__cell">
                ${teamMemberView.fullName}
            </td>
            <td class="govuk-table__cell">
              <ul class="govuk-list">
                <li>${teamMemberView.emailAddress}</li>
                <li>${teamMemberView.telephoneNo}</li>
              </ul>
            </td>
            <td class="govuk-table__cell">
                <#list teamMemberView.roleViews?sort_by("displaySequence") as roleView>
                    ${roleView.title}
                  <br>
                </#list>
            </td>
            <#if userCanManageAccess>
              <td class="govuk-table__cell">
                <ul class="govuk-list">
                  <li>
                      <@fdsAction.link
                        linkUrl=springUrl(teamMemberView.editRoute)
                        linkText="Edit"
                        linkScreenReaderText="Edit ${teamMemberView.fullName} user"
                        linkClass="govuk-link govuk-link--no-visited-state"/>
                  </li>
                  <li>
                      <@fdsAction.link
                        linkUrl=springUrl(teamMemberView.removeRoute)
                        linkText="Remove"
                        linkScreenReaderText="Remove ${teamMemberView.fullName} user"
                        linkClass="govuk-link govuk-link--no-visited-state"/>
                  </li>
                </ul>
              </td>
            </#if>
          </tr>
        </#items>
        </tbody>
      </table>
    </#list>

    <#if userCanAccessTaskList?has_content && userCanAccessTaskList>
      <@fdsAction.link linkText="Complete section" linkClass="govuk-button"  linkUrl=springUrl(completeSectionUrl) role=true/>
    </#if>

</@defaultPage>