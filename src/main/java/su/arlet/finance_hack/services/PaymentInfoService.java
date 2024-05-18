package su.arlet.finance_hack.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.core.ItemCategory;
import su.arlet.finance_hack.core.PaymentInfo;
import su.arlet.finance_hack.exceptions.WasteAlreadyDeletedException;
import su.arlet.finance_hack.repos.ItemCategoryRepo;
import su.arlet.finance_hack.repos.PaymentInfoRepo;

import java.util.Optional;


@Service
public class PaymentInfoService {

    private final PaymentInfoRepo paymentInfoRepo;
    private final ItemCategoryRepo itemCategoryRepo;

    @Autowired
    public PaymentInfoService(PaymentInfoRepo paymentInfoRepo, ItemCategoryRepo itemCategoryRepo) {
        this.paymentInfoRepo = paymentInfoRepo;
        this.itemCategoryRepo = itemCategoryRepo;
    }

    public Long addWaste(PaymentInfo info) {


        if (info.getItemCategory() != null) {
            Optional<ItemCategory> itemCategoryByName =
                    itemCategoryRepo.findItemCategoryByName(info.getItemCategory().getName());

            if (itemCategoryByName.isPresent()) {
                info.setItemCategory(itemCategoryByName.get());
            } else {
                ItemCategory itemCategory = itemCategoryRepo.save(info.getItemCategory());
                info.setItemCategory(itemCategory);
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
