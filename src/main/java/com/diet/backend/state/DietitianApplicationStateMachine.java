package com.diet.backend.state;

import com.diet.backend.model.ApplicationStatus;
import org.springframework.stereotype.Component;

@Component
public class DietitianApplicationStateMachine {

    /**
     * Verilen durum geçişinin geçerli olup olmadığını kontrol eder ve yeni durumu döner.
     * Geçersiz geçişlerde IllegalStateException fırlatır.
     *
     * @param currentStatus Mevcut durum
     * @param targetStatus Hedeflenen durum
     * @return Yeni durum
     */
    public ApplicationStatus transition(ApplicationStatus currentStatus, ApplicationStatus targetStatus) {
        if (currentStatus == targetStatus) {
            return currentStatus;
        }

        switch (currentStatus) {
            case PENDING:
                if (targetStatus == ApplicationStatus.UNDER_REVIEW) {
                    return targetStatus;
                }
                break;
                
            case UNDER_REVIEW:
                if (targetStatus == ApplicationStatus.APPROVED || targetStatus == ApplicationStatus.REJECTED) {
                    return targetStatus;
                }
                break;
                
            case REJECTED:
                if (targetStatus == ApplicationStatus.PENDING || targetStatus == ApplicationStatus.UNDER_REVIEW) {
                    return targetStatus;
                }
                break;
                
            case APPROVED:
                // Onaylanmış bir başvuru terminal durumdadır, geçiş yapılamaz
                break;
        }

        throw new IllegalStateException("Geçersiz durum geçişi: " + currentStatus + " -> " + targetStatus);
    }
}
