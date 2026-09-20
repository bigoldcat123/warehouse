package com.example.demo.system.service.impl;

import com.example.demo.common.Utils;
import com.example.demo.system.entity.DTO.DataDetailDTO;
import com.example.demo.system.entity.DTO.HouseTempRecordDTO;
import com.example.demo.system.entity.PO.Data;
import com.example.demo.system.entity.PO.Entry;
import com.example.demo.system.entity.PO.House;
import com.example.demo.system.entity.PO.Warehouse;
import com.example.demo.system.mapper.DataMapper;
import com.example.demo.system.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.ToDoubleFunction;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author czh
 * @since 2024-07-28
 */
@Service
public class DataServiceImpl extends ServiceImpl<DataMapper, Data> implements IDataService {

    @Autowired
    private IHouseService houseService;
    @Autowired
    private IEntryService entryService;
    @Autowired
    private IUserAuthService userAuthService;
    @Autowired
    IWarehouseService warehouseService;

    /** 随机模拟序列点数 */
    private static final int RANDOM_SERIES_COUNT = 24;
    /** 随机模拟序列时间间隔（小时） */
    private static final int RANDOM_SERIES_HOUR_STEP = 4;
    /** 湿度随机模拟范围（%） */
    private static final double HUMIDITY_MIN = 20.0;
    private static final double HUMIDITY_MAX = 90.0;

    @Override
    public DataDetailDTO getDataDetail(Integer id) {
        Data data = baseMapper.selectById(id);
        House house = houseService.getHouseByNo(data.getHouseNo());
        DataDetailDTO dataDetailDTO = new DataDetailDTO();
        if(house == null) {
            return dataDetailDTO;
        }
        dataDetailDTO.setHouseName(house.getHouseName());
        dataDetailDTO.setHouseNo(house.getHouseNo());

        Warehouse warehouse = warehouseService.getById(house.getWarehouseID());
        dataDetailDTO.setWareHouseName(warehouse.getWarehouseName());

        dataDetailDTO.setTestTime(data.getTestDate());

        dataDetailDTO.setInHumidity(Utils.is_valid_humidity(data.getInHumidity())? String.format("%.1f",data.getInHumidity()):"");
        dataDetailDTO.setInTemperature(Utils.is_valid_temperature(data.getInTemperature()) ? String.format("%.1f",data.getInTemperature()) : "");
        dataDetailDTO.setOutHumidity(Utils.is_valid_humidity(data.getOutHumidity())? String.format("%.1f",data.getOutHumidity()) : "");
        dataDetailDTO.setOutTemperature(Utils.is_valid_temperature(data.getOutTemperature()) ? String.format("%.1f",data.getOutTemperature()) : "");

        Entry entry =  entryService.getNewest(houseService.getHouseByNo(data.getHouseNo()).getId());
        if (entry != null) {
            dataDetailDTO.setKeeper(userAuthService.getById(entry.getStockman()).getUsername());
            dataDetailDTO.setBreed(entry.getBreed());
            dataDetailDTO.setWater(String.format("%.1f",entry.getWater()));
            dataDetailDTO.setEntryTime(entry.getEntryTime().getYear() + "-" + entry.getEntryTime().getMonthValue() + "-" + entry.getEntryTime().getDayOfMonth());
        }else  {
            dataDetailDTO.setKeeper("no entry");
            dataDetailDTO.setBreed("no entry");
            dataDetailDTO.setWater("no entry");
            dataDetailDTO.setEntryTime("no entry");
        }


//        dataDetailDTO.setListAuto(List.of(data.getTemperatureSet().split(",")).stream().map(x -> Float.parseFloat(x)).toList(),house);
//        dataDetailDTO.setList(parseLayers(data.getTemperatureSet().split(","),house));
        String[] temps=get_temps(data.getTemperatureSet().split("#"),house);

        dataDetailDTO.setList(parseLayers(temps,house));
        return dataDetailDTO;
    }

    @Override
    public List<HouseTempRecordDTO> getTempRecordsByHouseNo(String houseNo, int ceng, int hang, int lie) {
        QueryWrapper<Data> q = new QueryWrapper<Data>();
        q.eq("HouseNo",houseNo);
        List<Data> list = list(q);

        return list.stream()
                .filter(x -> x.getGasStrength() != null)
                .flatMap(x -> {
                    // 按 # 分割每条记录
                    String[] segments = x.getGasStrength().split("#");
                    return Arrays.stream(segments)
                            .map(seg -> {
                                // 按 $ 分割：列:行:层$温度$5
                                String[] parts = seg.split("\\$");
                                if (parts.length < 2) return null;
                                // 按 : 分割坐标：列:行:层（1-based）
                                String[] coords = parts[0].split(":");
                                if (coords.length < 3) return null;
                                try {
                                    int segLie = Integer.parseInt(coords[0]);
                                    int segHang = Integer.parseInt(coords[1]);
                                    int segCeng = Integer.parseInt(coords[2]);
                                    // 过滤匹配的 层、行、列
                                    if (segCeng == ceng && segHang == hang && segLie == lie) {
                                        HouseTempRecordDTO dto = new HouseTempRecordDTO();
                                        dto.setTestDate(x.getTestDate());
                                        String tempStr = parts[1].trim();
                                        if ("NULL".equals(tempStr)) {
                                            dto.setTemp(0f);
                                        } else {
                                            dto.setTemp(Float.parseFloat(tempStr));
                                        }
                                        return dto;
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                                return null;
                            })
                            .filter(dto -> dto != null);
                })
                .toList();
    }

    @Override
    public List<HouseTempRecordDTO> getLayerAvgTempByHouseNo(String houseNo, int ceng) {
        QueryWrapper<Data> q = new QueryWrapper<Data>();
        q.eq("HouseNo", houseNo);
        List<Data> list = list(q);

        return list.stream()
                .filter(x -> x.getGasStrength() != null)
                .map(x -> {
                    // 按 # 分割每条记录
                    String[] segments = x.getGasStrength().split("#");
                    // 收集该层所有温度值
                    List<Float> temps = Arrays.stream(segments)
                            .map(seg -> {
                                String[] parts = seg.split("\\$");
                                if (parts.length < 2) return null;
                                String[] coords = parts[0].split(":");
                                if (coords.length < 3) return null;
                                try {
                                    int segCeng = Integer.parseInt(coords[2]);
                                    if (segCeng == ceng) {
                                        String tempStr = parts[1].trim();
                                        if ("NULL".equals(tempStr)) return 0f;
                                        return Float.parseFloat(tempStr);
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                                return null;
                            })
                            .filter(t -> t != null)
                            .toList();
                    // 计算该层平均温度
                    if (temps.isEmpty()) return null;
                    float avg = (float) temps.stream()
                            .mapToDouble(f -> f)
                            .average()
                            .orElse(0);
                    HouseTempRecordDTO dto = new HouseTempRecordDTO();
                    dto.setTestDate(x.getTestDate());
                    dto.setTemp(avg);
                    return dto;
                })
                .filter(dto -> dto != null)
                .toList();
    }

    @Override
    public List<HouseTempRecordDTO> getAllAvgTempByHouseNo(String houseNo) {
        QueryWrapper<Data> q = new QueryWrapper<Data>();
        q.eq("HouseNo", houseNo);
        List<Data> list = list(q);

        return list.stream()
                .filter(x -> x.getGasStrength() != null)
                .map(x -> {
                    // 按 # 分割每条记录
                    String[] segments = x.getGasStrength().split("#");
                    // 收集全部温度值
                    List<Float> temps = Arrays.stream(segments)
                            .map(seg -> {
                                String[] parts = seg.split("\\$");
                                if (parts.length < 2) return null;
                                try {
                                    String tempStr = parts[1].trim();
                                    if ("NULL".equals(tempStr)) return 0f;
                                    return Float.parseFloat(tempStr);
                                } catch (NumberFormatException ignored) {
                                }
                                return null;
                            })
                            .filter(t -> t != null)
                            .toList();
                    // 计算全部点的平均温度
                    if (temps.isEmpty()) return null;
                    double avg = temps.stream()
                            .mapToDouble(f -> f)
                            .average()
                            .orElse(0);
                    HouseTempRecordDTO dto = new HouseTempRecordDTO();
                    dto.setTestDate(x.getTestDate());
                    dto.setTemp((float) avg);
                    return dto;
                })
                .filter(dto -> dto != null)
                .toList();
    }

    // ==================== 湿度（随机模拟）/ 气体浓度（真实数据） 折线数据 ====================

    @Override
    public List<HouseTempRecordDTO> getHumidityRecordsByHouseNo(String houseNo, int ceng, int hang, int lie) {
        House house = houseService.getHouseByNo(houseNo);
        if (house == null || house.getX() == null || house.getY() == null || house.getZ() == null
                || ceng < 1 || ceng > house.getZ()
                || hang < 1 || hang > house.getX()
                || lie < 1 || lie > house.getY()) {
            return List.of();
        }
        long seed = seedOf(houseNo, "humidity");
        return randomRecords(house, HUMIDITY_MIN, HUMIDITY_MAX, seed,
                cube -> cube[ceng - 1][hang - 1][lie - 1]);
    }

    @Override
    public List<HouseTempRecordDTO> getLayerAvgHumidityByHouseNo(String houseNo, int ceng) {
        House house = houseService.getHouseByNo(houseNo);
        if (house == null || house.getX() == null || house.getY() == null || house.getZ() == null
                || ceng < 1 || ceng > house.getZ()) {
            return List.of();
        }
        long seed = seedOf(houseNo, "humidity");
        return randomRecords(house, HUMIDITY_MIN, HUMIDITY_MAX, seed,
                cube -> layerAvg(cube[ceng - 1]));
    }

    @Override
    public List<HouseTempRecordDTO> getAllAvgHumidityByHouseNo(String houseNo) {
        House house = houseService.getHouseByNo(houseNo);
        if (house == null || house.getX() == null || house.getY() == null || house.getZ() == null) {
            return List.of();
        }
        long seed = seedOf(houseNo, "humidity");
        return randomRecords(house, HUMIDITY_MIN, HUMIDITY_MAX, seed, DataServiceImpl::cubeAvg);
    }

    @Override
    public List<HouseTempRecordDTO> getGasRecordsByHouseNo(String houseNo, int ceng, int hang, int lie) {
        QueryWrapper<Data> q = new QueryWrapper<Data>();
        q.eq("HouseNo", houseNo);
        List<Data> list = list(q);

        return list.stream()
                .filter(x -> x.getGasStrength() != null)
                .flatMap(x -> {
                    // 按 # 分割每条记录
                    String[] segments = x.getGasStrength().split("#");
                    return Arrays.stream(segments)
                            .map(seg -> {
                                // 按 $ 分割：列:行:层$气体浓度$5
                                String[] parts = seg.split("\\$");
                                if (parts.length < 2) return null;
                                // 按 : 分割坐标：列:行:层（1-based）
                                String[] coords = parts[0].split(":");
                                if (coords.length < 3) return null;
                                try {
                                    int segLie = Integer.parseInt(coords[0]);
                                    int segHang = Integer.parseInt(coords[1]);
                                    int segCeng = Integer.parseInt(coords[2]);
                                    // 过滤匹配的 层、行、列
                                    if (segCeng == ceng && segHang == hang && segLie == lie) {
                                        HouseTempRecordDTO dto = new HouseTempRecordDTO();
                                        dto.setTestDate(x.getTestDate());
                                        String tempStr = parts[1].trim();
                                        if ("NULL".equals(tempStr)) {
                                            dto.setTemp(0f);
                                        } else {
                                            dto.setTemp(Float.parseFloat(tempStr));
                                        }
                                        return dto;
                                    }
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                                return null;
                            })
                            .filter(dto -> dto != null);
                })
                .toList();
    }

    @Override
    public List<HouseTempRecordDTO> getLayerAvgGasByHouseNo(String houseNo, int ceng) {
        QueryWrapper<Data> q = new QueryWrapper<Data>();
        q.eq("HouseNo", houseNo);
        List<Data> list = list(q);

        return list.stream()
                .filter(x -> x.getGasStrength() != null)
                .map(x -> {
                    // 按 # 分割每条记录
                    String[] segments = x.getGasStrength().split("#");
                    // 收集该层所有气体浓度值
                    List<Float> temps = Arrays.stream(segments)
                            .map(seg -> {
                                String[] parts = seg.split("\\$");
                                if (parts.length < 2) return null;
                                String[] coords = parts[0].split(":");
                                if (coords.length < 3) return null;
                                try {
                                    int segCeng = Integer.parseInt(coords[2]);
                                    if (segCeng == ceng) {
                                        String tempStr = parts[1].trim();
                                        if ("NULL".equals(tempStr)) return 0f;
                                        return Float.parseFloat(tempStr);
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                                return null;
                            })
                            .filter(t -> t != null)
                            .toList();
                    // 计算该层平均气体浓度
                    if (temps.isEmpty()) return null;
                    float avg = (float) temps.stream()
                            .mapToDouble(f -> f)
                            .average()
                            .orElse(0);
                    HouseTempRecordDTO dto = new HouseTempRecordDTO();
                    dto.setTestDate(x.getTestDate());
                    dto.setTemp(avg);
                    return dto;
                })
                .filter(dto -> dto != null)
                .toList();
    }

    @Override
    public List<HouseTempRecordDTO> getAllAvgGasByHouseNo(String houseNo) {
        QueryWrapper<Data> q = new QueryWrapper<Data>();
        q.eq("HouseNo", houseNo);
        List<Data> list = list(q);

        return list.stream()
                .filter(x -> x.getGasStrength() != null)
                .map(x -> {
                    // 按 # 分割每条记录
                    String[] segments = x.getGasStrength().split("#");
                    // 收集全部气体浓度值
                    List<Float> temps = Arrays.stream(segments)
                            .map(seg -> {
                                String[] parts = seg.split("\\$");
                                if (parts.length < 2) return null;
                                try {
                                    String tempStr = parts[1].trim();
                                    if ("NULL".equals(tempStr)) return 0f;
                                    return Float.parseFloat(tempStr);
                                } catch (NumberFormatException ignored) {
                                }
                                return null;
                            })
                            .filter(t -> t != null)
                            .toList();
                    // 计算全部点的平均气体浓度
                    if (temps.isEmpty()) return null;
                    double avg = temps.stream()
                            .mapToDouble(f -> f)
                            .average()
                            .orElse(0);
                    HouseTempRecordDTO dto = new HouseTempRecordDTO();
                    dto.setTestDate(x.getTestDate());
                    dto.setTemp((float) avg);
                    return dto;
                })
                .filter(dto -> dto != null)
                .toList();
    }

    /**
     * 按采集时间生成随机模拟三维数组序列（[层][行][列]），并映射为折线记录
     * @param house 仓房（取 z/x/y 维度）
     * @param min 随机最小值（含）
     * @param max 随机最大值（含）
     * @param seed 随机种子（同一仓房+同一指标多次请求数值一致）
     * @param selector 从单个时间点的三维数组取该时间点的值（点位 / 层平均 / 全仓平均）
     */
    private List<HouseTempRecordDTO> randomRecords(House house, double min, double max, long seed,
                                                   ToDoubleFunction<double[][][]> selector) {
        return buildRandomCubeSeries(house, min, max, seed)
                .stream()
                .map(p -> {
                    HouseTempRecordDTO dto = new HouseTempRecordDTO();
                    dto.setTestDate(p.time);
                    dto.setTemp(round1(selector.applyAsDouble(p.cube)));
                    return dto;
                })
                .toList();
    }

    /**
     * 生成随机模拟三维数组时间序列：每 RANDOM_SERIES_HOUR_STEP 小时一个点，共 RANDOM_SERIES_COUNT 个点，终点为当前整点
     */
    private static List<RandomCubePoint> buildRandomCubeSeries(House house, double min, double max, long seed) {
        if (house.getX() == null || house.getY() == null || house.getZ() == null) {
            return List.of();
        }
        Random random = new Random(seed);
        int x = house.getX();
        int y = house.getY();
        int z = house.getZ();
        LocalDateTime end = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        List<RandomCubePoint> series = new ArrayList<>(RANDOM_SERIES_COUNT);
        for (int i = 0; i < RANDOM_SERIES_COUNT; i++) {
            LocalDateTime time = end.minusHours((long) (RANDOM_SERIES_COUNT - 1 - i) * RANDOM_SERIES_HOUR_STEP);
            double[][][] cube = new double[z][x][y];
            for (int ceng = 0; ceng < z; ceng++) {
                for (int hang = 0; hang < x; hang++) {
                    for (int lie = 0; lie < y; lie++) {
                        cube[ceng][hang][lie] = min + random.nextDouble() * (max - min);
                    }
                }
            }
            series.add(new RandomCubePoint(time, cube));
        }
        return series;
    }

    /** 由仓房编号+指标生成稳定随机种子 */
    private static long seedOf(String houseNo, String metric) {
        return (long) houseNo.hashCode() * 31L + metric.hashCode();
    }

    /** 二维数组（一层 行×列）平均值 */
    private static double layerAvg(double[][] layer) {
        double sum = 0;
        int n = 0;
        for (double[] row : layer) {
            for (double v : row) {
                sum += v;
                n++;
            }
        }
        return n == 0 ? 0 : sum / n;
    }

    /** 三维数组（全部点）平均值 */
    private static double cubeAvg(double[][][] cube) {
        double sum = 0;
        int n = 0;
        for (double[][] layer : cube) {
            for (double[] row : layer) {
                for (double v : row) {
                    sum += v;
                    n++;
                }
            }
        }
        return n == 0 ? 0 : sum / n;
    }

    /** 保留 1 位小数 */
    private static float round1(double v) {
        return Math.round(v * 10.0) / 10f;
    }

    /** 随机模拟时间点：采集时间 + 该时刻的三维数组 [层][行][列] */
    private static final class RandomCubePoint {
        final LocalDateTime time;
        final double[][][] cube;

        RandomCubePoint(LocalDateTime time, double[][][] cube) {
            this.time = time;
            this.cube = cube;
        }
    }

    @Override
    public int insertRandomData(String houseNo, int count, int x, int y, int z) {
        Random random = new Random();
        List<Data> dataList = new ArrayList<>();

        // 基准时间：2026-01-01 08:00:00
        LocalDateTime baseTime = LocalDateTime.of(2026, 1, 1, 8, 0, 0);

        for (int n = 0; n < count; n++) {
            Data data = new Data();
            data.setHouseNo(houseNo);
            // 每条记录间隔 4 小时
            data.setTestDate(baseTime.plusHours(4L * n));
            data.setInTemperature(15.0f + random.nextFloat() * 10);
            data.setInHumidity(40.0f + random.nextFloat() * 30);
            data.setOutTemperature(5.0f + random.nextFloat() * 20);
            data.setOutHumidity(30.0f + random.nextFloat() * 40);
            data.setGrainWater(10.0f + random.nextFloat() * 5);

            // 生成 gasStrength：列(lie):行(hang):层(ceng)$值$5
            StringBuilder sb = new StringBuilder();
            for (int lie = 1; lie <= y; lie++) {
                for (int hang = 1; hang <= x; hang++) {
                    for (int ceng = 1; ceng <= z; ceng++) {
                        if (sb.length() > 0) sb.append("#");
                        float temp = 10.0f + random.nextFloat() * 25;
                        sb.append(String.format("%d:%d:%d$%.1f$5", lie, hang, ceng, temp));
                    }
                }
            }
            data.setGasStrength(sb.toString());
            data.setTemperatureSet(sb.toString());
            dataList.add(data);
        }

        saveBatch(dataList);
        return dataList.size();
    }

    @Override
    public Map<LocalDateTime, List<List<List<String>>>> getTemperatureCubeByHouseNo(String houseNo) {
        Map<LocalDateTime, List<List<List<String>>>> result = new LinkedHashMap<>();
        House house = houseService.getHouseByNo(houseNo);
        if (house == null) {
            return result;
        }
        QueryWrapper<Data> q = new QueryWrapper<Data>();
        q.eq("HouseNo", houseNo);
        q.orderByAsc("TestDate");
        List<Data> list = list(q);

        for (Data data : list) {
            if (data.getGasStrength() == null) continue;
            String[] temps = get_temps(data.getGasStrength().split("#"), house);
            result.put(data.getTestDate(), parseLayers(temps, house));
        }
        return result;
    }

    @Override
    public Map<LocalDateTime, List<List<List<String>>>> getHumidityCubeByHouseNo(String houseNo) {
        Map<LocalDateTime, List<List<List<String>>>> result = new LinkedHashMap<>();
        House house = houseService.getHouseByNo(houseNo);
        if (house == null) {
            return result;
        }
        result.put(LocalDateTime.now().withNano(0), buildRandomCube(house, 20, 90));
        return result;
    }

    @Override
    public Map<LocalDateTime, List<List<List<String>>>> getGasCubeByHouseNo(String houseNo) {
        Map<LocalDateTime, List<List<List<String>>>> result = new LinkedHashMap<>();
        House house = houseService.getHouseByNo(houseNo);
        if (house == null) {
            return result;
        }
        result.put(LocalDateTime.now().withNano(0), buildRandomCube(house, 300, 3000));
        return result;
    }

    /**
     * 根据仓房 xyz 维度生成随机三维数组 [层][行][列]，保留 1 位小数
     * @param house 仓房（取 z/x/y 维度）
     * @param min 随机最小值（含）
     * @param max 随机最大值（含）
     */
    static private List<List<List<String>>> buildRandomCube(House house, double min, double max) {
        Random random = new Random();
        int x = house.getX();
        int y = house.getY();
        int z = house.getZ();
        List<List<List<String>>> cube = new ArrayList<>();
        for (int ceng = 0; ceng < z; ceng++) {
            List<List<String>> layer = new ArrayList<>();
            for (int hang = 0; hang < x; hang++) {
                List<String> row = new ArrayList<>();
                for (int lie = 0; lie < y; lie++) {
                    row.add(String.format("%.1f", min + random.nextDouble() * (max - min)));
                }
                layer.add(row);
            }
            cube.add(layer);
        }
        return cube;
    }


    static private String[] get_temps(String[] v_temps,House house)
    {
        int num=v_temps.length;

        String[] v_data=new String[num];

        int x = house.getX();
        int y = house.getY();
        int z = house.getZ();
        int ceng,hang,lie;
        for(int i=0;i<num;i++)
        {
            String[] jie=v_temps[i].split("\\$");
            if(jie.length>=2)
            {
                String[] pailie=jie[0].split(":");
                if(pailie.length>2) {
                    lie = Integer.parseInt(pailie[0])-1;
                    hang = Integer.parseInt(pailie[1])-1;
                    ceng = Integer.parseInt(pailie[2])-1;
//                    System.out.println("ceng:"+ceng + "lie" + lie + "hang" + hang);
                    String wendu = jie[1];
                    if(wendu.equals("NULL")) wendu="0";
                    if((ceng + hang*z + lie*x*z) < num)
                        v_data[(ceng + hang*z + lie*x*z) ]=wendu;//这好像忘改了， 我测试的时候 没问题 奇怪了

                } else if(pailie.length==2)
                {
                    ceng = 0;
                    z=1;
                    hang = Integer.parseInt(pailie[1]) - 1;
                    lie = Integer.parseInt(pailie[0]) - 1;
                    String wendu = jie[1];
                    if(wendu.equals("NULL")) wendu="0";
                    if((ceng + hang*z + lie*x*z) < num)
                        v_data[(ceng + hang*z + lie*x*z)]=wendu;
                }

            }
        }
        for(int i=0;i<num;i++)
            if(v_data[i]==null) v_data[i]="-999";//前端得转数字校验但是有温度是0的ok 应该没问题了OK那就这样？好
        return v_data;

    }
    static private  List<List<List<String>>> parseLayers(String[] temps, House house) {



        int x = house.getX();// row
        int y = house.getY();// column
        int z = house.getZ();// layer


        List<List<List<String>>> layers = new ArrayList<>();

        for (int i = 0; i < z; i++) {
            List<List<String>> layer = new ArrayList<>();
            for (int j = 0; j < x; j++) {
                List<String> row = new ArrayList<>();
                for (int k = 0; k < y; k++) {
                    if((i + j*z + k*z*x) >= temps.length)
                        row.add("0");
                    else
                        row.add(String.format("%.1f",Float.parseFloat(temps[i + j*z + k*z*x])));
                }
                layer.add(row);
            }
            layers.add(layer);
        }
        return layers;
    }

    public static void main(String[] args) {//这是对的
        House h = new House();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= 3; i++) {// lie
            for (int j = 1; j <= 2; j++) {// hang
                for (int k = 1; k <= 2; k++) {// ceng
                    stringBuilder.append(i + ":" + j + ":" + k + "$" + i+j + "."+k + "#");
                }
            }
        }
        System.out.println(stringBuilder.toString());
        h.setX(2);//hang
        h.setY(3);//lie
        h.setZ(2);//ceng
        String[] x = get_temps(stringBuilder.toString().split("#"), h);
        System.out.println(Arrays.toString(x));
        List<List<List<String>>> lists = parseLayers(x, h);
        System.out.println(lists);
    }

}
