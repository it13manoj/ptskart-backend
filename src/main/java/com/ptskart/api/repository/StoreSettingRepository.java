package com.ptskart.api.repository;

import com.ptskart.api.entity.StoreSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreSettingRepository extends JpaRepository<StoreSetting, Long> {
    Optional<StoreSetting> findBySettingKey(String settingKey);
    List<StoreSetting> findBySettingGroup(String settingGroup);
}

