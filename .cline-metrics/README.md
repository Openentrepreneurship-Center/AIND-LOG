# Cline 활동 추적 시스템 사용 가이드

Cline 익스텐션을 수정하지 않고, Cline이 제공하는 **Hooks** 기능과 **git post-commit hook**만으로 Dev Agent 활동 10가지 지표를 수집합니다.

---

## 1. 구성 요소

```
<프로젝트 루트>/
├── .clinerules/hooks/          # Cline이 인식하는 hook 디렉터리
│   ├── _record.py              # hook 본체 (이벤트 → JSONL append)
│   ├── _wrapper.sh             # _record.py 실행 래퍼
│   └── TaskStart, TaskResume, TaskCancel, TaskComplete,
│       PreToolUse, PostToolUse, UserPromptSubmit, PreCompact
│                               # ↑ 모두 _wrapper.sh 로의 심볼릭 링크
│
├── .cline-metrics/
│   ├── events.jsonl            # 모든 이벤트 append-only 로그
│   ├── commits/<sha>.patch     # post-commit hook이 저장한 커밋 스냅샷
│   ├── final/<sha>.marker      # "[reviewed]" 태그 커밋 표식
│   ├── post-commit             # git post-commit hook 스크립트
│   ├── setup.sh                # git init + hook 설치 원샷
│   └── analyze.py              # 지표 집계/리포트
│
└── .gitignore                  # .cline-metrics 내 데이터 제외
```

---

## 2. 최초 설정 (프로젝트당 1회)

```sh
./.cline-metrics/setup.sh
```

하는 일:
- `.git`이 없으면 `git init`
- `.git/hooks/post-commit` → `../../.cline-metrics/post-commit` 심볼릭 링크 설치
- 실행 권한 부여

그 다음 **VSCode(또는 Cline)를 한 번 재시작**하세요. Cline UI의 Hooks 탭에 8개 hook이 표시되면 인식 완료입니다.

---

## 3. 평소 사용법

그냥 **Cline을 평소처럼 쓰면 됩니다.** 별도 조작 불필요.

- Cline이 새 task를 시작/종료/중단하거나, 도구를 호출하거나, 당신이 프롬프트를 보낼 때마다 `events.jsonl`에 한 줄이 append됩니다.
- `git commit` 할 때마다 `commits/<sha>.patch` 스냅샷이 생성되고 `events.jsonl`에 `GitCommit` 이벤트가 기록됩니다.
- **"개발자 검수 최종본"을 표시하려면** 커밋 메시지에 `[reviewed]` 태그를 포함하세요. 예:
  ```
  git commit -m "fix: handle null user [reviewed]"
  ```
  → `final/<sha>.marker` 자동 생성.

---

## 4. 리포트 보기

```sh
python3 .cline-metrics/analyze.py
```

JSON 리포트가 stdout으로 출력됩니다. 파일로 저장하려면:
```sh
python3 .cline-metrics/analyze.py > report.json
```

### 출력 예시 (발췌)

```json
{
  "total_tasks": 3,
  "total_resumes": 1,
  "rework_rate": 0.33,
  "total_commits": 5,
  "reviewed_commits": ["abc123..."],
  "tasks": {
    "task-id-1": {
      "spec_analysis": "...",
      "first_prompt": "...",
      "resumed": false,
      "resume_prompts": [],
      "test_runs_count": 4,
      "test_runs_total_ms": 12340,
      "time_to_first_code_ms": 15200,
      "time_to_last_commit_ms": 432000,
      "commits": ["sha1", "sha2"],
      "reviewed_vs_initial_similarity": [
        {"from": "sha1", "to": "sha2", "similarity": 0.87}
      ]
    }
  }
}
```

---

## 5. 수집 지표 매핑

| # | 지표 | 출처 |
|---|---|---|
| 1 | Spec analysis 결과 | `tasks[*].spec_analysis` (TaskStart 메시지) |
| 2 | 단위테스트 시행 횟수/시간 | `tasks[*].test_runs_count`, `test_runs_total_ms` |
| 3 | 최초 코드 생성까지 시간 | `tasks[*].time_to_first_code_ms` |
| 4 | Committed 코드 | `.cline-metrics/commits/<sha>.patch` |
| 5 | 개발자 검수 최종본 | 커밋 메시지 `[reviewed]` → `final/<sha>.marker` |
| 6 | 4·5 간 유사도 | `tasks[*].reviewed_vs_initial_similarity` |
| 7 | 총 Commit 횟수 | `total_commits` |
| 8 | Dev Agent 재업무율 | `rework_rate` (TaskResume / TaskStart) |
| 9 | 재업무 시 프롬프트 | `tasks[*].resume_prompts` |
| 10 | 최종 코드 생성까지 총 시간 | `tasks[*].time_to_last_commit_ms` |

---

## 6. 동작 원리

- Cline은 8개 이벤트(`TaskStart`, `TaskResume`, `TaskCancel`, `TaskComplete`, `PreToolUse`, `PostToolUse`, `UserPromptSubmit`, `PreCompact`) 시점마다 `.clinerules/hooks/<이벤트명>` 실행 파일을 호출하고 stdin으로 JSON을 넘깁니다.
- 8개 파일 모두 `_wrapper.sh` → `_record.py`를 실행하며, `_record.py`는 stdin JSON을 `.cline-metrics/events.jsonl`에 한 줄 append한 뒤 `{"cancel": false}`로 응답합니다.
- Hook 실행 중 예외가 나도 Cline이 블로킹되지 않도록 모든 예외는 swallow되며 `~/.cline-metrics-errors/hook.log`에 남습니다.
- git post-commit hook은 커밋 시점에 `git show HEAD --format=fuller`를 스냅샷으로 저장합니다.

---

## 7. 제약 사항

- **어시스턴트 응답 본문(모델이 생성한 자연어 텍스트)은 hook API로 직접 수집할 수 없습니다.** 대신 tool 호출 파라미터/결과(`PreToolUse`/`PostToolUse`)로 에이전트 행동을 재구성합니다.
- Hook 실행이 실패해도 Cline은 계속 동작하지만, 이벤트가 누락될 수 있습니다. 주기적으로 `~/.cline-metrics-errors/hook.log`를 확인하세요.
- `events.jsonl`은 append-only로 무한정 커질 수 있습니다. 프로젝트 종료 후 아카이브/삭제 정책을 정하세요.

---

## 8. 문제 해결

**Cline UI에 hook이 안 보인다**
→ VSCode 재시작. 또는 `.clinerules/hooks/` 아래 8개 파일이 실행 권한(`chmod +x`)을 가지고 있고 확장자가 없는지 확인.

**`events.jsonl`이 생기지 않는다**
→ `echo '{"hookName":"TaskStart","taskId":"t","timestamp":0,"workspaceRoots":["'$(pwd)'"]}' | ./.clinerules/hooks/TaskStart` 로 수동 실행해 확인. `~/.cline-metrics-errors/hook.log`에 오류 기록 확인.

**git post-commit hook이 안 걸린다**
→ `ls -la .git/hooks/post-commit` 이 `../../.cline-metrics/post-commit`을 가리키는지 확인. 아니면 `./.cline-metrics/setup.sh` 재실행.

**다른 프로젝트에도 적용하려면?**
→ `.clinerules/hooks/`와 `.cline-metrics/` 디렉터리를 복사한 뒤 새 프로젝트 루트에서 `./.cline-metrics/setup.sh` 실행. 또는 글로벌 hook 경로(`~/Documents/Cline/Hooks/`)로 이동하면 모든 프로젝트에 공통 적용됩니다(단, 프로젝트별 격리는 사라짐).
