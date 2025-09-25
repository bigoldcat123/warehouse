package com.example.demo.system.service.impl;

import com.example.demo.common.Utils;
import com.example.demo.system.entity.DTO.DataDTO;
import com.example.demo.system.entity.DTO.DataDetailDTO;
import com.example.demo.system.entity.PO.Data;
import com.example.demo.system.entity.PO.Entry;
import com.example.demo.system.entity.PO.House;
import com.example.demo.system.entity.PO.Warehouse;
import com.example.demo.system.mapper.DataMapper;
import com.example.demo.system.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    @Override
    public List<DataDTO> parseDTO(List<Data> list) {
        List<DataDTO> dataDTOList = new ArrayList<>();
        list.forEach(data -> {
            DataDTO dataDTO = new DataDTO();
            House house = houseService.getHouseByNo(data.getHouseNo());
            dataDTO.setId(data.getId());
            dataDTO.setHouseNo(data.getHouseNo());
            dataDTO.setInTemperature(Utils.is_valid_temperature(data.getInTemperature()) ? data.getInTemperature().toString() : "");
            dataDTO.setInHumidity(Utils.is_valid_humidity(data.getInHumidity())? data.getInHumidity().toString() : "");
            dataDTO.setTestDate(data.getTestDate());
            String[] temps_1 = data.getTemperatureSet().split("#");
            String[] temps=get_temps(temps_1,house);
            dataDTO.setHouse_type(house.getHouseType());
            List<List<List<String>>> layers = parseLayers(temps, house);
            dataDTO.setHouseName(house.getHouseName());
            dataDTO.setLayerTemplate(layers);
            dataDTO.setTemperatures(List.of(temps));
            dataDTOList.add(dataDTO);
//            if(house==null){
////                dataDTO.setHouse_type("无此仓房");
////                dataDTO.setHouseName("无此仓房");
////                dataDTO.setTemperatures(List.of(temps));
////                dataDTO.setLayerTemplate(null);
////                dataDTOList.add(dataDTO);
//            }else {
//
//            }
        });

        return dataDTOList;
    }

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
