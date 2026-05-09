package com.tfg.rentalplatform.repository;

import com.tfg.rentalplatform.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    List<Item> findByActiveTrueAndModerationBlockedFalseAndOwnerRemovedFalseAndOwnerActiveTrueOrderByCreatedAtDesc();
    List<Item> findByOwnerIdAndOwnerRemovedFalseOrderByCreatedAtDesc(Long ownerId);
}
