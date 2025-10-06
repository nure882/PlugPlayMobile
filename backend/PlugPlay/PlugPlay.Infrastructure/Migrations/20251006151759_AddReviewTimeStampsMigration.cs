using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace PlugPlay.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class AddReviewTimeStampsMigration : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "fk_wishlists_asp_net_users_user_id",
                table: "wishlist");

            migrationBuilder.DropForeignKey(
                name: "fk_wishlists_products_product_id",
                table: "wishlist");

            migrationBuilder.DropPrimaryKey(
                name: "pk_wishlists",
                table: "wishlist");

            migrationBuilder.RenameIndex(
                name: "ix_wishlists_user_id",
                table: "wishlist",
                newName: "ix_wish_list_user_id");

            migrationBuilder.RenameIndex(
                name: "ix_wishlists_product_id",
                table: "wishlist",
                newName: "ix_wish_list_product_id");

            migrationBuilder.AddColumn<DateTime>(
                name: "created_at",
                table: "review",
                type: "timestamptz",
                nullable: false,
                defaultValue: new DateTime(1, 1, 1, 0, 0, 0, 0, DateTimeKind.Unspecified));

            migrationBuilder.AddColumn<DateTime>(
                name: "updated_at",
                table: "review",
                type: "timestamptz",
                nullable: true);

            migrationBuilder.AddPrimaryKey(
                name: "pk_wish_list",
                table: "wishlist",
                column: "id");

            migrationBuilder.AddForeignKey(
                name: "fk_wish_list_asp_net_users_user_id",
                table: "wishlist",
                column: "user_id",
                principalTable: "user",
                principalColumn: "user_id",
                onDelete: ReferentialAction.Cascade);

            migrationBuilder.AddForeignKey(
                name: "fk_wish_list_products_product_id",
                table: "wishlist",
                column: "product_id",
                principalTable: "product",
                principalColumn: "id",
                onDelete: ReferentialAction.Cascade);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "fk_wish_list_asp_net_users_user_id",
                table: "wishlist");

            migrationBuilder.DropForeignKey(
                name: "fk_wish_list_products_product_id",
                table: "wishlist");

            migrationBuilder.DropPrimaryKey(
                name: "pk_wish_list",
                table: "wishlist");

            migrationBuilder.DropColumn(
                name: "created_at",
                table: "review");

            migrationBuilder.DropColumn(
                name: "updated_at",
                table: "review");

            migrationBuilder.RenameIndex(
                name: "ix_wish_list_user_id",
                table: "wishlist",
                newName: "ix_wishlists_user_id");

            migrationBuilder.RenameIndex(
                name: "ix_wish_list_product_id",
                table: "wishlist",
                newName: "ix_wishlists_product_id");

            migrationBuilder.AddPrimaryKey(
                name: "pk_wishlists",
                table: "wishlist",
                column: "id");

            migrationBuilder.AddForeignKey(
                name: "fk_wishlists_asp_net_users_user_id",
                table: "wishlist",
                column: "user_id",
                principalTable: "user",
                principalColumn: "user_id",
                onDelete: ReferentialAction.Cascade);

            migrationBuilder.AddForeignKey(
                name: "fk_wishlists_products_product_id",
                table: "wishlist",
                column: "product_id",
                principalTable: "product",
                principalColumn: "id",
                onDelete: ReferentialAction.Cascade);
        }
    }
}
