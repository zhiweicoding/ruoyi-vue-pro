package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "管理后台 - 客户导入 check Request VO")
@Data
@Builder
public class CrmCustomerCheckOutReqVO {
    /**
     * md5Phone : 71cad276b6c128bc2c8bad24e7b54d7a
     * sign : 4f312a3223883ed610a03ed6a430f020
     * source : hmc
     * timestamp : 1678241481
     */
    @Schema(description = "md5Phone", requiredMode = Schema.RequiredMode.REQUIRED)
    private String md5Phone;
    @Schema(description = "sign", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sign;
    @Schema(description = "source", requiredMode = Schema.RequiredMode.REQUIRED)
    private String source;
    @Schema(description = "timestamp", requiredMode = Schema.RequiredMode.REQUIRED)
    private String timestamp;

}
