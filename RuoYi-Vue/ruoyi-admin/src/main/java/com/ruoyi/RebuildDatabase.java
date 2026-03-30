package com.ruoyi;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class RebuildDatabase {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&allowMultiQueries=true";
        String username = "root";
        String password = "root";

        System.out.println("Connecting to database server...");
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            try (Statement stmt = conn.createStatement()) {
                System.out.println("Dropping and creating database `my-assets`...");
                stmt.execute("DROP DATABASE IF EXISTS `my-assets`");
                stmt.execute("CREATE DATABASE `my-assets` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
            }
        }

        String dbUrl = "jdbc:mysql://localhost:3306/my-assets?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&allowMultiQueries=true";
        try (Connection conn = DriverManager.getConnection(dbUrl, username, password)) {
            ScriptRunner runner = new ScriptRunner(conn);
            runner.setAutoCommit(true);
            runner.setStopOnError(false); // 避免重复 DDL 时中断
            runner.setLogWriter(null); // 关闭 SQL 语句打印，保持控制台整洁
            runner.setErrorLogWriter(new java.io.PrintWriter(System.err));
            runner.setFullLineDelimiter(false);

            String basePath = "e:/my-project/assets-project/RuoYi-Vue/sql/";
            String[] files = {
                "ry_20250522.sql",
                "quartz.sql",
                "asset_20260324.sql",
                "asset_menu_20260324.sql",
                "asset_role_20260324.sql",
                "asset_user_role_20260324.sql",
                "asset_patch_20260325_asset_user_self_scope.sql",
                "asset_patch_20260325_attachment_audit_columns.sql",
                "asset_patch_20260325_inventory_result_nullable.sql",
                "asset_patch_20260325_order_inventory_unique.sql",
                "asset_patch_20260326_asset_status_repairing.sql",
                "asset_patch_20260326_category_field_template.sql",
                "asset_patch_20260326_cleanup_qa_data.sql",
                "asset_patch_20260326_operate_order_source_link.sql",
                "asset_patch_20260326_repair_module.sql",
                "asset_patch_20260327_asset_status_repairing_fix.sql",
                "asset_patch_20260327_category_template_history.sql",
                "asset_patch_20260327_info_create_page.sql",
                "asset_patch_20260327_order_create_page.sql",
                "asset_patch_20260327_qa_asset_name_fix.sql",
                "asset_patch_20260327_repair_multi_item.sql",
                "asset_patch_20260327_repair_page_routes.sql",
                "asset_patch_20260329_asset_type_foundation.sql",
                "asset_patch_20260329_fixed_asset_rebranding.sql",
        "asset_patch_20260329_asset_dicts.sql",
        "asset_patch_20260329_category_asset_type.sql",
        "asset_patch_20260329_demo_data.sql"
            };

            for (String file : files) {
                System.out.println("Executing " + file + "...");
                try (InputStreamReader isr = new InputStreamReader(new FileInputStream(basePath + file), StandardCharsets.UTF_8)) {
                    runner.runScript(isr);
                }
            }
            System.out.println("================================");
            System.out.println("All scripts executed successfully!");
            System.out.println("================================");
        }
    }
}
