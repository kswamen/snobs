package com.back.snobs.dto.snob.dailyLog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyLogId implements Serializable {
    private String snob;
    private Long log;
}