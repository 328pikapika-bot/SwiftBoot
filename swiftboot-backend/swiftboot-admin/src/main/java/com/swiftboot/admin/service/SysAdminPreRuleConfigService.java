package com.swiftboot.admin.service;

import com.swiftboot.admin.domain.dto.SysAdminPreRuleConfigDTO;
import com.swiftboot.admin.domain.vo.SysAdminPreRuleConfigVO;

/**
 * Administrator pre-check safety rule configuration service.
 */
public interface SysAdminPreRuleConfigService {

    SysAdminPreRuleConfigVO getConfig();

    void updateConfig(SysAdminPreRuleConfigDTO dto);

    String check(String content);

    String buildGovernancePrompt();

    String applyAnswerRules(String answer);
}
