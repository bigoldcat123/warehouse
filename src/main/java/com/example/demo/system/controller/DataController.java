package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.CurrentUser;
import com.example.demo.common.R;
import com.example.demo.system.entity.DTO.DataDTO;
import com.example.demo.system.entity.DTO.DataDetailDTO;
import com.example.demo.system.entity.DTO.HouseTempRecordDTO;
import com.example.demo.system.entity.PO.Data;
import com.example.demo.system.entity.PO.House;
import com.example.demo.system.entity.PO.Warehouse;
import com.example.demo.system.entity.query.DataQuery;
import com.example.demo.system.service.IDataService;
import com.example.demo.system.service.IHouseService;
import com.example.demo.system.service.IWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;


import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.List;
import java.util.function.Consumer;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author czh
 * @since 2024-07-28
 *
 * 可查看全部数据，可根据时间、仓库名称、仓房名称查询数据
 */
@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    IDataService dataService;
    @Autowired
    IHouseService houseService;
    @Autowired
    IWarehouseService warehouseService;
    @PostMapping("{current}/{size}")
    public R data(@RequestBody(required = false) DataQuery query,@PathVariable Integer current,@PathVariable Integer size) {

        QueryWrapper<Data> queryWrapper = new QueryWrapper<>();
        QueryWrapper<House> houseQueryWrapper = new QueryWrapper<>();
        CurrentUser details = (CurrentUser) SecurityContextHolder.getContext().getAuthentication().getDetails();



        Integer companyID = details.getCompanyID();
        if(!CurrentUser.isMainCompany()) {
            Warehouse warehouse = warehouseService.getById(companyID);
            QueryWrapper<House> houseQueryWrapper1   = new QueryWrapper<>();
            houseQueryWrapper1.eq("warehouseID", warehouse.getId());
            queryWrapper.in("HouseNo", houseService.list(houseQueryWrapper1).stream().map(House::getHouseNo).toArray());
        }
        if(query != null && query.getFrom() != null) {
            queryWrapper.ge("TestDate", query.getFrom());
        }
        if(query != null && query.getTo() != null) {
            queryWrapper.le("TestDate", query.getTo());
        }
//        if(query != null && query.getWarehouseName() != null) {
//            queryWrapper.le("TestDate", query.getTo());
//        }
        if(query != null && query.getHouseName() != null) {
            houseQueryWrapper.like("house_name", query.getHouseName());
            List<House> list = houseService.list(houseQueryWrapper);
            if(!list.isEmpty()){
                queryWrapper.in("HouseNo", list.stream().map(House::getHouseNo).toArray());
            }else{
                queryWrapper.eq("HouseNo", -999);


            }
        }
        if(query != null && query.getWarehouseName() != null) {
            QueryWrapper<Warehouse> warehouseQueryWrapper = new QueryWrapper<>();
            warehouseQueryWrapper.like("warehouse_name", query.getWarehouseName());
            List<Warehouse> list = warehouseService.list(warehouseQueryWrapper);
            if(!list.isEmpty()){
                QueryWrapper<House> houseQueryWrapper1 = new QueryWrapper<>();
                houseQueryWrapper1.in("warehouseID", list.stream().map(Warehouse::getId).toArray());
                List<House> list1 = houseService.list(houseQueryWrapper1);
                if(!list1.isEmpty()){
                    queryWrapper.in("HouseNo", list1.stream().map(House::getHouseNo).toArray());
                }else{
                    queryWrapper.eq("HouseNo", -999);
                }
            }else {
                queryWrapper.eq("HouseNo", -999);
            }
        }
        queryWrapper.in("HouseNo",houseService.list().stream().map(House::getHouseNo).toList());
        Page<Data> page = dataService.page(new Page<>(current, size), queryWrapper);
        List<DataDTO> list_dto = dataService.parseDTO(page.getRecords());
        Page<DataDTO> dataDTOPage = new Page<>();
        dataDTOPage.setRecords(list_dto);
        dataDTOPage.setTotal(page.getTotal());
        dataDTOPage.setCurrent(page.getCurrent());
        dataDTOPage.setSize(page.getSize());
        return R.ok(dataDTOPage);
    }

    @GetMapping("{id}")
    public R dataDetail(@PathVariable Integer id) {
       DataDetailDTO d =  dataService.getDataDetail(id);
       return R.ok(d);
    }

    /**
     * 根据粮房编号和指定点坐标，返回该点的温度记录数组
     * @param houseNo 粮房编号
     * @param ceng 层坐标（从1开始）
     * @param hang 行坐标（从1开始）
     * @param lie 列坐标（从1开始）
     */
    @GetMapping("/tempRecords")
    public R getTempRecords(@RequestParam String houseNo,
                            @RequestParam int ceng,
                            @RequestParam int hang,
                            @RequestParam int lie) {
        List<HouseTempRecordDTO> records = dataService.getTempRecordsByHouseNo(houseNo, ceng, hang, lie);
        return R.ok(records);
    }

    /**
     * 根据粮房编号和层坐标，返回该层每个时间点的平均温度
     * @param houseNo 粮房编号
     * @param ceng 层坐标（从1开始）
     */
    @GetMapping("/layerAvgTemp")
    public R getLayerAvgTemp(@RequestParam String houseNo,
                             @RequestParam int ceng) {
        List<HouseTempRecordDTO> records = dataService.getLayerAvgTempByHouseNo(houseNo, ceng);
        return R.ok(records);
    }

    /**
     * 根据粮房编号，返回每个时间点全部温度点的平均值
     * @param houseNo 粮房编号
     */
    @GetMapping("/allAvgTemp")
    public R getAllAvgTemp(@RequestParam String houseNo) {
        List<HouseTempRecordDTO> records = dataService.getAllAvgTempByHouseNo(houseNo);
        return R.ok(records);
    }

    /**
     * 根据粮房编号和指定点坐标，返回该点的湿度记录数组（当前无真实数据，返回随机模拟数据）
     * @param houseNo 粮房编号
     * @param ceng 层坐标（从1开始）
     * @param hang 行坐标（从1开始）
     * @param lie 列坐标（从1开始）
     */
    @GetMapping("/humidityRecords")
    public R getHumidityRecords(@RequestParam String houseNo,
                                @RequestParam int ceng,
                                @RequestParam int hang,
                                @RequestParam int lie) {
        List<HouseTempRecordDTO> records = dataService.getHumidityRecordsByHouseNo(houseNo, ceng, hang, lie);
        return R.ok(records);
    }

    /**
     * 根据粮房编号和层坐标，返回该层每个时间点的平均湿度（当前无真实数据，返回随机模拟数据）
     * @param houseNo 粮房编号
     * @param ceng 层坐标（从1开始）
     */
    @GetMapping("/layerAvgHumidity")
    public R getLayerAvgHumidity(@RequestParam String houseNo,
                                 @RequestParam int ceng) {
        List<HouseTempRecordDTO> records = dataService.getLayerAvgHumidityByHouseNo(houseNo, ceng);
        return R.ok(records);
    }

    /**
     * 根据粮房编号，返回每个时间点全部湿度点的平均值（当前无真实数据，返回随机模拟数据）
     * @param houseNo 粮房编号
     */
    @GetMapping("/allAvgHumidity")
    public R getAllAvgHumidity(@RequestParam String houseNo) {
        List<HouseTempRecordDTO> records = dataService.getAllAvgHumidityByHouseNo(houseNo);
        return R.ok(records);
    }

    /**
     * 根据粮房编号和指定点坐标，返回该点的气体浓度记录数组
     * @param houseNo 粮房编号
     * @param ceng 层坐标（从1开始）
     * @param hang 行坐标（从1开始）
     * @param lie 列坐标（从1开始）
     */
    @GetMapping("/gasRecords")
    public R getGasRecords(@RequestParam String houseNo,
                           @RequestParam int ceng,
                           @RequestParam int hang,
                           @RequestParam int lie) {
        List<HouseTempRecordDTO> records = dataService.getGasRecordsByHouseNo(houseNo, ceng, hang, lie);
        return R.ok(records);
    }

    /**
     * 根据粮房编号和层坐标，返回该层每个时间点的平均气体浓度
     * @param houseNo 粮房编号
     * @param ceng 层坐标（从1开始）
     */
    @GetMapping("/layerAvgGas")
    public R getLayerAvgGas(@RequestParam String houseNo,
                            @RequestParam int ceng) {
        List<HouseTempRecordDTO> records = dataService.getLayerAvgGasByHouseNo(houseNo, ceng);
        return R.ok(records);
    }

    /**
     * 根据粮房编号，返回每个时间点全部气体浓度点的平均值
     * @param houseNo 粮房编号
     */
    @GetMapping("/allAvgGas")
    public R getAllAvgGas(@RequestParam String houseNo) {
        List<HouseTempRecordDTO> records = dataService.getAllAvgGasByHouseNo(houseNo);
        return R.ok(records);
    }
    /**
     * 根据粮房编号，返回每个时间点的三维温度数组 [层][行][列]
     * 源数据按 # 分割记录，按 $ 分割：列:行:层$温度$5
     * @param houseNo 粮房编号
     * @return {采集时间: 三维温度数组}，按时间升序
     */
    @GetMapping("/temperatureCube")
    public R getTemperatureCube(@RequestParam String houseNo) {
        return R.ok(dataService.getTemperatureCubeByHouseNo(houseNo));
    }
    /**
     * 根据粮房编号，返回每个时间点的湿度三维数组 [层][行][列]
     * 当前无真实数据，按仓房 xyz 维度返回随机模拟数据
     * @param houseNo 粮房编号
     * @return {采集时间: 湿度三维数组}，按时间升序
     */
    @GetMapping("/humidityCube")
    public R getHumidityCube(@RequestParam String houseNo) {
        return R.ok(dataService.getHumidityCubeByHouseNo(houseNo));
    }

    /**
     * 根据粮房编号，返回每个时间点的气体浓度三维数组 [层][行][列]
     * 当前无真实数据，按仓房 xyz 维度返回随机模拟数据
     * @param houseNo 粮房编号
     * @return {采集时间: 气体浓度三维数组}，按时间升序
     */
    @GetMapping("/gasCube")
    public R getGasCube(@RequestParam String houseNo) {
        return R.ok(dataService.getGasCubeByHouseNo(houseNo));
    }
}
