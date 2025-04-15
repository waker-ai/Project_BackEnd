package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.dto.CheckoutRequest;
import com.example.tomatomall.enums.OrderStatusEnum;
import com.example.tomatomall.exception.TomatoException;
import com.example.tomatomall.po.*;
import com.example.tomatomall.repository.CartRepository;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.StockpileRepository;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.vo.CartItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockpileRepository stockpileRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;


    @Override
    public Map<String, Object> addCartItem(Long productId, Integer quantity) {
        // 检查商品是否存在
        if (!productRepository.existsById(productId)) {
            throw TomatoException.productNotFound();
        }
        // 检查库存是否足够
        if (stockpileRepository.findById(productId).get().getAmount() < quantity) {
            throw TomatoException.insufficientStock();
        }
        //获取当前用户ID
        User user = securityUtil.getCurrentUser();
        Long userId = user.getId();
        //检查购物车中是否已经存在该商品
        CartItem cartItem = cartRepository.findByUserIdAndProductId(userId, productId).
                orElse(new CartItem(userId, productId, 0));
        //更新购物车中的商品数量
        int newQuantity = quantity + cartItem.getQuantity();
        cartItem.setQuantity(newQuantity);
        //保存购物车中的商品
        cartRepository.save(cartItem);

        Product product = productRepository.findById(productId).get();

        Map<String ,Object> result = new HashMap<>();
        result.put("cartItem", cartItem.getCartItemId());
        result.put("productId", productId);
        result.put("title", product.getTitle());
        result.put("price", product.getPrice());
        result.put("description", product.getDescription());
        result.put("cover", product.getCover());
        result.put("detail", product.getDetail());
        result.put("quantity", newQuantity);
        return result;
    }

    //删除购物车中的商品
    @Override
    public void deleteCartItem(Long cartItemId) {
        User user = securityUtil.getCurrentUser();
        Long userId = user.getId();
        CartItem cartItem = cartRepository.findByUserIdAndProductId(userId, cartItemId)
                .orElseThrow(TomatoException::cartItemNotFound);
        cartRepository.delete(cartItem);
    }

    //修改购物车中的商品数量
    @Override
    public void updateCartItemQuantity(Long cartItemId, Integer quantity) {
        User user = securityUtil.getCurrentUser();
        Long userId = user.getId();
        CartItem cartItem = cartRepository.findByUserIdAndProductId(userId, cartItemId)
                .orElseThrow(TomatoException::cartItemNotFound);

        //检查库存是否足够
        if (stockpileRepository.findById(cartItem.getProductId()).get().getAmount() < quantity) {
            throw TomatoException.insufficientStock();
        }
        cartItem.setQuantity(quantity);
        cartRepository.save(cartItem);
    }

    @Override
    public Map<String, Object> getAllCartItems() {
        User user = securityUtil.getCurrentUser();
        List<CartItem> cartItems = cartRepository.findByUserId(user.getId());

        List<Map<String, Object>> itemList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId()).orElseThrow(TomatoException::productNotFound);
            Map<String, Object> item = new HashMap<>();
            item.put("cartItemId", cartItem.getCartItemId());
            item.put("productId", product.getId());
            item.put("title", product.getTitle());
            item.put("price", product.getPrice());
            item.put("description", product.getDescription());
            item.put("cover", product.getCover());
            item.put("detail", product.getDetail());
            item.put("quantity", cartItem.getQuantity());
            itemList.add(item);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("items", itemList);
        result.put("total", itemList.size());
        result.put("totalAmount", totalAmount);
        return result;
    }

    @Override
    public Order checkout(CheckoutRequest request) {
        List<CartItem> selectedItems = cartRepository.findAllById(request.getCartItemIds());

        // 检查库存是否足够
        for (CartItem cartItem : selectedItems) {
            Stockpile stockpile = stockpileRepository.findByProductId(cartItem.getProductId()).orElseThrow(TomatoException::productNotFound);
            if(stockpile.getAmount() < cartItem.getQuantity()){
                throw TomatoException.insufficientStock();
            }
        }

        //计算总价
        BigDecimal totalAmount = selectedItems.stream()
                .map(cartItem -> {
                    Product product = productRepository.findById(cartItem.getProductId()).orElseThrow(TomatoException::productNotFound);
                    return product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //创建订单
        Order order = new Order();
        order.setUserId(securityUtil.getCurrentUser().getId());
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(OrderStatusEnum.PENDING);
        order.setCreateTime(new Date());

        //保存订单
        orderRepository.save(order);

        //锁定库存
        for(CartItem cartItem : selectedItems){
            Stockpile stockpile = stockpileRepository.findByProductId(cartItem.getProductId()).orElseThrow(TomatoException::productNotFound);
            stockpile.setAmount(stockpile.getAmount() - cartItem.getQuantity());
            stockpile.setFrozen(stockpile.getFrozen() + cartItem.getQuantity());
            stockpileRepository.save(stockpile);
        }
        return order;
    }
}
