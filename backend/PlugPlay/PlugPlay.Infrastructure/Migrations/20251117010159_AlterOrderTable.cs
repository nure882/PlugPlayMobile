using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace PlugPlay.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class AlterOrderTable : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "delivery_address",
                table: "order");

            migrationBuilder.RenameTable(
                name: "wishlist",
                newName: "wish_list");

            migrationBuilder.RenameTable(
                name: "user_refresh_token",
                newName: "user_refresh_tokens");

            migrationBuilder.RenameTable(
                name: "user_address",
                newName: "user_addresses");

            migrationBuilder.RenameTable(
                name: "user",
                newName: "asp_net_users");

            migrationBuilder.RenameTable(
                name: "review",
                newName: "reviews");

            migrationBuilder.RenameTable(
                name: "product_image",
                newName: "product_images");

            migrationBuilder.RenameTable(
                name: "product_attribute",
                newName: "product_attributes");

            migrationBuilder.RenameTable(
                name: "product",
                newName: "products");

            migrationBuilder.RenameTable(
                name: "order_item",
                newName: "order_items");

            migrationBuilder.RenameTable(
                name: "order",
                newName: "orders");

            migrationBuilder.RenameTable(
                name: "category",
                newName: "categories");

            migrationBuilder.RenameTable(
                name: "cart_item",
                newName: "cart_items");

            migrationBuilder.RenameTable(
                name: "attribute",
                newName: "attributes");

            migrationBuilder.RenameIndex(
                name: "IX_user_refresh_token_token",
                table: "user_refresh_tokens",
                newName: "IX_user_refresh_tokens_token");

            migrationBuilder.RenameColumn(
                name: "user_id",
                table: "asp_net_users",
                newName: "id");

            migrationBuilder.AddColumn<int>(
                name: "delivery_address_id",
                table: "orders",
                type: "integer",
                nullable: true);

            migrationBuilder.AddColumn<int>(
                name: "delivery_method",
                table: "orders",
                type: "integer",
                nullable: false,
                defaultValue: 0);

            migrationBuilder.AddColumn<int>(
                name: "payment_status",
                table: "orders",
                type: "integer",
                nullable: false,
                defaultValue: 0);

            migrationBuilder.AddColumn<DateTime>(
                name: "updated_at",
                table: "orders",
                type: "timestamp with time zone",
                nullable: false,
                defaultValue: new DateTime(1, 1, 1, 0, 0, 0, 0, DateTimeKind.Unspecified));

            migrationBuilder.CreateIndex(
                name: "ix_orders_delivery_address_id",
                table: "orders",
                column: "delivery_address_id");

            migrationBuilder.AddForeignKey(
                name: "fk_orders_user_addresses_delivery_address_id",
                table: "orders",
                column: "delivery_address_id",
                principalTable: "user_addresses",
                principalColumn: "id",
                onDelete: ReferentialAction.SetNull);

        // Enable required extensions
        migrationBuilder.Sql("CREATE EXTENSION IF NOT EXISTS pg_trgm;");
        migrationBuilder.Sql("CREATE EXTENSION IF NOT EXISTS btree_gin;"); // For GIN with integers

        migrationBuilder.Sql(@"
            CREATE INDEX IF NOT EXISTS ix_product_attributes_attr_value_btree 
            ON product_attributes (attribute_id, value);
        ");

        // Product Attributes - Composite with INCLUDE for covering index
        migrationBuilder.Sql(@"
            CREATE INDEX IF NOT EXISTS ix_product_attributes_attr_value_product 
            ON product_attributes (attribute_id, value) 
            INCLUDE (product_id);
        ");

        // Product Attributes - Composite for joins (with INCLUDE)
        migrationBuilder.Sql(@"
            CREATE INDEX IF NOT EXISTS ix_product_attributes_product_attribute 
            ON product_attributes (product_id, attribute_id) 
            INCLUDE (value);
        ");

        // Products - Partial index for stock availability
        migrationBuilder.Sql(@"
            CREATE INDEX IF NOT EXISTS ix_products_stock_quantity 
            ON products (stock_quantity) 
            WHERE stock_quantity != 0;
        ");

        // Products - Composite for category + stock queries (with INCLUDE)
        migrationBuilder.Sql(@"
            CREATE INDEX IF NOT EXISTS ix_products_category_stock 
            ON products (category_id, stock_quantity) 
            INCLUDE (id, name, price, created_at);
        ");

        // Products - Trigram index for name search
        migrationBuilder.Sql(@"
            CREATE INDEX IF NOT EXISTS ix_products_name_trgm 
            ON products USING gin (name gin_trgm_ops);
        ");

        // Products - Trigram index for description search
        migrationBuilder.Sql(@"
            CREATE INDEX IF NOT EXISTS ix_products_description_trgm 
            ON products USING gin (description gin_trgm_ops);
        ");

        // Categories - Composite for recursive queries (with INCLUDE)
        migrationBuilder.Sql(@"
            CREATE INDEX IF NOT EXISTS ix_categories_parent_id_id 
            ON categories (parent_category_id, id) 
            INCLUDE (name);
        ");

        // Additional helpful indexes

        // Reviews - For loading product reviews efficiently
        migrationBuilder.Sql(@"
            CREATE INDEX IF NOT EXISTS ix_reviews_product_created 
            ON reviews (product_id, created_at DESC) 
            INCLUDE (user_id, rating, comment, updated_at);
        ");

        // Orders - For user order history
        migrationBuilder.Sql(@"
            CREATE INDEX IF NOT EXISTS ix_orders_user_status 
            ON orders (user_id, status) 
            INCLUDE (order_date, total_amount, payment_status)
            WHERE user_id IS NOT NULL;
        ");

        // Product Images - Covering index
        migrationBuilder.Sql(@"
            CREATE INDEX IF NOT EXISTS ix_product_images_product_covering 
            ON product_images (product_id) 
            INCLUDE (image_url);
        ");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "fk_orders_user_addresses_delivery_address_id",
                table: "orders");

            migrationBuilder.DropIndex(
                name: "ix_orders_delivery_address_id",
                table: "orders");

            migrationBuilder.DropColumn(
                name: "delivery_address_id",
                table: "orders");

            migrationBuilder.DropColumn(
                name: "delivery_method",
                table: "orders");

            migrationBuilder.DropColumn(
                name: "payment_status",
                table: "orders");

            migrationBuilder.DropColumn(
                name: "updated_at",
                table: "orders");

            migrationBuilder.RenameTable(
                name: "wish_list",
                newName: "wishlist");

            migrationBuilder.RenameTable(
                name: "user_refresh_tokens",
                newName: "user_refresh_token");

            migrationBuilder.RenameTable(
                name: "user_addresses",
                newName: "user_address");

            migrationBuilder.RenameTable(
                name: "reviews",
                newName: "review");

            migrationBuilder.RenameTable(
                name: "products",
                newName: "product");

            migrationBuilder.RenameTable(
                name: "product_images",
                newName: "product_image");

            migrationBuilder.RenameTable(
                name: "product_attributes",
                newName: "product_attribute");

            migrationBuilder.RenameTable(
                name: "orders",
                newName: "order");

            migrationBuilder.RenameTable(
                name: "order_items",
                newName: "order_item");

            migrationBuilder.RenameTable(
                name: "categories",
                newName: "category");

            migrationBuilder.RenameTable(
                name: "cart_items",
                newName: "cart_item");

            migrationBuilder.RenameTable(
                name: "attributes",
                newName: "attribute");

            migrationBuilder.RenameTable(
                name: "asp_net_users",
                newName: "user");

            migrationBuilder.RenameIndex(
                name: "IX_user_refresh_tokens_token",
                table: "user_refresh_token",
                newName: "IX_user_refresh_token_token");

            migrationBuilder.RenameColumn(
                name: "id",
                table: "user",
                newName: "user_id");

            migrationBuilder.AddColumn<string>(
                name: "delivery_address",
                table: "order",
                type: "character varying(500)",
                maxLength: 500,
                nullable: true);
            
            migrationBuilder.Sql("DROP INDEX IF EXISTS ix_product_attributes_attr_value_btree;");
            migrationBuilder.Sql("DROP INDEX IF EXISTS ix_product_attributes_attr_value_product;");
            migrationBuilder.Sql("DROP INDEX IF EXISTS ix_product_attributes_product_attribute;");
            migrationBuilder.Sql("DROP INDEX IF EXISTS ix_products_stock_quantity;");
            migrationBuilder.Sql("DROP INDEX IF EXISTS ix_products_category_stock;");
            migrationBuilder.Sql("DROP INDEX IF EXISTS ix_products_name_trgm;");
            migrationBuilder.Sql("DROP INDEX IF EXISTS ix_products_description_trgm;");
            migrationBuilder.Sql("DROP INDEX IF EXISTS ix_categories_parent_id_id;");
            migrationBuilder.Sql("DROP INDEX IF EXISTS ix_reviews_product_created;");
            migrationBuilder.Sql("DROP INDEX IF EXISTS ix_orders_user_status;");
            migrationBuilder.Sql("DROP INDEX IF EXISTS ix_product_images_product_covering;");
        }
    }
}
