package cn.iocoder.yudao.framework.common.util.object;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.lang.reflect.Field;


/**
 * Bean 工具类
 * <p>
 * 1. 默认使用 {@link cn.hutool.core.bean.BeanUtil} 作为实现类，虽然不同 bean 工具的性能有差别，但是对绝大多数同学的项目，不用在意这点性能
 * 2. 针对复杂的对象转换，可以搜参考 AuthConvert 实现，通过 mapstruct + default 配合实现
 *
 * @author 芋道源码
 */
public class BeanUtils {

    private static final Logger log = LoggerFactory.getLogger(BeanUtils.class);


    public static <T> T toBean(Object source, Class<T> targetClass) {
        return BeanUtil.toBean(source, targetClass);
    }

    public static <T> T toBean(Object source, Class<T> targetClass, Consumer<T> peek) {
        T target = toBean(source, targetClass);
        if (target != null) {
            peek.accept(target);
        }
        return target;
    }

    public static <S, T> List<T> toBean(List<S> source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        return CollectionUtils.convertList(source, s -> toBean(s, targetType));
    }

    public static <S, T> List<T> toBean(List<S> source, Class<T> targetType, Consumer<T> peek) {
        List<T> list = toBean(source, targetType);
        if (list != null) {
            list.forEach(peek);
        }
        return list;
    }

    public static <S, T> PageResult<T> toBean(PageResult<S> source, Class<T> targetType) {
        return toBean(source, targetType, null);
    }

    public static <S, T> PageResult<T> toBean(PageResult<S> source, Class<T> targetType, Consumer<T> peek) {
        if (source == null) {
            return null;
        }
        List<T> list = toBean(source.getList(), targetType);
        if (peek != null) {
            list.forEach(peek);
        }
        return new PageResult<>(list, source.getTotal());
    }

    public static void setEmptyStringFieldsToNull(Object bean) {
        if (bean == null) {
            return;
        }

        Class<?> clazz = bean.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(bean);
                    if ("".equals(value)) {
                        field.set(bean, null);
                    }
                } catch (IllegalAccessException e) {
                    log.error("setEmptyStringFieldsToNull fail,{}", e.getMessage(), e);
                }
            }
        }
    }

}