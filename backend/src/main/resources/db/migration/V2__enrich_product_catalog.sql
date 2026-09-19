ALTER TABLE products ADD COLUMN brand VARCHAR(100);
ALTER TABLE products ADD COLUMN category VARCHAR(100);
ALTER TABLE products ADD COLUMN image_url VARCHAR(500);

UPDATE products SET brand = 'NovaGear', category = 'Periféricos', image_url = '/assets/products/wireless-mouse.svg' WHERE name = 'Wireless Mouse';
UPDATE products SET brand = 'KeyForge', category = 'Periféricos', image_url = '/assets/products/mechanical-keyboard.svg' WHERE name = 'Mechanical Keyboard Pro';

INSERT INTO products (name, description, price, active, brand, category, image_url, created_at, updated_at) VALUES
('Aurora Headphones', 'Auriculares inalámbricos over-ear con cancelación de ruido y 40 horas de autonomía.', 189.90, true, 'SonicLab', 'Audio', '/assets/products/headphones.svg', NOW(), NOW()),
('Vision 27 QHD', 'Monitor IPS QHD de 27 pulgadas, 165 Hz y compatibilidad Adaptive Sync.', 429.00, true, 'PixelWorks', 'Monitores', '/assets/products/monitor.svg', NOW(), NOW()),
('StreamCam 2K', 'Webcam 2K con autoenfoque, doble micrófono y corrección automática de luz.', 119.50, true, 'FrameOne', 'Streaming', '/assets/products/webcam.svg', NOW(), NOW()),
('Pulse Mini Speaker', 'Parlante Bluetooth compacto con sonido estéreo, USB-C y resistencia IPX6.', 79.90, true, 'SonicLab', 'Audio', '/assets/products/speaker.svg', NOW(), NOW()),
('Vault SSD 1TB', 'SSD portátil NVMe de 1 TB con USB 3.2 Gen 2 y carcasa de aluminio.', 139.00, true, 'ByteCore', 'Almacenamiento', '/assets/products/ssd.svg', NOW(), NOW()),
('Orbit USB-C Hub', 'Hub USB-C 7 en 1 con HDMI 4K, lector SD, USB 3.0 y Power Delivery.', 69.90, true, 'NovaGear', 'Accesorios', '/assets/products/hub.svg', NOW(), NOW()),
('Glide Desk Mat XL', 'Desk mat XL de superficie microtexturada y base antideslizante.', 34.90, true, 'NovaGear', 'Accesorios', '/assets/products/deskmat.svg', NOW(), NOW()),
('AirStand Pro', 'Soporte de aluminio regulable para notebook con diseño ventilado y plegable.', 54.00, true, 'DeskForm', 'Accesorios', '/assets/products/stand.svg', NOW(), NOW()),
('Focus Mechanical TKL', 'Teclado mecánico TKL hot-swap, iluminación RGB y switches lineales.', 129.90, true, 'KeyForge', 'Periféricos', '/assets/products/keyboard-tkl.svg', NOW(), NOW()),
('Vector Gamepad', 'Control inalámbrico multiplataforma con vibración dual y batería recargable.', 74.90, true, 'NovaGear', 'Gaming', '/assets/products/gamepad.svg', NOW(), NOW()),
('Beam Light Bar', 'Barra de luz para monitor con temperatura regulable y control táctil.', 59.90, true, 'DeskForm', 'Iluminación', '/assets/products/lightbar.svg', NOW(), NOW());
