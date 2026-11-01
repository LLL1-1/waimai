package com.sky.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 员工分页查询入参。
 *
 * <p>page/pageSize 不传时使用默认值，name 支持模糊查询。</p>
 */
@Data
@Schema(description = "员工分页查询")
public class EmployeePageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 页码，从 1 开始 */
    @Schema(description = "页码", defaultValue = "1")
    private Integer page = 1;

    /** 每页记录数 */
    @Schema(description = "每页记录数", defaultValue = "10")
    private Integer pageSize = 10;

    /** 员工姓名，模糊查询 */
    @Schema(description = "员工姓名")
    private String name;
}
