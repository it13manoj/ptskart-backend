package com.ptskart.api.controller;

import com.ptskart.api.dto.ApiResponse;
import com.ptskart.api.entity.StoreSetting;
import com.ptskart.api.repository.StoreSettingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingController {

    private final StoreSettingRepository settingRepository;

    public SettingController(StoreSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreSetting>>> getAllSettings(@RequestParam(required = false) String group) {
        List<StoreSetting> settings = (group != null && !group.isBlank())
                ? settingRepository.findBySettingGroup(group.toUpperCase())
                : settingRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Store settings loaded", settings));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<StoreSetting>> getSettingByKey(@PathVariable String key) {
        return settingRepository.findBySettingKey(key)
                .map(s -> ResponseEntity.ok(ApiResponse.ok("Setting found", s)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StoreSetting>> saveSetting(@RequestBody StoreSetting setting) {
        return settingRepository.findBySettingKey(setting.getSettingKey()).map(existing -> {
            existing.setSettingValue(setting.getSettingValue());
            if (setting.getSettingGroup() != null) existing.setSettingGroup(setting.getSettingGroup());
            if (setting.getDescription() != null) existing.setDescription(setting.getDescription());
            return ResponseEntity.ok(ApiResponse.ok("Setting updated", settingRepository.save(existing)));
        }).orElseGet(() -> ResponseEntity.ok(ApiResponse.ok("Setting created", settingRepository.save(setting))));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<StoreSetting>>> saveBulkSettings(@RequestBody List<StoreSetting> settings) {
        for (StoreSetting s : settings) {
            settingRepository.findBySettingKey(s.getSettingKey()).ifPresentOrElse(
                    existing -> {
                        existing.setSettingValue(s.getSettingValue());
                        settingRepository.save(existing);
                    },
                    () -> settingRepository.save(s)
            );
        }
        return ResponseEntity.ok(ApiResponse.ok("Settings saved successfully", settingRepository.findAll()));
    }
}

