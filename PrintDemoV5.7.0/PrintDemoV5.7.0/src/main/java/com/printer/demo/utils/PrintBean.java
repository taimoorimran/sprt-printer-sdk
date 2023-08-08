package com.printer.demo.utils;


public class PrintBean {
    public String transferWaybillNo;
    public String goodsQty;
    public String flowDirectionCode;
    public String serviceProduct;
    public String goodsNum;

    public PrintBean(String transferWaybillNo,
                     String goodsQty,
                     String flowDirectionCode,
                     String serviceProduct,
                     String goodsNum) {
        this.transferWaybillNo = transferWaybillNo;
        this.goodsQty = goodsQty;
        this.flowDirectionCode = flowDirectionCode;
        this.serviceProduct = serviceProduct;
        this.goodsNum = goodsNum;
    }
}
