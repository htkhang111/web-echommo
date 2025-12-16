package com.echommo.controller;

import com.echommo.dto.SpaStatusResponse;
import com.echommo.enums.SpaPackage;
import com.echommo.service.SpaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spa")
public class SpaController {

    @Autowired private SpaService spaService;

    // Bắt đầu nghỉ (Gửi packageType: STANDARD hoặc VIP)
    @PostMapping("/start")
    public ResponseEntity<?> startSpa(@RequestParam SpaPackage packageType) {
        try {
            return ResponseEntity.ok(spaService.startSpa(packageType));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Kết thúc nghỉ (Gọi khi hết giờ đếm ngược)
    @PostMapping("/complete")
    public ResponseEntity<?> completeSpa() {
        try {
            return ResponseEntity.ok(spaService.completeSpa());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}