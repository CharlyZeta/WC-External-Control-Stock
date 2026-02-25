package com.stockdual.domain.repository;

import com.stockdual.domain.entity.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppSettingRepository extends JpaRepository<AppSetting, Long> {

    Optional<AppSetting> findByKey(String key);

    boolean existsByKey(String key);
}
