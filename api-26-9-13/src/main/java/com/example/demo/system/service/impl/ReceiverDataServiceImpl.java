package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.system.entity.PO.ReceiverData;
import com.example.demo.system.entity.PO.PointDefine;
import com.example.demo.system.mapper.ReceiverDataMapper;
import com.example.demo.system.service.IPointDefineService;
import com.example.demo.system.service.IReceiverDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author czh
 * @since 2024-07-27
 */
@Service
public class ReceiverDataServiceImpl extends ServiceImpl<ReceiverDataMapper, ReceiverData> implements IReceiverDataService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    IPointDefineService pointDefineService;

    @Override
    public List<String> getTestDates(String houseNo) {
        validateHouseNo(houseNo);
        QueryWrapper<ReceiverData> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT TestDate")
                .eq("HouseNo", houseNo)
                .isNotNull("TestDate")
                .orderByAsc("TestDate");
        return list(queryWrapper).stream()
                .map(ReceiverData::getTestDate)
                .map(DATE_TIME_FORMATTER::format)
                .toList();
    }

    @Override
    public List<List<Double>> getTemperatureMatrix(String houseNo, LocalDateTime testDate) {
        return getMatrix(houseNo, testDate, "TestDate_THW", ReceiverData::getTempData, "温度");
    }

    @Override
    public List<List<Double>> getHumidityMatrix(String houseNo, LocalDateTime testDate) {
        return getMatrix(houseNo, testDate, "TestDate_THW", ReceiverData::getHumiData, "湿度");
    }

    @Override
    public List<List<Double>> getPh3Matrix(String houseNo, LocalDateTime testDate) {
        return getMatrix(houseNo, testDate, "TestDate", ReceiverData::getTemperatureSet, "PH3");
    }

    private List<List<Double>> getMatrix(
            String houseNo,
            LocalDateTime testDate,
            String dateColumn,
            Function<ReceiverData, String> dataGetter,
            String dataName) {
        validateHouseNo(houseNo);
        PointDefine pointDefine = pointDefineService.getById(houseNo);
        if (pointDefine == null) {
            throw new IllegalArgumentException("没有该仓房的测点定义");
        }

        int stringCount = parseDimension(pointDefine.getLength(), "Length");
        int layerCount = parseDimension(pointDefine.getWidth(), "Width");

        QueryWrapper<ReceiverData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("HouseNo", houseNo)
                .eq(dateColumn, testDate)
                .orderByDesc("ID")
                .last("LIMIT 1");
        ReceiverData receiverData = getOne(queryWrapper, false);
        if (receiverData == null) {
            throw new IllegalArgumentException("没有找到指定时间的采集数据");
        }

        String rawData = dataGetter.apply(receiverData);
        if (rawData == null || rawData.isBlank()) {
            throw new IllegalArgumentException(dataName + "数据为空");
        }
        return parseMatrix(rawData, stringCount, layerCount, dataName);
    }

    private void validateHouseNo(String houseNo) {
        if (houseNo == null || houseNo.isBlank()) {
            throw new IllegalArgumentException("仓房编号不能为空");
        }
    }

    private int parseDimension(String value, String fieldName) {
        try {
            int dimension = Integer.parseInt(value);
            if (dimension <= 0) {
                throw new NumberFormatException();
            }
            return dimension;
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("测点定义中的 " + fieldName + " 必须是正整数");
        }
    }

    private List<List<Double>> parseMatrix(
            String rawData,
            int stringCount,
            int layerCount,
            String dataName) {
        String[] values = rawData.trim().split("\\s*,\\s*");
        int expectedCount = stringCount * layerCount;
        if (values.length != expectedCount) {
            throw new IllegalArgumentException(
                    dataName + "数据点数量错误，应为 " + expectedCount + " 个，实际为 " + values.length + " 个");
        }

        List<List<Double>> matrix = new ArrayList<>(stringCount);
        int index = 0;
        try {
            for (int stringIndex = 0; stringIndex < stringCount; stringIndex++) {
                List<Double> layers = new ArrayList<>(layerCount);
                for (int layerIndex = 0; layerIndex < layerCount; layerIndex++) {
                    layers.add(Double.parseDouble(values[index++]));
                }
                matrix.add(layers);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(dataName + "数据包含非数字内容");
        }
        return matrix;
    }
}
