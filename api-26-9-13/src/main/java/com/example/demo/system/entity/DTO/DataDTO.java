package com.example.demo.system.entity.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class DataDTO {
    private Integer id;
    private String houseNo;
    private String houseName;

    private String house_type;

    private String inTemperature;
    private String inHumidity;
    private String maxTemperature;
    private String minTemperature;
    private String avgTemperature;
    private String layerMax;
    private String layerMin;
    private String layerAvg;
    @JsonFormat(pattern = "yy-MM-dd")
    private LocalDateTime testDate;

    public void setTemperatures(List<String> temps) {
        DecimalFormat df = new DecimalFormat("#.0");
        List<Float> list = new ArrayList<>(temps.stream().map(Float::parseFloat).filter(x -> x > -40.0 && x < 80.0).toList());
        list.sort(Float::compareTo);

        if (!list.isEmpty()) {
            setMinTemperature((df.format(list.get(0))));
            setMaxTemperature((df.format(list.get(list.size() - 1))));
        }

        Float s = 0f;
        for (Float t : list) {
            s += (t);
        }
        setAvgTemperature(df.format(s / list.size()));
    }

    public static void main(String[] args) {

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < 289; i++) {
            builder.append(i).append(",");
        }
        System.out.println(builder.toString());
    }
    public void setLayerTemplate(List<List<List<String>>> lists) {
        if(lists == null) {
            setLayerMax("数据不全");
            setLayerMin("数据不全");
            setLayerAvg("数据不全");
            return;
        }
        StringBuilder max = new StringBuilder();
        StringBuilder min = new StringBuilder();
        StringBuilder avg = new StringBuilder();
        for (List<List<String> >list : lists) {
            List<String> list1 = new ArrayList<>();
            list.forEach(list1::addAll);
            setTemperatures(list1);
            max.append(maxTemperature).append(" |");
            min.append(minTemperature).append(" |");
            avg.append(avgTemperature).append(" |");
        }
        setLayerMax(max.toString());
        setLayerMin(min.toString());
        setLayerAvg(avg.toString());
    }
}
