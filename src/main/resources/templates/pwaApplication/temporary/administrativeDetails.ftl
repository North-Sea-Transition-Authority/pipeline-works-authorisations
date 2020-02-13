<#include '../../layout.ftl'>
<#import '../../dummyFileUpload.ftl' as dummyFileUpload/>

<@defaultPage htmlTitle="Administrative Details" pageHeading="Administrative details" twoThirdsColumn=true>

    <@fdsForm.htmlForm>
        <@fdsFieldset.fieldset legendHeading="Project overview" legendHeadingClass="govuk-fieldset__legend--l" legendHeadingSize="h2"/>
        <@fdsTextArea.textarea path="form.projectDescription" labelText="Project description" hintText="A brief description of the project, and why it is needed"/>
        <h3 class="govuk-heading-m">
            Project diagram
            <span class="govuk-hint">Provide an overall project layout diagram showing pipeline(s) to be covered by the Authorisation and route of the pipeline(s)</span>
        </h3>
        <@dummyFileUpload.fileUpload id="upload" allowedExtensions="txt" deleteUrl="" downloadUrl="" maxAllowedSize="200" uploadUrl=""/>
        <hr class="govuk-section-break govuk-section-break--m">

        <h2 class="govuk-heading-l">HSE</h2>
        <@fdsRadio.radioGroup path="form.withinSafetyZone" labelText="Is your application within 500m of a HSE safety zone?" hiddenContent=true fieldsetHeadingSize="h3">
            <@fdsRadio.radioItem path="form.withinSafetyZone" itemMap={"WITHIN_SAFETY_ZONE": "Yes"}>
                <@fdsTextInput.textInput path="form.structureName" labelText="What is the name of the structure that you will be within 500m of?"/>
            </@fdsRadio.radioItem>
            <@fdsRadio.radioItem path="form.withinSafetyZone" itemMap={"PARTIAL": "Partially"}>
                <@fdsTextInput.textInput path="form.structureName" labelText="What is the name of the structure that you will be within 500m of?"/>
            </@fdsRadio.radioItem>
            <@fdsRadio.radioItem path="form.withinSafetyZone" itemMap={"NOT_WITHIN_SAFETY_ZONE": "No"}/>
        </@fdsRadio.radioGroup>

        <h2 class="govuk-heading-l">Location</h2>
        <@fdsRadio.radioGroup path="form.whollyOffshore" labelText="Are all facilities wholly offshore and subsea?" hiddenContent=true fieldsetHeadingSize="h3">
            <@fdsRadio.radioYes path="form.whollyOffshore"/>
            <@fdsRadio.radioNo path="form.whollyOffshore">
                <@fdsTextArea.textarea path="form.methodOfTransportation" labelText="Method of transportation to shore"/>
                <@fdsTextArea.textarea path="form.landfallDetails" labelText="Landfall details"/>
            </@fdsRadio.radioNo>
        </@fdsRadio.radioGroup>
        <@fdsTextInput.textInput path="form.locationFromShore" labelText="Approximate location from shore"/>

        <@fdsFieldset.fieldset legendHeading="Acknowledgements" legendHeadingClass="govuk-fieldset__legend--l" legendHeadingSize="h2">
            <@fdsCheckbox.checkbox path="form.agreesToFdp" labelText="The proposed works outlined in this application are consistent with the development as described in the FDP."/>
            <@fdsCheckbox.checkbox path="form.acceptFundsLiability" labelText="I hereby confirm that ${holderCompanyName} has funds available to discharge any liability for damage attributable to the release or escape of anything from the pipeline."/>
            <@fdsCheckbox.checkbox path="form.acceptOpolLiability" labelText="I acknowledge liability insurance in respect of North Sea operations is arranged under the General Third Party Liability Risk Insurance and the complementary arrangements effected under the Offshore Pollution Liability Agreement (OPOL) of ${holderCompanyName}."/>
        </@fdsFieldset.fieldset>
        <@fdsAction.submitButtons errorMessage="" linkSecondaryAction=true primaryButtonText="Complete" secondaryLinkText="Save and continue later"/>
    </@fdsForm.htmlForm>

</@defaultPage>