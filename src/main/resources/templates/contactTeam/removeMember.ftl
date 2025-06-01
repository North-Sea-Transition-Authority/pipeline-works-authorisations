<#-- @ftlvariable name="teamName" type="String" -->
<#-- @ftlvariable name="error" type="String" -->
<#-- @ftlvariable name="teamMember" type="uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.ContactTeamMemberView" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="showTopNav" type="java.lang.Boolean" -->

<#include '../layout.ftl'>

<@defaultPage htmlTitle="Remove User" backLink=true topNavigation=showTopNav twoThirdsColumn=false>

    <#if error?has_content>
        <@fdsError.singleErrorSummary errorMessage=error/>
    </#if>

    <h1 class="govuk-heading-xl">Are you sure you want to remove this user from the ${teamName} team?</h1>

    <@fdsForm.htmlForm>

        <@fdsCheckAnswers.checkAnswers summaryListClass="">

          <@fdsCheckAnswers.checkAnswersRow keyText="Full name" actionText="" actionUrl="" screenReaderActionText="">
              ${teamMember.getFullName()}
          </@fdsCheckAnswers.checkAnswersRow>

          <@fdsCheckAnswers.checkAnswersRow keyText="Email address" actionText="" actionUrl="" screenReaderActionText="">
              <#if teamMember.emailAddress?has_content>
                  ${teamMember.emailAddress}
              </#if>
          </@fdsCheckAnswers.checkAnswersRow>

          <@fdsCheckAnswers.checkAnswersRow keyText="Telephone number" actionText="" actionUrl="" screenReaderActionText="">
              <#if teamMember.telephoneNo?has_content>
                  ${teamMember.telephoneNo}
              </#if>
          </@fdsCheckAnswers.checkAnswersRow>

          <@fdsCheckAnswers.checkAnswersRow keyText="Roles" actionText="" actionUrl="" screenReaderActionText="">
              <#list teamMember.roleViews?sort_by("displaySequence") as role>
                  ${role.title}<#if role_has_next>,</#if>
              </#list>
          </@fdsCheckAnswers.checkAnswersRow>

        </@fdsCheckAnswers.checkAnswers>

        <@fdsAction.submitButtons primaryButtonText="Remove" secondaryLinkText="Cancel" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>
</@defaultPage>