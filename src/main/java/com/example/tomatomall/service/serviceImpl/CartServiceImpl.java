package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.config.LoginInterceptor;
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
import com.example.tomatomall.util.TokenUtil;
import com.example.tomatomall.vo.CartItemVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.hibernate.internal.CoreLogging.logger;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private TokenUtil tokenUtil;

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
        //获取当前用户ID
        User user = securityUtil.getCurrentUser();
        Long userId = user.getId();

        //检查购物车中是否已经存在该商品
        CartItem cartItem = cartRepository.findByUserIdAndProductId(userId, productId).
                orElse(new CartItem(userId, productId, 0));
        //更新购物车中的商品数量
        int newQuantity = quantity + cartItem.getQuantity();
        // 检查库存是否足够
        if (stockpileRepository.findById(productId).get().getAmount() < newQuantity) {
            throw TomatoException.insufficientStock();
        }
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
        CartItem cartItem = cartRepository.findByUserIdAndCartItemId(userId, cartItemId)
                .orElseThrow(TomatoException::cartItemNotFound);
        cartRepository.delete(cartItem);
    }

    //修改购物车中的商品数量
        @Transactional
        @Override
        public void updateCartItemQuantity(Long cartItemId, Integer quantity) {
            System.out.println(quantity);
            User user = securityUtil.getCurrentUser();
            Long userId = user.getId();
            CartItem cartItem = cartRepository.findByUserIdAndCartItemId(userId, cartItemId)
                    .orElseThrow(TomatoException::cartItemNotFound);

            if (quantity < 0) {
                throw TomatoException.insufficientStock();
            }
            //检查库存是否足够
            Stockpile stock = stockpileRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> TomatoException.stockpileNotFound());
            int stockAmount = stock.getAmount();
            System.out.println("库存为：" + stockAmount + "，用户请求数量为：" + quantity);
            if (stockAmount < quantity) {
                System.out.println("库存不足，即将抛出异常");
                throw TomatoException.insufficientStock();
            }
            cartItem.setQuantity(quantity);
            cartRepository.save(cartItem);
        }

    @Override
    public Map<String, Object> getAllCartItems() {
        User user = securityUtil.getCurrentUser();
//        logger.info("当前登录用户：" + securityUtil.getCurrentUser());
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

        User user = securityUtil.getCurrentUser();
        if (user == null) {
            logger.error("当前会话中找不到用户，请确认是否登录且已写入 session");
        } else {
            logger.info("当前用户为：" + user.getUsername());
        }
//        logger.info("当前登录用户：", user.getUsername());
        System.out.println(request);
        if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            throw new IllegalArgumentException("cartItemIDs 不能为空");
        }
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

        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            if (authentication != null && authentication.isAuthenticated()) {
//                System.out.println("Authenticated user: " + authentication.getName());
//            } else {
//                System.out.println("No authenticated user found.");
//            }
            Long userId = securityUtil.getCurrentUser().getId();
            logger.info("userId:" + userId);
            order.setUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user ID", e);
        }
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(OrderStatusEnum.PENDING);
        order.setCreateTime(new Date());

        //保存订单
        orderRepository.save(order);

        //锁定库存
        for(CartItem cartItem : selectedItems){
            Stockpile stockpile = stockpileRepository.findByProductId(cartItem.getProductId()).orElseThrow(TomatoException::productNotFound);
            Integer currentAmount = Optional.ofNullable(stockpile.getAmount()).orElse(0);
            Integer currentFrozen = Optional.ofNullable(stockpile.getFrozen()).orElse(0);

            stockpile.setAmount(currentAmount - cartItem.getQuantity());
            stockpile.setFrozen(currentFrozen + cartItem.getQuantity());
            stockpileRepository.save(stockpile);
        }
        return order;
    }
}
