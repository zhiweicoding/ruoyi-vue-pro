package cn.iocoder.yudao.module.crm.controller.admin.customer;

import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.customer.CrmCustomerCheckOutReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.customer.CrmCustomerImportExcelVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.customer.CrmCustomerImportOutReqVO;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerOutService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;

@Tag(name = "管理后台 - CRM 客户")
@RestController
@RequestMapping("/crm/customer/out/")
@Slf4j
public class CrmCustomerOutController {

    @Resource
    private CrmCustomerOutService crmCustomerOutService;

    private static final String CHARSET_NAME = "UTF-8";
    private static final String CIPHER_NAME = "AES/CBC/PKCS5Padding";
    private static final String SIGN_KEY = "yz00cbe2ae";
    private static final String SOURCE = "hzjf-yrk";
    private static final String key = "vlpmcx5i5omn3nt0";
    private static final String iv = "cih0bpfc5o6lavb3";

    @PostMapping("/check")
    @Operation(summary = "检测手机号是否存在")
    public CommonResult<Object> importOut(@Valid CrmCustomerCheckOutReqVO checkOutReqVO) {
        try {
            String jsonString = JSON.toJSONString(checkOutReqVO);
            Map<String, String> dict = JSON.parseObject(jsonString, new TypeReference<Map<String, String>>() {
            });
            String sign = dict.remove("sign");
            String signGenerate = getSign(dict, SIGN_KEY);
            if (signGenerate.equals(sign)) {
                log.info("检测手机号是否存在 sign success");
            }
        } catch (Exception e) {
            log.error("检测手机号是否存在 sign fail:{}", e.getMessage(), e);
        }
        long l = crmCustomerOutService.checkExist(checkOutReqVO.getMd5Phone());
        if (l == 0) {
            CommonResult<Object> success = success(null);
            success.setMsg("撞库通过");
            return success;
        } else {
            CommonResult<Object> error = error(1001, "已存在");
            error.setData(null);
            return error;
        }

    }

    @PostMapping("/import")
    @Operation(summary = "导入外部用户信息")
    public CommonResult<Object> importOut(@Valid CrmCustomerImportOutReqVO importReqVO) {
        try {
            String jsonString = JSON.toJSONString(importReqVO);
            Map<String, String> dict = JSON.parseObject(jsonString, new TypeReference<Map<String, String>>() {
            });
            String sign = dict.remove("sign");
            String signGenerate = getSign(dict, SIGN_KEY);
            if (signGenerate != null && signGenerate.equals(sign)) {
                log.info("导入外部用户信息 sign success");
            }
        } catch (Exception e) {
            log.error("导入外部用户信息 sign fail:{}", e.getMessage(), e);
        }
        boolean result = crmCustomerOutService.importOut(importReqVO);
        if (result) {
            CommonResult<Object> success = success(null);
            success.setMsg("入库成功");
            return success;
        } else {
            CommonResult<Object> error = error(1001, "签名不正确");
            error.setData(null);
            return error;
        }
    }

    /**
     * 签名
     */
    public static String getSign(Map<String, String> paramMap, String signKey) {
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
}
