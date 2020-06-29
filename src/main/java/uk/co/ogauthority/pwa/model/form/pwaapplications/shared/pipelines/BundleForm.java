package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines;

import java.util.Objects;
import java.util.Set;

public class BundleForm {

  private String bundleName;
  private Set<Integer> padPipelineIds;

  public BundleForm() {
  }

  public BundleForm(String name, Set<Integer> padPipelineIds) {
    this.bundleName = name;
    this.padPipelineIds = padPipelineIds;
  }

  public String getBundleName() {
    return bundleName;
  }

  public void setBundleName(String name) {
    this.bundleName = name;
  }

  public Set<Integer> getPadPipelineIds() {
    return padPipelineIds;
  }

  public void setPadPipelineIds(Set<Integer> padPipelineIds) {
    this.padPipelineIds = padPipelineIds;
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
        && Objects.equals(padPipelineIds, that.padPipelineIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bundleName, padPipelineIds);
  }
}
