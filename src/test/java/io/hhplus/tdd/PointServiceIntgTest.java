package io.hhplus.tdd;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointServiceIntgTest {

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointService pointService;

    final long USER_ID = 1L;
    final long INIT_POINT = 10_000L;

    @Test
    @DisplayName("포인트_충전_통합_테스트")
    void 포인트_충전_통합_테스트() {
        // given
        // 충전 Point 설정
        long chargePoint = 5000L;

        // 초기데이터 insert
        userPointTable.insertOrUpdate(USER_ID, INIT_POINT);

        // when
        // 포인트 충전 메소드 호출
        UserPoint updateUp = pointService.chargeUserPoint(USER_ID, chargePoint);

        // then
        // 검증
        assertThat(updateUp.point()).isEqualTo(INIT_POINT + chargePoint);
    }

    @Test
    @DisplayName("포인트_사용_통합_테스트")
    void 포인트_사용_통합_테스트() {
        // given
        // 사용 Point 설정
        long usePoint = 5000L;

        // 초기데이터 insert
        userPointTable.insertOrUpdate(USER_ID, INIT_POINT);

        // when
        // 포인트 사용 메소드 호출
        UserPoint updateUp = pointService.usePoint(USER_ID, usePoint);

        // then
        // 검증
        assertThat(updateUp.point()).isEqualTo(INIT_POINT - usePoint);
    }

}
