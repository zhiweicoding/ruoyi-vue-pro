package cn.iocoder.yudao.module.crm.service.customer;

import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.customer.CrmCustomerImportOutReqVO;

/**
 * 客户 Service 接口
 *
 * @author Wanwan
 */
public interface CrmCustomerOutService {

    long checkExist(String md5phone);

    boolean importOut(CrmCustomerImportOutReqVO importOutReqVO);

}
