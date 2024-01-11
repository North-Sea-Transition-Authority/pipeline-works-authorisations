<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="Field information" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>

        <@fdsRadio.radioGroup path="form.linkedToArea" labelText="Is this PWA related to a field?" fieldsetHeadingSize="h1" fieldsetHeadingClass="govuk-fieldset__legend--l" hiddenContent=true hintText="This is the field name referenced in your PWA title.">
            <@fdsRadio.radioYes path="form.linkedToArea">
                <@fdsSearchSelector.searchSelectorRest path="form.linkedAreas" restUrl=springUrl(fieldNameRestUrl) labelText="Which fields does this PWA cover?" multiSelect=true nestingPath="form.linkedToArea"  preselectedItems=preSelectedItems />
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path="form.linkedToArea">
                <@fdsTextarea.textarea path="form.noLinkedAreaDescription" labelText="What is this PWA related to?" characterCount=true maxCharacterLength=maxCharacterLength?c/>
            </@fdsRadio.radioNo>
        </@fdsRadio.radioGroup>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>
