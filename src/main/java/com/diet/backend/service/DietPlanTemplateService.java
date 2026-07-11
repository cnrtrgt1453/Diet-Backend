package com.diet.backend.service;

import com.diet.backend.model.DietPlan;
import com.diet.backend.model.DietPlanTemplate;
import com.diet.backend.model.User;

import java.time.LocalDate;
import java.util.List;

public interface DietPlanTemplateService {
    // Yeni diyet şablonu kaydeder
    DietPlanTemplate saveTemplate(DietPlanTemplate templateRequest, User dietitian);

    // Mevcut bir diyet planından yeni şablon oluşturur
    DietPlanTemplate createTemplateFromPlan(Long planId, String title, User dietitian);

    // Diyetisyenin tüm şablonlarını getirir
    List<DietPlanTemplate> getTemplates(User dietitian);

    // Şablonu belirli bir danışana atar (tarih belirtilebilir, varsayılan bugündür)
    DietPlan assignTemplate(Long templateId, Long clientId, LocalDate date, User dietitian);

    // Şablonu siler
    void deleteTemplate(Long templateId, User dietitian);
}
