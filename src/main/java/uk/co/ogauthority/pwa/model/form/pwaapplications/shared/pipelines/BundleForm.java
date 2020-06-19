package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines;

import java.util.List;
import java.util.Objects;

public class BundleForm {

  private String bundleName;
  private List<Integer> pipelineIds;

  public BundleForm() {
  }

  public BundleForm(String name, List<Integer> pipelineIds) {
    this.bundleName = name;
    this.pipelineIds = pipelineIds;
  }

  public String getBundleName() {
    return bundleName;
  }

  public void setBundleName(String name) {
    this.bundleName = name;
  }

  public List<Integer> getPipelineIds() {
    return pipelineIds;
  }

  public void setPipelineIds(List<Integer> pipelineIds) {
    this.pipelineIds = pipelineIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BundleForm that = (BundleForm) o;
    return Objects.equals(bundleName, that.bundleName) 
        && Objects.equals(pipelineIds, that.pipelineIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bundleName, pipelineIds);
  }
}
