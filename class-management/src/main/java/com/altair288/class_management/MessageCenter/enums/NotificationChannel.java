package com.altair288.class_management.MessageCenter.enums;

public enum NotificationChannel {
    INBOX(1),
    EMAIL(2),
    SMS(4), // 预留
    WEBHOOK(8); // 预留

    private final int bit;
    NotificationChannel(int bit){this.bit = bit;}
    public int bit(){return bit;}
}
