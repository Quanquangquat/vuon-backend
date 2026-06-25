package com.vuon.service;

import com.vuon.model.SiteSetting;
import com.vuon.repository.SiteSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Quản lý cài đặt giao diện toàn site. Luôn dùng 1 dòng duy nhất
 * (tạo mặc định nếu chưa có).
 */
@Service
@RequiredArgsConstructor
public class SiteSettingService {

    private final SiteSettingRepository repo;

    @Transactional
    public SiteSetting get() {
        return repo.findAll().stream().findFirst()
                .orElseGet(() -> repo.save(SiteSetting.builder().build()));
    }

    @Transactional
    public SiteSetting update(Map<String, Object> f) {
        SiteSetting s = get();
        if (f.containsKey("brandName"))         s.setBrandName((String) f.get("brandName"));
        if (f.containsKey("logoUrl"))           s.setLogoUrl((String) f.get("logoUrl"));
        if (f.containsKey("logoWhiteUrl"))      s.setLogoWhiteUrl((String) f.get("logoWhiteUrl"));
        if (f.containsKey("faviconUrl"))        s.setFaviconUrl((String) f.get("faviconUrl"));
        if (f.containsKey("footerDescription")) s.setFooterDescription((String) f.get("footerDescription"));
        if (f.containsKey("footerPhone"))       s.setFooterPhone((String) f.get("footerPhone"));
        if (f.containsKey("footerEmail"))       s.setFooterEmail((String) f.get("footerEmail"));
        if (f.containsKey("footerAddress"))     s.setFooterAddress((String) f.get("footerAddress"));
        if (f.containsKey("facebookUrl"))       s.setFacebookUrl((String) f.get("facebookUrl"));
        if (f.containsKey("instagramUrl"))      s.setInstagramUrl((String) f.get("instagramUrl"));
        if (f.containsKey("youtubeUrl"))        s.setYoutubeUrl((String) f.get("youtubeUrl"));
        if (f.containsKey("promoBarText"))      s.setPromoBarText((String) f.get("promoBarText"));
        return repo.save(s);
    }
}
