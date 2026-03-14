package com.swiftboot.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysNotice;
import com.swiftboot.admin.mapper.SysNoticeMapper;
import com.swiftboot.admin.service.SysNoticeService;
import org.springframework.stereotype.Service;

/**
 * 系统公告 Service 实现
 */
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements SysNoticeService {
}
