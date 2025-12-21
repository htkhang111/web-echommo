package com.echommo.controller;

import com.echommo.service.DumpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dump")
public class DumpController {

    @Autowired private DumpService dumpService;

    @PostMapping("/fish")
    public ResponseEntity<?> dumpFish(@RequestBody Map<String, Object> payload) {
        try {
            String type = (String) payload.get("type"); // "NORMAL" or "SHARK"
            int amount = (int) payload.get("amount");
            return ResponseEntity.ok(dumpService.dumpFish(type, amount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}