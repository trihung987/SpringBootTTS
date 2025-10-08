package me.trihung.enums;

import lombok.Getter;

@Getter
public enum EventStatus {
    DRAFT(1),       // Bản nháp
    PUBLISHED(2),   // Đã xuất bản
	PENDING(3), //Chờ duyệt
	REJECTED(4); //Từ chối

    private final int value;

    EventStatus(int value) {
        this.value = value;
    }
}