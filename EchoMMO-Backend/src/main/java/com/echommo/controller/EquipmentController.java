//package com.echommo.controller;
//
//import com.echommo.entity.User;
//import com.echommo.service.EquipmentService;
//import com.echommo.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/equipment")
//@CrossOrigin(origins = "*")
//public class EquipmentController {
//
//    @Autowired
//    private EquipmentService equipmentService;
//
//    @Autowired
//    private UserService userService;
//
//    @PostMapping("/enhance")
//    public ResponseEntity<?> enhance(@RequestBody Map<String, Object> req) {
//        try {
//            if (req.get("userItemId") == null) {
//                return ResponseEntity.badRequest().body("Thiếu userItemId");
//            }
//            // [FIX] Chuyển đổi an toàn sang Integer
//            Integer userItemId = Integer.parseInt(req.get("userItemId").toString());
//
//            return ResponseEntity.ok(equipmentService.enhanceItem(userItemId));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @PostMapping("/evolve")
//    public ResponseEntity<?> evolve(@RequestBody Map<String, Object> req) {
//        try {
//            if (req.get("userItemId") == null) {
//                return ResponseEntity.badRequest().body("Thiếu userItemId");
//            }
//            // [FIX] Chuyển đổi an toàn sang Integer
//            Integer userItemId = Integer.parseInt(req.get("userItemId").toString());
//
//            return ResponseEntity.ok(equipmentService.evolveToMythic(userItemId));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @PostMapping("/enhance-stars")
//    public ResponseEntity<?> enhanceStars(@RequestBody Map<String, Object> req, Authentication auth) {
//        try {
//            if (req.get("userItemId") == null) {
//                return ResponseEntity.badRequest().body("Thiếu userItemId");
//            }
//
//            User user = userService.getUserFromAuth(auth);
//
//            // [FIX] Chuyển đổi an toàn sang Integer
//            Integer userItemId = Integer.parseInt(req.get("userItemId").toString());
//
//            return ResponseEntity.ok(equipmentService.enhanceMythicStars(userItemId, user.getUserId()));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//}

package com.echommo.controller;

import com.echommo.entity.User;
import com.echommo.service.EquipmentService;
import com.echommo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipment")
@CrossOrigin(origins = "*")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private UserService userService;

    // [FIX] Sửa để nhận ID từ URL (khớp với FE: /enhance/123)
    @PostMapping("/enhance/{userItemId}")
    public ResponseEntity<?> enhance(@PathVariable Integer userItemId) {
        try {
            return ResponseEntity.ok(equipmentService.enhanceItem(userItemId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // [FIX] Sửa để nhận ID từ URL
    @PostMapping("/evolve/{userItemId}")
    public ResponseEntity<?> evolve(@PathVariable Integer userItemId) {
        try {
            return ResponseEntity.ok(equipmentService.evolveToMythic(userItemId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // [FIX] Đổi tên endpoint thành "enhance-mythic-star" để khớp FE
    // [FIX] Nhận ID từ URL thay vì Body
    @PostMapping("/enhance-mythic-star/{userItemId}")
    public ResponseEntity<?> enhanceStars(@PathVariable Integer userItemId, Authentication auth) {
        try {
            User user = userService.getUserFromAuth(auth);
            return ResponseEntity.ok(equipmentService.enhanceMythicStars(userItemId, user.getUserId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}