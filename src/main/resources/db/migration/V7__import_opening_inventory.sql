
-- V7: Import opening inventory from the maintenance register.
-- Do not modify previously applied migrations V1-V6.

-- Safety checks: stop the import if required records are missing
-- or inventory data has already been inserted.
DO $$
    BEGIN
        IF EXISTS (SELECT 1 FROM stock_items) THEN
            RAISE EXCEPTION
                'Inventory import stopped: stock_items is not empty.';
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM locations
            WHERE code = 'STORE' AND active = TRUE
        ) THEN
            RAISE EXCEPTION
                'Inventory import stopped: active Central Store (STORE) not found.';
        END IF;

        IF (SELECT COUNT(*) FROM stock_categories WHERE code IN
                                                        ('ELE', 'PLU', 'HOU', 'PAI', 'ROP', 'HTP', 'STP')) <> 7 THEN
            RAISE EXCEPTION
                'Inventory import stopped: one or more required categories are missing.';
        END IF;

        IF NOT EXISTS (SELECT 1 FROM users WHERE id = 1) THEN
            RAISE EXCEPTION
                'Inventory import stopped: audit user ID 1 not found.';
        END IF;
    END $$;

-- Temporary staging table for the register data
CREATE TEMPORARY TABLE register_import_items (
                                                 item_code VARCHAR(50) NOT NULL,
                                                 item_name VARCHAR(255) NOT NULL,
                                                 category_code VARCHAR(20) NOT NULL,
                                                 unit VARCHAR(30) NOT NULL,
                                                 opening_quantity NUMERIC NOT NULL,
                                                 low_stock_threshold NUMERIC NOT NULL
) ON COMMIT DROP;

-- Electrical: 44 items
INSERT INTO register_import_items
(item_code, item_name, category_code, unit, opening_quantity, low_stock_threshold)
VALUES
    ('ELE-0001', '1'' Casing Patty', 'ELE', 'Nos', 0, 2),
    ('ELE-0002', '1.5 Sq mm Wire Green', 'ELE', 'Mtr', 90, 17),
    ('ELE-0003', '1.5Red Wire', 'ELE', 'Nos', 0, 1),
    ('ELE-0004', '10mm Nail Clip', 'ELE', 'Pkt', 0, 1),
    ('ELE-0005', '12 Module Box With Plate', 'ELE', 'Nos', 0, 1),
    ('ELE-0006', '12 m Pvc Box', 'ELE', 'Nos', 0, 1),
    ('ELE-0007', '12 m Pvc Plate', 'ELE', 'Nos', 0, 1),
    ('ELE-0008', '15 A Switch Roma', 'ELE', 'Nos', 0, 1),
    ('ELE-0009', '16 A Scoket Rooma', 'ELE', 'Nos', 7, 3),
    ('ELE-0010', '16 A Switch Cona', 'ELE', 'Nos', 0, 1),
    ('ELE-0011', '16 A Switch Rooma', 'ELE', 'Nos', 7, 3),
    ('ELE-0012', '16A 3Pin Top', 'ELE', 'Nos', 1, 1),
    ('ELE-0013', '16A Scoket Cona', 'ELE', 'Nos', 6, 1),
    ('ELE-0014', '2 Feet Tube Light', 'ELE', 'Nos', 0, 1),
    ('ELE-0015', '2.5 Sq mm Red Wire', 'ELE', 'Mtr', 90, 26),
    ('ELE-0016', '2.5 Sqmm Green', 'ELE', 'Nos', 0, 1),
    ('ELE-0017', '2.5 Sqmm Wire Block', 'ELE', 'Mtr', 90, 1),
    ('ELE-0018', '20 W Tube', 'ELE', 'Nos', 11, 6),
    ('ELE-0019', '3 Core 2.5 Sqmm Cable', 'ELE', 'Mtr', 45, 23),
    ('ELE-0020', '3/4'' POP Screw', 'ELE', 'Box', 0, 1),
    ('ELE-0021', '300mm cable tie', 'ELE', 'Pack', 0, 1),
    ('ELE-0022', '32 Amps DP Switch', 'ELE', 'Nos', 0, 1),
    ('ELE-0023', '32A DP Switch', 'ELE', 'Nos', 0, 1),
    ('ELE-0024', '36 W Tube Light Starter', 'ELE', 'Nos', 0, 1),
    ('ELE-0025', '36 Watts tubelight', 'ELE', 'Nos', 5, 2),
    ('ELE-0026', '40 Amps MCB', 'ELE', 'Nos', 0, 1),
    ('ELE-0027', '400 mm Wall Fan', 'ELE', 'Nos', 0, 1),
    ('ELE-0028', '50w Led', 'ELE', 'Nos', 1, 4),
    ('ELE-0029', '6 A Rooma Switch', 'ELE', 'Nos', 12, 2),
    ('ELE-0030', '6 A Scoket Rooma', 'ELE', 'Nos', 13, 5),
    ('ELE-0031', '6 A Socket Roma', 'ELE', 'Nos', 0, 2),
    ('ELE-0032', '6 A Switch Cona', 'ELE', 'Nos', 5, 3),
    ('ELE-0033', '6 Module Box', 'ELE', 'Nos', 7, 3),
    ('ELE-0034', '6 Module Plate', 'ELE', 'Nos', 7, 3),
    ('ELE-0035', '6A Scoket Cona', 'ELE', 'Nos', 6, 1),
    ('ELE-0036', '8 M Pvc Box', 'ELE', 'Nos', 0, 1),
    ('ELE-0037', '8 M Pvc Plate', 'ELE', 'Nos', 0, 1),
    ('ELE-0038', '9 WAtt Led', 'ELE', 'Nos', 7, 8),
    ('ELE-0039', 'Ceiling Older', 'ELE', 'Nos', 0, 1),
    ('ELE-0040', 'Dummy Switch', 'ELE', 'Nos', 6, 1),
    ('ELE-0041', 'Fan Capacitor', 'ELE', 'Nos', 4, 17),
    ('ELE-0042', 'Fan Regulator Cona', 'ELE', 'Nos', 6, 3),
    ('ELE-0043', 'Fan Regulator Rooma', 'ELE', 'Nos', 0, 1),
    ('ELE-0044', 'Wood Gatta', 'ELE', 'Pkt', 0, 1);

-- Heat Pump: 7 items
INSERT INTO register_import_items
VALUES
    ('HTP-0001', 'Circulation Pump', 'HTP', 'Nos', 0, 1),
    ('HTP-0002', 'Connects', 'HTP', 'Nos', 0, 1),
    ('HTP-0003', 'Gas Gauge', 'HTP', 'Nos', 0, 1),
    ('HTP-0004', 'Heat Pump Comressor', 'HTP', 'Nos', 0, 1),
    ('HTP-0005', 'R134A Gas Refiling', 'HTP', 'Nos', 0, 1),
    ('HTP-0006', 'Return Line Pump 1 HP', 'HTP', 'Nos', 0, 1),
    ('HTP-0007', 'Sensor Wire', 'HTP', 'Mtr', 0, 1);

-- Housekeeping: 21 items
INSERT INTO register_import_items
VALUES
    ('HK-0001', 'Acid', 'HOU', 'Can', 1, 2),
    ('HK-0002', 'Bleaching poeder', 'HOU', 'Pack', 0, 1),
    ('HK-0003', 'Bucket', 'HOU', 'Nos', 1, 1),
    ('HK-0004', 'Cooconut Broon', 'HOU', 'Nos', 0, 1),
    ('HK-0005', 'Garabagr Bage Mi Block Colour', 'HOU', 'Nos', 0, 31),
    ('HK-0006', 'Garbage Bage Yellow Colour', 'HOU', 'Nos', 6, 26),
    ('HK-0007', 'Garbage Medium Siz', 'HOU', 'Nos', 4, 31),
    ('HK-0008', 'Grabage Bage Exl', 'HOU', 'Nos', 0, 30),
    ('HK-0009', 'Hand Gloves', 'HOU', 'Box', 0, 1),
    ('HK-0010', 'Harpick', 'HOU', 'Nos', 2, 38),
    ('HK-0011', 'Mattres', 'HOU', 'Set', 0, 1),
    ('HK-0012', 'Mirror', 'HOU', 'Nos', 0, 1),
    ('HK-0013', 'Mop Clip Set', 'HOU', 'Nos', 0, 20),
    ('HK-0014', 'Phynoil', 'HOU', 'Can', 5, 14),
    ('HK-0015', 'Road brush', 'HOU', 'Nos', 0, 2),
    ('HK-0016', 'Soap Oil', 'HOU', 'Can', 0, 26),
    ('HK-0017', 'Soft Broom', 'HOU', 'Nos', 7, 8),
    ('HK-0018', 'Steel Schotch Pad', 'HOU', 'Nos', 0, 35),
    ('HK-0019', 'Toilet Cleaner Brush', 'HOU', 'Nos', 0, 16),
    ('HK-0020', 'Wheel powder', 'HOU', 'Pack', 0, 1),
    ('HK-0021', 'Whyper Big siz', 'HOU', 'Nos', 0, 4);

-- Painting: 13 items
INSERT INTO register_import_items
VALUES
    ('PNT-0001', 'Birla Patty 30 Kg Bag', 'PAI', 'Nos', 0, 1),
    ('PNT-0002', 'Broun Colour Oil Paint 1500 ML', 'PAI', 'Nos', 0, 1),
    ('PNT-0003', 'Distemper 20 Ltr Bucket', 'PAI', 'Nos', 5, 9),
    ('PNT-0004', 'Paint Brush 3''', 'PAI', 'Nos', 0, 1),
    ('PNT-0005', 'Paint Brush 4''', 'PAI', 'Nos', 0, 1),
    ('PNT-0006', 'Patty Bleade 4''', 'PAI', 'Nos', 0, 1),
    ('PNT-0007', 'Patty Bleade 8''', 'PAI', 'Nos', 0, 1),
    ('PNT-0008', 'Pop 10 Kg Bag', 'PAI', 'Nos', 0, 1),
    ('PNT-0009', 'Primer10 Ltr Bucket', 'PAI', 'Nos', 0, 1),
    ('PNT-0010', 'Roller 8 ''', 'PAI', 'Nos', 0, 1),
    ('PNT-0011', 'Violet 600 ML', 'PAI', 'Nos', 0, 1),
    ('PNT-0012', 'yellow Oxide 500ML', 'PAI', 'Nos', 0, 1),
    ('PNT-0013', 'yellow Oxide Fast 500ML', 'PAI', 'Nos', 0, 1);

-- Plumbing: 33 items
INSERT INTO register_import_items
VALUES
    ('PLM-0001', '1/1x4'' Cpvc Coller', 'PLU', 'Nos', 0, 1),
    ('PLM-0002', '1/1x4'' Cpvc Elbow', 'PLU', 'Nos', 0, 1),
    ('PLM-0003', '2 In 1 Bib Cock', 'PLU', 'Nos', 0, 1),
    ('PLM-0004', '2x2/1'' Pvc End Cap', 'PLU', 'Nos', 1, 1),
    ('PLM-0005', '3/4'' Coller', 'PLU', 'Nos', 0, 1),
    ('PLM-0006', '4'' Bit', 'PLU', 'Nos', 0, 2),
    ('PLM-0007', 'Angle Cock', 'PLU', 'Nos', 0, 1),
    ('PLM-0008', 'Cpvc 1/4x1/2''', 'PLU', 'Nos', 0, 1),
    ('PLM-0009', 'Cpvc Bush 2'' x2/1''', 'PLU', 'Nos', 0, 1),
    ('PLM-0010', 'Cpvc Coller 1''', 'PLU', 'Nos', 0, 1),
    ('PLM-0011', 'Cpvc Coller 1x1/2''', 'PLU', 'Nos', 0, 1),
    ('PLM-0012', 'Cpvc Elbow 1''', 'PLU', 'Nos', 0, 1),
    ('PLM-0013', 'Cpvc Gum', 'PLU', 'Ltr', 0, 1),
    ('PLM-0014', 'Cpvc Long Bend 1''', 'PLU', 'Nos', 0, 1),
    ('PLM-0015', 'Cpvc Long Bend 2''', 'PLU', 'Nos', 0, 1),
    ('PLM-0016', 'Cpvc MTA 1''', 'PLU', 'Nos', 0, 1),
    ('PLM-0017', 'Cpvc MTA 2''', 'PLU', 'Nos', 0, 1),
    ('PLM-0018', 'Cpvc R B Bush 1x1/2''', 'PLU', 'Nos', 0, 1),
    ('PLM-0019', 'Cpvc Tee 1x1/2''', 'PLU', 'Nos', 0, 1),
    ('PLM-0020', 'Cpvc Union', 'PLU', 'Nos', 0, 1),
    ('PLM-0021', 'Cpvc Union 2''', 'PLU', 'Nos', 0, 1),
    ('PLM-0022', 'Cuppling', 'PLU', 'Nos', 0, 1),
    ('PLM-0023', 'Door Magnet', 'PLU', 'Nos', 0, 1),
    ('PLM-0024', 'Ess Piller Cock', 'PLU', 'Nos', 9, 6),
    ('PLM-0025', 'EssLong Body Tape', 'PLU', 'Nos', 2, 6),
    ('PLM-0026', 'Flash Tank Ball Cock', 'PLU', 'Nos', 0, 3),
    ('PLM-0027', 'Grass Cutter Wire', 'PLU', 'Nos', 1, 1),
    ('PLM-0028', 'Gun Set', 'PLU', 'Nos', 0, 3),
    ('PLM-0029', 'Hand Shiwer', 'PLU', 'Nos', 0, 1),
    ('PLM-0030', 'R E 3/4''x1/2''', 'PLU', 'Nos', 0, 1),
    ('PLM-0031', 'Sink Wast Pipe', 'PLU', 'Nos', 1, 3),
    ('PLM-0032', 'Tapland Tape', 'PLU', 'Nos', 0, 1),
    ('PLM-0033', 'Toilet Gun', 'PLU', 'Nos', 0, 5);

-- RO Plant: 11 items
INSERT INTO register_import_items
VALUES
    ('ROP-0001', '1 Hp Motor', 'ROP', 'Nos', 0, 1),
    ('ROP-0002', '1 Hp Motor For Boys Hostel Ro Plant Repairing', 'ROP', 'Nos', 0, 1),
    ('ROP-0003', '1 Hp Motor Monablck', 'ROP', 'Nos', 0, 1),
    ('ROP-0004', '20'' Micron Filter', 'ROP', 'Nos', 0, 1),
    ('ROP-0005', 'Bearing', 'ROP', 'Nos', 0, 1),
    ('ROP-0006', 'Bearing end shield reendition', 'ROP', 'Nos', 0, 1),
    ('ROP-0007', 'Collacttion tank 3 H P Feed pump', 'ROP', 'Nos', 0, 1),
    ('ROP-0008', 'Flow Meter 1200', 'ROP', 'Nos', 0, 1),
    ('ROP-0009', 'Rewinding', 'ROP', 'Nos', 0, 1),
    ('ROP-0010', 'Ro Antiscalant', 'ROP', 'Kg', 21, 4),
    ('ROP-0011', 'Send Media', 'ROP', 'Nos', 0, 1);

-- STP Plant: 3 items
INSERT INTO register_import_items
VALUES
    ('STP-0001', '10mm Bathi', 'STP', 'Nos', 0, 4),
    ('STP-0002', '8mm Barhi', 'STP', 'Nos', 6, 1),
    ('STP-0003', 'HP 320 Oil', 'STP', 'Ltr', 0, 3);

-- Validate the staged register before inserting anything.
DO $$
    BEGIN
        IF (SELECT COUNT(*) FROM register_import_items) <> 132 THEN
            RAISE EXCEPTION
                'Inventory import stopped: expected 132 items, found %.',
                (SELECT COUNT(*) FROM register_import_items);
        END IF;

        IF EXISTS (
            SELECT 1
            FROM register_import_items r
                     LEFT JOIN stock_categories c ON c.code = r.category_code
            WHERE c.id IS NULL
        ) THEN
            RAISE EXCEPTION
                'Inventory import stopped: a staged item has an unknown category code.';
        END IF;

        IF EXISTS (
            SELECT item_code
            FROM register_import_items
            GROUP BY item_code
            HAVING COUNT(*) > 1
        ) THEN
            RAISE EXCEPTION
                'Inventory import stopped: duplicate item codes found in the register.';
        END IF;
    END $$;

-- 1. Insert the stock master records.
INSERT INTO stock_items
(item_code, name, category_id, unit, low_stock_threshold,
 active, created_at, updated_at)
SELECT
    r.item_code,
    r.item_name,
    c.id,
    r.unit,
    r.low_stock_threshold,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM register_import_items r
         JOIN stock_categories c ON c.code = r.category_code
ORDER BY r.item_code;

-- 2. Create a Central Store balance for every item, even when quantity is zero.
INSERT INTO stock_balances
(stock_item_id, location_id, quantity_on_hand,
 quantity_reserved, updated_at)
SELECT
    si.id,
    l.id,
    r.opening_quantity,
    0,
    CURRENT_TIMESTAMP
FROM register_import_items r
         JOIN stock_items si ON si.item_code = r.item_code
         JOIN locations l ON l.code = 'STORE';

-- 3. Record opening transactions only for positive quantities.
-- User ID 1 is testuser01, confirmed in the users table.
INSERT INTO stock_transactions
(stock_item_id, location_id, transaction_type, quantity_change,
 performed_by, created_at, note)
SELECT
    si.id,
    l.id,
    'OPENING',
    r.opening_quantity,
    1,
    CURRENT_TIMESTAMP,
    'Opening inventory imported from maintenance register'
FROM register_import_items r
         JOIN stock_items si ON si.item_code = r.item_code
         JOIN locations l ON l.code = 'STORE'
WHERE r.opening_quantity > 0;
