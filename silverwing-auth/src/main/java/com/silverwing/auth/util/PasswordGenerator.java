package com.silverwing.auth.util;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码生成工具
 * <p>
 * 生成数据库初始化脚本中使用的 BCrypt 密码哈希。
 * 运行 main 方法后，将输出可复制到 init.sql 的 SQL 字段值。
 * </p>
 *
 * @author silverwing
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        generate("admin", "admin123");
        generate("nurse1", "nurse123");
    }

    /**
     * 生成指定用户的 BCrypt 哈希
     *
     * @param username 用户名
     * @param rawPassword 明文密码
     */
    private static void generate(String username, String rawPassword) {
        String hash = BCrypt.hashpw(rawPassword);

        System.out.println("========================================");
        System.out.println(username + " 用户:");
        System.out.println("  明文密码: " + rawPassword);
        System.out.println("  BCrypt 哈希: " + hash);
        System.out.println("  校验结果: " + BCrypt.checkpw(rawPassword, hash));
        System.out.println();
        System.out.println("INSERT 片段: ('" + username + "', '" + hash + "', ...) ");
        System.out.println();
    }
}
