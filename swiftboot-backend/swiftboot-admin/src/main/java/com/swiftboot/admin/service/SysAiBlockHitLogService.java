package com.swiftboot.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.admin.domain.entity.SysAiBlockHitLog;
import com.swiftboot.common.core.domain.PageQuery;

import java.util.Map;

/**
 * AI block word hit log service.
 */
public interface SysAiBlockHitLogService {

    Page<SysAiBlockHitLog> selectPage(SysAiBlockHitLog query, PageQuery pageQuery);

    Map<String, Object> getStats();

    void recordHit(SysAiBlockHitLog log);
}
