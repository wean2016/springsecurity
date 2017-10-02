package cn.ssd.wean2016.springsecurity.model.vo;

import java.util.HashMap;

/**
 * @version V1.0.1
 * @Description ResultMap 里 data 栏的类
 *                  补充：作为 dto 里 ChatMessage 类的 message 类型
 * @Author liuyuequn weanyq@gmail.com
 * @Date 2017/8/8 11:11
 */
public class Data extends HashMap{

    public Data addObj(String key, Object value){
        this.put(key, value);
        return this;
    }

    public Data addChat(String chat){
        this.put("chat", chat);
        return this;
    }

    public Data addAmount(String amount){
        this.put("amount", amount);
        return this;
    }

    public Data addOutTradeNo(String outTradeNo){
        this.put("outTradeNo", outTradeNo);
        return this;
    }

    public Data addActualProfit(String actualProfit){
        this.put("actualProfit", actualProfit);
        return this;
    }

    public Data addTradeStatus(String tradeStatus){
        this.put("tradeStatus", tradeStatus);
        return this;
    }

    public Data addPresentGiftUuid(String presentGiftUuid){
        this.put("presentGiftUuid", presentGiftUuid);
        return this;
    }

}
