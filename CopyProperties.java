import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 拷贝对象属性工具
 * 将源对象中不为空的属性拷贝到目标对象中，当目标对象的属性值为null时，会先为改属性创建一个对象，再将源对象的属性拷贝过去
 *
 * 注：不支持通过内部类定义属性，因为通过反射创建对象有问题
 * @author weijiayou
 */
public class CopyProperties extends BeanUtilsBean {

  private static CopyProperties  INSTANCE = null;

  private static byte[] lock = new byte[0];

  private CopyProperties(){

  }

  private static void init() {
    synchronized (lock) {
      if (null == INSTANCE) {
        INSTANCE = new CopyProperties();
      }
    }
  }

  public static CopyProperties getInstance() {
    if (null == INSTANCE) {
      init();
    }
    return INSTANCE;
  }

  private Log log = LogFactory.getLog(CopyProperties.class);

  //定义需要排掉的数据类型
  private final List<String> exceptTypeList = Arrays.asList("Boolean", "Byte", "Character", "Short", "Integer", "Long", "Float", "Double", "String");

  /**
   * 如果源对象的值为null，不对目标对象对应属性做操作
   * @param bean 目标对象
   * @param name 属性名
   * @param value 属性值
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  @Override
  public void copyProperty(Object bean, String name, Object value)
      throws IllegalAccessException, InvocationTargetException {
    if (value == null) {
      return;
    }
    super.copyProperty(bean, name, value);
  }

  /**
   * 拷贝属性方法
   * @param dest 目标对象
   * @param orig 源对象
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  @Override
  public void copyProperties(Object dest, Object orig)
      throws IllegalAccessException, InvocationTargetException {

    // Validate existence of the specified beans
    if (dest == null) {
      throw new IllegalArgumentException
          ("No destination bean specified");
    }
    if (orig == null) {
      throw new IllegalArgumentException("No origin bean specified");
    }
    if (log.isDebugEnabled()) {
//      log.debug("BeanUtils.copyProperties(" + dest + ", " +
//          orig + ")");
    }

    // Copy the properties, converting as necessary
    if (orig instanceof DynaBean) {
      DynaProperty origDescriptors[] =
          ((DynaBean) orig).getDynaClass().getDynaProperties();
      for (int i = 0; i < origDescriptors.length; i++) {
        String name = origDescriptors[i].getName();
        if (getPropertyUtils().isWriteable(dest, name)) {
          Object value = ((DynaBean) orig).get(name);
          copyProperty(dest, name, value);
        }
      }
    } else if (orig instanceof Map) {
      Iterator names = ((Map) orig).keySet().iterator();
      while (names.hasNext()) {
        String name = (String) names.next();
        if (getPropertyUtils().isWriteable(dest, name)) {
          Object value = ((Map) orig).get(name);
          copyProperty(dest, name, value);
        }
      }
    } else /* if (orig is a standard JavaBean) */ {
      //修改了目标对象为JavaBean类型时，拷贝属性的方法
      PropertyDescriptor origDescriptors[] =
          getPropertyUtils().getPropertyDescriptors(orig);
      for (int i = 0; i < origDescriptors.length; i++) {
        String name = origDescriptors[i].getName();//对象的属性名称
        String type = origDescriptors[i].getPropertyType().getSimpleName();//对象的属性类型
        if ("class".equals(name)) {
          continue; // No point in trying to set an object's class
        }
        if (!exceptTypeList.contains(type)) {
          //过滤出没有被排掉的对象，也就是要处理的自定义对象
//          System.out.println(type + " is not base data type");
          Object deepDest = null;
          try {
            deepDest = getPropertyUtils().getProperty(dest, name);
          }catch (NoSuchMethodException e){
            ; // Should not happen
          }
          //如果目标对象中的属性是null，需要为该属性new一个对象
          if(deepDest == null){
            Object obj = null;
            try {
              Class classType = dest.getClass().getField(name).getType();
              obj = classType.newInstance();
            }catch (NoSuchFieldException e){
              ; // create object fail
            }catch (InstantiationException e){
              ; // create object fail
            }
            //反射创建对象失败，后面拷贝对象会报错
            if(obj==null){
              continue;
            }
            //将new的对象赋给dest对象
            origDescriptors[i].getWriteMethod().invoke(dest,obj);
            deepDest = obj;
          }
          //获取源中的深层对象
          Object deepOrig = origDescriptors[i].getReadMethod().invoke(orig);
          if(deepOrig == null){
            System.out.println("No deep origin bean specified");
            continue;
          }
          //递归的将源的深层对象赋给目标深层对象
          copyProperties(deepDest,deepOrig);
          continue;
        }
        if (getPropertyUtils().isReadable(orig, name) &&
            getPropertyUtils().isWriteable(dest, name)) {
          try {
            Object value =
                getPropertyUtils().getSimpleProperty(orig, name);
            //将值赋给目标对象名为name的属性
            copyProperty(dest, name, value);
          } catch (NoSuchMethodException e) {
            ; // Should not happen
          }
        }
      }
    }

  }
}
