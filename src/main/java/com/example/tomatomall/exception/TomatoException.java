package com.example.tomatomall.exception;

public class TomatoException extends RuntimeException{
    public TomatoException(String message) { super(message); }

    public static TomatoException insufficientStock()  { return new TomatoException("库存不足"); }

    public static TomatoException cartItemNotFound() { return new TomatoException("购物车商品不存在"); }

    public static TomatoException payError() { return new TomatoException("支付失败"); }

    public static TomatoException orderNotFound() { return new TomatoException("未发现该订单"); }

    public static TomatoException NumberFormatError() { return new TomatoException("数字类型转化错误"); }

    public static TomatoException illegalOrderState() { return new TomatoException("订单状态错误"); }

    public static TomatoException productNotFound() { return new TomatoException("未发现该商品"); }

    public static TomatoException notLogin() { return new TomatoException("未登录"); }

    public static TomatoException stockpileNotFound() { return new TomatoException("为发现相关库存"); }
    public static TomatoException userNotFount() { return new TomatoException("未发现该用户"); }
    public static TomatoException orderItemNotFound() { return new TomatoException("未发现该购物车商品");    }
}
