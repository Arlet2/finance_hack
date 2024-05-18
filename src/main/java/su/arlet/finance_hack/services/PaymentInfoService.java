package su.arlet.finance_hack.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.core.ItemCategory;
import su.arlet.finance_hack.core.PaymentInfo;
import su.arlet.finance_hack.core.PaymentInfoFilter;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.core.enums.PaymentType;
import su.arlet.finance_hack.exceptions.WasteAlreadyDeletedException;
import su.arlet.finance_hack.repos.ItemCategoryRepo;
import su.arlet.finance_hack.repos.PaymentInfoRepo;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


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
        info.validate();

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

    public List<PaymentInfo> getByFilter(PaymentInfoFilter paymentInfoFilter, User user) {

        // TODO при создании нового переводы и незаданные категории будут помечены unknown

        List<PaymentInfo> paymentInfos = paymentInfoRepo.findAllByUser(user);

        return paymentInfos.stream()
                .filter(info -> (paymentInfoFilter.isTransfer() && info.getIsTransfer() && paymentInfoFilter.getPaymentType() == info.getPaymentType())
                        || (!paymentInfoFilter.isTransfer() && (paymentInfoFilter.getItemCategory() == null || paymentInfoFilter.getItemCategory() == info.getItemCategory())))
                .toList();

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

    public List<PaymentInfo> updateWastes(List<PaymentInfo> paymentInfoList) {

        // TODO работа с лимитами и голами
        return paymentInfoRepo.saveAll(paymentInfoList);
    }

}
