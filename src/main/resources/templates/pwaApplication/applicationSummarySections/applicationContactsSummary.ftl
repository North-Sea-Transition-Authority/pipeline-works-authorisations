<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="teamMemberViews" type="java.util.List<uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView>" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="appContactDetails">${sectionDisplayText}</h2>

    <@appContactDetails teamMemberViews/>

</div>


<#macro appContactDetails teamMemberViews>
  
    <#list teamMemberViews as teamMemberView>
        <@fdsCheckAnswers.checkAnswers>
            
            <h3 class="govuk-heading-m">${teamMemberView.getFullName()}</h3>
            
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
    </#list>

</#macro>


