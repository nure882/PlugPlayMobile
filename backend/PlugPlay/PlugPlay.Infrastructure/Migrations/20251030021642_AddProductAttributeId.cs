using Microsoft.EntityFrameworkCore.Migrations;
using Npgsql.EntityFrameworkCore.PostgreSQL.Metadata;

#nullable disable

namespace PlugPlay.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class AddProductAttributeId : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropPrimaryKey(
                name: "PK_product_attribute",
                table: "product_attribute");

            migrationBuilder.AddColumn<int>(
                name: "id",
                table: "product_attribute",
                type: "integer",
                nullable: false,
                defaultValue: 0)
                .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn);

            migrationBuilder.AddPrimaryKey(
                name: "pk_product_attributes",
                table: "product_attribute",
                column: "id");

            migrationBuilder.CreateIndex(
                name: "ix_product_attributes_product_id",
                table: "product_attribute",
                column: "product_id");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropPrimaryKey(
                name: "pk_product_attributes",
                table: "product_attribute");

            migrationBuilder.DropIndex(
                name: "ix_product_attributes_product_id",
                table: "product_attribute");

            migrationBuilder.DropColumn(
                name: "id",
                table: "product_attribute");

            migrationBuilder.AddPrimaryKey(
                name: "PK_product_attribute",
                table: "product_attribute",
                columns: new[] { "product_id", "attribute_id" });
        }
    }
}
