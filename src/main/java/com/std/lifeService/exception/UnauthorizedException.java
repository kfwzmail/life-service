package com.std.lifeService.exception;

import com.std.lifeService.common.ResultCode;
import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {

    private final int code;

    public UnauthorizedException() {
        super(ResultCode.UNAUTHORIZED.getMessage());
        this.code = ResultCode.UNAUTHORIZED.getCode();
    }
}
