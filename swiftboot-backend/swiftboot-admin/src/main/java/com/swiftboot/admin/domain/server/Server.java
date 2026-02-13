package com.swiftboot.admin.domain.server;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * 服务器相关信息
 */
public class Server {
    private static final int OSHI_WAIT_SECOND = 1000;

    /**
     * CPU相关信息
     */
    private Cpu cpu = new Cpu();

    /**
     * 內存相关信息
     */
    private Mem mem = new Mem();

    /**
     * JVM相关信息
     */
    private Jvm jvm = new Jvm();

    /**
     * 服务器相关信息
     */
    private Sys sys = new Sys();

    /**
     * 磁盘相关信息
     */
    private List<SysFile> sysFiles = new LinkedList<>();

    public Cpu getCpu() {
        return cpu;
    }

    public void setCpu(Cpu cpu) {
        this.cpu = cpu;
    }

    public Mem getMem() {
        return mem;
    }

    public void setMem(Mem mem) {
        this.mem = mem;
    }

    public Jvm getJvm() {
        return jvm;
    }

    public void setJvm(Jvm jvm) {
        this.jvm = jvm;
    }

    public Sys getSys() {
        return sys;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
    }

    public List<SysFile> getSysFiles() {
        return sysFiles;
    }

    public void setSysFiles(List<SysFile> sysFiles) {
        this.sysFiles = sysFiles;
    }

    public void copyTo() throws Exception {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        setCpuInfo(hal.getProcessor());
        setMemInfo(hal.getMemory());
        setSysInfo();
        setJvmInfo();
        setSysFiles(si.getOperatingSystem());
    }

    /**
     * 设置CPU信息
     */
    private void setCpuInfo(CentralProcessor processor) {
        // CPU信息
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(OSHI_WAIT_SECOND);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[TickType.NICE.ordinal()] - prevTicks[TickType.NICE.ordinal()];
        long irq = ticks[TickType.IRQ.ordinal()] - prevTicks[TickType.IRQ.ordinal()];
        long softIrq = ticks[TickType.SOFTIRQ.ordinal()] - prevTicks[TickType.SOFTIRQ.ordinal()];
        long steal = ticks[TickType.STEAL.ordinal()] - prevTicks[TickType.STEAL.ordinal()];
        long cSys = ticks[TickType.SYSTEM.ordinal()] - prevTicks[TickType.SYSTEM.ordinal()];
        long user = ticks[TickType.USER.ordinal()] - prevTicks[TickType.USER.ordinal()];
        long iowait = ticks[TickType.IOWAIT.ordinal()] - prevTicks[TickType.IOWAIT.ordinal()];
        long idle = ticks[TickType.IDLE.ordinal()] - prevTicks[TickType.IDLE.ordinal()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softIrq + steal;
        cpu.setCpuNum(processor.getLogicalProcessorCount());
        cpu.setTotal(totalCpu);
        cpu.setSys(cSys);
        cpu.setUsed(user);
        cpu.setWait(iowait);
        cpu.setFree(idle);
    }

    /**
     * 设置内存信息
     */
    private void setMemInfo(GlobalMemory memory) {
        mem.setTotal(memory.getTotal());
        mem.setUsed(memory.getTotal() - memory.getAvailable());
        mem.setFree(memory.getAvailable());
    }

    // 系统信息缓存（IP、Hostname等基本不变的信息）
    private static Sys cachedSys = null;
    
    /**
     * 设置服务器信息（带缓存）
     */
    private void setSysInfo() {
        if (cachedSys == null) {
            Properties props = System.getProperties();
            cachedSys = new Sys();
            cachedSys.setComputerName(getComputerName());
            cachedSys.setComputerIp(getHostIp());
            cachedSys.setOsName(props.getProperty("os.name"));
            cachedSys.setOsArch(props.getProperty("os.arch"));
            cachedSys.setUserDir(props.getProperty("user.dir"));
        }
        this.sys = cachedSys;
    }

    /**
     * 设置Java虚拟机
     */
    private void setJvmInfo() {
        Properties props = System.getProperties();
        jvm.setTotal(Runtime.getRuntime().totalMemory());
        jvm.setMax(Runtime.getRuntime().maxMemory());
        jvm.setFree(Runtime.getRuntime().freeMemory());
        jvm.setVersion(props.getProperty("java.version"));
        jvm.setHome(props.getProperty("java.home"));
    }

    /**
     * 设置磁盘信息
     */
    private void setSysFiles(OperatingSystem os) {
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fileStores = fileSystem.getFileStores();
        for (OSFileStore fs : fileStores) {
            long free = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            long used = total - free;
            SysFile sysFile = new SysFile();
            sysFile.setDirName(fs.getMount());
            sysFile.setTypeName(fs.getType());
            sysFile.setTotal(convertFileSize(total));
            sysFile.setFree(convertFileSize(free));
            sysFile.setUsed(convertFileSize(used));
            sysFile.setUsage(mul(div(used, total, 4), 100));
            sysFiles.add(sysFile);
        }
    }

    /**
     * 字节转换
     */
    public String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }
    }

    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到
     * 小数点以后10位，以后的数字四舍五入。
     */
    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        if (b1.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.doubleValue();
        }
        return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 提供精确的乘法运算。
     */
    public static double mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }
    
    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 获取计算机名称
     */
    public static String getComputerName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "未知";
        }
    }

    /**
     * 获取计算机IP
     */
    public static String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    public class Cpu {
        private int cpuNum;
        private double total;
        private double sys;
        private double used;
        private double wait;
        private double free;

        public int getCpuNum() {
            return cpuNum;
        }

        public void setCpuNum(int cpuNum) {
            this.cpuNum = cpuNum;
        }

        public double getTotal() {
            return round(total, 2);
        }

        public void setTotal(double total) {
            this.total = total;
        }

        public double getSys() {
            return round(sys / total * 100, 2);
        }

        public void setSys(double sys) {
            this.sys = sys;
        }

        public double getUsed() {
            return round(used / total * 100, 2);
        }

        public void setUsed(double used) {
            this.used = used;
        }

        public double getWait() {
            return round(wait / total * 100, 2);
        }

        public void setWait(double wait) {
            this.wait = wait;
        }

        public double getFree() {
            return round(free / total * 100, 2);
        }

        public void setFree(double free) {
            this.free = free;
        }
    }

    public class Mem {
        private double total;
        private double used;
        private double free;

        public double getTotal() {
            return div(total, (1024 * 1024 * 1024), 2);
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public double getUsed() {
            return div(used, (1024 * 1024 * 1024), 2);
        }

        public void setUsed(long used) {
            this.used = used;
        }

        public double getFree() {
            return div(free, (1024 * 1024 * 1024), 2);
        }

        public void setFree(long free) {
            this.free = free;
        }

        public double getUsage() {
            return mul(div(used, total, 4), 100);
        }
    }

    public class Jvm {
        private double total;
        private double max;
        private double free;
        private String version;
        private String home;

        public double getTotal() {
            return div(total, (1024 * 1024), 2);
        }

        public void setTotal(double total) {
            this.total = total;
        }

        public double getMax() {
            return div(max, (1024 * 1024), 2);
        }

        public void setMax(double max) {
            this.max = max;
        }

        public double getFree() {
            return div(free, (1024 * 1024), 2);
        }

        public void setFree(double free) {
            this.free = free;
        }

        public double getUsed() {
            return div(total - free, (1024 * 1024), 2);
        }

        public double getUsage() {
            return mul(div(total - free, total, 4), 100);
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getHome() {
            return home;
        }

        public void setHome(String home) {
            this.home = home;
        }
        
        public String getRunTime() {
            long time = ManagementFactory.getRuntimeMXBean().getUptime();
            long days = time / (1000 * 60 * 60 * 24);
            long hours = (time % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
            long minutes = (time % (1000 * 60 * 60)) / (1000 * 60);
            return days + "天" + hours + "小时" + minutes + "分";
        }
        
        public String getStartTime() {
             long time = ManagementFactory.getRuntimeMXBean().getStartTime();
             return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(time));
        }

        public long getStartTimeLong() {
            return ManagementFactory.getRuntimeMXBean().getStartTime();
        }
    }

    public class Sys {
        private String computerName;
        private String computerIp;
        private String userDir;
        private String osName;
        private String osArch;

        public String getComputerName() {
            return computerName;
        }

        public void setComputerName(String computerName) {
            this.computerName = computerName;
        }

        public String getComputerIp() {
            return computerIp;
        }

        public void setComputerIp(String computerIp) {
            this.computerIp = computerIp;
        }

        public String getUserDir() {
            return userDir;
        }

        public void setUserDir(String userDir) {
            this.userDir = userDir;
        }

        public String getOsName() {
            return osName;
        }

        public void setOsName(String osName) {
            this.osName = osName;
        }

        public String getOsArch() {
            return osArch;
        }

        public void setOsArch(String osArch) {
            this.osArch = osArch;
        }
    }

    public class SysFile {
        private String dirName;
        private String typeName;
        private String total;
        private String free;
        private String used;
        private double usage;

        public String getDirName() {
            return dirName;
        }

        public void setDirName(String dirName) {
            this.dirName = dirName;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public String getFree() {
            return free;
        }

        public void setFree(String free) {
            this.free = free;
        }

        public String getUsed() {
            return used;
        }

        public void setUsed(String used) {
            this.used = used;
        }

        public double getUsage() {
            return usage;
        }

        public void setUsage(double usage) {
            this.usage = usage;
        }
    }
}
