package cn.iocoder.yudao.module.crm.service.customer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessTransferReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactTransferReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.contract.CrmContractTransferReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.customer.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerLimitConfigDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerPoolConfigDO;
import cn.iocoder.yudao.module.crm.dal.mysql.customer.CrmCustomerMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmSceneTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.contact.CrmContactService;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.service.customer.bo.CrmCustomerCreateReqBO;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.crm.enums.LogRecordConstants.*;
import static cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerLimitConfigTypeEnum.CUSTOMER_LOCK_LIMIT;
import static cn.iocoder.yudao.module.crm.enums.customer.CrmCustomerLimitConfigTypeEnum.CUSTOMER_OWNER_LIMIT;
import static java.util.Collections.singletonList;

/**
 * 客户 Service 实现类
 *
 * @author Wanwan
 */
@Service
@Slf4j
@Validated
public class CrmCustomerOutServiceImpl implements CrmCustomerOutService {

    @Resource
    private CrmCustomerMapper customerMapper;

    @Resource
    private CrmPermissionService permissionService;
    @Resource
    private CrmCustomerLimitConfigService customerLimitConfigService;
    @Resource
    @Lazy
    private CrmCustomerPoolConfigService customerPoolConfigService;
    @Resource
    @Lazy
    private CrmContactService contactService;
    @Resource
    @Lazy
    private CrmBusinessService businessService;
    @Resource
    @Lazy
    private CrmContractService contractService;

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private static final String CHARSET_NAME = "UTF-8";
    private static final String CIPHER_NAME = "AES/CBC/PKCS5Padding";
    private static final String SIGN_KEY = "yz00cbe2ae";
    private static final String SOURCE = "hzjf-yrk";
    private static final String key = "vlpmcx5i5omn3nt0";
    private static final String iv = "cih0bpfc5o6lavb3";

    @Override
    public long checkExist(String md5phone) {
        return customerMapper.countByMd5Phone(md5phone);
    }

    @Override
    public boolean importOut(CrmCustomerImportOutReqVO importOutReqVO) {
        String phone = importOutReqVO.getPhone();
        try {
            phone = decrypt(phone, key, iv);
        } catch (Exception e) {
            log.error("导入importOut:{}", e.getMessage(), e);
        }
        // 情况一：判断如果不存在，在进行插入
        CrmCustomerDO existCustomer = customerMapper.selectByCustomerName(phone);
        if (existCustomer == null) {
            // 1.1 插入客户信息
            CrmCustomerDO customer = new CrmCustomerDO();
            customer.setName(importOutReqVO.getName());
            customer.setFollowUpStatus(false);
            String remark = String.format("客户%s，来自%s，性别%s，出生日期%s，信用状况：%s，住房情况：%s，社保情况：%s，车辆情况：%s，职业：%s，芝麻信用分：%s。",
                    importOutReqVO.getName(), importOutReqVO.getCity(), importOutReqVO.getSex(), importOutReqVO.getBirthDate(), importOutReqVO.getOverdue(),
                    importOutReqVO.getHouse(), importOutReqVO.getSocial(), importOutReqVO.getVehicle(), importOutReqVO.getVocation(), importOutReqVO.getSesame());
            customer.setRemark(remark);
            customer.setOwnerUserId(142L);
            customer.setMobile(phone);
            customer.setOwnerTime(LocalDateTime.now());
            customer.setCreator("142");
            customer.setUpdater("142");
            customer.setCreateTime(LocalDateTime.now());
            customer.setUpdateTime(LocalDateTime.now());
            customerMapper.insert(customer);
            // 1.3 记录操作日志
            getSelf().importCustomerLog(customer, false);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 记录导入客户时的操作日志
     *
     * @param customer 客户信息
     * @param isUpdate 是否更新；true - 更新，false - 新增
     */
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_IMPORT_SUB_TYPE, bizNo = "{{#customer.id}}",
            success = CRM_CUSTOMER_IMPORT_SUCCESS)
    public void importCustomerLog(CrmCustomerDO customer, boolean isUpdate) {
        LogRecordContext.putVariable("customer", customer);
        LogRecordContext.putVariable("isUpdate", isUpdate);
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private CrmCustomerOutServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }


    /**
     * 签名
     */
    public String getSign(Map<String, String> paramMap, String signKey) {
        try {
            StringBuilder signBuilder = new StringBuilder();
            paramMap.forEach((k, v) -> {
                if (!k.equals("sign")) {
                    signBuilder.append(v);
                }
            });
            //将传入的所有参数加秘钥后一起 MD5 计算的结果即为验证值
            return DigestUtil.md5Hex(signBuilder + signKey);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 加密
     */
    public String encrypt(String content, String key, String iv) throws Exception {
        byte[] raw = key.getBytes(CHARSET_NAME);
        SecretKeySpec secretKey = new SecretKeySpec(raw, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
        Cipher cipher = Cipher.getInstance(CIPHER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encrypted = cipher.doFinal(content.getBytes(CHARSET_NAME));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 解密
     */
    public String decrypt(String content, String key, String iv) throws Exception {
        byte[] raw = key.getBytes(CHARSET_NAME);
        SecretKeySpec secretKey = new SecretKeySpec(raw, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
        Cipher cipher = Cipher.getInstance(CIPHER_NAME);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        return new String(cipher.doFinal(Base64.getDecoder().decode(content)));
    }

}
