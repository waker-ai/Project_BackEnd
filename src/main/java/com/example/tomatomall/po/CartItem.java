package com.example.tomatomall.po;

import com.example.tomatomall.vo.CartItemVO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "carts")
public class CartItem {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @Basic
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Basic
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Basic
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    public CartItemVO toVO() {
        CartItemVO cartItemVO = new CartItemVO();
        cartItemVO.setCartItemId(cartItemId);
        cartItemVO.setUserId(userId);
        cartItemVO.setProductId(productId);
        cartItemVO.setQuantity(quantity);
        return cartItemVO;
    }

    public CartItem(Long userId, Long productId, Integer quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }
}
