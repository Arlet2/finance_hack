package su.arlet.finance_hack.services;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.core.*;
import su.arlet.finance_hack.core.enums.PaymentType;
import su.arlet.finance_hack.exceptions.EntityNotFoundException;
import su.arlet.finance_hack.exceptions.EntityWasAlreadyDeletedException;
import su.arlet.finance_hack.exceptions.ValidationException;
import su.arlet.finance_hack.repos.ItemCategoryRepo;
import su.arlet.finance_hack.repos.PaymentInfoRepo;
import su.arlet.finance_hack.repos.UserRepo;

import java.util.List;
import java.util.Optional;


@Service
public class PaymentInfoService {

    private final PaymentInfoRepo paymentInfoRepo;
    private final ItemCategoryRepo itemCategoryRepo;
    private final GoalService goalService;
    private final NotificationSender sender;
    private final UserRepo userRepo;

    private final Counter wasteCounter;

    @Autowired
    public PaymentInfoService(PaymentInfoRepo paymentInfoRepo, ItemCategoryRepo itemCategoryRepo, GoalService goalService, NotificationSender sender, UserService userService, AuthService authService, UserService userService1, UserRepo userRepo, MeterRegistry meterRegistry) {
        this.paymentInfoRepo = paymentInfoRepo;
        this.itemCategoryRepo = itemCategoryRepo;
        this.goalService = goalService;
        this.sender = sender;
        this.userRepo = userRepo;
        wasteCounter = meterRegistry.counter("waste_counter");
    }

    public ItemCategory getItemCategory(String name) {
        return itemCategoryRepo.findItemCategoryByName(name).orElseThrow(
                () -> new EntityNotFoundException("item category")
        );
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
            userRepo.save(user);
        }

        if (save.getPaymentType() == PaymentType.FOR_GOAL) {
            List<Goal> goals = calculateGoals(goalService.getGoalsByUser(info.getUser()), save.getSum());
            goals.forEach(goalService::updateGoal);
        }
        return save.getId();
    }

    public List<PaymentInfo> getPayments(User user) {
        return paymentInfoRepo.findAllByUser(user);
    }


    public void deleteWaste(Long paymentId) {
        PaymentInfo info = paymentInfoRepo.findById(paymentId).orElseThrow(EntityWasAlreadyDeletedException::new);

        if (info.getPaymentType() == PaymentType.SAVED) {
            User user = changeUserCurrentWasting(info.getUser(), -info.getSum());
            userRepo.save(user);
        }

        if (info.getPaymentType() == PaymentType.FOR_GOAL) {
            throw new ValidationException("you cannot delete paymentInfo that has been sent to the goal");
        }

        paymentInfoRepo.deleteById(paymentId);
    }

    public PaymentInfo getByIdBeforeDeleting(Long id) {
        return paymentInfoRepo.findById(id).orElseThrow(EntityWasAlreadyDeletedException::new);
    }

    public List<PaymentInfo> updateWastes(List<PaymentInfo> paymentInfoList) {
        paymentInfoList.forEach(PaymentInfo::validate);

        long freeMoney = 0;

        for (var paymentInfo : paymentInfoList) {
            if (paymentInfo.getPaymentType() == PaymentType.SAVED) {
                User user = changeUserCurrentWasting(paymentInfo.getUser(), paymentInfo.getSum());
                userRepo.save(user);
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
        if (user.getLimit() <= 0 && user.getLimit() < user.getCurrentWastings() && wastingSum > 0)
            sender.sendNotification(new Notification(
                    "You have exceeded your limit",
                    NotificationType.INTERNAL,
                    null));
        else if (user.getLimit() <= 0 && (double) user.getCurrentWastings() / user.getLimit() >= 0.8 && wastingSum > 0)
            sender.sendNotification(new Notification(
                    "You're approaching your spending limits",
                    NotificationType.INTERNAL,
                    null));

        if (user.getCurrentWastings() < 0)
            user.setCurrentWastings(0);

        return user;
    }

}
