<#include '../layout.ftl'>

<@defaultPage htmlTitle=team.getName() twoThirdsColumn=false backLink=true>
    <@fdsForm.htmlForm>
        <@fdsCheckbox.checkboxes path="form.userRoles" checkboxes=roles fieldsetHeadingText="What actions does " + teamUser.getFullName() + " perform?" fieldsetHeadingSize="h2" fieldsetHeadingClass="govuk-fieldset__legend--l"/>
        <@fdsAction.submitButtons primaryButtonText="Save" secondaryLinkText="Cancel" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>