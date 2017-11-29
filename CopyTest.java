public class CopyTest {

  public static void main(String[] args){

    Man man = new Man();
    man.setName("Rick");
    man.setSex(true);
    Family fam = new Family();
    fam.setWife("Ann");
//    man.setFamily(fam);

    Man man1 = new Man();
    man1.setName("Frank");
    man1.setAge(20);
    Family fam1 = new Family();
    fam1.setWife("Carry");
    fam1.setChild("Wade");
//    man1.setFamily(fam1);

    System.out.println("origin man:"+man);
    System.out.println("dest man1:"+man1);

    CopyProperties cp = CopyProperties.getInstance();
    try{
      cp.copyProperties(man1,man);
    }catch (Exception e){
      e.printStackTrace();
    }
    System.out.println("copy man to man1:"+man1);

//    man1.getFamily().setWife("Ann1");
//    System.out.println("after modify man1,man:"+man);
//    System.out.println(man.hashCode());
//    System.out.println(man.getFamily().hashCode());
//    System.out.println("after modify man1,man1:"+man1);
//    System.out.println(man1.hashCode());
//    System.out.println(man1.getFamily().hashCode());
  }
}
