package com.momento.repository;

import com.momento.entity.Capsule;
import com.momento.entity.MediaObject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MediaObjectRepository extends JpaRepository<MediaObject, UUID> {
    List<MediaObject> findByCapsule(Capsule capsule);
}
