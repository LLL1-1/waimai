package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.ReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 管理端报表接口。 */
@RestController
@RequestMapping("/admin/report")
@RequiredArgsConstructor
@Tag(name = "报表")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/statistics")
    @Operation(summary = "今日经营概览")
    public Result<ReportVO> statistics() {
        return Result.success(reportService.today());
    }
}
