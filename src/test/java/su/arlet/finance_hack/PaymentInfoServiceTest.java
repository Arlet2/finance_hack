package su.arlet.finance_hack;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import su.arlet.finance_hack.core.*;
import su.arlet.finance_hack.core.enums.PaymentType;
import su.arlet.finance_hack.exceptions.WasteAlreadyDeletedException;
import su.arlet.finance_hack.repos.ItemCategoryRepo;
import su.arlet.finance_hack.repos.PaymentInfoRepo;
import su.arlet.finance_hack.services.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private MeterRegistry meterRegistry;

    @Mock
    private Counter wasteCounter;

    @InjectMocks
    private PaymentInfoService paymentInfoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(meterRegistry.counter("waste_counter")).thenReturn(wasteCounter);
        paymentInfoService = new PaymentInfoService(paymentInfoRepo, itemCategoryRepo, goalService, sender, userService, null, userService, meterRegistry);
    }

    @Test
    void testAddWaste_Success() {
        PaymentInfo info = new PaymentInfo();
        info.setPaymentType(PaymentType.SAVED);
        info.setSum(100L);
        info.setUser(new User());
        info.validate();

        when(paymentInfoRepo.save(any(PaymentInfo.class))).thenReturn(info);

        Long result = paymentInfoService.addWaste(info);

        assertNotNull(result);
        verify(paymentInfoRepo, times(1)).save(info);
    }

    @Test
    void testAddWaste_Product() {
        PaymentInfo info = new PaymentInfo();
        ItemCategory itemCategory = new ItemCategory();
        itemCategory.setName("Product");
        info.setItemCategory(itemCategory);
        info.setPaymentType(PaymentType.SAVED);
        info.setSum(100L);
        info.setUser(new User());
        info.validate();

        when(itemCategoryRepo.findItemCategoryByName("New Category")).thenReturn(Optional.empty());
        when(itemCategoryRepo.save(any(ItemCategory.class))).thenReturn(info.getItemCategory());
        when(paymentInfoRepo.save(any(PaymentInfo.class))).thenReturn(info);

        Long result = paymentInfoService.addWaste(info);

        assertNotNull(result);
        verify(itemCategoryRepo, times(1)).save(info.getItemCategory());
        verify(paymentInfoRepo, times(1)).save(info);
        verify(wasteCounter, times(1)).increment();
    }

    @Test
    void testDeleteWaste_Success() {
        PaymentInfo info = new PaymentInfo();
        info.setPaymentType(PaymentType.SAVED);

        when(paymentInfoRepo.findById(1L)).thenReturn(Optional.of(info));

        assertDoesNotThrow(() -> paymentInfoService.deleteWaste(1L));
        verify(paymentInfoRepo, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteWaste_WasteAlreadyDeleted() {
        when(paymentInfoRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(WasteAlreadyDeletedException.class, () -> paymentInfoService.deleteWaste(1L));
        verify(paymentInfoRepo, never()).deleteById(1L);
    }

    @Test
    void testUpdateWastes_Success() {
        PaymentInfo info1 = new PaymentInfo();
        info1.setPaymentType(PaymentType.SAVED);
        info1.setSum(100L);
        info1.setUser(new User());
        info1.validate();

        PaymentInfo info2 = new PaymentInfo();
        info2.setPaymentType(PaymentType.FOR_GOAL);
        info2.setSum(200L);
        info2.setUser(info1.getUser());
        info2.validate();

        List<PaymentInfo> paymentInfoList = List.of(info1, info2);

        when(paymentInfoRepo.saveAll(anyList())).thenReturn(paymentInfoList);

        List<PaymentInfo> result = paymentInfoService.updateWastes(paymentInfoList);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(paymentInfoRepo, times(1)).saveAll(paymentInfoList);
    }
}
