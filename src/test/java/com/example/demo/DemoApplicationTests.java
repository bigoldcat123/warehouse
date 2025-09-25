package com.example.demo;


import com.example.demo.system.entity.PO.Entry;
import com.example.demo.system.service.IEntryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

	@Autowired
    IEntryService entryService;

    @Test
    public  void xx() {
        Entry entry = new Entry();
        entry.setWater(18f);
        entryService.save(entry);
    }

}
