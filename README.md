# AIND-LOG

Cline(AI 코딩 에이전트) 활동을 훅 기반으로 자동 수집·분석하는 프로젝트입니다.

## 구조

```
├── calculator.html          # 수집 대상 프로젝트 (계산기 앱)
├── .clinerules/hooks/       # Cline 훅 스크립트 (8종 이벤트 수집)
├── .cline-metrics/          # 수집 데이터 및 Excel 내보내기 스크립트
├── dashboard/               # FastAPI 백엔드 + React 실시간 대시보드
├── generate_hook_guide.py   # 훅 설명 Excel 자동 생성 스크립트
└── cline_hook_guide.xlsx    # Cline 8종 훅 가이드 (실제 데이터 기반)
```

## 수집 훅 종류

| 훅 | 설명 |
|---|---|
| `TaskStart` | 작업 시작 – 소요시간 측정 기준점 |
| `TaskResume` | 작업 재개 – 재업무율 측정 |
| `TaskCancel` | 작업 취소 |
| `TaskComplete` | 작업 완료 – 결과 요약 저장 |
| `PreToolUse` | 도구 실행 직전 – 코드 사전 캡처 |
| `PostToolUse` | 도구 실행 완료 – 소요시간·성공 여부 기록 |
| `UserPromptSubmit` | 사용자 메시지 전송 |
| `PreCompact` | 대화 히스토리 압축 직전 |

## 빠른 시작

```bash
# 백엔드
cd dashboard/backend && pip install -r requirements.txt
uvicorn main:app --reload --port 8000

# 프론트엔드
cd dashboard/frontend && npm install && npm run dev

# Excel 내보내기
python3 .cline-metrics/export_excel_friendly.py
python3 generate_hook_guide.py
```
