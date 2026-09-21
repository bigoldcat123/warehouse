package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.system.entity.DTO.DataDTO;
import com.example.demo.system.entity.DTO.DataDetailDTO;
import com.example.demo.system.entity.PO.HouseInfor;
import com.example.demo.system.entity.PO.ReceiverData;
import com.example.demo.system.entity.PO.PointDefine;
import com.example.demo.system.entity.PO.House;
import com.example.demo.system.entity.PO.StoreName;
import com.example.demo.system.mapper.ReceiverDataMapper;
import com.example.demo.system.service.IHouseInforService;
import com.example.demo.system.service.IHouseService;
import com.example.demo.system.service.IPointDefineService;
import com.example.demo.system.service.IReceiverDataService;
import com.example.demo.system.service.IStoreNameService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
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
    @Autowired
    IHouseService houseService;
    @Autowired
    IHouseInforService houseInforService;
    @Autowired
    IStoreNameService storeNameService;

    @Override
    public List<DataDTO> parseDataDTO(List<ReceiverData> records) {
        return records.stream().map(this::toDataDTO).toList();
    }

    @Override
    public DataDetailDTO getDataDetail(Integer id) {
        ReceiverData receiverData = getById(id);
        if (receiverData == null) {
            return null;
        }

        DataDetailDTO dto = new DataDetailDTO();
        dto.setHouseNo(receiverData.getHouseNo());
        dto.setTestTime(receiverData.getTestDate());
        dto.setOAir(receiverData.getOAir());
        dto.setCo2Air(receiverData.getCo2Air());
        dto.setHousePh3(getLastValue(receiverData.getTemperatureSet()));

        House house = houseService.getHouseByNo(receiverData.getHouseNo());
        if (house != null) {
            dto.setHouseName(house.getHouseName());
            StoreName storeName = storeNameService.getById(house.getWarehouseID());
            if (storeName != null) {
                dto.setWareHouseName(storeName.getStoreName());
            }
        }

        HouseInfor houseInfor = houseInforService.getLatestByHouseNo(receiverData.getHouseNo());
        if (houseInfor != null) {
            dto.setGrainName(houseInfor.getGrainName());
            dto.setGrainWater(houseInfor.getGrainWater());
            dto.setDateOfIn(houseInfor.getDateOfIn());
            dto.setKeeperName(houseInfor.getKeeperName());
        }

        PointDefine pointDefine = pointDefineService.getById(receiverData.getHouseNo());
        if (pointDefine == null) {
            throw new IllegalArgumentException("没有该仓房的测点定义");
        }
        int stringCount = parseDimension(pointDefine.getLength(), "Length");
        int layerCount = parseDimension(pointDefine.getWidth(), "Width");
        dto.setPh3Matrix(parseOptionalMatrix(
                receiverData.getTemperatureSet(), stringCount, layerCount, "PH3"));
        dto.setTemperatureMatrix(parseOptionalMatrix(
                receiverData.getTempData(), stringCount, layerCount, "温度"));
        return dto;
    }

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
        return getMatrix(houseNo, testDate, ReceiverData::getTempData, "温度");
    }

    @Override
    public List<List<Double>> getHumidityMatrix(String houseNo, LocalDateTime testDate) {
        return getMatrix(houseNo, testDate, ReceiverData::getHumiData, "湿度");
    }

    @Override
    public List<List<Double>> getPh3Matrix(String houseNo, LocalDateTime testDate) {
        return getMatrix(houseNo, testDate, ReceiverData::getTemperatureSet, "PH3");
    }

    private List<List<Double>> getMatrix(
            String houseNo,
            LocalDateTime testDate,
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
                .eq("TestDate", testDate)
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

    private String getLastValue(String rawData) {
        if (rawData == null || rawData.isBlank()) {
            return null;
        }
        String[] values = rawData.trim().split("\\s*,\\s*");
        return values[values.length - 1];
    }

    private List<List<Double>> parseOptionalMatrix(
            String rawData,
            int stringCount,
            int layerCount,
            String dataName) {
        if (rawData == null || rawData.isBlank()) {
            return List.of();
        }
        return parseMatrix(rawData, stringCount, layerCount, dataName);
    }

    private DataDTO toDataDTO(ReceiverData receiverData) {
        DataDTO dto = new DataDTO();
        dto.setId(receiverData.getId());
        dto.setHouseNo(receiverData.getHouseNo());
        dto.setTestDate(receiverData.getTestDate());
        dto.setOAir(receiverData.getOAir());
        dto.setCo2Air(receiverData.getCo2Air());

        House house = houseService.getHouseByNo(receiverData.getHouseNo());
        if (house != null) {
            dto.setHouseName(house.getHouseName());
            dto.setHouse_type(house.getHouseType());
        }

        String rawPh3 = receiverData.getTemperatureSet();
        if (rawPh3 == null || rawPh3.isBlank()) {
            setIncompleteLayerStats(dto);
            return dto;
        }
        String[] values = rawPh3.trim().split("\\s*,\\s*");
        dto.setHousePh3(values[values.length - 1]);

        PointDefine pointDefine = pointDefineService.getById(receiverData.getHouseNo());
        Integer stringCount = tryParseDimension(pointDefine == null ? null : pointDefine.getLength());
        Integer layerCount = tryParseDimension(pointDefine == null ? null : pointDefine.getWidth());
        if (stringCount == null || layerCount == null) {
            setIncompleteLayerStats(dto);
            return dto;
        }
        setPh3LayerStats(dto, values, stringCount, layerCount);
        return dto;
    }

    private void setPh3LayerStats(DataDTO dto, String[] values, int stringCount, int layerCount) {
        DecimalFormat decimalFormat = new DecimalFormat("#.0");
        StringBuilder layerMax = new StringBuilder();
        StringBuilder layerMin = new StringBuilder();
        StringBuilder layerAvg = new StringBuilder();
        int sensorValueCount = Math.min(stringCount * layerCount, Math.max(0, values.length - 1));

        for (int layerIndex = 0; layerIndex < layerCount; layerIndex++) {
            List<Double> layerValues = new ArrayList<>();
            for (int stringIndex = 0; stringIndex < stringCount; stringIndex++) {
                int valueIndex = stringIndex * layerCount + layerIndex;
                if (valueIndex >= sensorValueCount) continue;
                try {
                    layerValues.add(Double.parseDouble(values[valueIndex]));
                } catch (NumberFormatException ignored) {
                }
            }
            if (layerValues.isEmpty()) {
                layerMax.append("-- |");
                layerMin.append("-- |");
                layerAvg.append("-- |");
                continue;
            }
            double max = layerValues.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            double min = layerValues.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double avg = layerValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            layerMax.append(decimalFormat.format(max)).append(" |");
            layerMin.append(decimalFormat.format(min)).append(" |");
            layerAvg.append(decimalFormat.format(avg)).append(" |");
        }
        dto.setLayerMax(layerMax.toString());
        dto.setLayerMin(layerMin.toString());
        dto.setLayerAvg(layerAvg.toString());
    }

    private Integer tryParseDimension(String value) {
        try {
            int dimension = Integer.parseInt(value);
            return dimension > 0 ? dimension : null;
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private void setIncompleteLayerStats(DataDTO dto) {
        dto.setLayerMax("数据不全");
        dto.setLayerMin("数据不全");
        dto.setLayerAvg("数据不全");
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
