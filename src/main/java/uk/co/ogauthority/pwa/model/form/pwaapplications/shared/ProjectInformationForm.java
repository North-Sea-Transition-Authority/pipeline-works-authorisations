package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public class ProjectInformationForm {

  public interface Full {
  }

  public interface Partial {
  }

  @NotNull(message = "Enter the project name", groups = {Full.class})
  @Length(max = 4000, message = "Project name must be 4000 characters or fewer", groups = {Full.class, Partial.class})
  private String projectName;

  @NotNull(message = "Enter the project overview", groups = {Full.class})
  @Length(max = 4000, message = "Project overview must be 4000 characters or fewer", groups = {Full.class, Partial.class})
  private String projectOverview;

  @NotNull(message = "Enter the pipeline installation method", groups = {Full.class})
  @Length(max = 4000, message = "Pipeline installation method must be 4000 characters or fewer", groups = {Full.class, Partial.class})
  private String methodOfPipelineDeployment;

  private Integer proposedStartDay;
  private Integer proposedStartMonth;
  private Integer proposedStartYear;

  private Integer mobilisationDay;
  private Integer mobilisationMonth;
  private Integer mobilisationYear;

  private Integer earliestCompletionDay;
  private Integer earliestCompletionMonth;
  private Integer earliestCompletionYear;

  private Integer latestCompletionDay;
  private Integer latestCompletionMonth;
  private Integer latestCompletionYear;

  @NotNull(message = "Select yes if using a campaign approach", groups = {Full.class})
  private Boolean usingCampaignApproach;

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getProjectOverview() {
    return projectOverview;
  }

  public void setProjectOverview(String projectOverview) {
    this.projectOverview = projectOverview;
  }

  public String getMethodOfPipelineDeployment() {
    return methodOfPipelineDeployment;
  }

  public void setMethodOfPipelineDeployment(String methodOfPipelineDeployment) {
    this.methodOfPipelineDeployment = methodOfPipelineDeployment;
  }

  public Integer getProposedStartDay() {
    return proposedStartDay;
  }

  public void setProposedStartDay(Integer proposedStartDay) {
    this.proposedStartDay = proposedStartDay;
  }

  public Integer getProposedStartMonth() {
    return proposedStartMonth;
  }

  public void setProposedStartMonth(Integer proposedStartMonth) {
    this.proposedStartMonth = proposedStartMonth;
  }

  public Integer getProposedStartYear() {
    return proposedStartYear;
  }

  public void setProposedStartYear(Integer proposedStartYear) {
    this.proposedStartYear = proposedStartYear;
  }

  public Integer getMobilisationDay() {
    return mobilisationDay;
  }

  public void setMobilisationDay(Integer mobilisationDay) {
    this.mobilisationDay = mobilisationDay;
  }

  public Integer getMobilisationMonth() {
    return mobilisationMonth;
  }

  public void setMobilisationMonth(Integer mobilisationMonth) {
    this.mobilisationMonth = mobilisationMonth;
  }

  public Integer getMobilisationYear() {
    return mobilisationYear;
  }

  public void setMobilisationYear(Integer mobilisationYear) {
    this.mobilisationYear = mobilisationYear;
  }

  public Integer getEarliestCompletionDay() {
    return earliestCompletionDay;
  }

  public void setEarliestCompletionDay(Integer earliestCompletionDay) {
    this.earliestCompletionDay = earliestCompletionDay;
  }

  public Integer getEarliestCompletionMonth() {
    return earliestCompletionMonth;
  }

  public void setEarliestCompletionMonth(Integer earliestCompletionMonth) {
    this.earliestCompletionMonth = earliestCompletionMonth;
  }

  public Integer getEarliestCompletionYear() {
    return earliestCompletionYear;
  }

  public void setEarliestCompletionYear(Integer earliestCompletionYear) {
    this.earliestCompletionYear = earliestCompletionYear;
  }

  public Integer getLatestCompletionDay() {
    return latestCompletionDay;
  }

  public void setLatestCompletionDay(Integer latestCompletionDay) {
    this.latestCompletionDay = latestCompletionDay;
  }

  public Integer getLatestCompletionMonth() {
    return latestCompletionMonth;
  }

  public void setLatestCompletionMonth(Integer latestCompletionMonth) {
    this.latestCompletionMonth = latestCompletionMonth;
  }

  public Integer getLatestCompletionYear() {
    return latestCompletionYear;
  }

  public void setLatestCompletionYear(Integer latestCompletionYear) {
    this.latestCompletionYear = latestCompletionYear;
  }

  public Boolean getUsingCampaignApproach() {
    return usingCampaignApproach;
  }

  public void setUsingCampaignApproach(Boolean usingCampaignApproach) {
    this.usingCampaignApproach = usingCampaignApproach;
  }
}
