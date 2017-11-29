public class Family {

  public Family(){}

  public String wife;

  public String child;

  public String getWife() {
    return wife;
  }

  public void setWife(String wife) {
    this.wife = wife;
  }

  public String getChild() {
    return child;
  }

  public void setChild(String child) {
    this.child = child;
  }

  @Override
  public String toString() {
    return "Family{" +
        "wife='" + wife + '\'' +
        ", child='" + child + '\'' +
        '}';
  }
}
