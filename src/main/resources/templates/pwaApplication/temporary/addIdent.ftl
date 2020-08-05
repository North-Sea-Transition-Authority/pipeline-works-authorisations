<#include '../../layout.ftl'>
<#import '../../pwaApplication/temporary/widgets/locationInput.ftl' as tempLocationInput>

<#-- @ftlvariable name="form" type="uk.co.ogauthority.pwa.temp.model.form.AddIdentForm" -->
<#-- @ftlvariable name="identNo" type="Integer" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->

<@defaultPage htmlTitle="Add ident" pageHeading="Add ident" breadcrumbs=true>
    <@fdsForm.htmlForm>

        <@fdsFieldset.fieldset legendHeading="Where does this ident start?" legendHeadingSize="h2">

            <@fdsTextInput.textInput path="form.from" labelText="Structure" />

            <@tempLocationInput.locationInput degreesLocationPath="form.fromLatitudeDegrees" minutesLocationPath="form.fromLatitudeMinutes" secondsLocationPath="form.fromLatitudeSeconds" />
            <@tempLocationInput.locationInput degreesLocationPath="form.fromLongitudeDegrees" minutesLocationPath="form.fromLongitudeMinutes" secondsLocationPath="form.fromLongitudeSeconds" direction="EW" />

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Where does this ident finish?" legendHeadingSize="h2">

            <@fdsTextInput.textInput path="form.to" labelText="Structure" />

            <@tempLocationInput.locationInput degreesLocationPath="form.toLatitudeDegrees" minutesLocationPath="form.toLatitudeMinutes" secondsLocationPath="form.toLatitudeSeconds" />
            <@tempLocationInput.locationInput degreesLocationPath="form.toLongitudeDegrees" minutesLocationPath="form.toLongitudeMinutes" secondsLocationPath="form.toLongitudeSeconds" direction="EW" />

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Ident information" legendHeadingSize="h2">

            <p class="govuk-body"><span class="govuk-!-font-weight-bold">Ident no.</span> ${identNo}</p>

            <@fdsTextInput.textInput path="form.componentParts" labelText="Description of component parts of the pipeline" hintText="e.g. 10\" production flowline, electrical lead d B, 2 x 6\" Production Jumper within a Wellhead Bundle, 6\" flexible gas lift flowline, control umbilical etc" />
            <@fdsTextInput.textInput path="form.typeOfInsulationOrCoating" labelText="Type of insulation / coating" />
            <@fdsTextInput.textInput path="form.productsToBeConveyed" labelText="Products to be conveyed" />

            <@fdsTextInput.textInput path="form.length" labelText="Length (m)" inputClass="govuk-input--width-4"/>
            <@fdsTextInput.textInput path="form.externalDiameter" labelText="External diameter (mm)" inputClass="govuk-input--width-4" />
            <@fdsTextInput.textInput path="form.internalDiameter" labelText="Internal diameter (mm)" inputClass="govuk-input--width-4" />
            <@fdsTextInput.textInput path="form.wallThickness" labelText="Wall thickness (mm)" inputClass="govuk-input--width-4" />
            <@fdsTextInput.textInput path="form.maop" labelText="MAOP (Barg)" inputClass="govuk-input--width-4" />

        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Add ident" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>
</@defaultPage>