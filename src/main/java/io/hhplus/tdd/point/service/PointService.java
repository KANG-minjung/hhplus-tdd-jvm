package io.hhplus.tdd.point.service;


import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.common.TransactionType;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable  pointHistoryTable;

    // 특정 유저의 포인트를 조회하는 기능
    public UserPoint getUserPoint(long id) {
        return userPointTable.selectById(id);
    }

    // 특정 유저의 포인트 충전/이용 내역을 조회하는 기능
    public List<PointHistory> getPointHistory(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    // 특정 유저의 포인트를 충전하는 기능
    public UserPoint chargeUserPoint(long id, long amount) {

        // TO-DO -- transaction이 발생할 수 있기 때문에 추가 조치 필요
        // 기존 포인트 조회
        UserPoint curPoint = userPointTable.selectById(id);

        // 충전 후 포인트 업데이트
        UserPoint futPoint = userPointTable.insertOrUpdate(id, curPoint.point() + amount);

        // History에 충전 등록 -- 이력남기기
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return futPoint;
    }

    // 특정 유저의 포인트를 사용하는 기능
    public UserPoint usePoint(long id, long amount) {

        // TO-DO -- transaction이 발생할 수 있기 때문에 추가 조치 필요
        // 기존 포인트 조회
        UserPoint curPoint = userPointTable.selectById(id);

        // 제약 - 만약 사용 Point 가 내가 가진 Point  보다 적을 경우 사용 불가
        if (curPoint.point() < amount) {
            throw new IllegalArgumentException("보유 포인트가 부족합니다.");
        }

        // 사용 후 포인트 업데이트
        UserPoint futPoint = userPointTable.insertOrUpdate(id, curPoint.point() - amount);

        // History에 사용 등록 -- 이력남기기
        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

        return userPointTable.selectById(id);
    }
}
