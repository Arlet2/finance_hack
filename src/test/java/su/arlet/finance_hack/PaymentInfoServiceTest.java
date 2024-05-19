package su.arlet.finance_hack;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import su.arlet.finance_hack.core.*;
import su.arlet.finance_hack.core.enums.PaymentType;
import su.arlet.finance_hack.exceptions.EntityWasAlreadyDeletedException;
import su.arlet.finance_hack.exceptions.ValidationException;
import su.arlet.finance_hack.repos.ItemCategoryRepo;
import su.arlet.finance_hack.repos.PaymentInfoRepo;
import su.arlet.finance_hack.repos.UserRepo;
import su.arlet.finance_hack.services.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentInfoServiceTest {

    @Mock
    private PaymentInfoRepo paymentInfoRepo;

    @Mock
    private ItemCategoryRepo itemCategoryRepo;

    @Mock
    private GoalService goalService;

    @Mock
    private NotificationSender sender;

    @Mock
    private UserService userService;

    @Mock
    private UserRepo userRepo;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private PaymentInfoService paymentInfoService;
    private User testUser;
    private ItemCategory testCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Counter mockCounter = mock(Counter.class);
        when(meterRegistry.counter(anyString())).thenReturn(mockCounter);

        // Инициализация PaymentInfoService с мокированными зависимостями
        paymentInfoService = new PaymentInfoService(
                paymentInfoRepo, itemCategoryRepo, goalService, sender, userService, null, userService, userRepo, meterRegistry);

        // Настройка тестовых данных
        testUser = new User();
        testUser.setUsername("testUser");

        testCategory = new ItemCategory();
        testCategory.setName("Food");
    }

    @Test
    void testAddWasteWithExistingCategory() {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setItemCategory(testCategory);
        paymentInfo.setPaymentType(PaymentType.SAVED);
        paymentInfo.setSum(100L); // Ensure sum is initialized
        paymentInfo.setIsTransfer(false); // Ensure isTransfer is initialized
        paymentInfo.setUser(testUser);

        when(itemCategoryRepo.findItemCategoryByName("Food")).thenReturn(Optional.of(testCategory));
        when(paymentInfoRepo.save(paymentInfo)).thenReturn(paymentInfo);

        Long id = paymentInfoService.addWaste(paymentInfo);

        assertNotNull(id);
        verify(itemCategoryRepo, never()).save(any(ItemCategory.class));
        verify(paymentInfoRepo).save(paymentInfo);
    }


    @Test
    void testAddWasteWithNewCategory() {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setItemCategory(testCategory);
        paymentInfo.setPaymentType(PaymentType.SAVED);
        paymentInfo.setSum(150L); // Ensure sum is initialized
        paymentInfo.setUser(testUser);

        when(itemCategoryRepo.findItemCategoryByName("Food")).thenReturn(Optional.empty());
        when(itemCategoryRepo.save(any(ItemCategory.class))).thenReturn(testCategory);
        when(paymentInfoRepo.save(paymentInfo)).thenReturn(paymentInfo);

        Long id = paymentInfoService.addWaste(paymentInfo);

        assertNotNull(id);
        verify(itemCategoryRepo).save(any(ItemCategory.class));
        verify(paymentInfoRepo).save(paymentInfo);
    }

    @Test
    void testDeleteWaste() {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setId(1L);
        paymentInfo.setPaymentType(PaymentType.SAVED);
        paymentInfo.setSum(100L); // Ensure sum is initialized
        paymentInfo.setUser(testUser);

        when(paymentInfoRepo.findById(1L)).thenReturn(Optional.of(paymentInfo));

        paymentInfoService.deleteWaste(1L);

        verify(paymentInfoRepo).deleteById(1L);
        verify(userRepo).save(any(User.class));
    }

    @Test
    void testDeleteWasteNotFound() {
        when(paymentInfoRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityWasAlreadyDeletedException.class, () -> paymentInfoService.deleteWaste(1L));

        verify(paymentInfoRepo, never()).deleteById(anyLong());
    }

    @Test
    void testUpdateWastesWithForGoalPayment() {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setSum(1000L);
        paymentInfo.setPaymentType(PaymentType.FOR_GOAL);
        paymentInfo.setIsTransfer(true);
        paymentInfo.setUser(new User());
        paymentInfo.setItemCategory(new ItemCategory());

        List<PaymentInfo> paymentInfos = List.of(paymentInfo);
        try {
            List<PaymentInfo> updatedPayments = paymentInfoService.updateWastes(paymentInfos);
        } catch (ValidationException e) {
            fail("Validation failed: " + e.getMessage());
        }
    }
}
