package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.common.PointConstants;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.service.Impl.PointServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    private PointServiceImpl pointService;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    final long USER_ID = 1L;

    long INIT_POINT = 10_000L;
    private UserPoint userPoint;

    @BeforeEach
    void setUp() {
        this.INIT_POINT = 10_000L;
        userPoint = new UserPoint(1L, 10000, System.currentTimeMillis());
    }

    @Test
    @DisplayName("포인트_조회_성공_테스트")
    void 포인트_조회_성공_테스트(){
        // given
        UserPoint userPoint = new UserPoint(USER_ID, INIT_POINT, System.currentTimeMillis());
        when(userPointTable.selectById(USER_ID)).thenReturn(userPoint);

        //when & then
        assertThat(pointService.getUserPoint(USER_ID)).isEqualTo(userPoint);
    }

    @DisplayName("포인트_충전_성공_테스트")
    @ParameterizedTest(name = "{0} 충전 후, 잔액 = {1}")
    @CsvSource({"5000,15000",
                "10000,20000"})
    void 포인트_충전_성공_테스트(long charge, long expectation){
        // given
        // usePointTable 초기데이터 주입
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, INIT_POINT, System.currentTimeMillis()));
        // userPointTable.insertOrUpdate
        when(userPointTable.insertOrUpdate(Mockito.eq(USER_ID), Mockito.anyLong()))
                .thenAnswer(invocation -> {
                    long updatePoint = invocation.getArgument(1);
                    return new UserPoint(USER_ID, updatePoint, System.currentTimeMillis());
                });
        // when
        // 포인트 적립
        UserPoint expectUp = pointService.chargeUserPoint(USER_ID, charge);


        // then
        // 잘 적립되었는지 확인
        assertThat(expectUp.point()).isEqualTo(expectation);
    }

    @DisplayName("포인트_충전_실패_테스트")
    @ParameterizedTest(name = "{0} 포인트 적립 시 실패 → 사유: {2}")
    @CsvSource({
            "110000, POINT_SAVE_EXCEEDS_TOTAL_LIMIT,    MAX 초과 충전",   // 보유포인트 + 충전 > 100000
            "0,      POINT_SAVE_ZERO_OR_NEGATIVE,       0 이하 충전",     // 0 이거나 그 이하
            "-5000,  POINT_SAVE_ZERO_OR_NEGATIVE,       음수 충전",       // 음수
            "3300,   POINT_SAVE_NOT_MULTIPLE_OF_5000,   5000 단위 아님"   // 5,000 단위 아님
    })
    void 포인트_충전_실패_테스트(long charge, String constantName, String caseName) {
        // given
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, INIT_POINT, System.currentTimeMillis()));

        // 메세지 조회(에러메세지)
        String expectedMessage = PointConstants.valueOf(constantName).message();

        // when
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointService.chargeUserPoint(USER_ID, charge)
        );

        // then
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @DisplayName("포인트_사용_성공_테스트")
    @ParameterizedTest(name = "{0} 사용 후, 잔액 = {1}")
    @CsvSource({"5000,5000",
                "10000,0"})
    void 포인트_사용_성공_테스트(long usePoint, long expectPoint) {
        // given
        // usePointTable 초기데이터 주입
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, INIT_POINT, System.currentTimeMillis()));
        // userPointTable.insertOrUpdate
        when(userPointTable.insertOrUpdate(Mockito.eq(USER_ID), Mockito.anyLong()))
                .thenAnswer(invocation -> {
                    long updatePoint = invocation.getArgument(1);
                    return new UserPoint(USER_ID, updatePoint, System.currentTimeMillis());
                });
        // when
        // 포인트 사용
        UserPoint expectUp = pointService.usePoint(USER_ID, usePoint);

        // then
        // 데이터가 잘 들어갔는지 확인
        assertThat(expectUp.point()).isEqualTo(expectPoint);
    }

    @DisplayName("포인트_사용_실패_테스트")
    @ParameterizedTest(name = "{0} 포인트 사용 시 실패 → 사유: {2}")
    @CsvSource({
            "110000, POINT_USE_EXCEEDS_LIMIT,       Max 이상 사용",         // 보유포인트 + 충전 > 100000
            "0,      POINT_USE_ZERO_OR_NEGATIVE,    0 이하 사용",           // 0
            "-5000,  POINT_USE_ZERO_OR_NEGATIVE,    마이너스 값 사용",       // 마이너스
            "80000,  POINT_USE_EXCEEDS_BALANCE,     보유포인트 < 사용포인트", // 보유포인트 < 사용포인트
            "3300,   POINT_USE_NOT_MULTIPLE_OF_5000,5000 단위 아님"         // 5,000 단위 아님
    })
    void 포인트_사용_실패_테스트(long usePoint, String constantName, String caseName) {
        // given
        // userPointTable 주입(초기데이터)
        when(userPointTable.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, INIT_POINT, System.currentTimeMillis()));

        // 메세지 조회(에러메세지)
        String expectedMessage = PointConstants.valueOf(constantName).message();

        // when
        // Point를 사용하면서, 에러 발생 시 Exception으로 Throw
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointService.usePoint(USER_ID, usePoint)
        );

        // then
        // 검증
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("유효하지_않은_사용자")
    void 유효하지_않은_사용자(){
        // given
        // 유효하지 않은 사용자 생성(검증용)
        long invalidUserId = 10L;

        // 사용자 주입
        // 유효하지 않은 사용자라 null을 반환
        when(userPointTable.selectById(invalidUserId)).thenReturn(null);

        // 메세지 조회(에러메세지)
        String expectedMessage = PointConstants.INVALID_USER.message();

        // when
        // 유효하지 않은 사용자 테스트 , Point는 임의의 숫자 등록,
        // 사용/적립 상관 없음
        // 에러 발생 시 Exception으로 Throw
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointService.usePoint(invalidUserId, 5000)
        );

        // then
        // 검증
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

}
