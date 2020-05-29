<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" --> 
<#-- @ftlvariable name="deposits" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositsOverview>" --> 


<#macro depositViewSummary deposit depositIndex>
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
                <#list deposit.pipelinesRefs as pipelineRef>${pipelineRef}<br> </#list>
            </dd>                    
        </div>
        <div class="govuk-summary-list__row">
            <dt class="govuk-summary-list__key">Proposed start date</dt>
            <dd class="govuk-summary-list__value"> ${deposit.fromMonth} / ${deposit.fromYear?c}</dd>                    
        </div>
        <div class="govuk-summary-list__row">
            <dt class="govuk-summary-list__key">End date</dt>
            <dd class="govuk-summary-list__value"> ${deposit.toMonth} / ${deposit.toYear?c}</dd>                 
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
                <@pwaCoordinate.display coordinatePair=permanentDepositDataFormatFactory.getFromCoordinatesPairFromForm(depositIndex) />                            
            </dd>   
        </div>
        <div class="govuk-summary-list__row">
            <dt class="govuk-summary-list__key">To (WGS84)</dt>
            <dd class="govuk-summary-list__value"> 
                <@pwaCoordinate.display coordinatePair=permanentDepositDataFormatFactory.getToCoordinatesPairFromForm(depositIndex) />      
            </dd>                    
        </div>  
    </dl>
</#macro>