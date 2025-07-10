package io.hhplus.tdd.point.service.Impl;

import io.hhplus.tdd.database.*;
import io.hhplus.tdd.point.common.ErrorResponse;
import io.hhplus.tdd.point.common.PointConstants;
import io.hhplus.tdd.point.domain.*;
import io.hhplus.tdd.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    // 특정 유저의 포인트를 조회하는 기능
    @Override
    public UserPoint getUserPoint(long id) {
        return userPointTable.selectById(id);
    }

    // 특정 유저의 포인트 충전/이용 내역을 조회하는 기능
    @Override
    public List<PointHistory> getPointHistory(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    // 특정 유저의 포인트를 충전하는 기능
    @Override
    public UserPoint chargeUserPoint(long id, long amount) {

        // 기존 포인트 조회
        UserPoint curPoint = userPointTable.selectById(id);

        if(curPoint == null) {
            throw new IllegalArgumentException(PointConstants.INVALID_USER.message());
        }

        PointConstants pointConstants = curPoint.chargedValid(amount);
        if(pointConstants != null) {
            throw new IllegalArgumentException(pointConstants.message());
        }

        // 충전 후 포인트 업데이트
        UserPoint futPoint = userPointTable.insertOrUpdate(id, curPoint.point() + amount);

        // History에 충전 등록 -- 이력남기기
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return futPoint;
    }

    // 특정 유저의 포인트를 사용하는 기능
    @Override
    public UserPoint usePoint(long id, long amount) {

        // 기존 포인트 조회
        UserPoint curPoint = userPointTable.selectById(id);

        if(curPoint == null) {
            throw new IllegalArgumentException(PointConstants.INVALID_USER.message());
        }

        // 제약 - 만약 사용 Point 가 내가 가진 Point  보다 적을 경우 사용 불가
        PointConstants pointConstants = curPoint.usedValid(amount);
        if(pointConstants != null) {
            throw new IllegalArgumentException(pointConstants.message());
        }

        // 사용 후 포인트 업데이트
        UserPoint futPoint = userPointTable.insertOrUpdate(id, curPoint.point() - amount);

        // History에 사용 등록 -- 이력남기기
        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

        return futPoint;
    }
}
