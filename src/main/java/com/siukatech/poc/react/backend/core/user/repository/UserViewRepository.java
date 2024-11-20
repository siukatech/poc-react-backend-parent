package com.siukatech.poc.react.backend.core.user.repository;

import com.siukatech.poc.react.backend.core.user.entity.UserViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserViewRepository extends JpaRepository<UserViewEntity, String> {

    Optional<UserViewEntity> findByLoginId(String loginId);
}