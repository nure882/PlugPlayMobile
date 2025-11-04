BEGIN;

-- ====== asp_net_roles ======
INSERT INTO public.asp_net_roles (id, name, normalized_name, concurrency_stamp)
SELECT 1, 'Admin', 'ADMIN', gen_random_uuid()::text
WHERE NOT EXISTS (SELECT 1 FROM public.asp_net_roles WHERE name = 'Admin');

INSERT INTO public.asp_net_roles (id, name, normalized_name, concurrency_stamp)
SELECT 2, 'User', 'USER', gen_random_uuid()::text
WHERE NOT EXISTS (SELECT 1 FROM public.asp_net_roles WHERE name = 'User');

-- ====== user ======
INSERT INTO public."user" (user_id, user_name, normalized_user_name, email, normalized_email,
 email_confirmed, password_hash, phone_number, phone_number_confirmed, two_factor_enabled,
 first_name, last_name, role, created_at, updated_at, security_stamp, concurrency_stamp, lockout_enabled, access_failed_count, google_id, picture_url)
SELECT 1, 'admin', 'ADMIN', 'admin@cowork.com', 'ADMIN@COWORK.COM',
 true, 'AQAAAAEAACcQAAAAEMOCKHASH==', '+10000000001', true, false,
 'Admin', 'User', 1, NOW(), NOW(), gen_random_uuid()::text, gen_random_uuid()::text, false, 0, '', NULL
WHERE NOT EXISTS (SELECT 1 FROM public."user" WHERE user_name = 'admin');

INSERT INTO public."user" (user_id, user_name, normalized_user_name, email, normalized_email,
 email_confirmed, password_hash, phone_number, phone_number_confirmed, two_factor_enabled,
 first_name, last_name, role, created_at, updated_at, security_stamp, concurrency_stamp, lockout_enabled, access_failed_count, google_id, picture_url)
SELECT 2, 'user1', 'USER1', 'user1@cowork.com', 'USER1@COWORK.COM',
 true, 'AQAAAAEAACcQAAAAEMOCKHASH==', '+10000000002', true, false,
 'John', 'Doe', 2, NOW(), NOW(), gen_random_uuid()::text, gen_random_uuid()::text, false, 0, '', NULL
WHERE NOT EXISTS (SELECT 1 FROM public."user" WHERE user_name = 'user1');

INSERT INTO public."user" (user_id, user_name, normalized_user_name, email, normalized_email,
 email_confirmed, password_hash, phone_number, phone_number_confirmed, two_factor_enabled,
 first_name, last_name, role, created_at, updated_at, security_stamp, concurrency_stamp, lockout_enabled, access_failed_count, google_id, picture_url)
SELECT 3, 'user2', 'USER2', 'user2@cowork.com', 'USER2@COWORK.COM',
 true, 'AQAAAAEAACcQAAAAEMOCKHASH==', '+10000000003', true, false,
 'Jane', 'Smith', 2, NOW(), NOW(), gen_random_uuid()::text, gen_random_uuid()::text, false, 0, '', NULL
WHERE NOT EXISTS (SELECT 1 FROM public."user" WHERE user_name = 'user2');

INSERT INTO public."user" (user_id, user_name, normalized_user_name, email, normalized_email,
 email_confirmed, password_hash, phone_number, phone_number_confirmed, two_factor_enabled,
 first_name, last_name, role, created_at, updated_at, security_stamp, concurrency_stamp, lockout_enabled, access_failed_count, google_id, picture_url)
SELECT 4, 'user3', 'USER3', 'user3@cowork.com', 'USER3@COWORK.COM',
 true, 'AQAAAAEAACcQAAAAEMOCKHASH==', '+10000000004', true, false,
 'Alex', 'Johnson', 2, NOW(), NOW(), gen_random_uuid()::text, gen_random_uuid()::text, false, 0, '', NULL
WHERE NOT EXISTS (SELECT 1 FROM public."user" WHERE user_name = 'user3');

-- ====== asp_net_user_roles ======
INSERT INTO public.asp_net_user_roles (user_id, role_id)
SELECT 1, 1
WHERE NOT EXISTS (SELECT 1 FROM public.asp_net_user_roles WHERE user_id = 1 AND role_id = 1);

INSERT INTO public.asp_net_user_roles (user_id, role_id)
SELECT user_id, 2
FROM public."user"
WHERE user_id IN (2,3,4)
AND NOT EXISTS (SELECT 1 FROM public.asp_net_user_roles WHERE user_id = user_id AND role_id = 2);


-- ====== category ======
INSERT INTO public.category (id, name, parent_category_id) VALUES
-- (Full list of 103 categories)
(1, 'Computers & Laptops', NULL),
(2, 'Laptops', 1),
(3, 'Desktops', 1),
(4, 'Monitors', 1),
(5, 'Computer Components', 1),
(6, 'Motherboards', 5),
(7, 'Processors (CPUs)', 5),
(8, 'Graphics Cards (GPUs)', 5),
(9, 'RAM (Memory)', 5),
(10, 'Storage (HDD, SSD, NVMe)', 5),
(11, 'Power Supplies (PSUs)', 5),
(12, 'Cooling & Fans', 5),
(13, 'Computer Cases', 5),
(14, 'Accessories', 1),
(15, 'Chargers & Adapters', 14),
(16, 'Bags & Sleeves', 14),
(17, 'Web-cameras', 14),
(18, 'Mouses', 14),
(19, 'Keyboards', 14),
(20, 'Docking Stations', 14),
(21, 'Stands & Cooling Pads', 14),
(22, 'Mobile Phones & Tablets', NULL),
(23, 'Smartphones', 22),
(24, 'Feature Phones', 22),
(25, 'Tablets', 22),
(26, 'E-Readers', 22),
(27, 'Mobile Accessories', 22),
(28, 'Chargers & Cables', 27),
(29, 'Phone Cases', 27),
(30, 'Screen Protectors', 27),
(31, 'Power Banks', 27),
(32, 'Earphones & Headsets', 27),
(33, 'Memory Cards', 27),
(34, 'Audio & Music', NULL),
(35, 'Headphones & Earbuds', 34),
(36, 'Bluetooth Speakers', 34),
(37, 'Soundbars', 34),
(38, 'Home Audio Systems', 34),
(39, 'Turntables & Vinyl Players', 34),
(40, 'Microphones', 34),
(41, 'Studio Equipment', 34),
(42, 'Musical Instruments (Electronic)', 34),
(43, 'TVs & Home Entertainment', NULL),
(44, 'Smart TVs', 43),
(45, 'LED / OLED / QLED TVs', 43),
(46, 'Projectors', 43),
(47, 'Streaming Devices', 43),
(48, 'TV Accessories', 43),
(49, 'Mounts & Stands', 48),
(50, 'Remotes & Controllers', 48),
(51, 'Cables (HDMI, Optical, etc.)', 48),
(52, 'Cameras & Photography', NULL),
(53, 'DSLR Cameras', 52),
(54, 'Mirrorless Cameras', 52),
(55, 'Compact Cameras', 52),
(56, 'Action Cameras', 52),
(57, 'Drones', 52),
(58, 'Camera Lenses', 52),
(59, 'Tripods & Mounts', 52),
(60, 'Lighting Equipment', 52),
(61, 'Memory Cards & Storage', 52),
(62, 'Smart Home', NULL),
(63, 'Smart Lights', 62),
(64, 'Smart Plugs', 62),
(65, 'Smart Speakers', 62),
(66, 'Smart Cameras', 62),
(67, 'Smart Thermostats', 62),
(68, 'Smart Locks & Security Systems', 62),
(69, 'Robot Vacuums', 62),
(70, 'Networking & Internet', NULL),
(71, 'Wi-Fi Routers', 70),
(72, 'Range Extenders', 70),
(73, 'Network Switches', 70),
(74, 'Modems', 70),
(75, 'Cables & Adapters', 70),
(76, 'Network Storage (NAS)', 70),
(77, 'Office Equipment', NULL),
(78, 'Printers & Scanners', 77),
(79, 'Projectors', 77),
(80, 'Fax Machines', 77),
(81, 'Office Phones', 77),
(82, 'Paper Shredders', 77),
(83, 'Presentation Tools', 77),
(84, 'Wearables', NULL),
(85, 'Smartwatches', 84),
(86, 'Fitness Bands', 84),
(87, 'VR & AR Devices', 84),
(88, 'Smart Glasses', 84),
(89, 'Car Electronics', NULL),
(90, 'Dash Cameras', 89),
(91, 'Car Audio Systems', 89),
(92, 'Car Chargers', 89),
(93, 'GPS & Navigation', 89),
(94, 'Parking Sensors', 89),
(95, 'Car Accessories', 89),
(96, 'Tools & Components', NULL),
(97, 'Soldering Equipment', 96),
(98, 'Multimeters & Testers', 96),
(99, 'Wires & Connectors', 96),
(100, 'Batteries & Power Supplies', 96),
(101, 'Electronic Modules (Arduino, ESP32, etc.)', 96),
(102, 'Sensors & Actuators', 96),
(103, 'Breadboards & Prototyping Kits', 96)
ON CONFLICT DO NOTHING;


-- ====== attribute ======
INSERT INTO public.attribute (id, name, unit, data_type) VALUES
  (1, 'Color', '', 'string'),
  (2, 'Weight', 'kg', 'string'),
  (3, 'Dimensions', 'cm', 'string'),
  (4, 'Storage', 'GB', 'decimal'),
  (5, 'Battery Capacity', 'mAh', 'decimal'),
  (6, 'Connectivity', '', 'string'),
  (7, 'Form Factor', '', 'string'),
  (8, 'Resolution', '', 'string'),
  (9, 'Color Variant', '', 'string'),
  (10, 'Weight (packaging)', 'kg', 'string')
ON CONFLICT DO NOTHING;


-- ====== product ======
INSERT INTO public.product (id, category_id, name, description, price, stock_quantity, created_at) VALUES
  (1, 2, 'Acme Ultrabook X1', 'Thin and light 14" laptop, ideal for travel.', 999.99, 10, '2025-01-05T10:15:00Z'),
  (2, 3, 'Acme Gaming Desktop G2', 'High-end desktop with liquid cooling.', 1499.00, 5, '2025-02-12T09:00:00Z'),
  (3, 10, 'Pro NVMe 1TB SSD', 'Fast NVMe storage drive, 1TB.', 129.99, 50, '2025-03-01T12:00:00Z'),
  (4, 6, 'Z-Series Motherboard', 'ATX motherboard with PCIe 4.0 support.', 199.99, 7, '2025-01-20T08:30:00Z'),
  (5, 8, 'RTX Super 3070', 'High-performance GPU for gaming and content creation.', 699.00, 3, '2025-02-25T14:45:00Z'),
  (6, 31, 'PowerMax 20000', 'Portable power bank 20000mAh, USB-C output.', 39.99, 25, '2025-03-10T16:00:00Z'),
  (7, 35, 'BlueSound B300', 'Bluetooth over-ear headphones with ANC.', 89.99, 15, '2025-01-30T11:00:00Z'),
  (8, 44, 'VisionSmart 55 OLED', '55" OLED Smart TV with 4K HDR.', 1199.00, 4, '2025-02-05T13:20:00Z'),
  (9, 85, 'FitTrack S2', 'Fitness smartwatch with heart-rate and GPS.', 149.99, 30, '2025-03-15T07:45:00Z'),
  (10, 15, 'USB-C 65W Charger', 'Compact 65W USB-C charger, PD support.', 29.99, 100, '2025-01-10T10:00:00Z')
ON CONFLICT DO NOTHING;


-- ====== product_attribute ======
INSERT INTO public.product_attribute (id, attribute_id, product_id, value) VALUES
  (1, 1, 1, 'Silver'),
  (2, 2, 1, '1.20'),
  (3, 3, 1, '32x22x1.5'),
  (4, 4, 1, '512'),
  (5, 6, 1, 'Wi-Fi 6; Bluetooth 5.2'),
  (6, 7, 1, 'Clamshell'),
  (7, 1, 2, 'Black'),
  (8, 2, 2, '8.50'),
  (9, 7, 2, 'Tower'),
  (10, 4, 3, '1000'),
  (11, 8, 3, 'N/A'),
  (12, 7, 4, 'ATX'),
  (13, 6, 4, 'PCIe 4.0; Gigabit LAN'),
  (14, 1, 5, 'Black/Red'),
  (15, 2, 5, '1.50'),
  (16, 7, 5, 'Dual-slot'),
  (17, 5, 6, '20000'),
  (18, 6, 6, 'USB-C; USB-A'),
  (19, 1, 7, 'Matte Black'),
  (20, 6, 7, 'Bluetooth 5.0'),
  (21, 2, 7, '0.30'),
  (22, 8, 8, '3840x2160'),
  (23, 1, 8, 'Black'),
  (24, 2, 8, '12.00'),
  (25, 1, 9, 'Black'),
  (26, 5, 9, '420'),
  (27, 6, 9, 'Bluetooth, GPS'),
  (28, 1, 10, 'White'),
  (29, 6, 10, 'USB-C PD'),
  (30, 10, 10, '0.15')
ON CONFLICT DO NOTHING;


-- ===============================================
-- ADDITIONAL ATTRIBUTES
-- ===============================================
INSERT INTO public.attribute (id, name, unit, data_type) VALUES
  (11, 'Processor', '', 'string'),
  (12, 'RAM', 'GB', 'decimal'),
  (13, 'Storage Type', '', 'string'),
  (14, 'Screen Size', 'inch', 'decimal'),
  (15, 'Battery Life', 'hours', 'decimal'),
  (16, 'Operating System', '', 'string'),
  (17, 'Camera Resolution', 'MP', 'decimal'),
  (18, 'Audio Type', '', 'string')
ON CONFLICT DO NOTHING;


-- ===============================================
-- ADDITIONAL PRODUCTS
-- ===============================================
INSERT INTO public.product (id, category_id, name, description, price, stock_quantity, created_at) VALUES
  (11, 23, 'Galaxy S24 Ultra', 'Flagship smartphone with advanced AI camera.', 1299.99, 20, '2025-04-01T09:00:00Z'),
  (12, 23, 'iPhone 15 Pro', 'Apple’s latest smartphone with A18 Bionic chip.', 1399.99, 25, '2025-03-25T11:30:00Z'),
  (13, 25, 'TabMate 12.4', 'Large-screen tablet for productivity and entertainment.', 749.99, 15, '2025-03-10T10:45:00Z'),
  (14, 35, 'AudioCore H500', 'Studio-grade over-ear headphones for professionals.', 199.99, 12, '2025-02-15T08:00:00Z'),
  (15, 36, 'BoomBox Mini 3', 'Compact Bluetooth speaker with deep bass.', 59.99, 30, '2025-01-28T09:30:00Z'),
  (16, 53, 'Canon EOS R7', 'Mirrorless camera with APS-C sensor and 4K video.', 1599.00, 8, '2025-02-10T10:00:00Z'),
  (17, 57, 'AeroCam X2 Drone', 'Lightweight drone with 4K stabilized camera.', 899.99, 10, '2025-03-05T12:00:00Z'),
  (18, 85, 'SmartFit Pro Band', 'Fitness band with heart-rate and SpO2 monitoring.', 89.99, 40, '2025-02-20T15:00:00Z'),
  (19, 64, 'HomePlug Smart Socket', 'Wi-Fi smart plug with energy monitoring.', 29.99, 100, '2025-01-15T14:00:00Z'),
  (20, 71, 'NetFast AX6000 Router', 'Dual-band Wi-Fi 6 router for large homes.', 249.99, 18, '2025-03-01T09:00:00Z')
ON CONFLICT DO NOTHING;


-- ===============================================
-- ADDITIONAL PRODUCT ATTRIBUTES
-- ===============================================
INSERT INTO public.product_attribute (id, attribute_id, product_id, value) VALUES
  -- Galaxy S24 Ultra
  (31, 11, 11, 'Snapdragon 8 Gen 3'),
  (32, 12, 11, '12'),
  (33, 4, 11, '512'),
  (34, 14, 11, '6.8'),
  (35, 17, 11, '200'),
  (36, 16, 11, 'Android 15'),
  (37, 5, 11, '5000'),
  (38, 1, 11, 'Titanium Gray'),

  -- iPhone 15 Pro
  (39, 11, 12, 'Apple A18 Pro'),
  (40, 12, 12, '8'),
  (41, 4, 12, '256'),
  (42, 14, 12, '6.1'),
  (43, 17, 12, '48'),
  (44, 16, 12, 'iOS 19'),
  (45, 1, 12, 'Blue Titanium'),

  -- TabMate 12.4
  (46, 11, 13, 'Snapdragon 8cx Gen 2'),
  (47, 12, 13, '8'),
  (48, 4, 13, '128'),
  (49, 14, 13, '12.4'),
  (50, 16, 13, 'Android 15'),
  (51, 5, 13, '10000'),
  (52, 1, 13, 'Silver'),

  -- AudioCore H500
  (53, 18, 14, 'Wired, 3.5mm'),
  (54, 1, 14, 'Black'),
  (55, 2, 14, '0.25'),
  (56, 6, 14, 'Detachable cable'),

  -- BoomBox Mini 3
  (57, 18, 15, 'Bluetooth 5.3'),
  (58, 5, 15, '3000'),
  (59, 1, 15, 'Blue'),
  (60, 2, 15, '0.75'),

  -- Canon EOS R7
  (61, 17, 16, '32'),
  (62, 14, 16, '3.0'),
  (63, 6, 16, 'Wi-Fi, Bluetooth'),
  (64, 1, 16, 'Black'),

  -- AeroCam X2 Drone
  (65, 17, 17, '48'),
  (66, 5, 17, '4500'),
  (67, 2, 17, '1.2'),
  (68, 6, 17, '2.4GHz; GPS'),
  (69, 1, 17, 'White'),

  -- SmartFit Pro Band
  (70, 14, 18, '1.4'),
  (71, 5, 18, '300'),
  (72, 16, 18, 'Custom OS'),
  (73, 1, 18, 'Black'),

  -- HomePlug Smart Socket
  (74, 6, 19, 'Wi-Fi'),
  (75, 16, 19, 'App Control'),
  (76, 1, 19, 'White'),

  -- NetFast AX6000 Router
  (77, 6, 20, 'Wi-Fi 6'),
  (78, 2, 20, '1.1'),
  (79, 7, 20, 'Desktop'),
  (80, 1, 20, 'Black')
ON CONFLICT DO NOTHING;

INSERT INTO public.product_image (id, product_id, image_url) VALUES (1, 9, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761419900/uploads/4b368387-5bae-4313-a090-0bdae62b28cf.png');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (2, 8, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761422322/uploads/92942d36-2b38-49d9-9f66-19f5992566b2.png');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (3, 1, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761867319/Gemini_Generated_Image_3vl0793vl0793vl0_ly2vhd.png');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (4, 2, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761420157/uploads/8e65b215-f887-40d3-9e69-e12bef53810d.png');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (5, 3, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761761430/5192691606795450845_b7twbr.jpg');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (6, 3, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761761430/5192691606795450844_p6odh2.jpg');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (7, 4, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761420213/uploads/92338612-f60f-4344-ab2f-479686b36251.png');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (8, 5, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761420763/uploads/a00e7a08-79b8-48cb-ab94-86cc9d8a069a.png');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (9, 6, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761599314/5188579197084367820_kechi9.jpg');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (10, 6, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761599314/5188579197084367819_1_gtlzzq.jpg');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (11, 7, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761422224/uploads/1135cd61-4ccf-4522-9ad4-62f206ef3113.png');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (12, 10, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761422224/uploads/1135cd61-4ccf-4522-9ad4-62f206ef3113.png');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (13, 11, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761422224/uploads/1135cd61-4ccf-4522-9ad4-62f206ef3113.png');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (14, 12, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761422224/uploads/1135cd61-4ccf-4522-9ad4-62f206ef3113.png');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (15, 17, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761422207/uploads/33560038-abe3-447e-bb99-675cbc0cfc16.png');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (16, 16, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761422224/uploads/1135cd61-4ccf-4522-9ad4-62f206ef3113.png');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (17, 19, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761422295/uploads/488a04d8-a483-47fd-b251-ea0b1d834a86.png');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (18, 20, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761870221/18308988_vjwt4o.jpg');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (19, 18, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761867003/s-l1600_sncgz8.webp');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (20, 13, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761420428/uploads/737e1894-6d50-429a-b6c8-a3576a92cc7a.png');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (21, 15, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761420428/uploads/737e1894-6d50-429a-b6c8-a3576a92cc7a.png');
INSERT INTO public.product_image (id, product_id, image_url) VALUES (22, 14, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761866164/maxresdefault_zkrrsb.jpg');

-- ====== sequence sync ======
DO $$
DECLARE
  r RECORD;
  seq_name text;
  tbl text;
  col text;
  maxval bigint;
BEGIN
  FOR r IN
    SELECT
      n.nspname AS schemaname,
      c.relname  AS tablename,
      a.attname  AS columnname,
      pg_get_serial_sequence(format('%I.%I', n.nspname, c.relname), a.attname) AS seqname
    FROM pg_attribute a
    JOIN pg_class c ON a.attrelid = c.oid
    JOIN pg_namespace n ON c.relnamespace = n.oid
    WHERE a.attnum > 0
      AND NOT a.attisdropped
      AND pg_get_serial_sequence(format('%I.%I', n.nspname, c.relname), a.attname) IS NOT NULL
      AND n.nspname = 'public'
  LOOP
    seq_name := r.seqname;                      -- sequence name e.g. public.table_col_seq
    tbl := format('%I.%I', r.schemaname, r.tablename);
    col := r.columnname;

    EXECUTE format('SELECT COALESCE(MAX(%I), 0) FROM %s', col, tbl) INTO maxval;

    IF maxval IS NULL THEN
      maxval := 0;
    END IF;

    IF maxval = 0 THEN
      -- ensure sequence starts at 1 and is marked as not called (next nextval yields 1)
      EXECUTE format('SELECT setval(%L, 1, false)', seq_name);
    ELSE
      -- set sequence to current max so next nextval yields max+1
      EXECUTE format('SELECT setval(%L, %s, true)', seq_name, maxval);
    END IF;
  END LOOP;
END
$$;

COMMIT;
