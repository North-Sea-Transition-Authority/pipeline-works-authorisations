<#-- @ftlvariable name="teamMemberView" type="uk.co.ogauthority.pwa.teams.management.view.TeamMemberView" -->
<#include '../layout.ftl'>
<#import 'roleDescriptions.ftl' as roleDescriptions>

<@defaultPage
    htmlTitle="Edit member roles"
    pageHeading=""
    twoThirdsColumn=true
>
    <@fdsForm.htmlForm>

        <#assign warning>
            <#if !userHasAllowedEmail>
                <@fdsWarning.warning>
                  This user's email is not from an approved domain for this team.
                </@fdsWarning.warning>
            </#if>
        </#assign>

        <@fdsCheckbox.checkboxes
            fieldsetHeadingText="What actions does ${teamMemberView.getDisplayName()} perform?"
            hintText=warning
            fieldsetHeadingSize="h1"
            fieldsetHeadingClass="govuk-fieldset__legend--l"
            path="form.roles"
            checkboxes=rolesNamesMap
        />

        <@roleDescriptions.roleDescriptions roles=rolesInTeam/>

        <@fdsAction.submitButtons
            primaryButtonText="Save and continue"
            secondaryLinkText="Cancel"
            linkSecondaryAction=true
            linkSecondaryActionUrl=springUrl(cancelUrl)
        />
    </@fdsForm.htmlForm>
</@defaultPage>