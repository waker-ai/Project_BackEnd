package com.example.tomatomall.vo;

import com.example.tomatomall.po.CartItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemVO {
    private Long cartItemId;
    private Long userId;
    private Long productId;
    private Integer quantity;

    public CartItem toPO()
    {
        CartItem cartItem = new CartItem();
        cartItem.setCartItemId(cartItemId);
        cartItem.setUserId(userId);
        cartItem.setProductId(productId);
        cartItem.setQuantity(quantity);
        return cartItem;
    }
}
