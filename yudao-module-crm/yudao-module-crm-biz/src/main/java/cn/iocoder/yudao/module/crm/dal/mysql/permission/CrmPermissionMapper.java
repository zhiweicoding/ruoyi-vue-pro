package cn.iocoder.yudao.module.crm.dal.mysql.permission;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * crm 数据权限 mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface CrmPermissionMapper extends BaseMapperX<CrmPermissionDO> {

    default CrmPermissionDO selectByBizTypeAndBizIdByUserId(Integer bizType, Long bizId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getBizType, bizType)
                .eq(CrmPermissionDO::getBizId, bizId)
                .eq(CrmPermissionDO::getUserId, userId));
    }

    default List<CrmPermissionDO> selectByBizTypeAndBizId(Integer bizType, Long bizId) {
        return selectList(new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getBizType, bizType)
                .eq(CrmPermissionDO::getBizId, bizId));
    }

    @Select("SELECT id, biz_type, biz_id, user_id, level, create_time, update_time, creator, updater, deleted " +
            "FROM crm_permission WHERE biz_type = #{bizType} AND biz_id = #{bizId}")
    List<CrmPermissionDO> selectByBizTypeAndBizIdNotCheckDel(@Param("bizType") Integer bizType, @Param("bizId") Long bizId);

    default List<CrmPermissionDO> selectByBizTypeAndBizIds(Integer bizType, Collection<Long> bizIds) {
        return selectList(new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getBizType, bizType)
                .in(CrmPermissionDO::getBizId, bizIds));
    }

    default List<CrmPermissionDO> selectListByBizTypeAndUserId(Integer bizType, Long userId) {
        return selectList(new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getBizType, bizType)
                .eq(CrmPermissionDO::getUserId, userId));
    }

    default List<CrmPermissionDO> selectListByBizTypeAndBizIdAndLevel(Integer bizType, Long bizId, Integer level) {
        return selectList(new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getBizType, bizType)
                .eq(CrmPermissionDO::getBizId, bizId)
                .eq(CrmPermissionDO::getLevel, level));
    }

    default CrmPermissionDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(CrmPermissionDO::getId, id,
                CrmPermissionDO::getUserId, userId);
    }

    default CrmPermissionDO selectByBizAndUserId(Integer bizType, Long bizId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getBizType, bizType)
                .eq(CrmPermissionDO::getBizId, bizId)
                .eq(CrmPermissionDO::getUserId, userId));
    }

    default int deletePermission(Integer bizType, Long bizId) {
        return delete(new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getBizType, bizType)
                .eq(CrmPermissionDO::getBizId, bizId));
    }

    default Long selectListByBiz(Collection<Integer> bizTypes, Collection<Long> bizIds, Collection<Long> userIds) {
        return selectCount(new LambdaQueryWrapperX<CrmPermissionDO>()
                .in(CrmPermissionDO::getBizType, bizTypes)
                .in(CrmPermissionDO::getBizId, bizIds)
                .in(CrmPermissionDO::getUserId, userIds));
    }

}
