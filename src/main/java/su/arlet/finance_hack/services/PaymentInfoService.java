package su.arlet.finance_hack.services;

import io.micrometer.core.instrument.MeterRegistry;
import io.prometheus.client.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.core.ItemCategory;
import su.arlet.finance_hack.core.PaymentInfo;
import su.arlet.finance_hack.exceptions.WasteAlreadyDeletedException;
import su.arlet.finance_hack.repos.ItemCategoryRepo;
import su.arlet.finance_hack.repos.PaymentInfoRepo;
import su.arlet.finance_hack.repos.UserRepo;

import java.util.Optional;


@Service
public class PaymentInfoService {

    private final PaymentInfoRepo paymentInfoRepo;
    private final ItemCategoryRepo itemCategoryRepo;

    private final Counter wasteCounter;

    @Autowired
    public PaymentInfoService(PaymentInfoRepo paymentInfoRepo, ItemCategoryRepo itemCategoryRepo, MeterRegistry meterRegistry, UserRepo userRepo) {
        this.paymentInfoRepo = paymentInfoRepo;
        this.itemCategoryRepo = itemCategoryRepo;
        wasteCounter = (Counter) meterRegistry.counter("waste_counter");
    }

    public Long addWaste(PaymentInfo info) {
        info.validate();

        if (info.getItemCategory() != null) {
            Optional<ItemCategory> itemCategoryByName =
                    itemCategoryRepo.findItemCategoryByName(info.getItemCategory().getName());

            if (itemCategoryByName.isPresent()) {
                info.setItemCategory(itemCategoryByName.get());
            } else {
                ItemCategory itemCategory = itemCategoryRepo.save(info.getItemCategory());
                info.setItemCategory(itemCategory);
                wasteCounter.inc();
            }
        }

        if (!info.getIsTransfer() && info.getItemCategory() != null) {
             // TODO : добавить работу с лимитами трат
        }

        PaymentInfo save = paymentInfoRepo.save(info);
        return save.getId();
    }

    public void deleteWaste(Long paymentId) {
        PaymentInfo info = paymentInfoRepo.findById(paymentId).orElseThrow(WasteAlreadyDeletedException::new);

        if (!info.getIsTransfer() && info.getItemCategory() != null) {
            // TODO : добавить работу с лимитами трат
        }

        paymentInfoRepo.deleteById(paymentId);
    }

    public PaymentInfo getByIdBeforeDeleting(Long id) {
        return paymentInfoRepo.findById(id).orElseThrow(WasteAlreadyDeletedException::new);
    }

}
