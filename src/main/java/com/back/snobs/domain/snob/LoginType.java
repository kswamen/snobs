package com.back.snobs.domain.snob;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoginType {
    local,
    google,
    kakao
}
