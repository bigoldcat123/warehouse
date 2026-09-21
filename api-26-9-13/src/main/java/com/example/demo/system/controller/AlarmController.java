package com.example.demo.system.controller;

import com.alibaba.excel.EasyExcel;

import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;

import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.alibaba.excel.write.style.row.SimpleRowHeightStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.R;
import com.example.demo.system.entity.DTO.AlarmArgDTO;
import com.example.demo.system.entity.DTO.AlarmArgExcelDTO;
import com.example.demo.system.entity.DTO.AlarmExcelDTO;
import com.example.demo.system.entity.PO.Alarm;
import com.example.demo.system.entity.PO.House;
import com.example.demo.system.entity.PO.StoreName;
import com.example.demo.system.entity.query.AlarmQuery;
import com.example.demo.system.service.IAlarmService;
import com.example.demo.system.service.IHouseService;
import com.example.demo.system.service.IStoreNameService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author czh
 * @since 2024-07-27
 */
@RestController
@RequestMapping("/alarm")
public class AlarmController {

    @Autowired
    IAlarmService alarmService;

    @Autowired
    IHouseService houseService;

    @Autowired
    IStoreNameService storeNameService;
    @PostMapping("/{current}/{size}")
    public R query(@RequestBody(required = false) AlarmQuery query,
                   @PathVariable int current, @PathVariable int size) {
        AlarmQuery.QUERY_BUFFER = query;
        QueryWrapper<Alarm> queryWrapper = new QueryWrapper<>();
        if(query!=null && query.getLevel() != null && !query.getLevel().isEmpty()) {
            queryWrapper.in("alert_level", query.getLevel());
        }
        if(query!=null && query.getType() != null&& !query.getType().isEmpty()) {
            queryWrapper.in("alert_type", query.getType());
        }
        if(query!=null && query.getVerify() != null && !query.getVerify().isEmpty()) {
            queryWrapper.in("is_verify", query.getVerify());
        }
        if(query !=null && query.getWareHouseID() != null && !query.getWareHouseID().isEmpty()) {
            List<House> list = houseService.list(new QueryWrapper<House>().eq("warehouseID", query.getWareHouseID()));
            List<String> no = list.stream().map(House::getHouseNo).toList();
            if(!no.isEmpty()){
                queryWrapper.in("houseNo", no);
            }else{
                Page<Alarm> alarmPage = new Page<>();
                alarmPage.setTotal(0);
                alarmPage.setPages(0);
                return  R.ok(alarmPage);
            }
        }
        checkPower(queryWrapper);
        val alarmPage = new Page<Alarm>(current, size);
        Page<Alarm> page = alarmService.page(alarmPage, queryWrapper);

        return R.ok(page);
    }


  /////////////////////////////////////////////////////////////

    @GetMapping("/excel")
    public void excel(HttpServletResponse response) {
        QueryWrapper<Alarm> queryWrapper = new QueryWrapper<>();
        val query = AlarmQuery.QUERY_BUFFER;
        String v_warename="";
        if(query == null) {
            return;
        }
        if(query!=null && query.getLevel() != null && !query.getLevel().isEmpty()) {
            queryWrapper.in("alert_level", query.getLevel());
        }
        if(query!=null && query.getType() != null&& !query.getType().isEmpty()) {
            queryWrapper.in("alert_type", query.getType());
        }
        if(query!=null && query.getVerify() != null && !query.getVerify().isEmpty()) {
            queryWrapper.in("is_verify", query.getVerify());
        }
        if(query !=null && query.getWareHouseID() != null && !query.getWareHouseID().isEmpty()) {

            List<StoreName> storeNames = storeNameService.list(
                    new QueryWrapper<StoreName>().eq("StoreNo", query.getWareHouseID()));
            List<String> warename = storeNames.stream().map(StoreName::getStoreName).toList();
            if(!warename.isEmpty()){
                v_warename = warename.getFirst();
            }else{
                return;
            }

            List<House> list = houseService.list(new QueryWrapper<House>().eq("warehouseID", query.getWareHouseID()));
            List<String> no = list.stream().map(House::getHouseNo).toList();
            if(!no.isEmpty()){
                queryWrapper.in("houseNo", no);
            }else{
                return;
            }
        }
//        checkPower(queryWrapper);
        if(!AlarmQuery.LEVEL_BUFFER.isEmpty()) {
            queryWrapper.ne("alert_level",AlarmQuery.LEVEL_BUFFER);
        }
        val list = alarmService.list(queryWrapper);
        try {
            if(list!=null && !list.isEmpty()){
                response.setContentType("application/vnd.ms-excel");
                response.setCharacterEncoding("utf-8");
                // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
                String fileName = URLEncoder.encode(v_warename+"报警信息","utf8");
                response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");
                val list1 = list.stream().map(x -> {
                    return new AlarmExcelDTO(x.getHouseNo(),x.getAlertType(),x.getAlertLevel(),x.getAlertTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")),x.getIsVerify()?"是":"否");
                }).toList();
                // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
                // 如果这里想使用03 则 传入excelType参数即可
                WriteCellStyle headWriteCellStyle = new WriteCellStyle();

                headWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());


                WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
                //设置边框样式
                contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);//细实线
                contentWriteCellStyle.setBorderTop(BorderStyle.THIN);
                contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
                contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);
                // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
                HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                        new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

                EasyExcel.write(response.getOutputStream(), AlarmExcelDTO.class)
                        .registerWriteHandler(horizontalCellStyleStrategy)

                        .registerWriteHandler(new SimpleColumnWidthStyleStrategy(17))
                        .registerWriteHandler(new SimpleRowHeightStyleStrategy((short)30,(short)20))
                        .sheet("sheet")
                        .head(head(v_warename))
                        .automaticMergeHead(true)

                        .doWrite(list1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private List<List<String>> head(String warename) {
        List<List<String>> list = new ArrayList<>();
        List<String> head0 = new ArrayList<>();
        String bjxx=warename+"报警信息";
        head0.add(bjxx);
        head0.add("仓房编号");
        List<String> head1 = new ArrayList<>();
        head1.add(bjxx);
        head1.add("报警等级");
        List<String> head2 = new ArrayList<>();
        head2.add(bjxx);
        head2.add("报警类型");
        List<String> head3 = new ArrayList<>();
        head3.add(bjxx);
        head3.add("报警时间");
        List<String> head4 = new ArrayList<>();
        head4.add(bjxx);
        head4.add("是否审核");
        list.add(head0);
        list.add(head1);
        list.add(head2);
        list.add(head3);
        list.add(head4);
        return list;
    }

/////////////////////////////////////////////////////////////


    public void checkPower(QueryWrapper<Alarm> queryWrapper) {
        AlarmQuery.LEVEL_BUFFER.clear();
    }
    @GetMapping("/arg/{current}/{size}")
    public R arg( @PathVariable int current, @PathVariable int size) {
        QueryWrapper<Alarm> queryWrapper = new QueryWrapper<>();
        checkPower(queryWrapper);
        queryWrapper.eq("is_verify",0);
        Map<String, AlarmArgDTO> map = new HashMap<>();
        List<Alarm> list = alarmService.list(queryWrapper);
        list.forEach(x -> {
            String houseNo = x.getHouseNo();
            System.out.println(houseNo);
            House house = houseService.getHouseByNo(houseNo);
            if (house == null) {
                return;
            }
            String warehouseID = house.getWarehouseID();
            String warehouseID1 = warehouseID;
            StoreName storeName = storeNameService.getById(warehouseID);

            if(storeName != null && !map.containsKey(warehouseID1)) {

                AlarmArgDTO dto = new AlarmArgDTO(storeName.getStoreNo(), storeName.getStoreName());
                map.put(warehouseID1, dto);
            }
            if (storeName == null) {
                return;
            }
                String alertType = x.getAlertType();
                String alertLevel = x.getAlertLevel();
                if(Alarm.LEVEL_COMMON.equals(alertLevel)) {
                    map.get(warehouseID1).addCommon();
                }else if(Alarm.LEVEL_SERIOUS.equals(alertLevel)) {
                    map.get(warehouseID1).addSerious();
                }
                if(Alarm.TYPE_HOT.equals(alertType)){
                    map.get(warehouseID1).addHot();
                } else if(Alarm.ARG_DEW_AND_MOULD.contains(alertType)){
                    map.get(warehouseID1).addDewAndMould();
                } else  {
                    map.get(warehouseID1).addException();
                }
        });
        List<AlarmArgDTO> list1 = map.values().stream().toList();
        List<AlarmArgDTO> alarmArgDTOS = list1.subList(current * size, Math.min(current * size + size,list1.size()));
        Page<AlarmArgDTO> page = new Page<>(current, size);
        page.setRecords(alarmArgDTOS);
        page.setPages((long) Math.ceil((double) list1.size() / size));
        page.setSize(size);
        page.setTotal(list1.size());
        return R.ok(page);
    }



    /////////////////////////////////////////////////////////////

    @GetMapping("/arg_excel")
    public void arg_excel(HttpServletResponse response) {
        QueryWrapper<Alarm> queryWrapper = new QueryWrapper<>();
       // checkPower(queryWrapper);
        queryWrapper.eq("is_verify",0);
        Map<String, AlarmArgDTO> map = new HashMap<>();
        List<Alarm> list = alarmService.list(queryWrapper);
        list.forEach(x -> {
            String houseNo = x.getHouseNo();
            String warehouseID = houseService.getHouseByNo(houseNo).getWarehouseID();
            String warehouseID1 = warehouseID;
            StoreName storeName = storeNameService.getById(warehouseID);

            if(storeName != null && !map.containsKey(warehouseID1)) {

                AlarmArgDTO dto = new AlarmArgDTO(storeName.getStoreNo(), storeName.getStoreName());
                map.put(warehouseID1, dto);
            }
            if (storeName == null) {
                return;
            }
            String alertType = x.getAlertType();
            String alertLevel = x.getAlertLevel();
            if(Alarm.LEVEL_COMMON.equals(alertLevel)) {
                map.get(warehouseID1).addCommon();
            }else if(Alarm.LEVEL_SERIOUS.equals(alertLevel)) {
                map.get(warehouseID1).addSerious();
            }
            if(Alarm.TYPE_HOT.equals(alertType)){
                map.get(warehouseID1).addHot();
            } else if(Alarm.ARG_DEW_AND_MOULD.contains(alertType)){
                map.get(warehouseID1).addDewAndMould();
            } else  {
                map.get(warehouseID1).addException();
            }

        });
        List<AlarmArgDTO> list2 = map.values().stream().toList();

        try {
            if(list2!=null && !list2.isEmpty()){
                response.setContentType("application/vnd.ms-excel");
                response.setCharacterEncoding("utf-8");
                // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
                String fileName = URLEncoder.encode("报警信息统计","utf8");
                response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");
                val list1 = list2.stream().map(x -> {
                    return new AlarmArgExcelDTO(x.getWareHouseNO(),x.getWareHouseName(),x.getCommon(),x.getSerious(),x.getHot(),x.getDewAndMould(),x.getException());
                }).toList();
                // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
                // 如果这里想使用03 则 传入excelType参数即可
                WriteCellStyle headWriteCellStyle = new WriteCellStyle();

                headWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());


                WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
                //设置边框样式
                contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);//细实线
                contentWriteCellStyle.setBorderTop(BorderStyle.THIN);
                contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
                contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);
                contentWriteCellStyle.setWrapped(true);
                // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
                HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                        new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

                EasyExcel.write(response.getOutputStream(), AlarmArgExcelDTO.class)
                        .registerWriteHandler(horizontalCellStyleStrategy)
                        .registerWriteHandler(new SimpleRowHeightStyleStrategy((short)30,(short)40))
                        .sheet("sheet")

                        .automaticMergeHead(true)

                        .doWrite(list1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
//
//    private List<List<String>> arg_head() {
//        List<List<String>> list = new ArrayList<>();
//        List<String> head0 = new ArrayList<>();
//        String bjxx="报警信息统计";
//        head0.add(bjxx);
//        head0.add("单位编号");
//        List<String> head1 = new ArrayList<>();
//        head1.add(bjxx);
//        head1.add("单位名称");
//        List<String> head2 = new ArrayList<>();
//        head2.add(bjxx);
//        head2.add("一般");
//        List<String> head3 = new ArrayList<>();
//        head3.add(bjxx);
//        head3.add("严重");
//        List<String> head4 = new ArrayList<>();
//        head4.add(bjxx);
//        head4.add("发热");
//        List<String> head5 = new ArrayList<>();
//        head5.add(bjxx);
//        head5.add("结露霉变");
//        List<String> head6 = new ArrayList<>();
//        head6.add(bjxx);
//        head6.add("异常");
//        list.add(head0);
//        list.add(head1);
//        list.add(head2);
//        list.add(head3);
//        list.add(head4);
//        list.add(head5);
//        list.add(head6);
//        return list;
//    }

/////////////////////////////////////////////////////////////









    public static void main(String[] args) {
        List<Integer> integers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        System.out.println(integers.subList(1, 10));
    }
    @PutMapping
    public R update(@RequestBody Alarm alarm) {
        if(alarm.getHandle() == null || alarm.getHandle().isEmpty() || alarm.getHandle().length() > 200) {
            return R.errorShow("不可抄过200,且不为空");
        }
        alarm.setIsVerify(true);
        boolean b = alarmService.updateById(alarm);
        return b ? R.ok() : R.error();
    }
}
