package com.echommo.service;

import com.echommo.entity.Announcement;
import com.echommo.entity.User;
import com.echommo.enums.NotificationType; // [FIX] Import Enum
import com.echommo.enums.Role;
import com.echommo.repository.AnnouncementRepository;
import com.echommo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnnouncementService {

    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationService notificationService;

    public List<Announcement> getActiveAnnouncements() {
        return announcementRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    @Transactional
    public Announcement createAnnouncement(Announcement req) {
        checkAdmin();
        req.setIsActive(true);
        Announcement saved = announcementRepository.save(req);

        // === LOGIC MỚI: GỬI THÔNG BÁO CHO TOÀN BỘ USER ===
        List<User> allUsers = userRepository.findAll();

        // [FIX] Chuyển đổi logic String -> Enum NotificationType
        NotificationType notiType = NotificationType.INFO;

        if ("EVENT".equals(req.getType())) {
            notiType = NotificationType.SUCCESS; // Hoặc REWARD tùy logic, ở đây giữ SUCCESS theo code cũ
        } else if ("MAINTAIN".equals(req.getType())) {
            notiType = NotificationType.WARNING;
        }

        for (User user : allUsers) {
            notificationService.sendNotification(
                    user,
                    "📢 " + req.getTitle(),
                    req.getContent(),
                    notiType // [FIX] Truyền Enum vào đây
            );
        }
        // =================================================

        return saved;
    }

    public void deleteAnnouncement(Integer id) {
        checkAdmin();
        announcementRepository.deleteById(id);
    }

    private void checkAdmin() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Bạn không có quyền Admin");
        }
    }
}