<#include '../../layout.ftl'>
<#import '../../fileUpload.ftl' as fdsFileUpload/>

<@defaultPage htmlTitle="Administrative Details" pageHeading="Administrative Details" twoThirdsColumn=false>

    <@fdsForm.htmlForm actionUrl="" useMethod="">
        <div class="govuk-form-group">
            <@fdsTextArea.textarea path="form.projectDescription" labelText="Project description" hintText="Please provide a detailed overview of your application"/>
            <@fdsFieldset.fieldset legendHeading="Project diagram" legendHeadingClass="govuk-label" legendHeadingSize="h3"/>
            <@fdsFileUpload.fileUpload id="upload" allowedExtensions="txt" deleteUrl="" downloadUrl="" maxAllowedSize="200" uploadUrl=""/>
        </div>

        <div class="govuk-form-group">
            <@fdsFieldset.fieldset legendHeading="HSE" legendHeadingClass="govuk-fieldset__legend--l" legendHeadingSize="h2"/>
            <@fdsRadio.radioGroup path="form.withinSafetyZone" labelText="Is your application within 500m of a HSE safety zone?" hiddenContent=true fieldsetHeadingSize="h3">
                <@fdsRadio.radioItem path="form.withinSafetyZone" itemMap={"WITHIN_SAFETY_ZONE": "Yes"}>
                    <@fdsTextInput.textInput path="form.structureName" labelText="What is the name of the structure that you will be within 500m of?"/>
                </@fdsRadio.radioItem>
                <@fdsRadio.radioItem path="form.withinSafetyZone" itemMap={"PARTIAL": "Partially"}>
                    <@fdsTextInput.textInput path="form.structureName" labelText="What is the name of the structure that you will be within 500m of?"/>
                </@fdsRadio.radioItem>
                <@fdsRadio.radioItem path="form.withinSafetyZone" itemMap={"NOT_WITHIN_SAFETY_ZONE": "No"}/>
            </@fdsRadio.radioGroup>
        </div>

        <div class="govuk-form-group">
            <@fdsFieldset.fieldset legendHeading="Location" legendHeadingClass="govuk-fieldset__legend--l" legendHeadingSize="h2"/>
            <@fdsRadio.radioGroup path="form.whollyOffshore" labelText="Are all facilities wholly offshore and subsea?" hiddenContent=true fieldsetHeadingSize="h3">
                <@fdsRadio.radioYes path="form.whollyOffshore"/>
                <@fdsRadio.radioNo path="form.whollyOffshore">
                    <@fdsSelect.select path="form.methodOfTransportation" options=transportationMethods labelText="Method of transportation to shore"/>
                    <@fdsTextArea.textarea path="form.landfallDetails" labelText="Details of nearest landfall"/>
                </@fdsRadio.radioNo>
            </@fdsRadio.radioGroup>
            <@fdsTextInput.textInput path="form.locationFromShore" labelText="Approx. location from shore"/>
        </div>

        <div class="govuk-form-group">
            <@fdsFieldset.fieldset legendHeading="Acknowledgements" legendHeadingClass="govuk-fieldset__legend--l" legendHeadingSize="h2"/>
            <@fdsCheckbox.checkbox path="form.agreesToFdp" labelText="The proposed works outlined in this application are consistent with the development as described in the FDP."/>
            <@fdsCheckbox.checkbox path="form.acceptFundsLiability" labelText="I hereby confirm that ${holderCompanyName} has funds available to discharge any liability for damage attributable to the release or escape of anything from the pipeline."/>
            <@fdsCheckbox.checkbox path="form.acceptOpolLiability" labelText="I acknowledge liability insurance in respect of North Sea operations is arranged under the General Third Party Liability Risk Insurance and the complementary arrangements effected under the Offshore Pollution Liability Agreement (OPOL) of ${holderCompanyName}."/>
        </div>
        <@fdsAction.submitButtons errorMessage="" linkSecondaryAction=true primaryButtonText="Complete" secondaryLinkText="Save and continue later"/>
    </@fdsForm.htmlForm>

</@defaultPage>