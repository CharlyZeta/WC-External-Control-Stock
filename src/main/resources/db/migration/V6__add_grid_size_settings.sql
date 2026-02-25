-- V6: Add image size and name column width to user settings
ALTER TABLE "user_settings" 
    ADD COLUMN "image_size" INTEGER DEFAULT 50,
    ADD COLUMN "name_column_width" VARCHAR(20) DEFAULT '300px';
