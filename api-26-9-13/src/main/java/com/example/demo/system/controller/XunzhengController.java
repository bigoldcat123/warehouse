package com.example.demo.system.controller;

import com.example.demo.common.R;
import com.example.demo.system.service.IXunzhengService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 熏蒸记录接口。
 */
@RestController
@RequestMapping("/xunzheng")
public class XunzhengController {

    @Autowired
    IXunzhengService xunzhengService;

    /**
     * 判断仓房在指定日期是否正在熏蒸，未传日期时查询今天。
     *
     * @param houseNo 仓房编号
     * @param date 日期，格式 yyyy-MM-dd
     */
    @GetMapping("/isFumigating")
    public R isFumigating(
            @RequestParam("houseNo") String houseNo,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (houseNo == null || houseNo.isBlank()) {
            return R.errorShow("仓房编号不能为空");
        }
        LocalDate queryDate = date == null ? LocalDate.now() : date;
        return R.ok(xunzhengService.isFumigating(houseNo, queryDate));
    }
}
