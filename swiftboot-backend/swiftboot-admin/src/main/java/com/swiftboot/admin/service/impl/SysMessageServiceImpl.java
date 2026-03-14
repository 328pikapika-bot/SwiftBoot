package com.swiftboot.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysMessage;
import com.swiftboot.admin.mapper.SysMessageMapper;
import com.swiftboot.admin.service.SysMessageService;
import org.springframework.stereotype.Service;

@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {
}
