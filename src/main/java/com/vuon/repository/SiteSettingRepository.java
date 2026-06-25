package com.vuon.repository;

import com.vuon.model.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SiteSettingRepository extends JpaRepository<SiteSetting, UUID> {
}
