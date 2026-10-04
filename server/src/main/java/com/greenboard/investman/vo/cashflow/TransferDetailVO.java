package com.greenboard.investman.vo.cashflow;

import com.greenboard.investman.model.cashflow.TransferKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferDetailVO {
    private String source;
    private String destination;
    private TransferKind transferKind;
}
