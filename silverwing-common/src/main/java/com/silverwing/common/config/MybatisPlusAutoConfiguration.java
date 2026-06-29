package com.silverwing.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动配置类
 * <p>
 * 公共配置，所有微服务共享：
 * <ul>
 *   <li>分页插件：根据数据源自动识别数据库类型（MySQL / PostgreSQL 等），不再硬编码</li>
 *   <li>自动填充处理器：自动填充 createTime / updateTime</li>
 * </ul>
 * </p>
 * <p>
 * 通过 {@link ConditionalOnClass} 仅在 classpath 存在 MyBatis-Plus 时加载，
 * 不使用 MyBatis-Plus 的服务（如 admin-web）不会触发本配置。
 * </p>
 *
 * @author silverwing
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass({MybatisPlusInterceptor.class, DataSource.class})
public class MybatisPlusAutoConfiguration {

    /**
     * 注册 MyBatis-Plus 拦截器（含分页插件）
     * <p>
     * 数据库类型从数据源元数据自动推断，兼容 MySQL 与 PostgreSQL 等多种数据库。
     * 修复此前硬编码 DbType.MYSQL 导致 PostgreSQL 服务（如 ai-service）分页失效的问题。
     * </p>
     *
     * @param dataSource 数据源，用于推断数据库类型
     * @return MybatisPlusInterceptor
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(DataSource dataSource) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(detectDbType(dataSource)));
        return interceptor;
    }

    /**
     * 自动填充处理器
     * <p>
     * 配合 @TableField(fill = FieldFill.INSERT) 和 @TableField(fill = FieldFill.INSERT_UPDATE) 使用，
     * 自动填充 createTime、updateTime 字段。建议实体继承 {@link com.silverwing.common.entity.BaseEntity}。
     * </p>
     *
     * @return MetaObjectHandler
     */
    @Bean
    @ConditionalOnMissingBean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                // 插入时自动填充创建时间
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                // 插入时自动填充更新时间
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时自动填充更新时间
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }

    /**
     * 根据数据源元数据自动识别数据库类型
     * <p>
     * 通过 JDBC DatabaseMetaData 获取数据库产品名，映射到 MyBatis-Plus 的 DbType 枚举。
     * 支持常见数据库：MySQL、PostgreSQL、Oracle、SQL Server 等。
     * 无法识别时回退到 MYSQL，保证向后兼容。
     * </p>
     *
     * @param dataSource 数据源
     * @return MyBatis-Plus 数据库类型
     */
    private DbType detectDbType(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            String lowerName = productName == null ? "" : productName.toLowerCase();
            // 按数据库产品名匹配 MyBatis-Plus DbType 枚举
            if (lowerName.contains("postgresql")) {
                return DbType.POSTGRE_SQL;
            }
            if (lowerName.contains("mysql")) {
                return DbType.MYSQL;
            }
            if (lowerName.contains("mariadb")) {
                return DbType.MARIADB;
            }
            if (lowerName.contains("oracle")) {
                return DbType.ORACLE;
            }
            if (lowerName.contains("sql server") || lowerName.contains("sqlserver")) {
                return DbType.SQL_SERVER;
            }
            if (lowerName.contains("h2")) {
                return DbType.H2;
            }
            log.warn("无法识别的数据库产品名 [{}]，回退到 MYSQL", productName);
            return DbType.MYSQL;
        } catch (SQLException e) {
            log.warn("获取数据库类型失败，回退到 MYSQL：{}", e.getMessage());
            return DbType.MYSQL;
        }
    }
}
