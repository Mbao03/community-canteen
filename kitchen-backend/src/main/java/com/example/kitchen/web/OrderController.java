package com.example.kitchen.web;

import com.example.kitchen.exception.DishNotEnoughException;
import com.example.kitchen.exception.OperationFailureException;
import com.example.kitchen.model.DishInfo;
import com.example.kitchen.model.MealOrder;
import com.example.kitchen.service.DishInfoService;
import com.example.kitchen.service.MealOrderService;
import com.example.kitchen.utils.PageUtils;
import com.example.kitchen.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/order")
public class OrderController {

    @Resource
    private MealOrderService mealOrderService;

    @Resource
    private DishInfoService dishInfoService;

    @GetMapping(value = "/queryOrdersByPage")
    public Map<String, Object> queryOrdersByPage(@RequestParam Map<String, Object> params) {
        PageUtils.parsePageParams(params);
        int count = mealOrderService.getSearchCount(params);
        List<MealOrder> orders = mealOrderService.searchMealOrdersByPage(params);
        return R.getListResultMap(0, "success", count, orders);
    }

    @GetMapping(value = "/getCount")
    public Integer getCount() {
        return mealOrderService.getCount();
    }

    @PostMapping(value = "/addOrder")
    public Integer addOrder(@RequestBody MealOrder order) {
        return mealOrderService.addMealOrder(order);
    }

    @DeleteMapping(value = "/deleteOrder")
    public Integer deleteOrder(@RequestBody MealOrder order) {
        return mealOrderService.deleteMealOrder(order);
    }

    @DeleteMapping(value = "/deleteOrders")
    public Integer deleteOrders(@RequestBody List<MealOrder> orders) {
        return mealOrderService.deleteMealOrders(orders);
    }

    @PutMapping(value = "/updateOrder")
    public Integer updateOrder(@RequestBody MealOrder order) {
        return mealOrderService.updateMealOrder(order);
    }

    @PostMapping(value = "/placeOrder")
    @Transactional
    public Integer placeOrder(Integer residentId, Integer dishId, Integer quantity, String mealSlot,
                              String deliveryType, String contactPhone, String deliveryAddress, String remark) {
        try {
            DishInfo dish = dishInfoService.queryDishInfoById(dishId);
            if (dish == null) {
                throw new NullPointerException("Dish not found: " + dishId);
            }
            if (dish.getIsAvailable() != null && dish.getIsAvailable() == 0) {
                throw new DishNotEnoughException("Dish is not available: " + dishId);
            }

            int qty = (quantity == null || quantity <= 0) ? 1 : quantity;
            int stock = dish.getStockQty() == null ? 0 : dish.getStockQty();
            if (stock < qty) {
                throw new DishNotEnoughException("Dish stock is not enough: " + dishId);
            }

            BigDecimal unitPrice = dish.getDishPrice() == null ? BigDecimal.ZERO : dish.getDishPrice();
            BigDecimal totalPrice = unitPrice.multiply(new BigDecimal(qty));

            DishInfo updateDish = new DishInfo();
            updateDish.setDishId(dishId);
            updateDish.setStockQty(stock - qty);
            updateDish.setIsReserved((byte) ((stock - qty) > 0 ? 0 : 1));
            Integer updateDishResult = dishInfoService.updateDishInfo(updateDish);
            if (updateDishResult == 0) {
                throw new OperationFailureException("Failed to update dish stock");
            }

            MealOrder order = new MealOrder();
            order.setUserid(residentId);
            order.setDishId(dishId);
            order.setMealOrdertime(new Date());
            order.setQuantity(qty);
            order.setUnitPrice(unitPrice);
            order.setTotalPrice(totalPrice);
            order.setOrderStatus("PENDING");
            order.setDeliveryType((deliveryType == null || deliveryType.trim().isEmpty()) ? "PICKUP" : deliveryType);
            order.setMealSlot(mealSlot);
            order.setContactPhone(contactPhone);
            order.setDeliveryAddress(deliveryAddress);
            order.setRemark(remark);
            order.setPayStatus("UNPAID");

            Integer createOrderResult = mealOrderService.addMealOrder2(order);
            if (createOrderResult == 0) {
                throw new OperationFailureException("Failed to create order");
            }
        } catch (Exception e) {
            log.error("placeOrder failed", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 0;
        }
        return 1;
    }

    @PostMapping(value = "/completeOrder")
    @Transactional
    public Integer completeOrder(Integer orderId, Integer dishId) {
        try {
            MealOrder existOrder = mealOrderService.queryMealOrdersById(orderId);
            if (existOrder == null) {
                throw new NullPointerException("Order not found: " + orderId);
            }
            if (existOrder.getReturntime() != null) {
                throw new DishNotEnoughException("Order already completed: " + orderId);
            }

            MealOrder updateOrder = new MealOrder();
            updateOrder.setMealOrderid(orderId);
            updateOrder.setReturntime(new Date());
            updateOrder.setOrderStatus("COMPLETED");

            Integer updateOrderResult = mealOrderService.updateMealOrder2(updateOrder);
            if (updateOrderResult == 0) {
                throw new OperationFailureException("Failed to complete order: " + orderId);
            }
        } catch (Exception e) {
            log.error("completeOrder failed", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 0;
        }
        return 1;
    }
}
