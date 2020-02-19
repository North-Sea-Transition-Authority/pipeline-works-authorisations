<#include '../../layout.ftl'>

<!-- @ftlvariable name="projectInformationUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Fast-track information" pageHeading="Fast-track information">
    <@fdsForm.htmlForm>
        <@fdsWarning.warning>
          You application will be fast-tracked due to its start date. Pre-approval is required for all fast-tracked applications.
          If this is a mistake, you can <@fdsAction.link linkUrl=springUrl(projectInformationUrl) linkText="modify your start date here"/>
        </@fdsWarning.warning>
        <@fdsTextarea.textarea path="form.justification" labelText="Justification" hintText="Why are you fast-tracking this application?"/>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Complete" secondaryLinkText="Save and continue later"/>
    </@fdsForm.htmlForm>
</@defaultPage>