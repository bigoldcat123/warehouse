package com.example.demo.system.controller;

import com.example.demo.common.R;
import com.example.demo.system.entity.DTO.NhlsttHouseNameDTO;
import com.example.demo.system.entity.PO.House;
import com.example.demo.system.service.IHouseService;
import com.example.demo.system.service.INhlsttService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author czh
 * @since 2025-10-10
 */
@RestController
@RequestMapping("/nhlstt")
public class NhlsttController {
    @Autowired
    INhlsttService nhlsttService;
    @Autowired
    IHouseService houseService;
    @GetMapping()
    public R nhlstt() {
        List<NhlsttHouseNameDTO> res = nhlsttService.list().stream().map(x -> {
            House houseByNo = houseService.getHouseByNo(x.getHouseNo());
            NhlsttHouseNameDTO nhlsttHouseNameDTO = new NhlsttHouseNameDTO();
            BeanUtils.copyProperties(x, nhlsttHouseNameDTO);
            if (houseByNo != null) {
                nhlsttHouseNameDTO.setHouseName(houseByNo.getHouseName());
            }
            return nhlsttHouseNameDTO;
        }).toList();
        return R.ok(res);
    }
}
