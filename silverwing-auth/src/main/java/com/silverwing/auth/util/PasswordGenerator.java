package com.silverwing.auth.util;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;

/**
 * 密码生成工具
 * <p>
 * 生成数据库初始化脚本中使用的 MD5(盐 + 明文) 密码哈希与盐值。
 * 运行 main 方法后，将输出可复制到 init.sql 的 SQL 字段值。
 * </p>
 *
 * @author silverwing
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        generate("admin", "123456");
        generate("nurse1", "123456");
    }

    /**
     * 生成指定用户的盐值与 MD5(盐 + 明文) 哈希
     * @param username 用户名
     * @param rawPassword 明文密码
     */
    private static void generate(String username, String rawPassword) {
        String salt = RandomUtil.randomString(16);
        String hash = DigestUtil.md5Hex(salt + rawPassword);

        System.out.println("========================================");
        System.out.println(username + " 用户:");
        System.out.println("  明文密码: " + rawPassword);
        System.out.println("  盐值: " + salt);
        System.out.println("  MD5(盐+明文): " + hash);
        System.out.println("  校验结果: " + hash.equalsIgnoreCase(DigestUtil.md5Hex(salt + rawPassword)));
        System.out.println();
        System.out.println("INSERT 片段: ('" + username + "', '" + hash + "', '" + salt + "', ...)");
        System.out.println();
    }
}
