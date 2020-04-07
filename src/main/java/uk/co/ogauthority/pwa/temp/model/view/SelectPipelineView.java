package uk.co.ogauthority.pwa.temp.model.view;

import java.util.List;

public class SelectPipelineView {

  private String number;
  private int identNumber;
  private List<String> holders;
  private List<String> users;
  private List<String> operators;
  private List<String> owners;

  public SelectPipelineView(PipelineView p) {
    number = p.getPipelineNumber();
    identNumber = 1;
    holders = p.getHolders();
    users = p.getUsers();
    operators = p.getOperators();
    owners = p.getOwners();
  }

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public int getIdentNumber() {
    return identNumber;
  }

  public void setIdentNumber(int identNumber) {
    this.identNumber = identNumber;
  }

  public List<String> getHolders() {
    return holders;
  }

  public void setHolders(List<String> holders) {
    this.holders = holders;
  }

  public List<String> getUsers() {
    return users;
  }

  public void setUsers(List<String> users) {
    this.users = users;
  }

  public List<String> getOperators() {
    return operators;
  }

  public void setOperators(List<String> operators) {
    this.operators = operators;
  }

  public List<String> getOwners() {
    return owners;
  }

  public void setOwners(List<String> owners) {
    this.owners = owners;
  }
}
