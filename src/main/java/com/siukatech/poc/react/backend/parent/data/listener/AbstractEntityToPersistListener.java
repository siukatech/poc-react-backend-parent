package com.siukatech.poc.react.backend.parent.data.listener;


import com.siukatech.poc.react.backend.parent.data.entity.AbstractEntity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
//@Component
public class AbstractEntityToPersistListener {

    @PrePersist
    protected void onSavePrePersist(final AbstractEntity abstractEntity) {
        log.debug("onSavePrePersist - abstractEntity.getId: [" + abstractEntity.getId()
                + "], abstractEntity.getVersionNo: [" + abstractEntity.getVersionNo()
                + "]");
        if (abstractEntity.getId() != null && abstractEntity.getVersionNo() == null) {
            // since the version is null, it treats "CREATE"
            String message = "VersionNo cannot null be null if abstractEntity.id#" + abstractEntity.getId() + " exists, onSavePrePersist";
            throw new IllegalArgumentException(message);
        }
    }

    @PreUpdate
    protected void onSavePreUpdate(final AbstractEntity abstractEntity) {
        log.debug("onSavePreUpdate - abstractEntity.getId: [" + abstractEntity.getId()
                + "], abstractEntity.getVersionNo: [" + abstractEntity.getVersionNo()
                + "]");
        if (abstractEntity.getVersionNo() == null) {
            throw new IllegalArgumentException("VersionNo cannot null be null for " + abstractEntity.getClass().getName() + ".id#" + abstractEntity.getId() + ", onSavePreUpdate");
        }
    }

}
