package com.backend.domain.terms.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.terms.dto.mapper.TermResponseMapper;
import com.backend.domain.terms.dto.mapper.UserTermConsentMapper;
import com.backend.domain.terms.dto.request.TermConsentRequest;
import com.backend.domain.terms.dto.response.TermConsentStatusResponse;
import com.backend.domain.terms.dto.response.TermResponse;
import com.backend.domain.terms.entity.Term;
import com.backend.domain.terms.entity.TermType;
import com.backend.domain.terms.exception.RequiredTermNotAgreedException;
import com.backend.domain.terms.repository.TermRepository;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.entity.UserTermConsent;
import com.backend.domain.user.repository.UserRepository;
import com.backend.domain.user.repository.UserTermConsentRepository;

import lombok.RequiredArgsConstructor;
import com.backend.global.util.DateTimeUtil;

@Service
@RequiredArgsConstructor
public class TermService {

    private final TermRepository termRepository;
    private final UserRepository userRepository;
    private final UserTermConsentRepository userTermConsentRepository;
    private final TermResponseMapper termResponseMapper;
    private final UserTermConsentMapper userTermConsentMapper;

    @Transactional(readOnly = true)
    public List<TermResponse> getCurrentTerms() {
        LocalDate now = DateTimeUtil.today();

        return Arrays.stream(TermType.values())
                .map(type -> termRepository.findFirstByTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(type, now))
                .filter(Optional::isPresent)
                .map(opt -> termResponseMapper.toResponse(opt.get()))
                .toList();
    }

    @Transactional
    public void acceptTerms(String userId, TermConsentRequest request) {
        User user = userRepository.getByIdAndDeletedAtIsNull(userId);

        List<String> agreedTermIds = request.consents().stream()
                .filter(TermConsentRequest.TermConsent::agreed)
                .map(TermConsentRequest.TermConsent::termId)
                .toList();

        validateRequiredTermsAgreed(agreedTermIds);

        for (String termId : agreedTermIds) {
            Term term = termRepository.findActiveTermOrThrow(termId);
            user.addTermConsent(userTermConsentMapper.toEntity(term));
        }

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public TermConsentStatusResponse getConsentStatus(String userId) {
        List<TermResponse> currentTerms = getCurrentTerms();
        List<UserTermConsent> userConsents = userTermConsentRepository.findByUserId(userId);

        Map<String, Integer> consentedVersions = userConsents.stream()
                .collect(Collectors.toMap(UserTermConsent::getTermId, UserTermConsent::getVersion));

        List<TermResponse> pendingTerms = currentTerms.stream()
                .filter(term -> {
                    Integer consentedVersion = consentedVersions.get(term.id());
                    return consentedVersion == null || consentedVersion < term.version();
                })
                .toList();

        return new TermConsentStatusResponse(!pendingTerms.isEmpty(), pendingTerms);
    }

    @Transactional(readOnly = true)
    public void validateRequiredTermsConsented(String userId) {
        List<Term> currentActiveTerms = getCurrentActiveTerms();
        List<UserTermConsent> userConsents = userTermConsentRepository.findByUserId(userId);

        Map<String, Integer> consentedVersions = userConsents.stream()
                .collect(Collectors.toMap(UserTermConsent::getTermId, UserTermConsent::getVersion));

        boolean allRequiredAgreed = currentActiveTerms.stream()
                .filter(Term::isRequired)
                .allMatch(term -> {
                    Integer consentedVersion = consentedVersions.get(term.getId());
                    return consentedVersion != null && consentedVersion >= term.getVersion();
                });

        if (!allRequiredAgreed) {
            throw new RequiredTermNotAgreedException();
        }
    }

    private List<Term> getCurrentActiveTerms() {
        LocalDate now = DateTimeUtil.today();
        List<Term> activeTerms = new ArrayList<>();

        for (TermType type : TermType.values()) {
            termRepository.findFirstByTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(type, now)
                    .ifPresent(activeTerms::add);
        }

        return activeTerms;
    }

    @Transactional(readOnly = true)
    public void validateRequiredTermsAgreed(List<String> agreedTermIds) {
        List<Term> requiredTerms = termRepository.findActiveRequiredTerms();

        Map<String, Term> latestByType = requiredTerms.stream()
                .collect(Collectors.toMap(
                        term -> term.getType().name(),
                        term -> term,
                        (t1, t2) -> t1.getVersion() > t2.getVersion() ? t1 : t2
                ));

        for (Term requiredTerm : latestByType.values()) {
            if (!agreedTermIds.contains(requiredTerm.getId())) {
                throw new RequiredTermNotAgreedException();
            }
        }
    }

    @Transactional(readOnly = true)
    public Term getActiveTerm(String termId) {
        return termRepository.findActiveTermOrThrow(termId);
    }
}
