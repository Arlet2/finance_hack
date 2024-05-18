package su.arlet.finance_hack.services;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
import su.arlet.finance_hack.repos.UserRepo;

import java.util.List;
import java.util.Optional;


@Service
public class PaymentInfoService {

    private final PaymentInfoRepo paymentInfoRepo;
    private final ItemCategoryRepo itemCategoryRepo;

    private final Counter wasteCounter;

    @Autowired
    public PaymentInfoService(PaymentInfoRepo paymentInfoRepo, ItemCategoryRepo itemCategoryRepo, MeterRegistry meterRegistry) {
        this.paymentInfoRepo = paymentInfoRepo;
        this.itemCategoryRepo = itemCategoryRepo;
        wasteCounter = meterRegistry.counter("waste_counter");
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
                wasteCounter.increment();
            }
        }

        PaymentInfo save = paymentInfoRepo.save(info);

        if (save.getPaymentType() == PaymentType.SAVED) {
            // TODO : добавить работу с лимитами трат
        }

        if (save.getPaymentType() == PaymentType.FOR_GOAL) {
            // TODO заполнить цель
        }
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

        if (info.getPaymentType() == PaymentType.SAVED) {
            // TODO : добавить работу с лимитами трат
        }

        if (info.getPaymentType() == PaymentType.FOR_GOAL) {
            // TODO минус из гола
        }

        paymentInfoRepo.deleteById(paymentId);
    }

    public PaymentInfo getByIdBeforeDeleting(Long id) {
        return paymentInfoRepo.findById(id).orElseThrow(WasteAlreadyDeletedException::new);
    }

    public List<PaymentInfo> updateWastes(List<PaymentInfo> paymentInfoList) {
        paymentInfoList.forEach(PaymentInfo::validate);

        for (var paymentInfo : paymentInfoList) {
            if (paymentInfo.getPaymentType() == PaymentType.SAVED) {
                // TODO : добавить работу с лимитами трат
            }

            if (paymentInfo.getPaymentType() == PaymentType.FOR_GOAL) {
                // TODO минус из гола
            }
        }

        return paymentInfoRepo.saveAll(paymentInfoList);
    }

}
