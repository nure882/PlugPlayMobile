using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace PlugPlay.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class AddGoogleIdMigration : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "google_id",
                table: "user",
                type: "character varying(255)",
                maxLength: 255,
                nullable: false,
                defaultValue: "");

            migrationBuilder.AddColumn<string>(
                name: "picture_url",
                table: "user",
                type: "character varying(400)",
                maxLength: 400,
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "google_id",
                table: "user");

            migrationBuilder.DropColumn(
                name: "picture_url",
                table: "user");
        }
    }
}
