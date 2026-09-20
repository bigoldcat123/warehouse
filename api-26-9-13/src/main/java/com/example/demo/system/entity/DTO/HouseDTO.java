package com.example.demo.system.entity.DTO;

import com.example.demo.system.entity.PO.House;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class HouseDTO extends House {
    String breed;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDateTime entryTime;
    Float water;
    boolean has_kt;

}
