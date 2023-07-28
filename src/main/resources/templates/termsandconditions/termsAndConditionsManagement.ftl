<#include '../layout.ftl'>

<@defaultPage htmlTitle="Terms and conditions" pageHeading="Terms and conditions" pageHeadingClass="govuk-heading-xl govuk-!-margin-bottom-2" fullWidthColumn=false wrapperWidth=true topNavigation=true>

    <@fdsAction.link linkText="Add new terms and conditions record" linkUrl=springUrl(termsAndConditionsFormUrl) linkClass="govuk-button" role=true/>

    <@fdsSearch.searchPage>
        <@fdsSearch.searchFilter>
            <@fdsSearch.searchFilterList filterButtonClass="govuk-button govuk-button--secondary" clearFilterUrl=springUrl(clearFilterUrl)>
                <@fdsSearch.searchFilterItem itemName="PWA reference">
                    <@fdsSearch.searchTextInput path="form.pwaReference" labelText="PWA reference" labelClass="govuk-visually-hidden"/>
                </@fdsSearch.searchFilterItem>
            </@fdsSearch.searchFilterList>
        </@fdsSearch.searchFilter>
        <@fdsSearch.searchPageContent>
            <#if (termsAndConditionsPageView.totalElements > 0)>
                <@fdsResultList.resultList resultCount=termsAndConditionsPageView.totalElements>
                    <#list termsAndConditionsPageView.pageContent as term>
                        <@fdsResultList.resultListItem linkHeadingUrl=springUrl(term.viewPwaUrl) linkHeadingText=term.pwaReference>
                            <@fdsResultList.resultListDataItem>
                                <@fdsResultList.resultListDataValue key="Variation term" value=term.variationTerm/>
                                <@fdsResultList.resultListDataValue key="HUOO terms" value=term.huooTerms/>
                                <@fdsResultList.resultListDataValue key="Depcon paragraph" value=term.depconParagraph/>
                                <@fdsResultList.resultListDataValue key="Depcon schedule" value=term.depconSchedule/>
                            </@fdsResultList.resultListDataItem>
                        </@fdsResultList.resultListItem>
                    </#list>
                </@fdsResultList.resultList>
                <@fdsPagination.pagination pageView=termsAndConditionsPageView/>
            <#else>
                <@fdsInsetText.insetText>
                    No terms and condition records found.
                </@fdsInsetText.insetText>
            </#if>
        </@fdsSearch.searchPageContent>
    </@fdsSearch.searchPage>

</@defaultPage>