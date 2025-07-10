package io.hhplus.tdd.point.domain;

import io.hhplus.tdd.point.common.ErrorResponse;
import io.hhplus.tdd.point.common.PointConstants;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }
    /*
        제약조건
            1) Point Max - 100,000
            2) 사용 Point가 보유 포인트 보다 많으면 사용 불가
            3) 사용 Point가 0 이거나, 0보다 작을 경우 불가
            4) 사용 Point가 100,000을 초과할 경우 불가
            5) 적립 Point가 (보유포인트 + 적립포인트) > 100,000 일 경우 적립 불가
            6) 적립 Point는 5,000씩 적립 가능 (5,000이 아니면 불가)
            7) 적립 Point가 0 이거나, 0보다 작을 경우 불가
     */
    public PointConstants usedValid(long amount){

        // 제약 - 만약 사용 Point가 100,000을 초과하면 불가
        if (amount > 100_000L) {
            return PointConstants.POINT_USE_EXCEEDS_LIMIT;
        }

        // 사용 Point가 보유 포인트 보다 많으면 사용 불가
        if (amount - point() > 0) {
            return PointConstants.POINT_USE_EXCEEDS_BALANCE;
        }

        // 사용 Point가 0 이거나, 0보다 작을 경우 불가
        if (amount <= 0) {
            return PointConstants.POINT_USE_ZERO_OR_NEGATIVE;
        }

        // 사용 Point는 5,000씩 적립 가능 (5,000이 아니면 불가)
        if (amount % 5000 != 0) {
            return PointConstants.POINT_USE_NOT_MULTIPLE_OF_5000;
        }

        return null;
    }

    public PointConstants chargedValid(long amount){

        // 적립 Point가 (보유포인트 + 적립포인트) > 100,000 일 경우 적립 불가
        if ((point() + amount) > 100_000L) {
            return PointConstants.POINT_SAVE_EXCEEDS_TOTAL_LIMIT;
        }
        // 적립 Point는 5,000씩 적립 가능 (5,000이 아니면 불가)
        if (amount % 5000 != 0) {
            return PointConstants.POINT_SAVE_NOT_MULTIPLE_OF_5000;
        }

        // 적립 Point가 0 이거나, 0보다 작을 경우 불가
        if (amount <= 0) {
            return PointConstants.POINT_SAVE_ZERO_OR_NEGATIVE;
        }

        return null;
    }
}
