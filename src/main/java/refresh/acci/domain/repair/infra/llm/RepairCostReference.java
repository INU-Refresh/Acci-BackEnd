package refresh.acci.domain.repair.infra.llm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 수리비 예측 가격 기준 데이터
 * LLM 프롬프트에 주입하여 일관된 수리비 산출을 유도하기 위한 참조 데이터.
 * 한국 시장 공업사 기준 실제 시장 가격을 기반으로 설정.
 * 모든 금액 단위: 만원
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RepairCostReference {

    //차급별 가격 배율 (중형 = 1.0 기준)
    private static final Map<String, Double> SEGMENT_MULTIPLIERS = Map.ofEntries(
            Map.entry("mini", 0.75),
            Map.entry("compact", 0.85),
            Map.entry("mid_size", 1.0),
            Map.entry("full_size", 1.3),
            Map.entry("light_duty", 0.9),
            Map.entry("luxury", 2.0),
            Map.entry("compact_luxury", 1.6),
            Map.entry("mid_size_luxury", 2.0),
            Map.entry("full_size_luxury", 2.5)
    );

    //시스템 프롬프트에 주입할 가격 기준 텍스트를 생성
    public static String buildPriceGuidelineText() {
        return """
                === 수리비 산출 참고 가격 가이드라인 (중형 차급 기준, 공업사 기준, 단위: 만원) ===
                
                [외장 패널 - 강판 부위]
                | 부위 | 교체 | 판금+도색 | 도색 |
                |------|------|----------|------|
                | 후드(Hood) | 70~100 | 30~45 | 15~25 |
                | 프론트 펜더 | 60~90 | 25~35 | 15~20 |
                | 프론트/리어 도어 | 80~120 | 25~40 | 15~25 |
                | 리어 쿼터 패널 | 100~150 | 30~50 | 20~30 |
                | 트렁크/테일게이트 | 70~100 | 25~40 | 15~25 |
                | 루프 패널 | 150~250 | 40~60 | 20~35 |
                | 리어 펜더 패널 | 80~120 | 25~40 | 15~25 |
                
                [외장 - 플라스틱/우레탄 부위]
                | 부위 | 교체 | 수리+도색 | 도색 |
                |------|------|----------|------|
                | 프론트 범퍼 커버 | 45~55 | 20~35 | 10~20 |
                | 리어 범퍼 커버 | 45~55 | 20~35 | 10~20 |
                | 라디에이터 그릴 | 10~30 | - | - |
                | 리어 스포일러 | 15~40 | - | 10~15 |
                
                [유리류 - 교체만 가능]
                | 부위 | 교체 |
                |------|------|
                | 윈드실드 글라스 | 20~80 |
                | 백 글라스 / 테일게이트 글라스 | 20~50 |
                | 쿼터 픽스드 글라스 | 10~30 |
                | 백 도어 글라스 | 15~35 |
                ※ 유리는 HUD, 차음 유리, 열선 등 옵션에 따라 가격이 크게 달라질 수 있음
                
                [조명류 - 교체만 가능]
                | 부위 | 교체 |
                |------|------|
                | 헤드 램프 (1개) | 30~80 |
                | 리어 콤비네이션 램프 (1개) | 15~40 |
                
                [기타]
                | 부위 | 교체 |
                |------|------|
                | 타이어 & 휠 (1개) | 15~50 |
                
                === 차급별 가격 배율 (위 기준가격에 곱하여 산출) ===
                | 차급 | 배율 |
                |------|------|
                | 경차 (mini) | ×0.75 |
                | 준중형 (compact) | ×0.85 |
                | 중형 (mid_size) | ×1.0 (기준) |
                | 대형 (full_size) | ×1.3 |
                | 소형 상용 (light_duty) | ×0.9 |
                | 럭셔리 (luxury) | ×2.0 |
                | 준중형 럭셔리 (compact_luxury) | ×1.6 |
                | 중형 럭셔리 (mid_size_luxury) | ×2.0 |
                | 대형 럭셔리 (full_size_luxury) | ×2.5 |
                
                ※ 위 가격은 부품비 + 공임비 + 도색비를 합산한 총 비용임
                ※ 가이드라인은 참고 범위이며, 차량 모델과 연식에 따라 범위 내에서 조정하세요
                """;
    }

    //손상 심각도 → 수리 방법 매핑 규칙 텍스트를 생성
    public static String buildSeverityRuleText() {
        return """
                === 손상 심각도별 수리 방법 결정 규칙 (반드시 준수) ===
                
                [절대 규칙 - 예외 없음]
                1. severe(반파) → 반드시 "replace" (교체)
                   - 반파된 부위는 구조적 안전성이 훼손되어 수리/도색으로는 복원 불가
                   - 유리류가 severe인 경우에도 반드시 "replace"
                   - 이 규칙을 위반하면 안전 문제가 발생할 수 있음
                
                2. crack(균열) → "replace" 또는 "repair_and_paint"
                   - 유리류(글라스): 반드시 "replace" (유리는 수리 불가)
                   - 조명류(램프): 반드시 "replace" (균열 시 방수/기능 불가)
                   - 강판 부위(도어, 펜더, 후드 등): "replace" 권장, 경미한 균열만 "repair_and_paint" 가능
                   - 플라스틱 부위(범퍼): 균열 범위에 따라 "replace" 또는 "repair_and_paint"
                
                3. dent(찌그러짐) → "repair_and_paint"
                   - 판금으로 복원 후 도색 처리
                   - 찌그러짐이 매우 심한 경우(예: 프레임까지 영향)에만 "replace" 가능
                
                4. scratch(스크래치) → "paint" 또는 "repair_and_paint"
                   - 표면 스크래치만 있는 경우: "paint"
                   - 깊은 스크래치로 하지(금속면)가 노출된 경우: "repair_and_paint"
                   - 유리류 스크래치: 경미하면 수리 불필요, 심하면 "replace"
                
                [부위별 특성]
                - 유리류: scratch/dent 시 "replace" 외 방법 없음 (유리는 판금/도색 불가)
                - 조명류: 모든 손상에서 "replace"만 가능 (수리/도색 불가)
                - 타이어 & 휠: 모든 손상에서 "replace"만 가능
                - 라디에이터 그릴: 모든 손상에서 "replace"만 가능
                """;
    }

    //차급(segment) 코드에 대한 가격 배율을 반환
    public static double getSegmentMultiplier(String segmentCode) {
        return SEGMENT_MULTIPLIERS.getOrDefault(segmentCode, 1.0);
    }
}