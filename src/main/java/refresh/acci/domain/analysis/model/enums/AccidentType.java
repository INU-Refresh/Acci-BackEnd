package refresh.acci.domain.analysis.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import refresh.acci.global.exception.CustomException;
import refresh.acci.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AccidentType {

    // 0
    CAR_TO_CAR_REAR_END_PRIMARY(
            0, "차대차", "직선 도로", "추돌 사고", "선행자동차(1차사고차량)를 추돌"
    ),

    // 1
    CAR_TO_CAR_REAR_END_SECONDARY(
            1, "차대차", "직선 도로", "추돌 사고", "후행 추돌"
    ),

    // 2
    CAR_TO_CAR_PARKED_REAR_END(
            2, "차대차", "직선 도로", "추돌 사고(주정차)", "후행 추돌"
    ),

    // 3
    CAR_TO_CAR_LANE_REDUCTION(
            3, "차대차", "직선 도로", "차로 감소 도로(합류)", "본선에서 직진"
    ),

    // 4
    CAR_TO_CAR_DOOR_OPEN(
            4, "차대차", "직선 도로", "열린 문 접촉사고", "후행 직진"
    ),

    // 5
    CAR_TO_CAR_WRONG_WAY(
            5, "차대차", "직선 도로", "역주행 사고(중앙선 침범)", "직진"
    ),

    // 6
    CAR_TO_CAR_OPPOSITE_DIRECTION(
            6, "차대차", "직선 도로", "이면도로 교행 사고", "(마주보며) 직진"
    ),

    // 7
    CAR_TO_CAR_OVERTAKE_FORWARD(
            7, "차대차", "직선 도로", "추월 사고", "선행 직진"
    ),

    // 8
    CAR_TO_CAR_OVERTAKE_FORWARD_2(
            8, "차대차", "직선 도로", "추월 사고", "선행 직진"
    ),

    // 9
    CAR_TO_CAR_CENTER_LINE_OVERTAKE(
            9, "차대차", "직선 도로", "추월 사고", "중앙선 침범 추월(후방)"
    ),

    // 10
    CAR_TO_CAR_SOLID_LINE_OVERTAKE(
            10, "차대차", "직선 도로", "추월 사고", "실선 추월"
    ),

    // 11
    CAR_TO_CAR_LANE_CHANGE(
            11, "차대차", "직선 도로", "차로변경(진로변경)", "선행 진로 변경"
    ),

    // 12
    CAR_TO_CAR_SIMULTANEOUS_LANE_CHANGE(
            12, "차대차", "직선 도로", "동시 차로변경(진로변경)", "차로변경(진로변경)"
    ),

    // 13
    CAR_TO_CAR_CONGESTION_LANE_CHANGE(
            13, "차대차", "직선 도로", "정체차로에서 대기 중 진로변경", "직진(측면 충돌)"
    ),

    // 14
    CAR_TO_CAR_SAFETY_ZONE_BEFORE(
            14, "차대차", "직선 도로", "안전지대 통과 사고", "후행 직진(안전지대 벗어나기 전)"
    ),

    // 15
    CAR_TO_CAR_SAFETY_ZONE_AFTER(
            15, "차대차", "직선 도로", "안전지대 통과 사고", "후행 직진(안전지대 벗어난 후)"
    ),

    // 16
    CAR_TO_CAR_STOP_AND_GO(
            16, "차대차", "직선 도로", "정차 후 출발 사고", "정차 후 출발"
    ),

    // 17
    CAR_TO_CAR_EMERGENCY_LEFT(
            17, "차대차", "직선 도로", "중앙선 왼쪽 통행(긴급자동차)", "직진"
    ),

    // 18
    CAR_TO_CAR_EMERGENCY_VEHICLE(
            18, "차대차", "직선 도로", "긴급자동차 사고", "후행 직진"
    );

    private final Integer accidentType;
    private final String objectType;
    private final String place;
    private final String situation;
    private final String vehicleADirection;

    public static AccidentType fromInt(int accidentType) {
        for (AccidentType type : AccidentType.values()) {
            if (type.getAccidentType().equals(accidentType)) {
                return type;
            }
        }
        throw new CustomException(ErrorCode.ACCIDENT_TYPE_NOT_DETECTED);
    }
}
