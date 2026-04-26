package com.backend.domain.terms.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.terms.entity.Term;
import com.backend.domain.terms.entity.TermType;
import com.backend.domain.terms.exception.InactiveTermException;
import com.backend.domain.terms.exception.InvalidEffectiveDateException;
import com.backend.domain.terms.exception.TermNotFoundException;
import com.backend.global.util.DateTimeUtil;

public interface TermRepository extends JpaRepository<Term, String> {

	List<Term> findByTypeOrderByVersionDesc(TermType type);

	Page<Term> findAllByOrderByCreatedAtDesc(Pageable pageable);

	Page<Term> findByTypeOrderByVersionDesc(TermType type, Pageable pageable);

	// 해당 타입에 시행일 이후의 약관이 존재하는지 확인 (시행일 검증용)
	boolean existsByTypeAndEffectiveDateGreaterThanEqual(TermType type, LocalDate effectiveDate);

	default void validateEffectiveDateOrThrow(TermType type, LocalDate effectiveDate) {
		if (existsByTypeAndEffectiveDateGreaterThanEqual(type, effectiveDate)) {
			throw new InvalidEffectiveDateException();
		}
	}

	// 해당 타입의 최신 버전 약관 조회
	Optional<Term> findFirstByTypeOrderByVersionDesc(TermType type);

	// 현재 날짜 기준 활성화된 약관 조회 (시행일이 현재 이하인 것 중 가장 최신)
	Optional<Term> findFirstByTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
		TermType type, LocalDate effectiveDate);


	default Term findByIdOrThrow(String id) {
		return findById(id)
			.orElseThrow(TermNotFoundException::new);
	}

	default Term findActiveTermOrThrow(String id) {
		Term term = findByIdOrThrow(id);
		if (term.getEffectiveDate().isAfter(DateTimeUtil.today())) {
			throw new InactiveTermException();
		}
		return term;
	}

	// 현재 활성화된 필수 약관 목록 조회
	List<Term> findByIsRequiredTrueAndEffectiveDateLessThanEqual(LocalDate date);

	default List<Term> findActiveRequiredTerms() {
		return findByIsRequiredTrueAndEffectiveDateLessThanEqual(DateTimeUtil.today());
	}
}
