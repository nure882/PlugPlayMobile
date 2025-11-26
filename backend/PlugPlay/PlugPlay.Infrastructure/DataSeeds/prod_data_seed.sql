TRUNCATE TABLE
  public.product_images,
  public.product_attributes,
  public.products,
  public.attributes,
  public.categories,
  public.asp_net_user_roles,
  public.asp_net_users,
  public.asp_net_roles
RESTART IDENTITY CASCADE;

BEGIN;

-- ====== asp_net_roles ======
INSERT INTO public.asp_net_roles (id, name, normalized_name, concurrency_stamp)
SELECT 0, 'User', 'USER', gen_random_uuid()::text
WHERE NOT EXISTS (SELECT 1 FROM public.asp_net_roles WHERE name = 'User');

INSERT INTO public.asp_net_roles (id, name, normalized_name, concurrency_stamp)
SELECT 1, 'Admin', 'ADMIN', gen_random_uuid()::text
WHERE NOT EXISTS (SELECT 1 FROM public.asp_net_roles WHERE name = 'Admin');

-- ====== asp_net_users ======
INSERT INTO public.asp_net_users (id, user_name, normalized_user_name, email, normalized_email,
 email_confirmed, password_hash, phone_number, phone_number_confirmed, two_factor_enabled,
 first_name, last_name, role, created_at, updated_at, security_stamp, concurrency_stamp, lockout_enabled, access_failed_count, google_id, picture_url)
SELECT 1, 'admin', 'ADMIN', 'admin@cowork.com', 'ADMIN@COWORK.COM',
 true, 'AQAAAAEAACcQAAAAEMOCKHASH==', '+10000000001', true, false,
 'Admin', 'User', 1, NOW(), NOW(), gen_random_uuid()::text, gen_random_uuid()::text, false, 0, '', NULL
WHERE NOT EXISTS (SELECT 1 FROM public.asp_net_users WHERE user_name = 'admin');

INSERT INTO public.asp_net_users (id, user_name, normalized_user_name, email, normalized_email,
 email_confirmed, password_hash, phone_number, phone_number_confirmed, two_factor_enabled,
 first_name, last_name, role, created_at, updated_at, security_stamp, concurrency_stamp, lockout_enabled, access_failed_count, google_id, picture_url)
SELECT 2, 'user1', 'USER1', 'user1@cowork.com', 'USER1@COWORK.COM',
 true, 'AQAAAAEAACcQAAAAEMOCKHASH==', '+10000000002', true, false,
 'John', 'Doe', 0, NOW(), NOW(), gen_random_uuid()::text, gen_random_uuid()::text, false, 0, '', NULL
WHERE NOT EXISTS (SELECT 1 FROM public.asp_net_users WHERE user_name = 'user1');

INSERT INTO public.asp_net_users (id, user_name, normalized_user_name, email, normalized_email,
 email_confirmed, password_hash, phone_number, phone_number_confirmed, two_factor_enabled,
 first_name, last_name, role, created_at, updated_at, security_stamp, concurrency_stamp, lockout_enabled, access_failed_count, google_id, picture_url)
SELECT 3, 'user2', 'USER2', 'user2@cowork.com', 'USER2@COWORK.COM',
 true, 'AQAAAAEAACcQAAAAEMOCKHASH==', '+10000000003', true, false,
 'Jane', 'Smith', 0, NOW(), NOW(), gen_random_uuid()::text, gen_random_uuid()::text, false, 0, '', NULL
WHERE NOT EXISTS (SELECT 1 FROM public.asp_net_users WHERE user_name = 'user2');

INSERT INTO public.asp_net_users (id, user_name, normalized_user_name, email, normalized_email,
 email_confirmed, password_hash, phone_number, phone_number_confirmed, two_factor_enabled,
 first_name, last_name, role, created_at, updated_at, security_stamp, concurrency_stamp, lockout_enabled, access_failed_count, google_id, picture_url)
SELECT 4, 'user3', 'USER3', 'user3@cowork.com', 'USER3@COWORK.COM',
 true, 'AQAAAAEAACcQAAAAEMOCKHASH==', '+10000000004', true, false,
 'Alex', 'Johnson', 0, NOW(), NOW(), gen_random_uuid()::text, gen_random_uuid()::text, false, 0, '', NULL
WHERE NOT EXISTS (SELECT 1 FROM public.asp_net_users WHERE user_name = 'user3');

-- ====== asp_net_user_roles ======
INSERT INTO public.asp_net_user_roles (user_id, role_id)
SELECT 1, 1
WHERE NOT EXISTS (SELECT 1 FROM public.asp_net_user_roles WHERE user_id = 1 AND role_id = 1);

INSERT INTO public.asp_net_user_roles (user_id, role_id)
SELECT 2, 0
WHERE NOT EXISTS (SELECT 1 FROM public.asp_net_user_roles WHERE user_id = 2 AND role_id = 0);

INSERT INTO public.asp_net_user_roles (user_id, role_id)
SELECT 3, 0
WHERE NOT EXISTS (SELECT 1 FROM public.asp_net_user_roles WHERE user_id = 3 AND role_id = 0);

INSERT INTO public.asp_net_user_roles (user_id, role_id)
SELECT 4, 0
WHERE NOT EXISTS (SELECT 1 FROM public.asp_net_user_roles WHERE user_id = 4 AND role_id = 0);


-- ====== categories ======
INSERT INTO public.categories (id, name, parent_category_id) VALUES
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


-- ====== attributes ======
INSERT INTO public.attributes (id, name, unit, data_type) VALUES
  (1, 'Color', '', 'string'),
  (2, 'Weight', 'kg', 'string'),
  (3, 'Dimensions', 'cm', 'string'),
  (4, 'Storage', 'GB', 'decimal'),
  (5, 'Battery Capacity', 'mAh', 'decimal'),
  (6, 'Connectivity', '', 'string'),
  (7, 'Form Factor', '', 'string'),
  (8, 'Resolution', '', 'string'),
  (9, 'Color Variant', '', 'string'),
  (10, 'Weight (packaging)', 'kg', 'string'),
  (11, 'Processor', '', 'string'),
  (12, 'RAM', 'GB', 'decimal'),
  (13, 'Storage Type', '', 'string'),
  (14, 'Screen Size', 'inch', 'decimal'),
  (15, 'Battery Life', 'hours', 'decimal'),
  (16, 'Operating System', '', 'string'),
  (17, 'Camera Resolution', 'MP', 'decimal'),
  (18, 'Audio Type', '', 'string')
ON CONFLICT DO NOTHING;

-- ====== products ======
INSERT INTO public.products (id, category_id, name, description, price, stock_quantity, created_at) VALUES
    (1, 2, 'Acme Ultrabook X1', 'Thin and light 14" laptop, ideal for travel.', 41999.58, 10, '2025-01-05T10:15:00Z'),
    (2, 3, 'Acme Gaming Desktop G2', 'High-end desktop with liquid cooling.', 62958.00, 5, '2025-02-12T09:00:00Z'),
    (3, 10, 'Pro NVMe 1TB SSD', 'Fast NVMe storage drive, 1TB.', 5459.58, 50, '2025-03-01T12:00:00Z'),
    (4, 6, 'Z-Series Motherboard', 'ATX motherboard with PCIe 4.0 support.', 8399.58, 7, '2025-01-20T08:30:00Z'),
    (5, 8, 'RTX Super 3070', 'High-performance GPU for gaming and content creation.', 29358.00, 3, '2025-02-25T14:45:00Z'),
    (6, 31, 'PowerMax 20000', 'Portable power bank 20000mAh, USB-C output.', 1679.58, 25, '2025-03-10T16:00:00Z'),
    (7, 35, 'BlueSound B300', 'Bluetooth over-ear headphones with ANC.', 3779.58, 15, '2025-01-30T11:00:00Z'),
    (8, 44, 'VisionSmart 55 OLED', '55" OLED Smart TV with 4K HDR.', 50358.00, 4, '2025-02-05T13:20:00Z'),
    (9, 85, 'FitTrack S2', 'Fitness smartwatch with heart-rate and GPS.', 6299.58, 30, '2025-03-15T07:45:00Z'),
    (10, 15, 'USB-C 65W Charger', 'Compact 65W USB-C charger, PD support.', 1259.58, 100, '2025-01-10T10:00:00Z'),
    (11, 23, 'Galaxy S24 Ultra', 'Flagship smartphone with advanced AI camera.', 54599.58, 20, '2025-04-01T09:00:00Z'),
    (12, 23, 'iPhone 15 Pro', 'Apple''s latest smartphone with A18 Bionic chip.', 58799.58, 25, '2025-03-25T11:30:00Z'),
    (13, 25, 'TabMate 12.4', 'Large-screen tablet for productivity and entertainment.', 31499.58, 15, '2025-03-10T10:45:00Z'),
    (14, 35, 'AudioCore H500', 'Studio-grade over-ear headphones for professionals.', 8399.58, 12, '2025-02-15T08:00:00Z'),
    (15, 36, 'BoomBox Mini 3', 'Compact Bluetooth speaker with deep bass.', 2519.58, 30, '2025-01-28T09:30:00Z'),
    (16, 53, 'Canon EOS R7', 'Mirrorless camera with APS-C sensor and 4K video.', 67158.00, 8, '2025-02-10T10:00:00Z'),
    (17, 57, 'AeroCam X2 Drone', 'Lightweight drone with 4K stabilized camera.', 37799.58, 10, '2025-03-05T12:00:00Z'),
    (18, 85, 'SmartFit Pro Band', 'Fitness band with heart-rate and SpO2 monitoring.', 3779.58, 40, '2025-02-20T15:00:00Z'),
    (19, 64, 'HomePlug Smart Socket', 'Wi-Fi smart plug with energy monitoring.', 1259.58, 100, '2025-01-15T14:00:00Z'),
    (20, 71, 'NetFast AX6000 Router', 'Dual-band Wi-Fi 6 router for large homes.', 10499.58, 18, '2025-03-01T09:00:00Z')
  ON CONFLICT DO NOTHING;


-- ====== product_attributes ======
INSERT INTO public.product_attributes (id, attribute_id, product_id, value) VALUES
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
  (30, 10, 10, '0.15'),

  -- Galaxy S24 Ultra
  (31, 11, 11, 'Snapdragon 8 Gen 3'),
  (32, 12, 11, '12'),
  (33, 4, 11, '512'),
  (34, 14, 11, '6.8'),
  (35, 17, 11, '200'),
  (36, 16, 11, 'Android 15'),
  (37, 5, 11, '5000'),
  (38, 1, 11, 'Slate'),

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
  (59, 1, 15, 'Black'),
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
  (69, 1, 17, 'Black'),

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

-- ====== product_images ======
INSERT INTO public.product_images (id, product_id, image_url) VALUES
  (2, 2, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893087/2_xcmzkd.jpg'),
  (3, 1, 'https://res.cloudinary.com/dovmlupww/image/upload/v1761867319/Gemini_Generated_Image_3vl0793vl0793vl0_ly2vhd.png'),
  (4, 4, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893127/4_qreasf.jpg'),
  (5, 3, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762894346/3_1_pkba7o.png'),
  (6, 3, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893125/3_cdesyr.webp'),
  (7, 7, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893126/7_ipklud.jpg'),
  (9, 6, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762894346/6_1_fvi8wa.jpg'),
  (10, 10, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893126/10_he3kbf.webp'),
  (11, 11, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893128/11_iwchbf.webp'),
  (12, 12, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893133/12_iw2uhc.png'),
  (13, 13, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893129/13_y4kltb.jpg'),
  (14, 14, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893129/14_za8nbc.jpg'),
  (15, 15, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893129/15_atexoi.webp'),
  (16, 16, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893131/16_vuzncm.png'),
  (17, 17, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893131/17_dkmcdq.webp'),
  (18, 18, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893132/18_brhb6y.jpg'),
  (19, 19, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893132/19_q2e55a.jpg'),
  (20, 20, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893132/20_uwb7rj.jpg'),
  (21, 6, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893126/6_zrf871.jpg'),
  (22, 8, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893126/8_r2cxdn.avif'),
  (23, 9, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893126/9_a9jdra.webp'),
  (24, 5, 'https://res.cloudinary.com/dovmlupww/image/upload/v1762893126/5_eveuyl.png')
ON CONFLICT DO NOTHING;

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
