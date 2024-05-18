package su.arlet.finance_hack.services;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.controllers.rest.ValidationException;
import su.arlet.finance_hack.core.*;
import su.arlet.finance_hack.core.enums.PaymentType;
import su.arlet.finance_hack.exceptions.EntityWasAlreadyDeleteException;
import su.arlet.finance_hack.repos.ItemCategoryRepo;
import su.arlet.finance_hack.repos.PaymentInfoRepo;

import java.util.List;
import java.util.Optional;


@Service
public class PaymentInfoService {

    private final PaymentInfoRepo paymentInfoRepo;
    private final ItemCategoryRepo itemCategoryRepo;
    private final GoalService goalService;
    private final NotificationSender sender;
    private final UserService userService;

    private final Counter wasteCounter;

    @Autowired
    public PaymentInfoService(PaymentInfoRepo paymentInfoRepo, ItemCategoryRepo itemCategoryRepo, GoalService goalService, NotificationSender sender, UserService userService, AuthService authService, UserService userService1, MeterRegistry meterRegistry) {
        this.paymentInfoRepo = paymentInfoRepo;
        this.itemCategoryRepo = itemCategoryRepo;
        this.goalService = goalService;
        this.sender = sender;
        this.userService = userService1;
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
            User user = changeUserCurrentWasting(save.getUser(), save.getSum());
//            userService.updateUserByUsername();
        }

        if (save.getPaymentType() == PaymentType.FOR_GOAL) {
            List<Goal> goals = calculateGoals(goalService.getGoalsByUser(info.getUser()), save.getSum());
            goals.forEach(goalService::updateGoal);
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
        PaymentInfo info = paymentInfoRepo.findById(paymentId).orElseThrow(EntityWasAlreadyDeleteException::new);

        if (info.getPaymentType() == PaymentType.SAVED) {
            // TODO : добавить работу с лимитами трат
        }

        // TODO: что делать в случае отката банком операции с типом FOR_GOAL? Пока просто не будем поддерживать операцию удаления
        if (info.getPaymentType() == PaymentType.FOR_GOAL) {
            throw new ValidationException("you cannot delete paymentInfo that has been sent to the goal");
        }

        paymentInfoRepo.deleteById(paymentId);
    }

    public PaymentInfo getByIdBeforeDeleting(Long id) {
        return paymentInfoRepo.findById(id).orElseThrow(EntityWasAlreadyDeleteException::new);
    }

    public List<PaymentInfo> updateWastes(List<PaymentInfo> paymentInfoList) {
        paymentInfoList.forEach(PaymentInfo::validate);

        long freeMoney = 0;

        for (var paymentInfo : paymentInfoList) {
            if (paymentInfo.getPaymentType() == PaymentType.SAVED) {
                // TODO : добавить работу с лимитами трат
            }

            if (paymentInfo.getPaymentType() == PaymentType.FOR_GOAL)
                freeMoney += paymentInfo.getSum();

        }
        if (freeMoney > 0) {
            List<Goal> goals = calculateGoals(goalService.getGoalsByUser(paymentInfoList.get(0).getUser()), freeMoney);
            goals.forEach(goalService::updateGoal);
        }

        return paymentInfoRepo.saveAll(paymentInfoList);
    }

    private List<Goal> calculateGoals(List<Goal> goals, long freeMoney) {
        long prioritySum = 0;
        for (var goal : goals)
            prioritySum += goal.getPriority();

        for (var goal : goals) {
            long moneyToGoal = freeMoney * (goal.getPriority() / prioritySum);
            goal.setCurrentTotal(goal.getCurrentTotal() + moneyToGoal);
            freeMoney -= moneyToGoal;
            prioritySum -= goal.getPriority();
        }
        return goals;
    }

    private User changeUserCurrentWasting(User user, long wastingSum) {
        user.setCurrentWastings(user.getCurrentWastings() + wastingSum);
        if (user.getLimit() < user.getCurrentWastings())
            sender.sendNotification(new Notification(
                    "You have exceeded your limit",
                    NotificationType.INTERNAL,
                    null));
        else if ((double) user.getCurrentWastings() / user.getLimit() >= 0.8)
            sender.sendNotification(new Notification(
                    "You're approaching your spending limits",
                    NotificationType.INTERNAL,
                    null));
        return user;
    }

}
