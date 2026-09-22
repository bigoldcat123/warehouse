package com.example.demo.system.controller;

import com.example.demo.common.R;
import com.example.demo.system.service.IReceiverDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author czh
 * @since 2024-07-27
 */
@RestController
@RequestMapping("/receiverData")
public class ReceiverDataController {

    @Autowired
    IReceiverDataService receiverDataService;

    /**
     * 查询仓房最新环境数据，供仓库全景图信息面板使用。
     */
    @GetMapping("/latest")
    public R getLatest(@RequestParam("houseNo") String houseNo) {
        return handleQuery(() -> receiverDataService.getLatestPanoramaInfo(houseNo));
    }

    /**
     * 查询仓房所有 TestDate，返回去重、升序的时间数组。
     */
    @GetMapping("/testDates")
    public R getTestDates(@RequestParam("houseNo") String houseNo) {
        return handleQuery(() -> receiverDataService.getTestDates(houseNo));
    }

    /**
     * 查询温度二维数据，返回结构为 [串][层]。
     */
    @GetMapping("/temperature")
    public R getTemperature(
            @RequestParam("houseNo") String houseNo,
            @RequestParam("testDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime testDate) {
        return handleQuery(() -> receiverDataService.getTemperatureMatrix(houseNo, testDate));
    }

    /**
     * 查询湿度二维数据，返回结构为 [串][层]。
     */
    @GetMapping("/humidity")
    public R getHumidity(
            @RequestParam("houseNo") String houseNo,
            @RequestParam("testDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime testDate) {
        return handleQuery(() -> receiverDataService.getHumidityMatrix(houseNo, testDate));
    }

    /**
     * 查询 PH3 二维数据，返回结构为 [串][层]。
     */
    @GetMapping("/ph3")
    public R getPh3(
            @RequestParam("houseNo") String houseNo,
            @RequestParam("testDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime testDate) {
        return handleQuery(() -> receiverDataService.getPh3Matrix(houseNo, testDate));
    }

    private R handleQuery(ReceiverDataQuery query) {
        try {
            return R.ok(query.execute());
        } catch (IllegalArgumentException e) {
            return R.errorShow(e.getMessage());
        }
    }

    @FunctionalInterface
    private interface ReceiverDataQuery {
        Object execute();
    }
}
