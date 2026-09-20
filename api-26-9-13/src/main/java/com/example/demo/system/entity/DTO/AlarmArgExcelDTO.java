package com.example.demo.system.entity.DTO;


import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.*;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.alibaba.excel.enums.poi.VerticalAlignmentEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@HeadStyle(fillForegroundColor = 9)
@HeadRowHeight(30)
@ContentRowHeight(40)
public class AlarmArgExcelDTO {


    @ExcelProperty({"报警信息统计","单位编号"})
    @ColumnWidth(17)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER,verticalAlignment = VerticalAlignmentEnum.CENTER)

    private String wareHouseNO;

    @ExcelProperty({"报警信息统计","单位名称"})
    @ColumnWidth(20)
    private String wareHouseName;

    @ExcelProperty({"报警信息统计","一般"})
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER,verticalAlignment = VerticalAlignmentEnum.CENTER)

    @ColumnWidth(8)
    private int common;

    @ExcelProperty({"报警信息统计","严重"})
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER,verticalAlignment = VerticalAlignmentEnum.CENTER)

    @ColumnWidth(8)
    private int serious;

    @ExcelProperty({"报警信息统计","发热"})
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER,verticalAlignment = VerticalAlignmentEnum.CENTER)

    @ColumnWidth(8)
    private int hot;

    @ExcelProperty({"报警信息统计","结露霉变"})
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER,verticalAlignment = VerticalAlignmentEnum.CENTER)

    @ColumnWidth(12)
    private int dewAndMould;

    @ExcelProperty({"报警信息统计","异常"})
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER,verticalAlignment = VerticalAlignmentEnum.CENTER)
    @ColumnWidth(8)
    private int exception;
}
