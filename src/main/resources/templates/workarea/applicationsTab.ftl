<#include '../layout.ftl'>
<#import '_applicationWorkAreaItem.ftl' as applicationWorkAreaItem>

<#-- @ftlvariable name="workAreaPageView" type="uk.co.ogauthority.pwa.mvc.PageView<uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem>" -->

<#macro tab workAreaPageView>

    <#if workAreaPageView?has_content>

        <#if !workAreaPageView.getPageContent()?has_content>
            <@fdsInsetText.insetText>You have no open applications.</@fdsInsetText.insetText>
        <#else>
            <table class="govuk-table">
                <thead class="govuk-table__head">
                    <tr class="govuk-table__row">
                        <th class="govuk-table__header govuk-!-width-one-third" scope="col">Application</th>
                        <th class="govuk-table__header" scope="col">Holder</th>
                        <th class="govuk-table__header govuk-!-width-one-third" scope="col">Summary</th>
                        <th class="govuk-table__header govuk-!-width-one-third" scope="col">Application status</th>
                    </tr>
                </thead>
                <tbody class="govuk-table__body">
                <#list workAreaPageView.getPageContent() as item>
                    <@applicationWorkAreaItem.workAreaItemContentRow item/>
                </#list>
                </tbody>
          </table>

          <@fdsPagination.pagination pageView=workAreaPageView />

        </#if>

    </#if>

</#macro>