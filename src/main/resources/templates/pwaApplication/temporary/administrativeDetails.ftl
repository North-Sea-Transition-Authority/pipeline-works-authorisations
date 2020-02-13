<#include '../../layout.ftl'>
<#import '../../fileUpload.ftl' as fdsFileUpload/>

<@defaultPage htmlTitle="Administrative Details" pageHeading="Administrative Details" twoThirdsColumn=false>

    <@fdsForm.htmlForm actionUrl="" useMethod="">
        <@fdsTextArea.textarea path="form.projectDescription" labelText="Project description" hintText="Please provide a detailed overview of your application"/>

        <@fdsRadio.radioGroup path="form.withinSafetyZone" labelText="Is your application within 500m of a HSE safety zone?" hiddenContent=true>
            <@fdsRadio.radioYes path="form.withinSafetyZone">
                <@fdsTextInput.textInput path="form.structureName" labelText="What is the name of the structure that you will be within 500m of?"/>
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path="form.withinSafetyZone"/>
        </@fdsRadio.radioGroup>
        <@fdsSelect.select path="form.methodOfTransportation" options=transportationMethods labelText="Method of transportation to shore"/>
        <@fdsTextInput.textInput path="form.locationFromShore" labelText="Approx. location from shore"/>
        <@fdsTextArea.textarea path="form.landfallDetails" labelText="Details of nearest landfall"/>

        <@fdsFieldset.fieldset legendHeading="Project diagram" legendHeadingClass="govuk-fieldset__legend--m" legendHeadingSize="h2" hintText="Please provide a project diagram"/>
        <@fdsFileUpload.fileUpload id="upload" allowedExtensions="txt" deleteUrl="" downloadUrl="" maxAllowedSize="200" uploadUrl=""/>

        <@fdsCheckbox.checkbox path="form.agreesToFdp" labelText="The proposed works outlined in this application are consistent with the development as described in the FDP."/>
        <@fdsCheckbox.checkbox path="form.acceptFundsLiability" labelText="I hereby confirm that ${holderCompanyName} has funds available to discharge any liability for damage attributable to the release or escape of anything from the pipeline."/>
        <@fdsCheckbox.checkbox path="form.acceptOpolLiability" labelText="I acknowledge liability insurance in respect of North Sea operations is arranged under the General Third Party Liability Risk Insurance and the complementary arrangements effected under the Offshore Pollution Liability Agreement (OPOL) of ${holderCompanyName}."/>
        <@fdsAction.submitButtons errorMessage="" linkSecondaryAction=true primaryButtonText="Complete" secondaryLinkText="Save and continue later"/>
    </@fdsForm.htmlForm>

</@defaultPage>