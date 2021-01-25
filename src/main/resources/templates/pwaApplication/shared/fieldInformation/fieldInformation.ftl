<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Field information" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
    </#if>

    <@fdsForm.htmlForm>

        <@fdsRadio.radioGroup path="form.linkedToField" labelText="Is this PWA related to a field?" fieldsetHeadingSize="h1" fieldsetHeadingClass="govuk-fieldset__legend--l" hiddenContent=true hintText="This is the field name referenced in your PWA title.">
            <@fdsRadio.radioYes path="form.linkedToField">
                <@fdsSearchSelector.searchSelectorRest path="form.fieldIds" restUrl=springUrl(fieldNameRestUrl) labelText="Which fields does this PWA cover?" multiSelect=true nestingPath="form.linkedToField"  preselectedItems=preSelectedItems />
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path="form.linkedToField">
                <@fdsTextarea.textarea path="form.noLinkedFieldDescription" labelText="What is this PWA related to?" hintText="e.g. Scotland/Ireland interconnector" characterCount=true maxCharacterLength="4000"/>
            </@fdsRadio.radioNo>
        </@fdsRadio.radioGroup>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>