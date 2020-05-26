<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" --> 
<#-- @ftlvariable name="deposit" type="uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm" --> 


<@defaultPage htmlTitle="Remove permanent deposit" pageHeading="Remove permanent deposit" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>
        <h2 class="govuk-heading-m">Are you sure you want to remove the deposit: ${deposit.depositReference}</h2>
        
        <dl class="govuk-summary-list govuk-!-margin-bottom-9">          

            <#assign size="" quantity="" contingency="" groutBagsDescription=""/>
            <#if deposit.materialType = "CONCRETE_MATTRESSES">
                <#assign size= deposit.concreteMattressLength + "m x " + deposit.concreteMattressWidth + "m x " + deposit.concreteMattressDepth + "m"/>
                <#assign quantity=deposit.quantityConcrete/>
                <#if deposit.contingencyConcreteAmount??> <#assign contingency=deposit.contingencyConcreteAmount/> </#if>     
                
            <#elseif deposit.materialType = "ROCK">
                <#assign size="Grade " + deposit.rocksSize/>
                <#assign quantity=deposit.quantityRocks/>
                <#if deposit.contingencyRocksAmount??> <#assign contingency=deposit.contingencyRocksAmount/> </#if>                  
                
            <#elseif deposit.materialType = "GROUT_BAGS">
                <#assign size=deposit.groutBagsSize + "kg"/>
                <#assign quantity=deposit.quantityGroutBags/>  
                <#if deposit.contingencyGroutBagsAmount??> <#assign contingency=deposit.contingencyGroutBagsAmount/> </#if>               
                <#if deposit.groutBagsBioDegradable?? && deposit.groutBagsBioDegradable == false> <#assign groutBagsDescription=deposit.bioGroutBagsNotUsedDescription/> </#if> 
                
            <#elseif deposit.materialType = "OTHER">
                <#assign size=deposit.otherMaterialSize/>
                <#assign quantity=deposit.quantityOther/>
                <#if deposit.contingencyOtherAmount??> <#assign contingency=deposit.contingencyOtherAmount/> </#if>    
            </#if>

            <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">Pipelines</dt>
                <dd class="govuk-summary-list__value">
                    <#list deposit.selectedPipelines as pipeline>${pipeline}<br> </#list>
                </dd>                    
            </div>
            <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">Proposed date</dt>
                <dd class="govuk-summary-list__value"> ${deposit.fromMonth} / ${deposit.fromYear}</dd>                    
            </div>
            <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">to date</dt>
                <dd class="govuk-summary-list__value"> ${deposit.toMonth} / ${deposit.toYear}</dd>                    
            </div>
            <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">Type of materials</dt>
                <dd class="govuk-summary-list__value"> ${deposit.materialType.getDisplayText()}</dd>                    
            </div>
            <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">Size</dt>
                <dd class="govuk-summary-list__value"> ${size} </dd>                    
            </div>
            <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">Quantity</dt>
                <dd class="govuk-summary-list__value"> ${quantity}</dd>                    
            </div>
            <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">Contingency included</dt>
                <dd class="govuk-summary-list__value"> ${contingency}</dd>                    
            </div>
            <#if deposit.groutBagsBioDegradable?? && deposit.groutBagsBioDegradable == false>
                <div class="govuk-summary-list__row">
                    <dt class="govuk-summary-list__key">Bio-degradable grout bags</dt>
                    <dd class="govuk-summary-list__value"> ${groutBagsDescription}</dd>                  
                </div>
            </#if>

            <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">From (WGS84)</dt>
                <dd class="govuk-summary-list__value"> 
                    <@pwaCoordinate.display coordinatePair=permanentDepositDataFormatFactory.getFromCoordinatesPairFromForm(0) />                            
                </dd>   
            </div>
            <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">To (WGS84)</dt>
                <dd class="govuk-summary-list__value"> 
                    <@pwaCoordinate.display coordinatePair=permanentDepositDataFormatFactory.getToCoordinatesPairFromForm(0) />      
                </dd>                    
            </div>  
        </dl>
       


        <@fdsAction.submitButtons primaryButtonText="Remove" secondaryLinkText="Cancel" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>