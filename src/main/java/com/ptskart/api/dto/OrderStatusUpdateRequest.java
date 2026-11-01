package com.ptskart.api.dto;

public class OrderStatusUpdateRequest {
    private String orderStatus;
    private String carrier;
    private String trackingNumber;

    public OrderStatusUpdateRequest() {}

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
}

