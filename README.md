#拷贝工具BeanUtilsBean扩展

####同事在使用ES的时候有这么样的需求：<br />
1. 拷贝对象时，如果源对象的属性为null，则不拷贝该属性到目标对象了；
2. 源对象的属性不为null才会将属性拷贝到目标对象；
3. 如果源对象的属性是自定义的bean，那么bean内的属性也按照1、2那样拷贝。

这时，使用BeanUtils的copyProperties就不满足要求，需要对该方法进行扩展。

####解决上面问题的封装类CopyProperties
话不多说，上源码<br />
`CopyProperties.java`<br />
import java.beans.PropertyDescriptor;<br />
import java.lang.reflect.InvocationTargetException;<br />
import java.util.Arrays;<br />
import java.util.Iterator;<br />
import java.util.List;<br />
import java.util.Map;<br />
import org.apache.commons.beanutils.BeanUtilsBean;<br />
import org.apache.commons.beanutils.DynaBean;<br />
import org.apache.commons.beanutils.DynaProperty;<br />
import org.apache.commons.logging.Log;<br />
import org.apache.commons.logging.LogFactory;<br />

/\*\*<br />
&nbsp;\* **拷贝对象属性工具**<br />
&nbsp;\* 将源对象中不为空的属性拷贝到目标对象中，当目标对象的属性值为null时，会先为改属性创建一个对象，再将源对象的属性拷贝过去<br />
&nbsp;\* 注：不支持通过内部类定义属性，因为通过反射创建对象有问题<br />
&nbsp;\* @author **wjy**<br />
&nbsp;\*/<br />
public class CopyProperties extends BeanUtilsBean {<br />

&nbsp;&nbsp;private static CopyProperties  INSTANCE = null;<br />

&nbsp;&nbsp;private static byte[] lock = new byte[0];<br />

&nbsp;&nbsp;private CopyProperties(){}<br />

&nbsp;&nbsp;private static void init() {<br />
&nbsp;&nbsp;&nbsp;&nbsp;synchronized (lock) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (null == INSTANCE) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;INSTANCE = new CopyProperties();<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;}<br />

&nbsp;&nbsp;public static CopyProperties getInstance() {<br />
&nbsp;&nbsp;&nbsp;&nbsp;if (null == INSTANCE) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;init();<br />
&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;return INSTANCE;<br />
&nbsp;&nbsp;}<br />

&nbsp;&nbsp;private Log log = LogFactory.getLog(CopyProperties.class);<br />

&nbsp;&nbsp;//定义需要排掉的数据类型<br />
&nbsp;&nbsp;private final List<String> exceptTypeList = Arrays.asList("Boolean", "Byte", "Character", "Short", "Integer", "Long", "Float", "Double", "String");<br />

&nbsp;&nbsp;/\*\*<br />
&nbsp;&nbsp;&nbsp;\* **如果源对象的值为null，不对目标对象对应属性做操作**<br />
&nbsp;&nbsp;&nbsp;\* @param bean 目标对象<br />
&nbsp;&nbsp;&nbsp;\* @param name 属性名<br />
&nbsp;&nbsp;&nbsp;\* @param value 属性值<br />
&nbsp;&nbsp;&nbsp;\* @throws IllegalAccessException<br />
&nbsp;&nbsp;&nbsp;\* @throws InvocationTargetException<br />
&nbsp;&nbsp;&nbsp;\*/<br />
&nbsp;&nbsp;@Override<br />
&nbsp;&nbsp;public void copyProperty(Object bean, String name, Object value) throws IllegalAccessException, InvocationTargetException {<br />
&nbsp;&nbsp;&nbsp;&nbsp;if (value == null) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return;<br />
&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;super.copyProperty(bean, name, value);<br />
&nbsp;&nbsp;}<br />

&nbsp;&nbsp;/\*\*<br />
&nbsp;&nbsp;&nbsp;\* **拷贝属性方法**<br />
&nbsp;&nbsp;&nbsp;\* @param dest 目标对象<br />
&nbsp;&nbsp;&nbsp;\* @param orig 源对象<br />
&nbsp;&nbsp;&nbsp;\* @throws IllegalAccessException<br />
&nbsp;&nbsp;&nbsp;\* @throws InvocationTargetException<br />
&nbsp;&nbsp;&nbsp;\*/<br />
&nbsp;&nbsp;@Override<br />
&nbsp;&nbsp;public void copyProperties(Object dest, Object orig) throws IllegalAccessException, InvocationTargetException {<br />
<br />
&nbsp;&nbsp;&nbsp;&nbsp;// Validate existence of the specified beans<br />
&nbsp;&nbsp;&nbsp;&nbsp;if (dest == null) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;throw new IllegalArgumentException ("No destination bean specified");<br />
&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;if (orig == null) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;throw new IllegalArgumentException("No origin bean specified");<br />
&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;if (log.isDebugEnabled()) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;log.debug("BeanUtils.copyProperties(" + dest + ", " + orig + ")");<br />
&nbsp;&nbsp;&nbsp;&nbsp;}<br />
<br />
&nbsp;&nbsp;&nbsp;&nbsp;// Copy the properties, converting as necessary<br />
&nbsp;&nbsp;&nbsp;&nbsp;if (orig instanceof DynaBean) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;DynaProperty origDescriptors[] = ((DynaBean) orig).getDynaClass().getDynaProperties();<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;for (int i = 0; i < origDescriptors.length; i++) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;String name = origDescriptors[i].getName();<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (getPropertyUtils().isWriteable(dest, name)) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Object value = ((DynaBean) orig).get(name);<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;copyProperty(dest, name, value);<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;} else if (orig instanceof Map) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Iterator names = ((Map) orig).keySet().iterator();<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;while (names.hasNext()) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;String name = (String) names.next();<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (getPropertyUtils().isWriteable(dest, name)) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Object value = ((Map) orig).get(name);<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;copyProperty(dest, name, value);<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;} else /* if (orig is a standard JavaBean) */ {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//修改了目标对象为JavaBean类型时，拷贝属性的方法<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;PropertyDescriptor origDescriptors[] = getPropertyUtils().getPropertyDescriptors(orig);<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;for (int i = 0; i < origDescriptors.length; i++) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;String name = origDescriptors[i].getName();//对象的属性名称<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;String type = origDescriptors[i].getPropertyType().getSimpleName();//对象的属性类型<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if ("class".equals(name)) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;continue; // No point in trying to set an object's class<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (!exceptTypeList.contains(type)) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//过滤出没有被排掉的对象，也就是要处理的自定义对象<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//System.out.println(type + " is not base data type");<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Object deepDest = null;<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;try {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deepDest = getPropertyUtils().getProperty(dest, name);<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}catch (NoSuchMethodException e){<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;; // Should not happen<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//如果目标对象中的属性是null，需要为该属性new一个对象<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if(deepDest == null){<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Object obj = null;<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;try {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Class classType = dest.getClass().getField(name).getType();<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;obj = classType.newInstance();<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}catch (NoSuchFieldException e){<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;; // create object fail<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}catch (InstantiationException e){<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;; // create object fail<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//反射创建对象失败，后面拷贝对象会报错<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if(obj==null){<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;continue;<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//将new的对象赋给dest对象<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;origDescriptors[i].getWriteMethod().invoke(dest,obj);<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deepDest = obj;<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//获取源中的深层对象<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Object deepOrig = origDescriptors[i].getReadMethod().invoke(orig);<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if(deepOrig == null){<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println("No deep origin bean specified");<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;continue;<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//递归的将源的深层对象赋给目标深层对象<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;copyProperties(deepDest,deepOrig);<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;continue;<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (getPropertyUtils().isReadable(orig, name) && getPropertyUtils().isWriteable(dest, name)) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;try {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Object value = getPropertyUtils().getSimpleProperty(orig, name);<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//将值赋给目标对象名为name的属性<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;copyProperty(dest, name, value);<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;} catch (NoSuchMethodException e) {<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;; // Should not happen<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;}<br />
}

`Man.java`<br />
/\*\*<br />
&nbsp;\* **男人：姓名、年龄、性别、家庭**<br />
&nbsp;\* @author **wjy**<br />
&nbsp;\*/<br />
public class Man {

&nbsp;&nbsp;public String name;

&nbsp;&nbsp;public Integer age;

&nbsp;&nbsp;public Boolean sex;

&nbsp;&nbsp;public Family family;

&nbsp;&nbsp;public String getName() {
&nbsp;&nbsp;&nbsp;&nbsp;return name;
&nbsp;&nbsp;}

&nbsp;&nbsp;public void setName(String name) {
&nbsp;&nbsp;this.name = name;
&nbsp;&nbsp;}

&nbsp;&nbsp;public Integer getAge() {
&nbsp;&nbsp;return age;
&nbsp;&nbsp;}

&nbsp;&nbsp;public void setAge(Integer age) {
&nbsp;&nbsp;this.age = age;
&nbsp;&nbsp;}

&nbsp;&nbsp;public Boolean getSex() {
&nbsp;&nbsp;return sex;
&nbsp;&nbsp;}

&nbsp;&nbsp;public void setSex(Boolean sex) {
&nbsp;&nbsp;this.sex = sex;
&nbsp;&nbsp;}
  
&nbsp;&nbsp;public Family getFamily() {
&nbsp;&nbsp;return family;
&nbsp;&nbsp;}

&nbsp;&nbsp;public void setFamily(Family family) {
&nbsp;&nbsp;this.family = family;
&nbsp;&nbsp;}

&nbsp;&nbsp;@Override<br />
&nbsp;&nbsp;public String toString() {<br />
&nbsp;&nbsp;&nbsp;&nbsp;return "Man{" +<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"name='" + name + '\'' +<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", age=" + age +<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", sex=" + sex +<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", family=" + family +<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'}';<br />
&nbsp;&nbsp;}<br />
}<br />
/\*\*<br />
&nbsp;\* **家庭：老婆、孩子**<br />
&nbsp;\* @author **wjy**<br />
&nbsp;\*/<br />
`Family.java`<br />
public class Family {

&nbsp;&nbsp;public Family(){}

&nbsp;&nbsp;public String wife;

&nbsp;&nbsp;public String child;

&nbsp;&nbsp;public String getWife() {
&nbsp;&nbsp;&nbsp;&nbsp;return wife;
&nbsp;&nbsp;}

&nbsp;&nbsp;public void setWife(String wife) {
&nbsp;&nbsp;this.wife = wife;
&nbsp;&nbsp;}

&nbsp;&nbsp;public String getChild() {
&nbsp;&nbsp;return child;
&nbsp;&nbsp;}

&nbsp;&nbsp;public void setChild(String child) {
&nbsp;&nbsp;this.child = child;
&nbsp;&nbsp;}

&nbsp;&nbsp;@Override<br />
&nbsp;&nbsp;public String toString() {<br />
&nbsp;&nbsp;&nbsp;&nbsp;return "Family{" +<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"wife='" + wife + '\'' +<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", child='" + child + '\'' +<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'}';<br />
&nbsp;&nbsp;}<br />
}
/\*\*<br />
&nbsp;\* **测试类**<br />
&nbsp;\* @author **wjy**<br />
&nbsp;\*/<br />
public class CopyTest {

&nbsp;&nbsp;public static void main(String[] args){

&nbsp;&nbsp;&nbsp;&nbsp;Man man = new Man();<br />
&nbsp;&nbsp;&nbsp;&nbsp;man.setName("Rick");<br />
&nbsp;&nbsp;&nbsp;&nbsp;man.setSex(true);<br />
&nbsp;&nbsp;&nbsp;&nbsp;Family fam = new Family();<br />
&nbsp;&nbsp;&nbsp;&nbsp;fam.setWife("Ann");<br />
&nbsp;&nbsp;&nbsp;&nbsp;man.setFamily(fam);[^1]<br />
[^1]:下文会注释掉的代码段1.
&nbsp;&nbsp;&nbsp;&nbsp;Man man1 = new Man();<br />
&nbsp;&nbsp;&nbsp;&nbsp;man1.setName("Frank");<br />
&nbsp;&nbsp;&nbsp;&nbsp;man1.setAge(20);<br />
&nbsp;&nbsp;&nbsp;&nbsp;Family fam1 = new Family();<br />
&nbsp;&nbsp;&nbsp;&nbsp;fam1.setWife("Carry");<br />
&nbsp;&nbsp;&nbsp;&nbsp;fam1.setChild("Wade");<br />
&nbsp;&nbsp;&nbsp;&nbsp;man1.setFamily(fam1);[^2]<br />
[^2]:下文会注释掉的代码段2.
&nbsp;&nbsp;&nbsp;&nbsp;System.out.println("origin man:"+man);<br />
&nbsp;&nbsp;&nbsp;&nbsp;System.out.println("dest man1:"+man1);<br />

&nbsp;&nbsp;&nbsp;&nbsp;CopyProperties cp = CopyProperties.getInstance();<br />
&nbsp;&nbsp;&nbsp;&nbsp;try{<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cp.copyProperties(man1,man);<br />
&nbsp;&nbsp;&nbsp;&nbsp;}catch (Exception e){<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;e.printStackTrace();<br />
&nbsp;&nbsp;&nbsp;&nbsp;}<br />
&nbsp;&nbsp;&nbsp;&nbsp;System.out.println("copy man to man1:"+man1);<br />
&nbsp;&nbsp;}<br />
}

####运行结果<br />
1. 当代码1跟代码2都不注释时，会将man中非null的属性赋值到man1对应的属性。<br />
![Smaller icon](https://raw.githubusercontent.com/weijiayou/JavaUtil_CopyProperties/master/CopyProperties_pic1.png "代码1、2都不注释时结果")

2. 当只有代码1注释时，会将man中非null的属性赋值到man1对应的属性。<br />
由于family属性是null，也所以不会对man1中family的属性进行修改。<br />
![Smaller icon](https://raw.githubusercontent.com/weijiayou/JavaUtil_CopyProperties/master/CopyProperties_pic2.png "代码1注释时结果")

3. 当只有代码2注释时，会将man中非null属性赋值到man1对应的属性。<br />
由于man1的family属性是null，因此会先为man1创建一个family对象，再将man的family的非空属性赋值给man1的family的对应属性。<br />
![Smaller icon](https://raw.githubusercontent.com/weijiayou/JavaUtil_CopyProperties/master/CopyProperties_pic3.png "代码2注释时结果")

4. 当代码1跟代码2都注释掉时，会将man中非null的属性复制到man1对应的属性。<br />
但是man、man1的family属性都是null，因此man1的family只会创建一个family对象，但是不会被man的family属性覆盖掉。<br />
![Smaller icon](https://raw.githubusercontent.com/weijiayou/JavaUtil_CopyProperties/master/CopyProperties_pic4.png "代码1、2都注释时结果")

**如果有写的不好的地方欢迎拍砖**
源码地址：<https://github.com/weijiayou/JavaUtil_CopyProperties>