package su.arlet.finance_hack.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import su.arlet.finance_hack.core.ItemCategory;

import java.util.Optional;

public interface ItemCategoryRepo extends JpaRepository<ItemCategory, Long> {

    Optional<ItemCategory> findItemCategoryByName(String name);
}
