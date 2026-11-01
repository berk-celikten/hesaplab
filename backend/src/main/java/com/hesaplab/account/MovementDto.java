package com.hesaplab.account;
public record MovementDto(String id, String direction, String amount, String balanceAfter,
                          String description, String createdAt) { }
