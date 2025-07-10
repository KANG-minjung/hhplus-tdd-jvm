package io.hhplus.tdd.point.common;

public enum PointConstants {

    // Point Use
    POINT_USE_EXCEEDS_BALANCE("P001", "보유 포인트보다 많은 포인트는 사용할 수 없습니다."),
    POINT_USE_ZERO_OR_NEGATIVE("P002", "사용하려는 포인트는 0보다 커야 합니다."),
    POINT_USE_EXCEEDS_LIMIT("P003", "최대 100,000까지 사용 가능합니다."),
    POINT_USE_NOT_MULTIPLE_OF_5000("P008", "5,000 단위로만 사용 가능합니다."),

    // Point Charge
    POINT_SAVE_EXCEEDS_TOTAL_LIMIT("P004", "적립 후 보유 포인트가 100,000을 초과할 수 없습니다."),
    POINT_SAVE_NOT_MULTIPLE_OF_5000("P005", "적립 포인트는 5,000 단위로만 가능합니다."),
    POINT_SAVE_ZERO_OR_NEGATIVE("P006", "적립 포인트는 0보다 커야 합니다."),

    // User Error
    INVALID_USER("P007", "사용자가 유효하지 않습니다.");


    private final String code;
    private final String message;

    PointConstants(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return "[" + code + "] " + message;
    }
}
