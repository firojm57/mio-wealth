package com.greenboard.investman.vo.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseVO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
