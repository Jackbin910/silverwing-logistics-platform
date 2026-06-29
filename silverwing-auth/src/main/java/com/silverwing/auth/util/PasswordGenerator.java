package com.silverwing.auth.util;

import cn.hutool.crypto.digest.BCrypt;

/**
 * BCrypt 密码生成工具
 * <p>
 * 用于生成数据库初始化脚本中使用的 BCrypt 密码哈希值。
 * 运行 main 方法后，将输出可复制到 init.sql 的 SQL 语句。
 * </p>
 *
 * @author silverwing
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        // 生成 admin 用户密码哈希
        String adminPassword = "123456";
        String adminHash = BCrypt.hashpw(adminPassword, BCrypt.gensalt());
        
        System.out.println("========================================");
        System.out.println("BCrypt 密码哈希生成结果");
        System.out.println("========================================");
        System.out.println();
        System.out.println("admin 用户:");
        System.out.println("  明文密码: " + adminPassword);
        System.out.println("  BCrypt哈希: " + adminHash);
        System.out.println("  验证结果: " + BCrypt.checkpw(adminPassword, adminHash));
        System.out.println();
        
        // 生成 nurse1 用户密码哈希
        String nursePassword = "123456";
        String nurseHash = BCrypt.hashpw(nursePassword, BCrypt.gensalt());
        
        System.out.println("nurse1 用户:");
        System.out.println("  明文密码: " + nursePassword);
        System.out.println("  BCrypt哈希: " + nurseHash);
        System.out.println("  验证结果: " + BCrypt.checkpw(nursePassword, nurseHash));
        System.out.println();
        
        System.out.println("========================================");
        System.out.println("SQL 插入语句（复制到 init.sql）:");
        System.out.println("========================================");
        System.out.println("INSERT INTO `sys_user` (`username`, `password`, `nickname`, `status`) VALUES");
        System.out.println("('admin', '" + adminHash + "', '系统管理员', 1),");
        System.out.println("('nurse1', '" + nurseHash + "', '护士-手术室1', 1);");
        System.out.println();
        System.out.println("提示：每次运行都会生成不同的哈希值（盐值随机），但都能验证同一明文密码。");
    }
}
