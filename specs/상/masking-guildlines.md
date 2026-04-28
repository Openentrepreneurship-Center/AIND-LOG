#masking-guildlines.md
## 마스킹 Util
**import 경로**: com.poc.backend.util.MaskingUtil

## 마스킹 함수
- public static String maskCardNumber(String cardNumber)
  - cardNumber: 마스킹할 카드번호
- public static String maskField(String field, int maskLength)
  - field: 마스킹할 필드
  - maskLength: 마스킹 제외 범위 (앞에서부터)
- public static String maskField(String field, int prefixLength, int suffixLength)
  - field: 마스킹할 필드
  - prefixLength: 앞에서 보여줄 글자 수
  - suffixLength: 뒤에서 보여줄 글자 수
- public static boolean isMasked(String field, int maskLength)
  - field: 판단할 필드
  - maskLength: 마스킹 제외 범위 (앞에서부터)
- public static boolean isMasked(String field, int prefixLength, int suffixLength)
  - field: 판단할 필드
  - prefixLength: 앞에서 보여줄 글자 수
  - suffixLength: 뒤에서 보여줄 글자 수