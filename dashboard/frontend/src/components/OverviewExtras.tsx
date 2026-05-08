/**
 * 스크린샷용 데모 패널 (정적 픽스처). 개요 탭 하단을 채워 빈 화면을 줄입니다.
 */
function MiniStat({ label, value, tone }: { label: string; value: string; tone: string }) {
  const tones: Record<string, string> = {
    blue: 'border-blue-200 bg-blue-50 text-blue-900',
    violet: 'border-violet-200 bg-violet-50 text-violet-900',
    emerald: 'border-emerald-200 bg-emerald-50 text-emerald-900',
    amber: 'border-amber-200 bg-amber-50 text-amber-950',
    rose: 'border-rose-200 bg-rose-50 text-rose-950',
    slate: 'border-slate-200 bg-slate-50 text-slate-900',
  }
  return (
    <div className={`rounded-xl border px-4 py-3 ${tones[tone] ?? tones.slate}`}>
      <p className="text-[11px] font-medium opacity-80">{label}</p>
      <p className="text-lg font-bold tracking-tight mt-0.5">{value}</p>
    </div>
  )
}

function ProgressRow({ label, pct, note }: { label: string; pct: number; note: string }) {
  return (
    <div className="space-y-1">
      <div className="flex justify-between text-xs">
        <span className="text-slate-700 font-medium">{label}</span>
        <span className="text-slate-500 font-mono">{note}</span>
      </div>
      <div className="h-2 rounded-full bg-slate-200 overflow-hidden border border-slate-100">
        <div className="h-full rounded-full bg-gradient-to-r from-sky-500 to-indigo-500" style={{ width: `${pct}%` }} />
      </div>
    </div>
  )
}

export default function OverviewExtras() {
  return (
    <div className="space-y-5">
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
        <MiniStat label="평균 Task 길이" value="42.3분" tone="blue" />
        <MiniStat label="승인 대기 회피율" value="94%" tone="emerald" />
        <MiniStat label="첫 테스트까지" value="3.8분" tone="violet" />
        <MiniStat label="워크플로 실패 복구" value="18회/주" tone="amber" />
        <MiniStat label="에이전트 동시 실행" value="최대 4" tone="rose" />
        <MiniStat label="스냅샷 무결성" value="✓ 검증됨" tone="slate" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        <div className="lg:col-span-5 rounded-xl border border-slate-200 bg-white shadow-sm p-5 space-y-4">
          <div>
            <h3 className="text-sm font-semibold text-slate-900 flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-indigo-500" />
              워크플로 단계별 체류 (데모)
            </h3>
            <p className="text-xs text-slate-500 mt-1">기획 → 구현 → 검증으로 이어지는 비율 표시입니다.</p>
          </div>
          <div className="space-y-3">
            <ProgressRow label="요청 이해 · 계획" pct={92} note="약 48초" />
            <ProgressRow label="코드 수정 · 테스트" pct={76} note="약 18분" />
            <ProgressRow label="리뷰 반영 · 정리" pct={54} note="약 11분" />
          </div>
        </div>

        <div className="lg:col-span-4 rounded-xl border border-slate-200 bg-white shadow-sm p-5">
          <h3 className="text-sm font-semibold text-slate-900 flex items-center gap-2 mb-3">
            <span className="w-2 h-2 rounded-full bg-teal-500" />
            최근 세션 노트 (데모)
          </h3>
          <ol className="space-y-2.5 text-xs text-slate-700 leading-relaxed list-decimal list-inside">
            <li>PR 템플릿에 테스트 체크리스트 자동 채우기 초안 작성</li>
            <li>UserService 검증 규칙을 공통 Validator 로 이동</li>
            <li>Gradle 빌드 캐시 경고 2건 정리 (문서 반영만)</li>
            <li>대시보드 SSE 연결 상태 배지 디자인 정렬</li>
            <li>스냅샷 디렉터리 용량 임계치 알림 (임계 800MB)</li>
          </ol>
        </div>

        <div className="lg:col-span-3 rounded-xl border border-slate-200 bg-white shadow-sm p-5">
          <h3 className="text-sm font-semibold text-slate-900 flex items-center gap-2 mb-3">
            <span className="w-2 h-2 rounded-full bg-amber-500" />
            오늘의 리스크 (데모)
          </h3>
          <ul className="space-y-2 text-xs">
            <li className="flex gap-2">
              <span className="text-amber-600 font-bold">!</span>
              <span className="text-slate-700"><b className="text-slate-900">테스트 flaky</b> — 사용자 서비스 2건 간헐 실패.</span>
            </li>
            <li className="flex gap-2">
              <span className="text-sky-600 font-bold">i</span>
              <span className="text-slate-700"><b className="text-slate-900">커버리지</b> 새 브랜치 기준 −1.4% → 보완 필요.</span>
            </li>
            <li className="flex gap-2">
              <span className="text-emerald-600 font-bold">✓</span>
              <span className="text-slate-700"><b className="text-slate-900">시크릿 스캔</b> 이상 무 — 스크린샷용 상태.</span>
            </li>
          </ul>
        </div>
      </div>

      <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50/80 px-4 py-3 text-center text-[11px] text-slate-500">
        위 수치와 문구는 스크린샷용 픽스처이며 실제 저장소 KPI와 무관합니다.
      </div>
    </div>
  )
}
