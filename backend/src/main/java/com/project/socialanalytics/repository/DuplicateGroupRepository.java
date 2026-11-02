package com.project.socialanalytics.repository;

import com.project.socialanalytics.entity.DuplicateGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DuplicateGroupRepository extends JpaRepository<DuplicateGroup, Long> {
    Optional<DuplicateGroup> findByGroupHash(String groupHash);
}

