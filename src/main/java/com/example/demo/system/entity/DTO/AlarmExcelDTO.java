package com.example.demo.system.entity.DTO;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@HeadStyle(fillForegroundColor = 9)
@HeadRowHeight(25)
@ContentRowHeight(25)
public class AlarmExcelDTO {
    @ExcelProperty({"报警信息","仓房编号"})
    @ColumnWidth(15)
    private String houseNo;


    /**
     * 报警类型
     */
    @ExcelProperty({"报警信息","报警类型"})

    @ColumnWidth(15)
    private String alertType;

    /**
     * 报警级别
     */
    @ExcelProperty({"报警信息","报警级别"})
    @ColumnWidth(15)
    private String alertLevel;

    /**
     * 报警时间
     */
    @ExcelProperty({"报警信息","报警时间"})
    @ColumnWidth(20)
    private String alertTime;

    /**
     * 处理意见
     */
//    @ExcelProperty({"报警信息","处理意见"})
//    @ColumnWidth(15)
//    private String handle;

    /**
     * 是否核准 0: no 1: yes
     */
    @ExcelProperty({"报警信息","是否核准"})
    @ColumnWidth(15)
    private String isVerify;

//    @ExcelProperty("云图")
//    @ColumnWidth(50)
//    private String yuntu;
}
