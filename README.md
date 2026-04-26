# AIND-LOG

Cline(AI 코딩 에이전트) 활동을 훅 기반으로 자동 수집·분석하는 프로젝트입니다.

## 구조

```
├── decapet-official/backend # 수집 대상 프로젝트 (Spring Boot, .gitignore 처리됨, 자체 .git 보유)
├── .clinerules/hooks/       # Cline 훅 스크립트 (8종 이벤트 수집)
├── .cline-metrics/          # 수집 데이터 및 Excel 내보내기 스크립트
├── dashboard/               # FastAPI 백엔드 + React 실시간 대시보드
├── generate_hook_guide.py   # 훅 설명 Excel 자동 생성 스크립트
└── cline_hook_guide.xlsx    # Cline 8종 훅 가이드 (실제 데이터 기반)
```

> 작업 대상 `decapet-official/backend` 는 자체 git repo 이며, 자체 `.git/hooks/post-commit` 에서 환경변수 `CLINE_METRICS_DIR` 로 AIND-LOG 의 `.cline-metrics` 를 가리키도록 연결되어 있다. 따라서 decapet 에서 git commit 이 발생하면 **patch + snapshot + GitCommit 이벤트가 모두 AIND-LOG 의 중앙 metrics 에 통합 저장**된다.

## 수집 훅 종류

| 훅                 | 설명                                     |
| ------------------ | ---------------------------------------- |
| `TaskStart`        | 작업 시작 – 소요시간 측정 기준점         |
| `TaskResume`       | 작업 재개 – 재업무율 측정                |
| `TaskCancel`       | 작업 취소                                |
| `TaskComplete`     | 작업 완료 – 결과 요약 저장               |
| `PreToolUse`       | 도구 실행 직전 – 코드 사전 캡처          |
| `PostToolUse`      | 도구 실행 완료 – 소요시간·성공 여부 기록 |
| `UserPromptSubmit` | 사용자 메시지 전송                       |
| `PreCompact`       | 대화 히스토리 압축 직전                  |

## 빠른 시작

```bash
# 백엔드
cd dashboard/backend && pip install -r requirements.txt
uvicorn main:app --reload --port 8000
# (선택) 다른 프로젝트를 추적 대상으로 바꿀 때
TARGET_REPO_ROOT=/absolute/path/to/repo uvicorn main:app --reload --port 8000

# 프론트엔드
cd dashboard/frontend && npm install && npm run dev

# Excel 내보내기
python3 .cline-metrics/export_excel_friendly.py
python3 generate_hook_guide.py
```

## 유사도 분석 (`/api/similarity`)

`file` 쿼리 파라미터는 **필수**이며, `TARGET_REPO_ROOT` (기본: `decapet-official/backend`) 기준의 상대 경로입니다.

```
GET /api/similarity?file=src/main/java/com/decapet/.../SomeService.java
```

대시보드의 "커밋별 코드 유사도" / "커밋 단계별 유사도" 패널에서 직접 파일 경로를 입력해 분석할 수 있습니다.
