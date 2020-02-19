<#include '../../layout.ftl'>

<!-- @ftlvariable name="projectInformationUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Fast-track information" pageHeading="Fast-track information">
    <@fdsForm.htmlForm>
        <@fdsWarning.warning>
          You application will be fast-tracked as it's due to start on ${startDate}, which is outside of the minimum review period. All fast-tracked applications require approval prior to being processed.
          <p>If this is a mistake, you can <@fdsAction.link linkUrl=springUrl(projectInformationUrl) linkText="modify your start date here"/></p>
        </@fdsWarning.warning>
        <@fdsTextarea.textarea path="form.justification" labelText="Justification" hintText="Why are you fast-tracking this application?"/>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Complete" secondaryLinkText="Save and continue later"/>
    </@fdsForm.htmlForm>
</@defaultPage>