package com.example.demo;

import com.example.demo.system.service.IDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InsertRandomDataTest {

    @Autowired
    IDataService dataService;

    @Test
    void insertRandomData() {
        // houseNo=003, 插入100条记录, z=4(层) x=10(行) y=5(列)
        int count = dataService.insertRandomData("003", 100, 10, 5, 4);
        System.out.println("成功插入 " + count + " 条随机数据");
    }
}
