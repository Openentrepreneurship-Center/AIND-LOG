"""AIND_SIMILARITY 적응 모듈 — HTML/JavaScript 코드 유사도 측정 (L1 ~ L4)

원본 프로젝트: https://github.com/Openentrepreneurship-Center/AIND_SIMILARITY
Java → HTML/JS 적응 내용:

  L1 Levenshtein   : 동일 알고리즘, 문자 단위 표면 유사도 (rapidfuzz)
  L2 CrystalBLEU   : javalang 토크나이저 → regex JS/HTML 범용 토크나이저 + nltk BLEU
  L3 TSED          : tree-sitter-java AST → difflib SequenceMatcher 라인 구조 유사도
  L4 CodeBERTScore : codebert-java(~500MB) → TF-IDF char n-gram cosine 경량 의미 유사도
"""
from __future__ import annotations

import re
from difflib import SequenceMatcher

# ── L1: Levenshtein ──────────────────────────────────────────────────────────
def l1_levenshtein(old: str, new: str) -> float:
    """L1 — 문자 단위 Levenshtein 표면 유사도.

    원본(AIND_SIMILARITY): Levenshtein.ratio(old_code, new_code)
    HTML/JS 적응: 동일 알고리즘 그대로 사용. Levenshtein 패키지 미설치 시
                  difflib.SequenceMatcher 로 fallback.
    반환값: [0.0, 1.0] — 1.0 이면 완전히 동일
    """
    try:
        import Levenshtein  # python-Levenshtein (rapidfuzz)
        return round(float(Levenshtein.ratio(old, new)), 4)
    except ImportError:
        return round(SequenceMatcher(None, old, new).ratio(), 4)


# ── L2: BLEU (JS/HTML 토크나이저) ────────────────────────────────────────────
_TOKEN_RE = re.compile(r'\w+|[^\w\s]')


def _js_tokenize(code: str) -> list[str]:
    """HTML/JS 코드를 어휘 단위 토큰으로 분리.

    원본(AIND_SIMILARITY): javalang.tokenizer.tokenize(code)
    HTML/JS 적응: regex로 단어(\w+)와 구두점/연산자 분리.
    """
    return _TOKEN_RE.findall(code)


def l2_bleu(old: str, new: str) -> float:
    """L2 — 토큰 n-gram BLEU (CrystalBLEU 적응).

    원본(AIND_SIMILARITY): crystalbleu.corpus_bleu([[old_tokens]], [new_tokens])
    HTML/JS 적응: javalang 대신 regex 토크나이저, nltk sentence_bleu + Smoothing.
    배경 코퍼스(trivial n-grams) 없이 vanilla BLEU 계산 — 원본 fallback과 동일.
    반환값: [0.0, 1.0]
    """
    try:
        from nltk.translate.bleu_score import sentence_bleu, SmoothingFunction
        ref = _js_tokenize(old)
        hyp = _js_tokenize(new)
        if not ref or not hyp:
            return 1.0 if ref == hyp else 0.0
        sf = SmoothingFunction().method1
        return round(float(sentence_bleu([ref], hyp, smoothing_function=sf)), 4)
    except Exception:
        return 0.0


# ── L3: 라인 구조적 유사도 (TSED 적응) ────────────────────────────────────────
def l3_structural(old: str, new: str) -> float:
    """L3 — 라인 단위 구조적 유사도 (TSED 적응).

    원본(AIND_SIMILARITY): tree-sitter-java AST → APTED 트리 편집 거리
      score = 1 - edit_distance / max(|T1|, |T2|)
    HTML/JS 적응: tree-sitter-java 대신 difflib.SequenceMatcher 를 빈 줄 제거한
                  라인 시퀀스에 적용. 동일한 [0,1] 정규화 비율 반환.
    반환값: [0.0, 1.0]
    """
    old_lines = [ln for ln in old.splitlines() if ln.strip()]
    new_lines = [ln for ln in new.splitlines() if ln.strip()]
    if not old_lines and not new_lines:
        return 1.0
    return round(SequenceMatcher(None, old_lines, new_lines).ratio(), 4)


# ── L4: TF-IDF cosine 의미론적 유사도 (CodeBERTScore 적응) ──────────────────
def l4_semantic(old: str, new: str) -> float:
    """L4 — TF-IDF char n-gram cosine 의미론적 유사도 (CodeBERTScore 적응).

    원본(AIND_SIMILARITY): code_bert_score.score(cands, refs, lang='java') → F1
    HTML/JS 적응: codebert-java 모델(~500MB + torch) 대신 scikit-learn TF-IDF
                  char(3~5)-gram vectorizer + cosine_similarity 사용.
                  설치 비용 없이 의미론적 어휘 패턴을 근사.
    반환값: [0.0, 1.0]
    """
    try:
        from sklearn.feature_extraction.text import TfidfVectorizer
        from sklearn.metrics.pairwise import cosine_similarity
        vect = TfidfVectorizer(analyzer="char_wb", ngram_range=(3, 5), max_features=10000)
        tfidf = vect.fit_transform([old, new])
        sim = cosine_similarity(tfidf[0:1], tfidf[1:2])[0][0]
        return round(float(sim), 4)
    except Exception:
        return 0.0


# ── 통합 계산 ────────────────────────────────────────────────────────────────
def compute_all(old: str, new: str) -> dict[str, float]:
    """L1~L4 전체 유사도 계산. 반환값은 각 레이어별 [0.0, 1.0] 점수."""
    return {
        "L1": l1_levenshtein(old, new),
        "L2": l2_bleu(old, new),
        "L3": l3_structural(old, new),
        "L4": l4_semantic(old, new),
    }
