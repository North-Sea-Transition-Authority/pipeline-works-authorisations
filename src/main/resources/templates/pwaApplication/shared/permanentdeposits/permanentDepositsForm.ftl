<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="pipelines" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="materialTypes" type="java.util.List<MaterialType>" -->
<#-- @ftlvariable name="longDirections" type="java.util.List<LongitudeDirection>" --> 
<#-- @ftlvariable name="proposedStartDate" type="java.lang.String" --> 

<@defaultPage htmlTitle="${screenAction.actionText} permanent deposit" pageHeading="${screenAction.actionText} permanent deposit" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.depositReference" labelText="Deposit reference" hintText="Uniquely identifies this deposit on this application, e.g. PL1234 grout bags"/>
        
        <@fdsSearchSelector.searchSelectorEnhanced path="form.selectedPipelines" options=pipelines labelText="Pipelines" multiSelect=true optionalInputDefault="Select one or more"
            hintText="Only add more than one pipeline on the same deposit if they’re within the same trench or piggy-backed."/>

        <@fdsNumberInput.twoNumberInputs pathOne="form.fromMonth" pathTwo="form.fromYear" labelText="Month and year of deposit start" formId="from-month-year">
            <@fdsNumberInput.numberInputItem path="form.fromMonth" labelText="Month" inputClass="govuk-input--width-2"/>
            <@fdsNumberInput.numberInputItem path="form.fromYear" labelText="Year" inputClass="govuk-input--width-4"/>
        </@fdsNumberInput.twoNumberInputs>

        <@fdsNumberInput.twoNumberInputs pathOne="form.toMonth" pathTwo="form.toYear" labelText="Month and year of deposit end" formId="to-month-year">
            <@fdsNumberInput.numberInputItem path="form.toMonth" labelText="Month" inputClass="govuk-input--width-2"/>
            <@fdsNumberInput.numberInputItem path="form.toYear" labelText="Year" inputClass="govuk-input--width-4"/>
        </@fdsNumberInput.twoNumberInputs>


        <@fdsRadio.radioGroup path="form.materialType" labelText="Select the material type to be used" hiddenContent=true>                
            <#assign firstItem=true/>
            <#assign contingencyGuidance = "Contingency should be included if appropriate as if more is required you will have to request a new consent"/>
            <#list materialTypes as  materialTypeOption>
                <@fdsRadio.radioItem path="form.materialType" itemMap={materialTypeOption : materialTypeOption.getDisplayText()} isFirstItem=firstItem>   
                    <#if materialTypeOption == "CONCRETE_MATTRESSES">
                        <@fdsTextInput.textInput path="form.concreteMattressLength" nestingPath="form.materialType" labelText="Length" suffix="m" inputClass="govuk-input--width-10"/>
                        <@fdsTextInput.textInput path="form.concreteMattressWidth" nestingPath="form.materialType" labelText="Width" suffix="m" inputClass="govuk-input--width-10"/>
                        <@fdsTextInput.textInput path="form.concreteMattressDepth" nestingPath="form.materialType" labelText="Depth" suffix="m" inputClass="govuk-input--width-10"/>
                        <@fdsTextInput.textInput path="form.quantityConcrete" nestingPath="form.materialType" labelText="Enter quantity of material to be used"/>
                        <@fdsInsetText.insetText> ${contingencyGuidance} </@fdsInsetText.insetText>
                        <@fdsTextInput.textInput path="form.contingencyConcreteAmount" nestingPath="form.materialType" labelText="How much contingency was included?"/> 

                    <#elseif materialTypeOption == "ROCK">
                        <@fdsTextInput.textInput path="form.rocksSize" nestingPath="form.materialType" labelText="Size" hintText="For example 1-5" inputClass="govuk-input--width-10"/>
                        <@fdsTextInput.textInput path="form.quantityRocks" nestingPath="form.materialType" labelText="Quantity of material to be used (decimal tonnes)" inputClass="govuk-input--width-10"/>
                        <@fdsInsetText.insetText> ${contingencyGuidance} </@fdsInsetText.insetText>
                        <@fdsTextInput.textInput path="form.contingencyRocksAmount" nestingPath="form.materialType" labelText="How much contingency was included?"/>

                    <#elseif materialTypeOption == "GROUT_BAGS">
                        <@fdsTextInput.textInput path="form.groutBagsSize" nestingPath="form.materialType" labelText="Size" suffix="kg" inputClass="govuk-input--width-20"/>
                        <@fdsRadio.radioGroup path="form.groutBagsBioDegradable" nestingPath="form.materialType" labelText="Are the grout bags bio-degradable?" hiddenContent=true>  
                            <@fdsRadio.radioYes path="form.groutBagsBioDegradable"/>                                
                            <@fdsRadio.radioNo path="form.groutBagsBioDegradable">
                                <@fdsTextInput.textInput path="form.bioGroutBagsNotUsedDescription" nestingPath="form.materialType" labelText="Why are bio-degradable grout bags not being used?" maxCharacterLength="4000"/>
                            </@fdsRadio.radioNo>                        
                        </@fdsRadio.radioGroup>
                        <@fdsTextInput.textInput path="form.quantityGroutBags" nestingPath="form.materialType" labelText="Quantity of material to be used" inputClass="govuk-input--width-20"/>
                        <@fdsInsetText.insetText> ${contingencyGuidance} </@fdsInsetText.insetText>
                        <@fdsTextInput.textInput path="form.contingencyGroutBagsAmount" nestingPath="form.materialType" labelText="How much contingency was included?"/>

                    <#elseif materialTypeOption == "OTHER">
                        <@fdsTextInput.textInput path="form.otherMaterialSize" nestingPath="form.materialType" labelText="Size" inputClass="govuk-input--width-20"/>
                        <@fdsTextInput.textInput path="form.quantityOther" nestingPath="form.materialType" labelText="Quantity of material to be used" inputClass="govuk-input--width-20"/>
                        <@fdsInsetText.insetText> ${contingencyGuidance} </@fdsInsetText.insetText>
                        <@fdsTextInput.textInput path="form.contingencyOtherAmount" nestingPath="form.materialType" labelText="How much contingency was included?"/>
                    </#if>
                </@fdsRadio.radioItem>
            <#assign firstItem=false/>
            </#list>
        </@fdsRadio.radioGroup>


        <@fdsFieldset.fieldset legendHeading="Where is the start location?">
            <@pwaLocationInput.locationInput degreesLocationPath="form.fromCoordinateForm.latitudeDegrees"
                                          minutesLocationPath="form.fromCoordinateForm.latitudeMinutes"
                                          secondsLocationPath="form.fromCoordinateForm.latitudeSeconds"
                                          formId="fromLatitude"
                                          labelText="Start point latitude"/>

            <@pwaLocationInput.locationInput degreesLocationPath="form.fromCoordinateForm.longitudeDegrees"
                                          minutesLocationPath="form.fromCoordinateForm.longitudeMinutes"
                                          secondsLocationPath="form.fromCoordinateForm.longitudeSeconds"
                                          direction="EW"
                                          directionPath="form.fromCoordinateForm.longitudeDirection"
                                          directionList=longDirections
                                          formId="fromLongitude"
                                          labelText="Start point longitude"/>
        </@fdsFieldset.fieldset> 

        <@fdsFieldset.fieldset legendHeading="Where is the end location?">
            <@pwaLocationInput.locationInput degreesLocationPath="form.toCoordinateForm.latitudeDegrees"
                                          minutesLocationPath="form.toCoordinateForm.latitudeMinutes"
                                          secondsLocationPath="form.toCoordinateForm.latitudeSeconds"
                                          formId="toLatitude" 
                                          labelText="Finish point latitude"/>

            <@pwaLocationInput.locationInput degreesLocationPath="form.toCoordinateForm.longitudeDegrees"
                                          minutesLocationPath="form.toCoordinateForm.longitudeMinutes"
                                          secondsLocationPath="form.toCoordinateForm.longitudeSeconds"
                                          direction="EW"
                                          directionPath="form.toCoordinateForm.longitudeDirection"
                                          directionList=longDirections
                                          formId="toLongitude"
                                          labelText="Finish point longitude"/>
        </@fdsFieldset.fieldset>

        


        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>