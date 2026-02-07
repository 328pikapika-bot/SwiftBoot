package com.swiftboot.common.core.utils.ip;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;

/**
 * 获取地址工具类
 */
@Slf4j
public class AddressUtils {

    private static final String UNKNOWN = "未知";
    private static Searcher searcher;

    static {
        try {
            // 尝试加载 ip2region.xdb
            ClassPathResource resource = new ClassPathResource("ip2region.xdb");
            if (resource.getStream() != null) {
                byte[] cBuff = resource.readBytes();
                searcher = Searcher.newWithBuffer(cBuff);
            } else {
                log.warn("Ip2region xdb file not found in classpath, address resolution will return 'Unknown'.");
            }
        } catch (Exception e) {
            log.error("Failed to load ip2region.xdb", e);
        }
    }

    /**
     * 根据IP获取地址
     */
    public static String getRealAddressByIP(String ip) {
        if (StrUtil.isBlank(ip)) {
            return UNKNOWN;
        }
        // 内网IP
        if (NetUtil.isInnerIP(ip)) {
            return "内网IP";
        }
        if (searcher == null) {
            return UNKNOWN;
        }
        try {
            String region = searcher.search(ip);
            if (StrUtil.isBlank(region)) {
                return UNKNOWN;
            }
            // 格式通常为: 国家|区域|省份|城市|ISP
            // 去除 0
            return region.replace("0|", "").replace("|0", "");
        } catch (Exception e) {
            log.error("Error searching IP address: {}", ip, e);
            return UNKNOWN;
        }
    }
}
