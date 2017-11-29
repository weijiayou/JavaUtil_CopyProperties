public class Man {

  public String name;

  public Integer age;

  public Boolean sex;

  public Family family;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public Boolean getSex() {
    return sex;
  }

  public void setSex(Boolean sex) {
    this.sex = sex;
  }

  public Family getFamily() {
    return family;
  }

  public void setFamily(Family family) {
    this.family = family;
  }

  @Override
  public String toString() {
    return "Man{" +
        "name='" + name + '\'' +
        ", age=" + age +
        ", sex=" + sex +
        ", family=" + family +
        '}';
  }
}
