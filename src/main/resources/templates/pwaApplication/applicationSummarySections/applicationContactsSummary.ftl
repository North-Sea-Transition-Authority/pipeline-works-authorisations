<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="teamMemberViews" type="java.util.List<uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView>" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="appContactDetails">${sectionDisplayText}</h2>

    <@appContactDetails teamMemberViews/>

</div>


<#macro appContactDetails teamMemberViews>
  
    <#list teamMemberViews as teamMemberView>
        <@fdsCheckAnswers.checkAnswersWrapper summaryListId=teamMemberView?index headingText=teamMemberView.getFullName() headingSize="h3" headingClass="govuk-heading-m">
            <@fdsCheckAnswers.checkAnswers>

                <@fdsCheckAnswers.checkAnswersRow keyText="Email address" actionUrl="" screenReaderActionText="" actionText="">
                    ${teamMemberView.emailAddress}
                </@fdsCheckAnswers.checkAnswersRow>

                <@fdsCheckAnswers.checkAnswersRow keyText="Phone" actionUrl="" screenReaderActionText="" actionText="">
                    ${teamMemberView.telephoneNo}
                </@fdsCheckAnswers.checkAnswersRow>

                <@fdsCheckAnswers.checkAnswersRow keyText="Roles" actionUrl="" screenReaderActionText="" actionText="">
                    <ul class="govuk-list">
                        <#list teamMemberView.roleViews as roleView>
                            <li> ${roleView.title} </li>
                        </#list>
                    </ul>
                </@fdsCheckAnswers.checkAnswersRow>

            </@fdsCheckAnswers.checkAnswers>
        </@fdsCheckAnswers.checkAnswersWrapper>
    </#list>

</#macro>


